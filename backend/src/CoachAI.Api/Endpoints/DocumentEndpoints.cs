using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using CoachAI.Api.Services;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class DocumentEndpoints
{
    public static void MapDocumentEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/documents").WithTags("Documents");

        group.MapGet("/", GetDocuments);
        group.MapGet("/{id:int}", GetDocument);
        group.MapGet("/{id:int}/chunks", GetDocumentChunks);
        group.MapPost("/upload", UploadDocument).DisableAntiforgery();
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
            Notes = notes
        };
        db.UploadedDocuments.Add(doc);
        await db.SaveChangesAsync(ct);

        var docId = doc.Id;
        var contentType = file.ContentType;
        var logger = loggerFactory.CreateLogger("DocumentExtraction");

        _ = Task.Run(async () =>
        {
            try
            {
                var chunks = await extractor.ExtractChunksAsync(filePath, contentType);
                await using var scope = scopeFactory.CreateAsyncScope();
                var scopedDb = scope.ServiceProvider.GetRequiredService<AppDbContext>();

                var dbChunks = chunks.Select((text, idx) => new DocumentChunk
                {
                    DocumentId = docId,
                    ChunkIndex = idx,
                    TextContent = text
                }).ToList();

                scopedDb.DocumentChunks.AddRange(dbChunks);
                var savedDoc = await scopedDb.UploadedDocuments.FindAsync(docId);
                if (savedDoc != null) savedDoc.ExtractionDone = true;
                await scopedDb.SaveChangesAsync();
                logger.LogInformation("Extraction complete for document {DocumentId}: {ChunkCount} chunks", docId, dbChunks.Count);
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "Background extraction failed for document {DocumentId}", docId);
            }
        }, CancellationToken.None);

        return Results.Created($"/api/documents/{doc.Id}",
            new DocumentResponse(doc.Id, doc.FileName, doc.ContentType, doc.FileSizeBytes,
                doc.Notes, doc.ExtractionDone, 0, doc.UploadedAt));
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
