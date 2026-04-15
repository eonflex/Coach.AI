namespace CoachAI.Api.DTOs;

public record ErrorResponse(string Error, string? Details = null);
public record PagedResponse<T>(List<T> Items, int Total, int Page, int PageSize);
