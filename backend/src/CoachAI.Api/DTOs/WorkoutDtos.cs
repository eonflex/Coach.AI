namespace CoachAI.Api.DTOs;

public record CreateExerciseRequest(
    string Name,
    int? Sets,
    int? Reps,
    decimal? WeightKg,
    string? Notes
);

public record CreateWorkoutLogRequest(
    string WorkoutType,
    string? Notes,
    int? CardioDurationMinutes,
    DateTime? LoggedAt,
    List<CreateExerciseRequest> Exercises
);

public record ExerciseResponse(
    int Id,
    string Name,
    int? Sets,
    int? Reps,
    decimal? WeightKg,
    string? Notes
);

public record WorkoutLogResponse(
    int Id,
    string WorkoutType,
    string? Notes,
    int? CardioDurationMinutes,
    DateTime LoggedAt,
    List<ExerciseResponse> Exercises
);

public record UpdateWorkoutLogRequest(
    string? WorkoutType,
    string? Notes,
    int? CardioDurationMinutes
);
