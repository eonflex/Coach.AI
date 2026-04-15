namespace CoachAI.Api.Services;

/// <summary>
/// Retrieves relevant document chunks for grounding LLM responses.
/// </summary>
/// <remarks>
/// v1 implementation uses keyword scoring — see <see cref="RetrievalService"/>.
/// v1.1 upgrade path: replace with a semantic/vector search implementation
/// (e.g. embedding-based cosine similarity using a local embedding model via Ollama).
/// The interface is the stable contract; swap the implementation without touching callers.
/// </remarks>
public interface IRetrievalService
{
    Task<IReadOnlyList<string>> RetrieveRelevantChunksAsync(string query, int maxChunks = 5, CancellationToken ct = default);
}
