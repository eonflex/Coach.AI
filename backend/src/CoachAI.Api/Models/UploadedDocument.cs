namespace CoachAI.Api.Models;

/// <summary>
/// Lifecycle of a document's text-extraction background task.
/// </summary>
public enum ExtractionStatus
{
    Pending = 0,
    Processing = 1,
    Completed = 2,
    Failed = 3
}

public class UploadedDocument
{
    public int Id { get; set; }
    public required string FileName { get; set; }
    public required string ContentType { get; set; }
    public required string StoragePath { get; set; }
    public long FileSizeBytes { get; set; }
    public string? Notes { get; set; }

    /// <summary>
    /// Kept for backwards compatibility. True when ExtractionStatus == Completed.
    /// </summary>
    public bool ExtractionDone { get; set; } = false;

    /// <summary>Granular status of the background extraction task.</summary>
    public ExtractionStatus ExtractionStatus { get; set; } = ExtractionStatus.Pending;

    /// <summary>Populated when ExtractionStatus == Failed.</summary>
    public string? ExtractionError { get; set; }

    public DateTime UploadedAt { get; set; } = DateTime.UtcNow;
    public ICollection<DocumentChunk> Chunks { get; set; } = new List<DocumentChunk>();
}
