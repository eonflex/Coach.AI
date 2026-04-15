namespace CoachAI.Api.DTOs;

public record CreateFoodItemRequest(
    string Name,
    string? Brand,
    decimal ServingSizeGrams,
    decimal Calories,
    decimal ProteinGrams,
    decimal CarbsGrams,
    decimal FatGrams,
    decimal FiberGrams
);

public record UpdateFoodItemRequest(
    string? Name,
    string? Brand,
    decimal? ServingSizeGrams,
    decimal? Calories,
    decimal? ProteinGrams,
    decimal? CarbsGrams,
    decimal? FatGrams,
    decimal? FiberGrams
);

public record FoodItemResponse(
    int Id,
    string Name,
    string? Brand,
    decimal ServingSizeGrams,
    decimal Calories,
    decimal ProteinGrams,
    decimal CarbsGrams,
    decimal FatGrams,
    decimal FiberGrams,
    bool IsCustom,
    DateTime CreatedAt
);

public record LogFoodRequest(
    int FoodItemId,
    decimal ServingsConsumed,
    string? Meal,
    string? Notes,
    DateTime? LoggedAt
);

public record FoodLogResponse(
    int Id,
    int FoodItemId,
    string FoodItemName,
    decimal ServingsConsumed,
    decimal Calories,
    decimal ProteinGrams,
    decimal CarbsGrams,
    decimal FatGrams,
    decimal FiberGrams,
    string? Meal,
    string? Notes,
    DateTime LoggedAt
);

public record DailySummaryResponse(
    DateTime Date,
    decimal TotalCalories,
    decimal TotalProteinGrams,
    decimal TotalCarbsGrams,
    decimal TotalFatGrams,
    decimal TotalFiberGrams,
    List<FoodLogResponse> Entries
);
