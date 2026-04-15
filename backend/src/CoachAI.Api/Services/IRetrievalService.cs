namespace CoachAI.Api.Services;

public interface IRetrievalService
{
    Task<IReadOnlyList<string>> RetrieveRelevantChunksAsync(string query, int maxChunks = 5, CancellationToken ct = default);
}
