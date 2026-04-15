namespace CoachAI.Api.Services;

public interface IModelService
{
    Task<string> GenerateAsync(string systemPrompt, string userMessage, CancellationToken ct = default);

    /// <summary>
    /// Requests a JSON-mode response from the model.
    /// The system prompt should instruct the model to return only valid JSON.
    /// Returns raw JSON string; caller is responsible for deserialization.
    /// </summary>
    Task<string> GenerateStructuredAsync(string systemPrompt, string userMessage, CancellationToken ct = default);

    Task<bool> IsAvailableAsync(CancellationToken ct = default);
}
