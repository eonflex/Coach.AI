using System.Net.Http.Headers;
using System.Text;
using Microsoft.Extensions.Options;

namespace CoachAI.Api.Auth;

public class AuthOptions
{
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}

public class BasicAuthMiddleware
{
    private readonly RequestDelegate _next;
    private readonly AuthOptions _auth;

    public BasicAuthMiddleware(RequestDelegate next, IOptions<AuthOptions> auth)
    {
        _next = next;
        _auth = auth.Value;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        // Skip auth for health check
        if (context.Request.Path.StartsWithSegments("/health"))
        {
            await _next(context);
            return;
        }

        if (!context.Request.Headers.TryGetValue("Authorization", out var authHeader))
        {
            context.Response.StatusCode = 401;
            context.Response.Headers["WWW-Authenticate"] = "Basic realm=\"CoachAI\"";
            await context.Response.WriteAsync("Unauthorized");
            return;
        }

        try
        {
            var parsed = AuthenticationHeaderValue.Parse(authHeader!);
            if (parsed.Scheme != "Basic" || parsed.Parameter is null)
            {
                Reject(context);
                return;
            }

            var credentials = Encoding.UTF8.GetString(Convert.FromBase64String(parsed.Parameter)).Split(':', 2);
            if (credentials.Length != 2 || credentials[0] != _auth.Username || credentials[1] != _auth.Password)
            {
                Reject(context);
                return;
            }
        }
        catch
        {
            Reject(context);
            return;
        }

        await _next(context);
    }

    private static void Reject(HttpContext context)
    {
        context.Response.StatusCode = 401;
        context.Response.Headers["WWW-Authenticate"] = "Basic realm=\"CoachAI\"";
    }
}
