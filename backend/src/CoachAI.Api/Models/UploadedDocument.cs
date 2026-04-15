namespace CoachAI.Api.Models;

public class UploadedDocument
{
    public int Id { get; set; }
    public required string FileName { get; set; }
    public required string ContentType { get; set; }
    public required string StoragePath { get; set; }
    public long FileSizeBytes { get; set; }
    public string? Notes { get; set; }
    public bool ExtractionDone { get; set; } = false;
    public DateTime UploadedAt { get; set; } = DateTime.UtcNow;
    public ICollection<DocumentChunk> Chunks { get; set; } = new List<DocumentChunk>();
}
