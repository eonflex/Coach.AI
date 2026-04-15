using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class WorkoutEndpoints
{
    public static void MapWorkoutEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/workouts").WithTags("Workouts");

        group.MapGet("/", GetWorkoutLogs);
        group.MapGet("/{id:int}", GetWorkoutLog);
        group.MapPost("/", CreateWorkoutLog);
        group.MapPut("/{id:int}", UpdateWorkoutLog);
        group.MapDelete("/{id:int}", DeleteWorkoutLog);
    }

    private static async Task<IResult> GetWorkoutLogs(AppDbContext db, DateTime? date, int page = 1, int pageSize = 20, CancellationToken ct = default)
    {
        var query = db.WorkoutLogs.Include(w => w.Exercises).AsQueryable();
        if (date.HasValue)
            query = query.Where(w => w.LoggedAt.Date == date.Value.Date);

        var total = await query.CountAsync(ct);
        var items = await query
            .OrderByDescending(w => w.LoggedAt)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(w => MapWorkout(w))
            .ToListAsync(ct);

        return Results.Ok(new PagedResponse<WorkoutLogResponse>(items, total, page, pageSize));
    }

    private static async Task<IResult> GetWorkoutLog(int id, AppDbContext db, CancellationToken ct)
    {
        var w = await db.WorkoutLogs.Include(w => w.Exercises).FirstOrDefaultAsync(w => w.Id == id, ct);
        if (w == null) return Results.NotFound(new ErrorResponse("Workout log not found"));
        return Results.Ok(MapWorkout(w));
    }

    private static async Task<IResult> CreateWorkoutLog(CreateWorkoutLogRequest req, AppDbContext db, CancellationToken ct)
    {
        var log = new WorkoutLog
        {
            WorkoutType = req.WorkoutType,
            Notes = req.Notes,
            CardioDurationMinutes = req.CardioDurationMinutes,
            LoggedAt = req.LoggedAt?.ToUniversalTime() ?? DateTime.UtcNow,
            Exercises = req.Exercises.Select(e => new Exercise
            {
                Name = e.Name,
                Sets = e.Sets,
                Reps = e.Reps,
                WeightKg = e.WeightKg,
                Notes = e.Notes
            }).ToList()
        };
        db.WorkoutLogs.Add(log);
        await db.SaveChangesAsync(ct);
        return Results.Created($"/api/workouts/{log.Id}", MapWorkout(log));
    }

    private static async Task<IResult> UpdateWorkoutLog(int id, UpdateWorkoutLogRequest req, AppDbContext db, CancellationToken ct)
    {
        var log = await db.WorkoutLogs.Include(w => w.Exercises).FirstOrDefaultAsync(w => w.Id == id, ct);
        if (log == null) return Results.NotFound(new ErrorResponse("Workout log not found"));

        if (req.WorkoutType != null) log.WorkoutType = req.WorkoutType;
        if (req.Notes != null) log.Notes = req.Notes;
        if (req.CardioDurationMinutes.HasValue) log.CardioDurationMinutes = req.CardioDurationMinutes.Value;

        await db.SaveChangesAsync(ct);
        return Results.Ok(MapWorkout(log));
    }

    private static async Task<IResult> DeleteWorkoutLog(int id, AppDbContext db, CancellationToken ct)
    {
        var log = await db.WorkoutLogs.FindAsync(new object[] { id }, ct);
        if (log == null) return Results.NotFound(new ErrorResponse("Workout log not found"));
        db.WorkoutLogs.Remove(log);
        await db.SaveChangesAsync(ct);
        return Results.NoContent();
    }

    private static WorkoutLogResponse MapWorkout(WorkoutLog w) => new(
        w.Id, w.WorkoutType, w.Notes, w.CardioDurationMinutes, w.LoggedAt,
        w.Exercises.Select(e => new ExerciseResponse(e.Id, e.Name, e.Sets, e.Reps, e.WeightKg, e.Notes)).ToList()
    );
}
