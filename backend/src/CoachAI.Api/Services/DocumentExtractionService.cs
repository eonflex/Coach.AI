using System.Text;
using DocumentFormat.OpenXml.Packaging;
using DocumentFormat.OpenXml.Wordprocessing;
using UglyToad.PdfPig;

namespace CoachAI.Api.Services;

public class DocumentExtractionService : IDocumentExtractionService
{
    private const int ChunkSize = 800;
    private readonly ILogger<DocumentExtractionService> _logger;

    public DocumentExtractionService(ILogger<DocumentExtractionService> logger)
    {
        _logger = logger;
    }

    public Task<IReadOnlyList<string>> ExtractChunksAsync(string filePath, string contentType, CancellationToken ct = default)
    {
        var text = contentType switch
        {
            "application/pdf" => ExtractPdf(filePath),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" => ExtractDocx(filePath),
            "text/plain" => File.ReadAllText(filePath),
            _ when contentType.StartsWith("image/") => string.Empty,
            _ => TryExtractAsText(filePath)
        };

        var chunks = SplitIntoChunks(text, ChunkSize);
        return Task.FromResult<IReadOnlyList<string>>(chunks);
    }

    private static string ExtractPdf(string filePath)
    {
        var sb = new StringBuilder();
        try
        {
            using var pdf = PdfDocument.Open(filePath);
            foreach (var page in pdf.GetPages())
            {
                sb.AppendLine(page.Text);
            }
        }
        catch (Exception)
        {
            // Return empty if can't parse
        }
        return sb.ToString();
    }

    private static string ExtractDocx(string filePath)
    {
        var sb = new StringBuilder();
        try
        {
            using var doc = WordprocessingDocument.Open(filePath, false);
            var body = doc.MainDocumentPart?.Document?.Body;
            if (body != null)
            {
                foreach (var para in body.Elements<Paragraph>())
                {
                    sb.AppendLine(para.InnerText);
                }
            }
        }
        catch (Exception)
        {
            // Return empty if can't parse
        }
        return sb.ToString();
    }

    private static string TryExtractAsText(string filePath)
    {
        try { return File.ReadAllText(filePath); }
        catch { return string.Empty; }
    }

    private static List<string> SplitIntoChunks(string text, int chunkSize)
    {
        var chunks = new List<string>();
        if (string.IsNullOrWhiteSpace(text)) return chunks;

        var sentences = text.Split(new[] { "\r\n\r\n", "\n\n", ". ", "! ", "? " }, StringSplitOptions.RemoveEmptyEntries);
        var current = new StringBuilder();

        foreach (var sentence in sentences)
        {
            if (current.Length + sentence.Length > chunkSize && current.Length > 0)
            {
                chunks.Add(current.ToString().Trim());
                current.Clear();
            }
            current.Append(sentence).Append(' ');
        }

        if (current.Length > 0)
            chunks.Add(current.ToString().Trim());

        return chunks;
    }
}
