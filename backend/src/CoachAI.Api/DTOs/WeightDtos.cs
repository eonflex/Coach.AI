namespace CoachAI.Api.DTOs;

public record LogWeightRequest(
    decimal WeightKg,
    string? Notes,
    DateTime? LoggedAt
);

public record UpdateWeightLogRequest(
    decimal? WeightKg,
    string? Notes
);

public record WeightLogResponse(
    int Id,
    decimal WeightKg,
    string? Notes,
    DateTime LoggedAt
);
