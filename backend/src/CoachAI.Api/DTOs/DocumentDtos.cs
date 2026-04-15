namespace CoachAI.Api.DTOs;

public record DocumentResponse(
    int Id,
    string FileName,
    string ContentType,
    long FileSizeBytes,
    string? Notes,
    bool ExtractionDone,
    int ChunkCount,
    DateTime UploadedAt
);

public record DocumentChunkResponse(
    int Id,
    int ChunkIndex,
    string TextContent,
    string? Tags
);
