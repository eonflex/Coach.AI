namespace CoachAI.Api.Models;

/// <summary>
/// Represents a user's diet/workout plan — either extracted from an uploaded document
/// or created manually. A single plan can have multiple PlanItems across days.
/// </summary>
public class MealPlan
{
    public int Id { get; set; }
    public required string Name { get; set; }
    public string? Description { get; set; }
    /// <summary>Phase label (e.g. "cut", "maintain", "bulk").</summary>
    public string? Phase { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<PlanItem> Items { get; set; } = new List<PlanItem>();
}
