namespace CoachAI.Api.DTOs;

public record SetTargetRequest(
    string Phase,
    decimal? TargetCalories,
    decimal? TargetProteinGrams,
    decimal? TargetCarbsGrams,
    decimal? TargetFatGrams,
    decimal? TargetFiberGrams,
    decimal? TargetWeightKg
);

public record TargetResponse(
    int Id,
    string Phase,
    decimal? TargetCalories,
    decimal? TargetProteinGrams,
    decimal? TargetCarbsGrams,
    decimal? TargetFatGrams,
    decimal? TargetFiberGrams,
    decimal? TargetWeightKg,
    bool IsActive,
    DateTime CreatedAt
);
