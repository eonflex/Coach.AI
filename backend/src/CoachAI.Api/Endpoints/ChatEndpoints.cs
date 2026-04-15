using CoachAI.Api.Data;
using CoachAI.Api.DTOs;
using CoachAI.Api.Models;
using CoachAI.Api.Services;
using Microsoft.EntityFrameworkCore;

namespace CoachAI.Api.Endpoints;

public static class ChatEndpoints
{
    public static void MapChatEndpoints(this WebApplication app)
    {
        var group = app.MapGroup("/api/chat").WithTags("Chat");

        group.MapGet("/history", GetChatHistory);
        group.MapPost("/", SendMessage);
        group.MapDelete("/history", ClearHistory);
    }

    private static async Task<IResult> GetChatHistory(AppDbContext db, int limit = 50, CancellationToken ct = default)
    {
        var history = await db.ChatMessages
            .OrderByDescending(m => m.CreatedAt)
            .Take(limit)
            .OrderBy(m => m.CreatedAt)
            .Select(m => new ChatHistoryResponse(m.Id, m.Role, m.Content, m.CreatedAt))
            .ToListAsync(ct);
        return Results.Ok(history);
    }

    private static async Task<IResult> SendMessage(
        ChatRequest req,
        AppDbContext db,
        IModelService modelService,
        ContextBuilderService contextBuilder,
        IWebHostEnvironment env,
        CancellationToken ct)
    {
        var userMsg = new ChatMessage { Role = "user", Content = req.Message };
        db.ChatMessages.Add(userMsg);
        await db.SaveChangesAsync(ct);

        var context = await contextBuilder.BuildContextAsync(req.Message, req.IncludeRecentLogs, ct);

        var systemPromptPath = Path.Combine(env.ContentRootPath, "Prompts", "system.txt");
        var systemPrompt = File.Exists(systemPromptPath)
            ? await File.ReadAllTextAsync(systemPromptPath, ct)
            : "You are Coach.AI, a helpful diet and fitness assistant.";

        var fullUserMessage = string.IsNullOrWhiteSpace(context)
            ? req.Message
            : $"Context:\n{context}\n\nUser question: {req.Message}";

        string reply;

        try
        {
            var modelAvailable = await modelService.IsAvailableAsync(ct);
            if (!modelAvailable)
            {
                reply = "[AI model not available. Please ensure Ollama is running with the configured model.]\n\n" +
                        "Based on your logs:\n" + context;
            }
            else
            {
                reply = await modelService.GenerateAsync(systemPrompt, fullUserMessage, ct);
            }
        }
        catch (Exception)
        {
            reply = "[AI model error. Please check Ollama configuration.]\n\nYour context data:\n" + context;
        }

        var contextSummary = context.Length > 100 ? context[..100] + "..." : context;

        var assistantMsg = new ChatMessage
        {
            Role = "assistant",
            Content = reply,
            ContextSummary = contextSummary
        };
        db.ChatMessages.Add(assistantMsg);
        await db.SaveChangesAsync(ct);

        return Results.Ok(new ChatResponse(assistantMsg.Id, reply, contextSummary, assistantMsg.CreatedAt));
    }

    private static async Task<IResult> ClearHistory(AppDbContext db, CancellationToken ct)
    {
        await db.ChatMessages.ExecuteDeleteAsync(ct);
        return Results.NoContent();
    }
}
