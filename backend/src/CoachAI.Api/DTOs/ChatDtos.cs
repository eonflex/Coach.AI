namespace CoachAI.Api.DTOs;

public record ChatRequest(
    string Message,
    bool IncludeRecentLogs = true
);

public record ChatResponse(
    int Id,
    string Reply,
    string? ContextSummary,
    DateTime CreatedAt
);

public record ChatHistoryResponse(
    int Id,
    string Role,
    string Content,
    DateTime CreatedAt
);
