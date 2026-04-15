namespace CoachAI.Api.Services;

/// <summary>
/// Extracts text chunks from uploaded files for storage and LLM retrieval.
/// </summary>
/// <remarks>
/// <para>
/// OCR seam (v1.1): For image/* content types, this interface should be extended or
/// a dedicated IOcrExtractionService injected to run Tesseract/ML Kit and return text
/// before chunking. In v1, images return a placeholder message.
/// </para>
/// <para>
/// LLM structured-extraction seam: After chunking, the LLM shard's IExtractionService
/// can consume chunks via <c>ExtractMealAsync</c> / <c>ExtractPlanAsync</c> etc. to
/// produce typed structured data. The file pipeline only produces raw text chunks.
/// </para>
/// </remarks>
public interface IDocumentExtractionService
{
    Task<IReadOnlyList<string>> ExtractChunksAsync(string filePath, string contentType, CancellationToken ct = default);
}
