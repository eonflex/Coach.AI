namespace CoachAI.Api.Models;

public class WeightLog
{
    public int Id { get; set; }
    public decimal WeightKg { get; set; }
    public string? Notes { get; set; }
    public DateTime LoggedAt { get; set; } = DateTime.UtcNow;
}
