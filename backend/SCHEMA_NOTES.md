# Coach.AI Backend — Schema Notes

Integration reference for API and LLM agent authors.

---

## Entity Overview

### FoodItem
Reusable food definition. `IsCustom=true` for user-created items.
Macros (Calories, ProteinGrams, CarbsGrams, FatGrams, FiberGrams, ServingSizeGrams) are stored per-serving.

- **Index:** `Name` (for search)

### FoodLog
A single logged food consumption event. Macros are **computed at read time** as `FoodItem.macro * ServingsConsumed` — they are NOT stored redundantly.

- **Composite Index:** `(LoggedAt, FoodItemId)` — supports date-range queries
- **FK:** FoodItemId → FoodItem (Restrict — prevents deletion of referenced food)
- **Daily aggregation note:** `LoggedAt` is stored in UTC. When filtering by "today", ensure the client sends a UTC date or the backend converts. The `/api/foods/logs/summary?date=` endpoint compares `.Date` in server UTC.

### WorkoutLog
A single workout session. Contains zero or more `Exercise` records.
Cardio-only sessions have `CardioDurationMinutes` set and no exercises.

- **Index:** `LoggedAt`

### Exercise
An exercise within a WorkoutLog. Supports strength (Sets/Reps/WeightKg) and free-form notes.

- **FK:** WorkoutLogId → WorkoutLog (Cascade)

### WeightLog
Date-stamped bodyweight entry. All values in kg.

- **Index:** `LoggedAt`

### UploadedDocument
Uploaded file metadata. `StoragePath` is the absolute or relative path on the local filesystem.

`ExtractionStatus` lifecycle:
- `Pending` (0) — just uploaded, background task not started
- `Processing` (1) — background extraction task running
- `Completed` (2) — chunks written, `ExtractionDone=true`
- `Failed` (3) — extraction threw, `ExtractionError` populated

`ExtractionDone` (bool) is kept for backward compatibility — it mirrors `ExtractionStatus == Completed`.

### DocumentChunk
Text chunks extracted from an UploadedDocument, used for LLM context retrieval.
`Tags` is a free-text field (comma-separated keywords) optionally assigned during extraction.

- **Composite Index:** `(DocumentId, ChunkIndex)` — ordered chunk retrieval
- **FK:** DocumentId → UploadedDocument (Cascade)

### UserTarget
Active user nutrition/fitness targets for a phase (cut/maintain/bulk).
Only one target should have `IsActive=true` at a time (enforced by application logic).

- **Index:** `Phase`

### ChatMessage
Persisted chat history. `Role` is "user" or "assistant".
`ContextSummary` stores a truncated version of the grounding context used by the LLM to generate the response.

### MealPlan
A structured diet/workout plan — either extracted from an uploaded document or created manually.
`Phase` mirrors the UserTarget phase concept ("cut", "maintain", "bulk").
`IsActive` marks the currently followed plan.

- **Index:** `IsActive`

### PlanItem
A single line item in a MealPlan (e.g., "Monday Breakfast: Oats 80g").
`FoodItemId` is optional — items can be free-text `Description` without a linked FoodItem.
`TargetCalories/Protein/Carbs/Fat` are the plan-specified targets for this item.

- **FK:** MealPlanId → MealPlan (Cascade)
- **FK:** FoodItemId → FoodItem (Restrict — optional)

---

## Integration Notes for API Agents

- Always use the `ExtractionStatus` enum (not just `ExtractionDone`) in new API endpoints.
- When deleting a FoodItem, check for PlanItem references (FK Restrict) in addition to FoodLog references.
- MealPlan CRUD endpoints are not yet implemented — see follow-up work.
- Chat history is not session-scoped in v1 (single user, flat log).

## Integration Notes for LLM Agent

- `DocumentChunk.Tags` can be used for metadata filtering during retrieval.
- `UserTarget.Phase` should be injected into extraction prompts as context.
- `MealPlan` and `PlanItem` tables are the "ground truth" plan — distinguish from AI suggestions.
- Never write to `FoodLog`, `WorkoutLog`, or `WeightLog` from the LLM layer; only read for context.
- `ContextBuilderService` assembles: active UserTarget → today's FoodLogs → latest WeightLog → recent WorkoutLogs → retrieved DocumentChunks.

---

## Migration History

| Migration | Description |
|---|---|
| `20260415215802_InitialCreate` | Baseline schema |
| `AddMealPlanAndEnhanceSchema` | MealPlan/PlanItem, ExtractionStatus enum, improved indexes, decimal precision |
