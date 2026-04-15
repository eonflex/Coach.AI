namespace CoachAI.Api.Services;

public interface IModelService
{
    Task<string> GenerateAsync(string systemPrompt, string userMessage, CancellationToken ct = default);
    Task<bool> IsAvailableAsync(CancellationToken ct = default);
}
