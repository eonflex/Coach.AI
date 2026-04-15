using CoachAI.Api.Models.Extraction;

namespace CoachAI.Api.Services;

/// <summary>
/// Orchestrates LLM-based structured extraction for meals, workouts, nutrition labels, and plans.
/// All methods return null if the model is unavailable or the response cannot be deserialized.
/// Results are suggestions only — they must not be written to the database without user confirmation.
/// </summary>
public interface IExtractionService
{
    /// <summary>Extract a single meal from free text (e.g. a user's meal description).</summary>
    Task<ExtractedMeal?> ExtractMealAsync(string text, CancellationToken ct = default);

    /// <summary>Extract a single exercise/workout entry from free text.</summary>
    Task<ExtractedWorkout?> ExtractWorkoutAsync(string text, CancellationToken ct = default);

    /// <summary>Extract nutrition label data from OCR text of a product label.</summary>
    Task<ExtractedNutritionLabel?> ExtractNutritionLabelAsync(string text, CancellationToken ct = default);

    /// <summary>Extract a structured diet/workout plan from a document's text content.</summary>
    Task<ExtractedPlan?> ExtractPlanAsync(string text, CancellationToken ct = default);
}
