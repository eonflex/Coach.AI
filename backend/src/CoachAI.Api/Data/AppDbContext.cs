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

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<FoodItem>(e =>
        {
            e.HasIndex(f => f.Name);
        });

        modelBuilder.Entity<FoodLog>(e =>
        {
            e.HasOne(f => f.FoodItem).WithMany(fi => fi.FoodLogs).HasForeignKey(f => f.FoodItemId).OnDelete(DeleteBehavior.Restrict);
            e.HasIndex(f => f.LoggedAt);
        });

        modelBuilder.Entity<Exercise>(e =>
        {
            e.HasOne(ex => ex.WorkoutLog).WithMany(w => w.Exercises).HasForeignKey(ex => ex.WorkoutLogId).OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<DocumentChunk>(e =>
        {
            e.HasOne(dc => dc.Document).WithMany(d => d.Chunks).HasForeignKey(dc => dc.DocumentId).OnDelete(DeleteBehavior.Cascade);
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
    }
}
