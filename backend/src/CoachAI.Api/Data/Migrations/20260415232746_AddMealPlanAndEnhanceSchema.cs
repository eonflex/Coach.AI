using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace CoachAI.Api.Data.Migrations
{
    /// <inheritdoc />
    public partial class AddMealPlanAndEnhanceSchema : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_FoodLogs_LoggedAt",
                table: "FoodLogs");

            migrationBuilder.DropIndex(
                name: "IX_DocumentChunks_DocumentId",
                table: "DocumentChunks");

            migrationBuilder.AddColumn<string>(
                name: "ExtractionError",
                table: "UploadedDocuments",
                type: "TEXT",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "ExtractionStatus",
                table: "UploadedDocuments",
                type: "INTEGER",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.CreateTable(
                name: "MealPlans",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    Name = table.Column<string>(type: "TEXT", nullable: false),
                    Description = table.Column<string>(type: "TEXT", nullable: true),
                    Phase = table.Column<string>(type: "TEXT", nullable: true),
                    IsActive = table.Column<bool>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MealPlans", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "PlanItems",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    MealPlanId = table.Column<int>(type: "INTEGER", nullable: false),
                    DayLabel = table.Column<string>(type: "TEXT", nullable: true),
                    Meal = table.Column<string>(type: "TEXT", nullable: true),
                    FoodItemId = table.Column<int>(type: "INTEGER", nullable: true),
                    Description = table.Column<string>(type: "TEXT", nullable: true),
                    TargetCalories = table.Column<decimal>(type: "TEXT", precision: 8, scale: 2, nullable: true),
                    TargetProteinGrams = table.Column<decimal>(type: "TEXT", precision: 8, scale: 2, nullable: true),
                    TargetCarbsGrams = table.Column<decimal>(type: "TEXT", precision: 8, scale: 2, nullable: true),
                    TargetFatGrams = table.Column<decimal>(type: "TEXT", precision: 8, scale: 2, nullable: true),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_PlanItems", x => x.Id);
                    table.ForeignKey(
                        name: "FK_PlanItems_FoodItems_FoodItemId",
                        column: x => x.FoodItemId,
                        principalTable: "FoodItems",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_PlanItems_MealPlans_MealPlanId",
                        column: x => x.MealPlanId,
                        principalTable: "MealPlans",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_FoodLogs_LoggedAt_FoodItemId",
                table: "FoodLogs",
                columns: new[] { "LoggedAt", "FoodItemId" });

            migrationBuilder.CreateIndex(
                name: "IX_DocumentChunks_DocumentId_ChunkIndex",
                table: "DocumentChunks",
                columns: new[] { "DocumentId", "ChunkIndex" });

            migrationBuilder.CreateIndex(
                name: "IX_MealPlans_IsActive",
                table: "MealPlans",
                column: "IsActive");

            migrationBuilder.CreateIndex(
                name: "IX_PlanItems_FoodItemId",
                table: "PlanItems",
                column: "FoodItemId");

            migrationBuilder.CreateIndex(
                name: "IX_PlanItems_MealPlanId",
                table: "PlanItems",
                column: "MealPlanId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "PlanItems");

            migrationBuilder.DropTable(
                name: "MealPlans");

            migrationBuilder.DropIndex(
                name: "IX_FoodLogs_LoggedAt_FoodItemId",
                table: "FoodLogs");

            migrationBuilder.DropIndex(
                name: "IX_DocumentChunks_DocumentId_ChunkIndex",
                table: "DocumentChunks");

            migrationBuilder.DropColumn(
                name: "ExtractionError",
                table: "UploadedDocuments");

            migrationBuilder.DropColumn(
                name: "ExtractionStatus",
                table: "UploadedDocuments");

            migrationBuilder.CreateIndex(
                name: "IX_FoodLogs_LoggedAt",
                table: "FoodLogs",
                column: "LoggedAt");

            migrationBuilder.CreateIndex(
                name: "IX_DocumentChunks_DocumentId",
                table: "DocumentChunks",
                column: "DocumentId");
        }
    }
}
