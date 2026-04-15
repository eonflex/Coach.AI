namespace CoachAI.Api.Models;

/// <summary>
/// A single line item in a MealPlan — e.g. "Monday Breakfast: Oats, 80g".
/// FoodItemId is optional; items can also be free-text descriptions.
/// </summary>
public class PlanItem
{
    public int Id { get; set; }
    public int MealPlanId { get; set; }
    public MealPlan MealPlan { get; set; } = null!;

    /// <summary>Day label (e.g. "Monday", "Day 1", or null for every day).</summary>
    public string? DayLabel { get; set; }
    /// <summary>Meal name (e.g. "Breakfast", "Pre-workout").</summary>
    public string? Meal { get; set; }
    /// <summary>Optional link to a saved FoodItem for macro roll-up.</summary>
    public int? FoodItemId { get; set; }
    public FoodItem? FoodItem { get; set; }
    /// <summary>Free-text description when no FoodItem is linked.</summary>
    public string? Description { get; set; }

    public decimal? TargetCalories { get; set; }
    public decimal? TargetProteinGrams { get; set; }
    public decimal? TargetCarbsGrams { get; set; }
    public decimal? TargetFatGrams { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
