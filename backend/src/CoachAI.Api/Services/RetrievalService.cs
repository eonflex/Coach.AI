using CoachAI.Api.Data;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Services;

public class RetrievalService : IRetrievalService
{
    private readonly AppDbContext _db;

    public RetrievalService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<IReadOnlyList<string>> RetrieveRelevantChunksAsync(string query, int maxChunks = 5, CancellationToken ct = default)
    {
        var keywords = query.ToLower()
            .Split(' ', StringSplitOptions.RemoveEmptyEntries)
            .Where(k => k.Length > 3)
            .Distinct()
            .Take(5)
            .ToList();

        if (!keywords.Any())
        {
            return await _db.DocumentChunks
                .OrderByDescending(c => c.CreatedAt)
                .Take(maxChunks)
                .Select(c => c.TextContent)
                .ToListAsync(ct);
        }

        var chunks = await _db.DocumentChunks
            .Select(c => new { c.TextContent, c.CreatedAt })
            .ToListAsync(ct);

        var scored = chunks
            .Select(c => new
            {
                c.TextContent,
                Score = keywords.Count(k => c.TextContent.ToLower().Contains(k))
            })
            .Where(c => c.Score > 0)
            .OrderByDescending(c => c.Score)
            .Take(maxChunks)
            .Select(c => c.TextContent)
            .ToList();

        if (!scored.Any())
        {
            return await _db.DocumentChunks
                .OrderByDescending(c => c.CreatedAt)
                .Take(maxChunks)
                .Select(c => c.TextContent)
                .ToListAsync(ct);
        }

        return scored;
    }
}
