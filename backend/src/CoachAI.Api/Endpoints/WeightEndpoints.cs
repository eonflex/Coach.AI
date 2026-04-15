using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class WeightEndpoints
{
    public static void MapWeightEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/weight").WithTags("Weight");

        group.MapGet("/", GetWeightLogs);
        group.MapGet("/latest", GetLatestWeight);
        group.MapPost("/", LogWeight);
        group.MapDelete("/{id:int}", DeleteWeightLog);
    }

    private static async Task<IResult> GetWeightLogs(AppDbContext db, int page = 1, int pageSize = 30, CancellationToken ct = default)
    {
        var total = await db.WeightLogs.CountAsync(ct);
        var items = await db.WeightLogs
            .OrderByDescending(w => w.LoggedAt)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(w => new WeightLogResponse(w.Id, w.WeightKg, w.Notes, w.LoggedAt))
            .ToListAsync(ct);

        return Results.Ok(new PagedResponse<WeightLogResponse>(items, total, page, pageSize));
    }

    private static async Task<IResult> GetLatestWeight(AppDbContext db, CancellationToken ct)
    {
        var w = await db.WeightLogs.OrderByDescending(w => w.LoggedAt).FirstOrDefaultAsync(ct);
        if (w == null) return Results.NotFound(new ErrorResponse("No weight logs found"));
        return Results.Ok(new WeightLogResponse(w.Id, w.WeightKg, w.Notes, w.LoggedAt));
    }

    private static async Task<IResult> LogWeight(LogWeightRequest req, AppDbContext db, CancellationToken ct)
    {
        var log = new WeightLog
        {
            WeightKg = req.WeightKg,
            Notes = req.Notes,
            LoggedAt = req.LoggedAt?.ToUniversalTime() ?? DateTime.UtcNow
        };
        db.WeightLogs.Add(log);
        await db.SaveChangesAsync(ct);
        return Results.Created($"/api/weight/{log.Id}", new WeightLogResponse(log.Id, log.WeightKg, log.Notes, log.LoggedAt));
    }

    private static async Task<IResult> DeleteWeightLog(int id, AppDbContext db, CancellationToken ct)
    {
        var log = await db.WeightLogs.FindAsync(new object[] { id }, ct);
        if (log == null) return Results.NotFound(new ErrorResponse("Weight log not found"));
        db.WeightLogs.Remove(log);
        await db.SaveChangesAsync(ct);
        return Results.NoContent();
    }
}
