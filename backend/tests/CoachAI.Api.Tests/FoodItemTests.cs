using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using CoachAI.Api.DTOs;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using CoachAI.Api.Data;
using Microsoft.AspNetCore.Hosting;
using Xunit;

namespace CoachAI.Api.Tests;

public class FoodItemTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public FoodItemTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory.WithWebHostBuilder(builder =>
        {
            builder.UseEnvironment("Testing");
            builder.ConfigureServices(services =>
            {
                var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(DbContextOptions<AppDbContext>));
                if (descriptor != null) services.Remove(descriptor);
                services.AddDbContext<AppDbContext>(options =>
                    options.UseInMemoryDatabase("TestDb_" + Guid.NewGuid()));
            });
        });
    }

    private HttpClient CreateAuthClient()
    {
        var client = _factory.CreateClient();
        var credentials = Convert.ToBase64String(Encoding.UTF8.GetBytes("coach:changeme"));
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Basic", credentials);
        return client;
    }

    [Fact]
    public async Task CreateFoodItem_ReturnsCreated()
    {
        var client = CreateAuthClient();
        var req = new CreateFoodItemRequest("Test Food", "TestBrand", 100, 200, 20, 25, 5, 3);
        var response = await client.PostAsJsonAsync("/api/foods/items", req);
        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var item = await response.Content.ReadFromJsonAsync<FoodItemResponse>();
        Assert.NotNull(item);
        Assert.Equal("Test Food", item!.Name);
    }

    [Fact]
    public async Task GetFoodItems_ReturnsOk()
    {
        var client = CreateAuthClient();
        var response = await client.GetAsync("/api/foods/items");
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task HealthCheck_ReturnsOk_WithoutAuth()
    {
        var client = _factory.CreateClient();
        var response = await client.GetAsync("/health");
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Fact]
    public async Task GetFoodItems_WithoutAuth_Returns401()
    {
        var client = _factory.CreateClient();
        var response = await client.GetAsync("/api/foods/items");
        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }
}
