namespace CoachAI.Api.Models;

public class DocumentChunk
{
    public int Id { get; set; }
    public int DocumentId { get; set; }
    public UploadedDocument Document { get; set; } = null!;
    public int ChunkIndex { get; set; }
    public required string TextContent { get; set; }
    public string? Tags { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
