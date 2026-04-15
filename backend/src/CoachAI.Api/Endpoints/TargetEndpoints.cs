using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class TargetEndpoints
{
    public static void MapTargetEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/targets").WithTags("Targets");

        group.MapGet("/active", GetActiveTarget);
        group.MapGet("/", GetAllTargets);
        group.MapPost("/", SetTarget);
        group.MapDelete("/{id:int}", DeleteTarget);
    }

    private static async Task<IResult> GetActiveTarget(AppDbContext db, CancellationToken ct)
    {
        var t = await db.UserTargets
            .Where(t => t.IsActive)
            .OrderByDescending(t => t.CreatedAt)
            .FirstOrDefaultAsync(ct);
        if (t == null) return Results.NotFound(new ErrorResponse("No active target found"));
        return Results.Ok(MapTarget(t));
    }

    private static async Task<IResult> GetAllTargets(AppDbContext db, CancellationToken ct)
    {
        var targets = await db.UserTargets
            .OrderByDescending(t => t.CreatedAt)
            .Select(t => MapTarget(t))
            .ToListAsync(ct);
        return Results.Ok(targets);
    }

    private static async Task<IResult> SetTarget(SetTargetRequest req, AppDbContext db, CancellationToken ct)
    {
        await db.UserTargets.Where(t => t.IsActive).ExecuteUpdateAsync(t => t.SetProperty(x => x.IsActive, false), ct);

        var target = new UserTarget
        {
            Phase = req.Phase,
            TargetCalories = req.TargetCalories,
            TargetProteinGrams = req.TargetProteinGrams,
            TargetCarbsGrams = req.TargetCarbsGrams,
            TargetFatGrams = req.TargetFatGrams,
            TargetFiberGrams = req.TargetFiberGrams,
            TargetWeightKg = req.TargetWeightKg,
            IsActive = true
        };
        db.UserTargets.Add(target);
        await db.SaveChangesAsync(ct);
        return Results.Created($"/api/targets/{target.Id}", MapTarget(target));
    }

    private static async Task<IResult> DeleteTarget(int id, AppDbContext db, CancellationToken ct)
    {
        var t = await db.UserTargets.FindAsync(new object[] { id }, ct);
        if (t == null) return Results.NotFound(new ErrorResponse("Target not found"));
        db.UserTargets.Remove(t);
        await db.SaveChangesAsync(ct);
        return Results.NoContent();
    }

    private static TargetResponse MapTarget(UserTarget t) => new(
        t.Id, t.Phase, t.TargetCalories, t.TargetProteinGrams, t.TargetCarbsGrams,
        t.TargetFatGrams, t.TargetFiberGrams, t.TargetWeightKg, t.IsActive, t.CreatedAt
    );
}
