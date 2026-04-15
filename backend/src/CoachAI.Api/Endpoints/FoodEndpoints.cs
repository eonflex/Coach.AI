using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class FoodEndpoints
{
    public static void MapFoodEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/foods").WithTags("Foods");

        group.MapGet("/items", GetFoodItems);
        group.MapGet("/items/{id:int}", GetFoodItem);
        group.MapPost("/items", CreateFoodItem);
        group.MapPut("/items/{id:int}", UpdateFoodItem);
        group.MapDelete("/items/{id:int}", DeleteFoodItem);

        group.MapGet("/logs", GetFoodLogs);
        group.MapGet("/logs/summary", GetDailySummary);
        group.MapPost("/logs", LogFood);
        group.MapDelete("/logs/{id:int}", DeleteFoodLog);
    }

    private static async Task<IResult> GetFoodItems(AppDbContext db, string? search, CancellationToken ct)
    {
        var query = db.FoodItems.AsQueryable();
        if (!string.IsNullOrWhiteSpace(search))
            query = query.Where(f => f.Name.ToLower().Contains(search.ToLower()));

        var items = await query
            .OrderBy(f => f.Name)
            .Select(f => new FoodItemResponse(f.Id, f.Name, f.Brand, f.ServingSizeGrams, f.Calories,
                f.ProteinGrams, f.CarbsGrams, f.FatGrams, f.FiberGrams, f.IsCustom, f.CreatedAt))
            .ToListAsync(ct);

        return Results.Ok(items);
    }

    private static async Task<IResult> GetFoodItem(int id, AppDbContext db, CancellationToken ct)
    {
        var f = await db.FoodItems.FindAsync(new object[] { id }, ct);
        if (f == null) return Results.NotFound(new ErrorResponse("Food item not found"));
        return Results.Ok(new FoodItemResponse(f.Id, f.Name, f.Brand, f.ServingSizeGrams, f.Calories,
            f.ProteinGrams, f.CarbsGrams, f.FatGrams, f.FiberGrams, f.IsCustom, f.CreatedAt));
    }

    private static async Task<IResult> CreateFoodItem(CreateFoodItemRequest req, AppDbContext db, CancellationToken ct)
    {
        var item = new FoodItem
        {
            Name = req.Name,
            Brand = req.Brand,
            ServingSizeGrams = req.ServingSizeGrams,
            Calories = req.Calories,
            ProteinGrams = req.ProteinGrams,
            CarbsGrams = req.CarbsGrams,
            FatGrams = req.FatGrams,
            FiberGrams = req.FiberGrams,
            IsCustom = true
        };
        db.FoodItems.Add(item);
        await db.SaveChangesAsync(ct);
        return Results.Created($"/api/foods/items/{item.Id}",
            new FoodItemResponse(item.Id, item.Name, item.Brand, item.ServingSizeGrams, item.Calories,
                item.ProteinGrams, item.CarbsGrams, item.FatGrams, item.FiberGrams, item.IsCustom, item.CreatedAt));
    }

    private static async Task<IResult> UpdateFoodItem(int id, UpdateFoodItemRequest req, AppDbContext db, CancellationToken ct)
    {
        var item = await db.FoodItems.FindAsync(new object[] { id }, ct);
        if (item == null) return Results.NotFound(new ErrorResponse("Food item not found"));

        if (req.Name != null) item.Name = req.Name;
        if (req.Brand != null) item.Brand = req.Brand;
        if (req.ServingSizeGrams.HasValue) item.ServingSizeGrams = req.ServingSizeGrams.Value;
        if (req.Calories.HasValue) item.Calories = req.Calories.Value;
        if (req.ProteinGrams.HasValue) item.ProteinGrams = req.ProteinGrams.Value;
        if (req.CarbsGrams.HasValue) item.CarbsGrams = req.CarbsGrams.Value;
        if (req.FatGrams.HasValue) item.FatGrams = req.FatGrams.Value;
        if (req.FiberGrams.HasValue) item.FiberGrams = req.FiberGrams.Value;

        await db.SaveChangesAsync(ct);
        return Results.Ok(new FoodItemResponse(item.Id, item.Name, item.Brand, item.ServingSizeGrams, item.Calories,
            item.ProteinGrams, item.CarbsGrams, item.FatGrams, item.FiberGrams, item.IsCustom, item.CreatedAt));
    }

    private static async Task<IResult> DeleteFoodItem(int id, AppDbContext db, CancellationToken ct)
    {
        var item = await db.FoodItems.FindAsync(new object[] { id }, ct);
        if (item == null) return Results.NotFound(new ErrorResponse("Food item not found"));
        db.FoodItems.Remove(item);
        await db.SaveChangesAsync(ct);
        return Results.NoContent();
    }

    private static async Task<IResult> GetFoodLogs(AppDbContext db, DateTime? date, CancellationToken ct)
    {
        var query = db.FoodLogs.Include(f => f.FoodItem).AsQueryable();
        if (date.HasValue)
            query = query.Where(f => f.LoggedAt.Date == date.Value.Date);

        var logs = await query
            .OrderByDescending(f => f.LoggedAt)
            .Select(f => MapFoodLog(f))
            .ToListAsync(ct);

        return Results.Ok(logs);
    }

    private static async Task<IResult> GetDailySummary(AppDbContext db, DateTime? date, CancellationToken ct)
    {
        var targetDate = (date ?? DateTime.UtcNow).Date;
        var logs = await db.FoodLogs
            .Include(f => f.FoodItem)
            .Where(f => f.LoggedAt.Date == targetDate)
            .ToListAsync(ct);

        var entries = logs.Select(f => MapFoodLog(f)).ToList();
        var summary = new DailySummaryResponse(
            Date: targetDate,
            TotalCalories: entries.Sum(e => e.Calories),
            TotalProteinGrams: entries.Sum(e => e.ProteinGrams),
            TotalCarbsGrams: entries.Sum(e => e.CarbsGrams),
            TotalFatGrams: entries.Sum(e => e.FatGrams),
            TotalFiberGrams: entries.Sum(e => e.FiberGrams),
            Entries: entries
        );

        return Results.Ok(summary);
    }

    private static async Task<IResult> LogFood(LogFoodRequest req, AppDbContext db, CancellationToken ct)
    {
        var foodItem = await db.FoodItems.FindAsync(new object[] { req.FoodItemId }, ct);
        if (foodItem == null) return Results.NotFound(new ErrorResponse("Food item not found"));

        var log = new FoodLog
        {
            FoodItemId = req.FoodItemId,
            ServingsConsumed = req.ServingsConsumed,
            Meal = req.Meal,
            Notes = req.Notes,
            LoggedAt = req.LoggedAt?.ToUniversalTime() ?? DateTime.UtcNow
        };
        db.FoodLogs.Add(log);
        await db.SaveChangesAsync(ct);

        log.FoodItem = foodItem;
        return Results.Created($"/api/foods/logs/{log.Id}", MapFoodLog(log));
    }

    private static async Task<IResult> DeleteFoodLog(int id, AppDbContext db, CancellationToken ct)
    {
        var log = await db.FoodLogs.FindAsync(new object[] { id }, ct);
        if (log == null) return Results.NotFound(new ErrorResponse("Food log not found"));
        db.FoodLogs.Remove(log);
        await db.SaveChangesAsync(ct);
        return Results.NoContent();
    }

    private static FoodLogResponse MapFoodLog(FoodLog f) => new(
        f.Id, f.FoodItemId, f.FoodItem.Name,
        f.ServingsConsumed,
        f.FoodItem.Calories * f.ServingsConsumed,
        f.FoodItem.ProteinGrams * f.ServingsConsumed,
        f.FoodItem.CarbsGrams * f.ServingsConsumed,
        f.FoodItem.FatGrams * f.ServingsConsumed,
        f.FoodItem.FiberGrams * f.ServingsConsumed,
        f.Meal, f.Notes, f.LoggedAt
    );
}
