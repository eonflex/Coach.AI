namespace CoachAI.Api.Models;

public class FoodItem
{
    public int Id { get; set; }
    public required string Name { get; set; }
    public string? Brand { get; set; }
    public decimal ServingSizeGrams { get; set; }
    public decimal Calories { get; set; }
    public decimal ProteinGrams { get; set; }
    public decimal CarbsGrams { get; set; }
    public decimal FatGrams { get; set; }
    public decimal FiberGrams { get; set; }
    public bool IsCustom { get; set; } = true;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public ICollection<FoodLog> FoodLogs { get; set; } = new List<FoodLog>();
}
