namespace CoachAI.Api.Models;

public class WorkoutLog
{
    public int Id { get; set; }
    public required string WorkoutType { get; set; }
    public string? Notes { get; set; }
    public int? CardioDurationMinutes { get; set; }
    public DateTime LoggedAt { get; set; } = DateTime.UtcNow;
    public ICollection<Exercise> Exercises { get; set; } = new List<Exercise>();
}
