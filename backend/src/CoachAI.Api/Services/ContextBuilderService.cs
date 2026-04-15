using CoachAI.Api.Data;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Services;

public class ContextBuilderService
{
    private readonly AppDbContext _db;
    private readonly IRetrievalService _retrieval;

    public ContextBuilderService(AppDbContext db, IRetrievalService retrieval)
    {
        _db = db;
        _retrieval = retrieval;
    }

    public async Task<string> BuildContextAsync(string userMessage, bool includeRecentLogs, CancellationToken ct = default)
    {
        var sb = new System.Text.StringBuilder();

        var target = await _db.UserTargets
            .Where(t => t.IsActive)
            .OrderByDescending(t => t.CreatedAt)
            .FirstOrDefaultAsync(ct);

        if (target != null)
        {
            sb.AppendLine($"[USER TARGETS - Phase: {target.Phase}]");
            if (target.TargetCalories.HasValue) sb.AppendLine($"  Calories: {target.TargetCalories} kcal");
            if (target.TargetProteinGrams.HasValue) sb.AppendLine($"  Protein: {target.TargetProteinGrams}g");
            if (target.TargetCarbsGrams.HasValue) sb.AppendLine($"  Carbs: {target.TargetCarbsGrams}g");
            if (target.TargetFatGrams.HasValue) sb.AppendLine($"  Fat: {target.TargetFatGrams}g");
            if (target.TargetFiberGrams.HasValue) sb.AppendLine($"  Fiber: {target.TargetFiberGrams}g");
            if (target.TargetWeightKg.HasValue) sb.AppendLine($"  Target Weight: {target.TargetWeightKg} kg");
            sb.AppendLine();
        }

        if (includeRecentLogs)
        {
            var today = DateTime.UtcNow.Date;
            var foodLogs = await _db.FoodLogs
                .Include(f => f.FoodItem)
                .Where(f => f.LoggedAt.Date == today)
                .OrderBy(f => f.LoggedAt)
                .ToListAsync(ct);

            if (foodLogs.Any())
            {
                sb.AppendLine("[TODAY'S FOOD LOGS]");
                decimal totalCal = 0, totalProt = 0, totalCarb = 0, totalFat = 0;
                foreach (var log in foodLogs)
                {
                    var cal = log.FoodItem.Calories * log.ServingsConsumed;
                    var prot = log.FoodItem.ProteinGrams * log.ServingsConsumed;
                    var carb = log.FoodItem.CarbsGrams * log.ServingsConsumed;
                    var fat = log.FoodItem.FatGrams * log.ServingsConsumed;
                    totalCal += cal; totalProt += prot; totalCarb += carb; totalFat += fat;
                    sb.AppendLine($"  {log.FoodItem.Name} x{log.ServingsConsumed} ({log.Meal ?? "?"}): {cal:F0} kcal P:{prot:F1}g C:{carb:F1}g F:{fat:F1}g");
                }
                sb.AppendLine($"  TOTAL: {totalCal:F0} kcal P:{totalProt:F1}g C:{totalCarb:F1}g F:{totalFat:F1}g");
                sb.AppendLine();
            }

            var recentWeight = await _db.WeightLogs
                .OrderByDescending(w => w.LoggedAt)
                .FirstOrDefaultAsync(ct);
            if (recentWeight != null)
            {
                sb.AppendLine($"[LAST WEIGHT] {recentWeight.WeightKg} kg on {recentWeight.LoggedAt:yyyy-MM-dd}");
                sb.AppendLine();
            }

            var recentWorkouts = await _db.WorkoutLogs
                .Include(w => w.Exercises)
                .OrderByDescending(w => w.LoggedAt)
                .Take(3)
                .ToListAsync(ct);
            if (recentWorkouts.Any())
            {
                sb.AppendLine("[RECENT WORKOUTS]");
                foreach (var wl in recentWorkouts)
                {
                    sb.AppendLine($"  {wl.LoggedAt:yyyy-MM-dd} {wl.WorkoutType}: {wl.Exercises.Count} exercises");
                }
                sb.AppendLine();
            }
        }

        var chunks = await _retrieval.RetrieveRelevantChunksAsync(userMessage, maxChunks: 3, ct);
        if (chunks.Any())
        {
            sb.AppendLine("[UPLOADED PLAN CONTEXT]");
            foreach (var chunk in chunks)
            {
                sb.AppendLine($"  {chunk}");
            }
            sb.AppendLine();
        }

        return sb.ToString();
    }
}
