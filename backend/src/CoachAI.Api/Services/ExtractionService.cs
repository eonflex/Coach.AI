using System.Text.Json;
using CoachAI.Api.Models.Extraction;

namespace CoachAI.Api.Services;

public class ExtractionService : IExtractionService
{
    private readonly IModelService _model;
    private readonly IWebHostEnvironment _env;
    private readonly ILogger<ExtractionService> _logger;

    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    public ExtractionService(IModelService model, IWebHostEnvironment env, ILogger<ExtractionService> logger)
    {
        _model = model;
        _env = env;
        _logger = logger;
    }

    public async Task<ExtractedMeal?> ExtractMealAsync(string text, CancellationToken ct = default)
    {
        var result = await ExtractAsync<ExtractedMeal>("meal_extraction.txt", text, ct);
        if (result is { Confidence: "low" })
            _logger.LogWarning("Low-confidence meal extraction — result should be reviewed by user before saving");
        return result;
    }

    public Task<ExtractedWorkout?> ExtractWorkoutAsync(string text, CancellationToken ct = default)
        => ExtractAsync<ExtractedWorkout>("workout_extraction.txt", text, ct);

    public Task<ExtractedNutritionLabel?> ExtractNutritionLabelAsync(string text, CancellationToken ct = default)
        => ExtractAsync<ExtractedNutritionLabel>("nutrition_label.txt", text, ct);

    public Task<ExtractedPlan?> ExtractPlanAsync(string text, CancellationToken ct = default)
        => ExtractAsync<ExtractedPlan>("plan_extraction.txt", text, ct);

    private async Task<T?> ExtractAsync<T>(string promptFileName, string userText, CancellationToken ct)
        where T : class
    {
        if (!await _model.IsAvailableAsync(ct))
        {
            _logger.LogWarning("Extraction skipped — model unavailable");
            return null;
        }

        var promptPath = Path.Combine(_env.ContentRootPath, "Prompts", promptFileName);
        string systemPrompt;
        try
        {
            systemPrompt = await File.ReadAllTextAsync(promptPath, ct);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to load extraction prompt: {PromptFile}", promptFileName);
            return null;
        }

        string rawJson;
        try
        {
            rawJson = await _model.GenerateStructuredAsync(systemPrompt, userText, ct);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Model call failed during extraction ({PromptFile})", promptFileName);
            return null;
        }

        try
        {
            return JsonSerializer.Deserialize<T>(rawJson, JsonOptions);
        }
        catch (JsonException ex)
        {
            _logger.LogWarning(ex, "Failed to deserialize extraction result for {Type}. Raw: {Raw}",
                typeof(T).Name, rawJson.Length > 200 ? rawJson[..200] + "..." : rawJson);
            return null;
        }
    }
}
