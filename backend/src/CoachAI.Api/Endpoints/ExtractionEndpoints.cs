using CoachAI.Api.Models.Extraction;
using CoachAI.Api.Services;

namespace CoachAI.Api.Endpoints;

public static class ExtractionEndpoints
{
    public static void MapExtractionEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/extract").WithTags("Extraction");

        group.MapPost("/meal", async (ExtractionRequest request, IExtractionService extraction, CancellationToken ct) =>
        {
            if (string.IsNullOrWhiteSpace(request.Text))
                return Results.BadRequest("Text is required.");
            var result = await extraction.ExtractMealAsync(request.Text, ct);
            return result is null ? Results.StatusCode(503) : Results.Ok(result);
        })
        .WithSummary("Extract structured meal data from free text")
        .Produces<ExtractedMeal>(200)
        .Produces(400)
        .Produces(503);

        group.MapPost("/workout", async (ExtractionRequest request, IExtractionService extraction, CancellationToken ct) =>
        {
            if (string.IsNullOrWhiteSpace(request.Text))
                return Results.BadRequest("Text is required.");
            var result = await extraction.ExtractWorkoutAsync(request.Text, ct);
            return result is null ? Results.StatusCode(503) : Results.Ok(result);
        })
        .WithSummary("Extract structured workout data from free text")
        .Produces<ExtractedWorkout>(200)
        .Produces(400)
        .Produces(503);

        group.MapPost("/nutrition-label", async (ExtractionRequest request, IExtractionService extraction, CancellationToken ct) =>
        {
            if (string.IsNullOrWhiteSpace(request.Text))
                return Results.BadRequest("Text is required.");
            var result = await extraction.ExtractNutritionLabelAsync(request.Text, ct);
            return result is null ? Results.StatusCode(503) : Results.Ok(result);
        })
        .WithSummary("Extract structured nutrition label data from OCR text")
        .Produces<ExtractedNutritionLabel>(200)
        .Produces(400)
        .Produces(503);

        group.MapPost("/plan", async (ExtractionRequest request, IExtractionService extraction, CancellationToken ct) =>
        {
            if (string.IsNullOrWhiteSpace(request.Text))
                return Results.BadRequest("Text is required.");
            var result = await extraction.ExtractPlanAsync(request.Text, ct);
            return result is null ? Results.StatusCode(503) : Results.Ok(result);
        })
        .WithSummary("Extract a structured diet/workout plan from document text")
        .Produces<ExtractedPlan>(200)
        .Produces(400)
        .Produces(503);
    }
}

public record ExtractionRequest(string Text);
