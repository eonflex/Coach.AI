namespace CoachAI.Api.Models.Extraction;

/// <summary>
/// Structured meal data extracted from free text by the LLM.
/// All macro fields are estimates and should be treated as suggestions, not ground truth.
/// Confidence reflects the model's self-assessed certainty ("high", "medium", "low").
/// </summary>
public record ExtractedMeal(
    string FoodName,
    string? ServingDescription,
    decimal? EstimatedCalories,
    decimal? EstimatedProteinGrams,
    decimal? EstimatedCarbsGrams,
    decimal? EstimatedFatGrams,
    /// <summary>"high", "medium", or "low" — low-confidence results should be flagged to the user.</summary>
    string Confidence
);
