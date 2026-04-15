using CoachAI.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<FoodItem> FoodItems => Set<FoodItem>();
    public DbSet<FoodLog> FoodLogs => Set<FoodLog>();
    public DbSet<WorkoutLog> WorkoutLogs => Set<WorkoutLog>();
    public DbSet<Exercise> Exercises => Set<Exercise>();
    public DbSet<WeightLog> WeightLogs => Set<WeightLog>();
    public DbSet<UploadedDocument> UploadedDocuments => Set<UploadedDocument>();
    public DbSet<DocumentChunk> DocumentChunks => Set<DocumentChunk>();
    public DbSet<UserTarget> UserTargets => Set<UserTarget>();
    public DbSet<ChatMessage> ChatMessages => Set<ChatMessage>();
    public DbSet<MealPlan> MealPlans => Set<MealPlan>();
    public DbSet<PlanItem> PlanItems => Set<PlanItem>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<FoodItem>(e =>
        {
            e.HasIndex(f => f.Name);
            e.Property(f => f.Calories).HasPrecision(8, 2);
            e.Property(f => f.ProteinGrams).HasPrecision(8, 2);
            e.Property(f => f.CarbsGrams).HasPrecision(8, 2);
            e.Property(f => f.FatGrams).HasPrecision(8, 2);
            e.Property(f => f.FiberGrams).HasPrecision(8, 2);
            e.Property(f => f.ServingSizeGrams).HasPrecision(8, 2);
        });

        modelBuilder.Entity<FoodLog>(e =>
        {
            e.HasOne(f => f.FoodItem).WithMany(fi => fi.FoodLogs).HasForeignKey(f => f.FoodItemId).OnDelete(DeleteBehavior.Restrict);
            // Composite index for date-range queries on food logs
            e.HasIndex(f => new { f.LoggedAt, f.FoodItemId });
        });

        modelBuilder.Entity<Exercise>(e =>
        {
            e.HasOne(ex => ex.WorkoutLog).WithMany(w => w.Exercises).HasForeignKey(ex => ex.WorkoutLogId).OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<DocumentChunk>(e =>
        {
            e.HasOne(dc => dc.Document).WithMany(d => d.Chunks).HasForeignKey(dc => dc.DocumentId).OnDelete(DeleteBehavior.Cascade);
            // Composite index for chunk retrieval in order
            e.HasIndex(dc => new { dc.DocumentId, dc.ChunkIndex });
        });

        modelBuilder.Entity<UserTarget>(e =>
        {
            e.HasIndex(u => u.Phase);
        });

        modelBuilder.Entity<WeightLog>(e =>
        {
            e.HasIndex(w => w.LoggedAt);
        });

        modelBuilder.Entity<WorkoutLog>(e =>
        {
            e.HasIndex(w => w.LoggedAt);
        });

        // MealPlan → PlanItem (cascade delete)
        modelBuilder.Entity<MealPlan>(e =>
        {
            e.HasMany(mp => mp.Items).WithOne(pi => pi.MealPlan).HasForeignKey(pi => pi.MealPlanId).OnDelete(DeleteBehavior.Cascade);
            e.HasIndex(mp => mp.IsActive);
        });

        // PlanItem → FoodItem (optional, restrict so food items can't be deleted while referenced)
        modelBuilder.Entity<PlanItem>(e =>
        {
            e.HasOne(pi => pi.FoodItem).WithMany().HasForeignKey(pi => pi.FoodItemId).IsRequired(false).OnDelete(DeleteBehavior.Restrict);
            e.Property(pi => pi.TargetCalories).HasPrecision(8, 2);
            e.Property(pi => pi.TargetProteinGrams).HasPrecision(8, 2);
            e.Property(pi => pi.TargetCarbsGrams).HasPrecision(8, 2);
            e.Property(pi => pi.TargetFatGrams).HasPrecision(8, 2);
        });

        // Store ExtractionStatus as integer
        modelBuilder.Entity<UploadedDocument>(e =>
        {
            e.Property(d => d.ExtractionStatus).HasConversion<int>();
        });
    }
}
