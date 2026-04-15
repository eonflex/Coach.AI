namespace CoachAI.Api.Models;

public class FoodLog
{
    public int Id { get; set; }
    public int FoodItemId { get; set; }
    public FoodItem FoodItem { get; set; } = null!;
    public decimal ServingsConsumed { get; set; } = 1;
    public string? Meal { get; set; }
    public string? Notes { get; set; }
    public DateTime LoggedAt { get; set; } = DateTime.UtcNow;
}
