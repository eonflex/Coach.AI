namespace CoachAI.Api.Services;

public interface IDocumentExtractionService
{
    Task<IReadOnlyList<string>> ExtractChunksAsync(string filePath, string contentType, CancellationToken ct = default);
}
