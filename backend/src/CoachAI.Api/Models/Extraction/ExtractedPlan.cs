namespace CoachAI.Api.Models.Extraction;

/// <summary>
/// A structured diet/workout plan extracted from a document by the LLM.
/// MealItems contains individual meal entries parsed from the plan text.
/// </summary>
public record ExtractedPlan(
    string? PlanName,
    string? Phase,
    decimal? DailyTargetCalories,
    decimal? DailyTargetProteinGrams,
    List<ExtractedMeal> MealItems,
    string? Notes
);
