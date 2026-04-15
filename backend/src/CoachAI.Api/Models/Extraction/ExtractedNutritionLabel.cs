namespace CoachAI.Api.Models.Extraction;

/// <summary>
/// Structured nutrition label data extracted from OCR text by the LLM.
/// All values are per serving. The model transcribes what is present — null means not found on the label.
/// </summary>
public record ExtractedNutritionLabel(
    string? ProductName,
    decimal? ServingSizeGrams,
    decimal? CaloriesPerServing,
    decimal? ProteinGrams,
    decimal? CarbsGrams,
    decimal? FatGrams,
    decimal? FiberGrams
);
