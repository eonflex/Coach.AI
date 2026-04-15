namespace CoachAI.Api.Models.Extraction;

/// <summary>
/// Structured workout data extracted from free text by the LLM.
/// Supports both strength (Sets/Reps/WeightKg) and cardio (CardioDurationMinutes).
/// </summary>
public record ExtractedWorkout(
    string? WorkoutType,
    string ExerciseName,
    int? Sets,
    int? Reps,
    decimal? WeightKg,
    int? CardioDurationMinutes,
    string? Notes
);
