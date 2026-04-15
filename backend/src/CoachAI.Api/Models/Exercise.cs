namespace CoachAI.Api.Models;

public class Exercise
{
    public int Id { get; set; }
    public int WorkoutLogId { get; set; }
    public WorkoutLog WorkoutLog { get; set; } = null!;
    public required string Name { get; set; }
    public int? Sets { get; set; }
    public int? Reps { get; set; }
    public decimal? WeightKg { get; set; }
    public string? Notes { get; set; }
}
