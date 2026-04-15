namespace CoachAI.Api.Models;

public class UserTarget
{
    public int Id { get; set; }
    public required string Phase { get; set; }  // cut | maintain | bulk
    public decimal? TargetCalories { get; set; }
    public decimal? TargetProteinGrams { get; set; }
    public decimal? TargetCarbsGrams { get; set; }
    public decimal? TargetFatGrams { get; set; }
    public decimal? TargetFiberGrams { get; set; }
    public decimal? TargetWeightKg { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }
}
