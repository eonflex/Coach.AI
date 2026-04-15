namespace CoachAI.Api.Models;

public class ChatMessage
{
    public int Id { get; set; }
    public required string Role { get; set; }   // user | assistant
    public required string Content { get; set; }
    public string? ContextSummary { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
