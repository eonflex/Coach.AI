using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using CoachAI.Api.Services;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class DocumentEndpoints
{
    /// <summary>
    /// Allowed MIME types for upload. Reject anything else with 400.
    /// v1.1: add image/* types once OCR is implemented.
    /// </summary>
    private static readonly HashSet<string> AllowedContentTypes = new(StringComparer.OrdinalIgnoreCase)
    {
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain",
        "text/csv",
        "text/tab-separated-values",
        "application/csv",
        // Images: accepted but extraction returns a placeholder until OCR is implemented
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
    };

    private const int DefaultMaxFileSizeMb = 50;

    public static void MapDocumentEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/documents").WithTags("Documents");

        group.MapGet("/", GetDocuments);
        group.MapGet("/{id:int}", GetDocument);
        group.MapGet("/{id:int}/chunks", GetDocumentChunks);
        group.MapPost("/upload", UploadDocument).DisableAntiforgery();
        group.MapPost("/{id:int}/reprocess", ReprocessDocument);
        group.MapDelete("/{id:int}", DeleteDocument);
    }

    private static async Task<IResult> GetDocuments(AppDbContext db, CancellationToken ct)
    {
        var docs = await db.UploadedDocuments
            .Include(d => d.Chunks)
            .OrderByDescending(d => d.UploadedAt)
            .Select(d => new DocumentResponse(d.Id, d.FileName, d.ContentType, d.FileSizeBytes,
                d.Notes, d.ExtractionDone, d.Chunks.Count, d.UploadedAt))
            .ToListAsync(ct);
        return Results.Ok(docs);
    }

    private static async Task<IResult> GetDocument(int id, AppDbContext db, CancellationToken ct)
    {
        var doc = await db.UploadedDocuments.Include(d => d.Chunks).FirstOrDefaultAsync(d => d.Id == id, ct);
        if (doc == null) return Results.NotFound(new ErrorResponse("Document not found"));
        return Results.Ok(new DocumentResponse(doc.Id, doc.FileName, doc.ContentType, doc.FileSizeBytes,
            doc.Notes, doc.ExtractionDone, doc.Chunks.Count, doc.UploadedAt));
    }

    private static async Task<IResult> GetDocumentChunks(int id, AppDbContext db, CancellationToken ct)
    {
        var exists = await db.UploadedDocuments.AnyAsync(d => d.Id == id, ct);
        if (!exists) return Results.NotFound(new ErrorResponse("Document not found"));

        var chunks = await db.DocumentChunks
            .Where(c => c.DocumentId == id)
            .OrderBy(c => c.ChunkIndex)
            .Select(c => new DocumentChunkResponse(c.Id, c.ChunkIndex, c.TextContent, c.Tags))
            .ToListAsync(ct);
        return Results.Ok(chunks);
    }

    private static async Task<IResult> UploadDocument(
        IFormFile file,
        string? notes,
        AppDbContext db,
        IDocumentExtractionService extractor,
        IServiceScopeFactory scopeFactory,
        IConfiguration config,
        ILoggerFactory loggerFactory,
        CancellationToken ct)
    {
        if (file == null || file.Length == 0)
            return Results.BadRequest(new ErrorResponse("No file provided"));

        if (!AllowedContentTypes.Contains(file.ContentType))
            return Results.BadRequest(new ErrorResponse(
                $"Unsupported file type '{file.ContentType}'. Allowed: PDF, DOCX, TXT, CSV, images."));

        var maxFileSizeMb = config.GetValue<int>("Storage:MaxFileSizeMb", DefaultMaxFileSizeMb);
        if (file.Length > (long)maxFileSizeMb * 1024 * 1024)
            return Results.BadRequest(new ErrorResponse($"File exceeds maximum size of {maxFileSizeMb} MB."));

        var uploadPath = config["Storage:UploadPath"] ?? "uploads";
        Directory.CreateDirectory(uploadPath);

        var safeFileName = Path.GetFileNameWithoutExtension(file.FileName).Replace(" ", "_")
            + "_" + DateTime.UtcNow.Ticks + Path.GetExtension(file.FileName);
        var filePath = Path.Combine(uploadPath, safeFileName);

        await using (var stream = File.Create(filePath))
        {
            await file.CopyToAsync(stream, ct);
        }

        var doc = new UploadedDocument
        {
            FileName = file.FileName,
            ContentType = file.ContentType,
            StoragePath = filePath,
            FileSizeBytes = file.Length,
            Notes = notes,
            ExtractionStatus = ExtractionStatus.Pending
        };
        db.UploadedDocuments.Add(doc);
        await db.SaveChangesAsync(ct);

        var docId = doc.Id;
        var contentType = file.ContentType;
        var logger = loggerFactory.CreateLogger("DocumentExtraction");

        RunExtractionAsync(docId, filePath, contentType, scopeFactory, logger);

        return Results.Created($"/api/documents/{doc.Id}",
            new DocumentResponse(doc.Id, doc.FileName, doc.ContentType, doc.FileSizeBytes,
                doc.Notes, doc.ExtractionDone, 0, doc.UploadedAt));
    }

    /// <summary>
    /// Re-triggers extraction for a document that previously failed or needs re-processing.
    /// Deletes existing chunks and resets ExtractionStatus to Pending before re-running.
    /// </summary>
    private static async Task<IResult> ReprocessDocument(
        int id,
        AppDbContext db,
        IDocumentExtractionService extractor,
        IServiceScopeFactory scopeFactory,
        ILoggerFactory loggerFactory,
        CancellationToken ct)
    {
        var doc = await db.UploadedDocuments.FindAsync(new object[] { id }, ct);
        if (doc == null) return Results.NotFound(new ErrorResponse("Document not found"));

        if (!File.Exists(doc.StoragePath))
            return Results.UnprocessableEntity(new ErrorResponse(
                "Source file no longer exists on disk. Cannot reprocess."));

        // Delete existing chunks and reset status
        await db.DocumentChunks.Where(c => c.DocumentId == id).ExecuteDeleteAsync(ct);
        doc.ExtractionDone = false;
        doc.ExtractionStatus = ExtractionStatus.Pending;
        doc.ExtractionError = null;
        await db.SaveChangesAsync(ct);

        var logger = loggerFactory.CreateLogger("DocumentExtraction");
        RunExtractionAsync(id, doc.StoragePath, doc.ContentType, scopeFactory, logger);

        return Results.Accepted($"/api/documents/{id}",
            new DocumentResponse(doc.Id, doc.FileName, doc.ContentType, doc.FileSizeBytes,
                doc.Notes, doc.ExtractionDone, 0, doc.UploadedAt));
    }

    /// <summary>
    /// Fires and forgets a background extraction task.
    /// Updates ExtractionStatus, ExtractionDone, and ExtractionError in a fresh DI scope.
    /// </summary>
    private static void RunExtractionAsync(
        int docId,
        string filePath,
        string contentType,
        IServiceScopeFactory scopeFactory,
        ILogger logger)
    {
        _ = Task.Run(async () =>
        {
            await using var scope = scopeFactory.CreateAsyncScope();
            var scopedDb = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            var scopedExtractor = scope.ServiceProvider.GetRequiredService<IDocumentExtractionService>();

            var savedDoc = await scopedDb.UploadedDocuments.FindAsync(docId);
            if (savedDoc == null) return;

            savedDoc.ExtractionStatus = ExtractionStatus.Processing;
            await scopedDb.SaveChangesAsync();

            try
            {
                var chunks = await scopedExtractor.ExtractChunksAsync(filePath, contentType);

                var dbChunks = chunks.Select((text, idx) => new DocumentChunk
                {
                    DocumentId = docId,
                    ChunkIndex = idx,
                    TextContent = text
                }).ToList();

                scopedDb.DocumentChunks.AddRange(dbChunks);
                savedDoc.ExtractionDone = true;
                savedDoc.ExtractionStatus = ExtractionStatus.Completed;
                savedDoc.ExtractionError = null;
                await scopedDb.SaveChangesAsync();
                logger.LogInformation("Extraction complete for document {DocumentId}: {ChunkCount} chunks", docId, dbChunks.Count);
            }
            catch (Exception ex)
            {
                savedDoc.ExtractionStatus = ExtractionStatus.Failed;
                savedDoc.ExtractionError = ex.Message;
                await scopedDb.SaveChangesAsync();
                logger.LogError(ex, "Background extraction failed for document {DocumentId}", docId);
            }
        }, CancellationToken.None);
    }

    private static async Task<IResult> DeleteDocument(int id, AppDbContext db, CancellationToken ct)
    {
        var doc = await db.UploadedDocuments.FindAsync(new object[] { id }, ct);
        if (doc == null) return Results.NotFound(new ErrorResponse("Document not found"));

        if (File.Exists(doc.StoragePath))
            File.Delete(doc.StoragePath);

        db.UploadedDocuments.Remove(doc);
        await db.SaveChangesAsync(ct);
        return Results.NoContent();
    }
}
