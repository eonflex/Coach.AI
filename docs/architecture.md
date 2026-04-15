# Coach.AI — MVP Architecture & Coordination Package

## 1. Final Recommended MVP Stack

| Component | Choice | Rationale |
|---|---|---|
| **Backend framework** | ASP.NET Core 8, Minimal APIs | Lightweight, fast, .NET-native |
| **Database** | SQLite via EF Core 8 | Single-user, zero-admin, file-backed |
| **ORM** | Entity Framework Core 8 | Migrations, LINQ, code-first |
| **Auth** | HTTP Basic Auth (custom middleware) | Sufficient for single-user LAN/WAN |
| **Android stack** | Kotlin + Jetpack Compose + Hilt + Retrofit | Modern, declarative, type-safe |
| **Local model runtime** | Ollama | Free, local, swappable |
| **LLM model** | llama3 (default, configurable) | Good reasoning, locally runnable |
| **File storage** | Local filesystem, metadata in SQLite | Simple, no extra infra |
| **Retrieval** | Keyword-based chunking + metadata filter | Sufficient for v1 without vector DB |
| **PDF extraction** | PdfPig (open-source) | No cloud dependency |
| **DOCX extraction** | DocumentFormat.OpenXml | Microsoft-backed, free |

---

## 2. MVP Scope

### v1 Must-Have
- Food logging: manual entry, saved custom foods, quick re-log from history
- Workout logging: workout type, exercises (sets/reps/weight), cardio duration, notes
- Weight logging: date-stamped weight entries
- File upload: PDF, DOCX, TXT, images, spreadsheets (text extraction where possible)
- Document chunking and storage for AI context
- User targets: calories, macros, fiber, phase (cut/maintain/bulk), target weight
- AI chat grounded in: uploaded plans, recent logs, active targets
- Ollama integration with graceful offline fallback
- Basic auth (single user)
- Daily nutrition summary
- Android app: all screens functional, settings screen for server config
- Health endpoint (no auth)
- EF Core migrations

### v1.1 Next
- Nutrition label photo parsing (OCR via ML Kit or Tesseract)
- Exercise history lookup (view past sets for an exercise)
- Streak / adherence summary
- Weekly calorie/macro averages
- Dynamic base URL support (Android settings → Retrofit base URL hot-swap)
- Push-style notifications: daily logging reminder

### Later Maybe
- Vector search / semantic retrieval (replace keyword scorer)
- Generic food database lookup (USDA FoodData Central API, offline)
- Multi-device sync
- Spreadsheet extraction (CSV/XLSX)
- Multiple plans/phases with switch support
- Progress photos
- Voice input

---

## 3. High-Level Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────┐
│                     Android App (Kotlin)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐  │
│  │   Food   │  │ Workout  │  │  Weight  │  │  Chat  │  │
│  │  Screen  │  │  Screen  │  │  Screen  │  │ Screen │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └───┬────┘  │
│       │              │              │             │       │
│  ┌────▼──────────────▼──────────────▼─────────────▼──┐  │
│  │            ViewModel + Repository Layer             │  │
│  └────────────────────┬───────────────────────────────┘  │
│                        │ Retrofit HTTP (Basic Auth)       │
└────────────────────────┼────────────────────────────────-┘
                         │  LAN/WAN
┌────────────────────────▼─────────────────────────────────┐
│              ASP.NET Core 8 Minimal API Backend           │
│                                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │              BasicAuthMiddleware                 │    │
│  └──────────────────────────────────────────────────┘    │
│                                                           │
│  /api/foods   /api/workouts   /api/weight   /api/chat     │
│  /api/documents   /api/targets   /health                  │
│                                                           │
│  ┌────────────────┐  ┌─────────────────────────────┐     │
│  │  AppDbContext  │  │     Services                │     │
│  │   (EF Core)   │  │  - IModelService (Ollama)    │     │
│  │               │  │  - IDocumentExtraction       │     │
│  │  SQLite DB    │  │  - IRetrievalService         │     │
│  │  coachai.db   │  │  - ContextBuilderService     │     │
│  └────────────────┘  └─────────────────────────────┘     │
│                              │                            │
│  ┌───────────────┐   ┌───────▼──────────────────────┐   │
│  │ Local Files   │   │     Ollama (separate process) │   │
│  │  ./uploads/   │   │     localhost:11434           │   │
│  └───────────────┘   └──────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

### Data Flow: Chat with Context

```
User message
     │
     ▼
ContextBuilderService
     ├── Active UserTarget (targets table)
     ├── Today's FoodLogs + computed macros
     ├── Last 3 WorkoutLogs
     ├── Latest WeightLog
     └── RetrievalService
              └── DocumentChunks (keyword scored)
     │
     ▼
system.txt (Prompts/) + full context + user message
     │
     ▼
IModelService → Ollama /api/chat
     │
     ▼
AI reply saved to ChatMessages table
     │
     ▼
ChatResponse → Android
```

---

## 4. Shared Contracts for Child Agents

### 4.1 Backend API Conventions

- Base path: `/api/{resource}`
- Versioning: none in v1 (add `/v2/` prefix later if needed)
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
- All timestamps: ISO 8601 UTC (`2026-04-15T21:00:00Z`)
- Pagination: `page` (1-based) + `pageSize` query params, response in `PagedResponse<T>`
- Content-Type: `application/json` for all except file upload (`multipart/form-data`)

### 4.2 Error Response Format

```json
{
  "error": "Human-readable error message",
  "details": "Optional additional context"
}
```

HTTP status codes:
- `200 OK` — successful read
- `201 Created` — successful create (includes `Location` header)
- `204 No Content` — successful delete
- `400 Bad Request` — validation failure
- `401 Unauthorized` — missing or invalid credentials
- `404 Not Found` — resource not found
- `500 Internal Server Error` — unexpected error

### 4.3 Auth Approach

Single-user Basic Auth:
- Header: `Authorization: Basic <base64(username:password)>`
- Credentials configured in `appsettings.json` under `Auth.Username` / `Auth.Password`
- `/health` endpoint is exempt from auth
- Android stores credentials in DataStore; `AuthInterceptor` attaches them to all requests

### 4.4 DTO Naming Conventions

- Request DTOs: `{Action}{Resource}Request` (e.g., `CreateFoodItemRequest`, `LogWeightRequest`)
- Response DTOs: `{Resource}Response` (e.g., `FoodItemResponse`, `WorkoutLogResponse`)
- List wrappers: `PagedResponse<T>` with `Items`, `Total`, `Page`, `PageSize`
- All DTOs are `record` types (C#) or `@JsonClass data class` (Kotlin)
- Properties: `camelCase` in JSON (ASP.NET Core default), `camelCase` in Kotlin

### 4.5 DB Naming Conventions

- Table names: plural PascalCase (`FoodItems`, `WorkoutLogs`)
- Primary keys: `Id` (int, auto-increment)
- Foreign keys: `{RelatedEntity}Id` (e.g., `FoodItemId`, `WorkoutLogId`)
- Timestamps: `LoggedAt`, `CreatedAt`, `UpdatedAt`, `UploadedAt`
- Nullable columns: use `?` (EF Core nullable reference types)

### 4.6 File Storage Conventions

- Upload path: configurable via `Storage:UploadPath` (default: `uploads/`)
- Storage filename format: `{original_name_no_spaces}_{ticks}{extension}`
- Metadata stored in `UploadedDocuments` table: `FileName`, `ContentType`, `StoragePath`, `FileSizeBytes`
- Chunks in `DocumentChunks` table with `ChunkIndex`, `TextContent`
- Target chunk size: ~800 characters
- Supported types for text extraction: PDF, DOCX, TXT (images stored but not extracted in v1)

### 4.7 Model Service Abstraction

```csharp
public interface IModelService
{
    Task<string> GenerateAsync(string systemPrompt, string userMessage, CancellationToken ct = default);
    Task<bool> IsAvailableAsync(CancellationToken ct = default);
}
```

- `OllamaModelService` is the concrete implementation
- To swap models: implement `IModelService` and register in DI
- Model configured via `Ollama:Model` in `appsettings.json`
- Chat endpoint always checks `IsAvailableAsync` before calling `GenerateAsync`

### 4.8 Prompt/Template Storage Conventions

- Location: `{ContentRootPath}/Prompts/`
- System prompt: `Prompts/system.txt`
- Prompts are plain text files loaded at request time (not cached in v1)
- Prompt guidelines: distinguish `[UPLOADED PLAN]`, `[LOG DATA]`, `[SUGGESTION]`

### 4.9 Logging Conventions

- Use `ILogger<T>` throughout (injected via DI)
- Log levels: `LogError` for exceptions, `LogWarning` for recoverable issues, `LogInformation` for key operations
- Structured logging: use message templates, not string interpolation
- Sensitive data (passwords, auth headers) must never be logged

### 4.10 Configuration Conventions

```json
{
  "Auth": { "Username": "...", "Password": "..." },
  "Ollama": { "BaseUrl": "...", "Model": "..." },
  "Storage": { "UploadPath": "..." },
  "ConnectionStrings": { "DefaultConnection": "..." }
}
```

- Bind via `IOptions<T>` (e.g., `AuthOptions`, `OllamaOptions`)
- Override via environment variables: `Auth__Username`, `Ollama__BaseUrl`, etc.
- Never commit real passwords; use `appsettings.Development.json` for local overrides

---

## 5. Work Decomposition

### Track 1: Backend Foundation & Auth
**Responsibilities:** Solution setup, Program.cs, BasicAuthMiddleware, configuration, health endpoint, EF Core setup, migrations  
**Inputs:** Stack decisions above  
**Outputs:** Running API with auth, DB migrations, health check  
**Dependencies:** None  
**Mocks if blocked:** N/A — this is the foundation  
**Status:** ✅ Complete

---

### Track 2: Database Schema & EF Core Models
**Responsibilities:** All entity classes, AppDbContext, indexes, relationships, migrations  
**Inputs:** Shared contracts (DB naming, relationships)  
**Outputs:** `InitialCreate` migration, all entity classes  
**Dependencies:** Track 1  
**Mocks if blocked:** Use InMemory DB for other tracks  
**Status:** ✅ Complete

---

### Track 3: Food / Workout / Weight Logging APIs
**Responsibilities:** All CRUD endpoints for FoodItem, FoodLog, WorkoutLog, Exercise, WeightLog, UserTarget  
**Inputs:** DB models (Track 2), DTO contracts  
**Outputs:** `/api/foods`, `/api/workouts`, `/api/weight`, `/api/targets` endpoints  
**Dependencies:** Tracks 1, 2  
**Mocks if blocked:** InMemory DB  
**Status:** ✅ Complete

---

### Track 4: File Upload & Document Extraction Pipeline
**Responsibilities:** File upload endpoint, filesystem storage, IDocumentExtractionService (PDF/DOCX/TXT), background chunking task, DocumentChunk storage  
**Inputs:** DB models, storage conventions  
**Outputs:** `/api/documents` endpoints, chunks in DB  
**Dependencies:** Track 2  
**Mocks if blocked:** Stub `IDocumentExtractionService` returning `["mock chunk"]`  
**Status:** ✅ Complete

---

### Track 5: LLM Integration, Prompt Orchestration & Retrieval
**Responsibilities:** IModelService, OllamaModelService, IRetrievalService (keyword-based), ContextBuilderService, system prompt, ChatEndpoints  
**Inputs:** All entity models (for context), retrieval contract  
**Outputs:** `/api/chat` with grounded responses, Prompts/system.txt  
**Dependencies:** Tracks 2, 3, 4  
**Mocks if blocked:** Stub `IModelService` returning fixed response  
**Status:** ✅ Complete

---

### Track 6: Android App Foundation & Networking
**Responsibilities:** Gradle setup, Hilt DI, Retrofit + OkHttp, AuthInterceptor, DataStore preferences, CoachAIApi interface, all model/DTO classes, repositories, navigation structure  
**Inputs:** API endpoint contracts, DTO shapes  
**Outputs:** Compilable app with working network layer  
**Dependencies:** Track 1 (for API contract)  
**Mocks if blocked:** MockWebServer or hardcoded mock data in ViewModel  
**Status:** ✅ Complete

---

### Track 7: Android Logging Screens
**Responsibilities:** FoodScreen, WorkoutScreen, WeightScreen, SettingsScreen — ViewModels + Compose UI, FAB dialogs for logging  
**Inputs:** Android foundation (Track 6)  
**Outputs:** All logging flows functional end-to-end  
**Dependencies:** Track 6  
**Mocks if blocked:** Stub repository returning sample data  
**Status:** ✅ Complete

---

### Track 8: Android Chat & Upload Screens
**Responsibilities:** ChatScreen (bubble UI, send/receive), FilesScreen (file picker, upload, delete), ChatViewModel, FilesViewModel  
**Inputs:** Android foundation (Track 6), document + chat API contracts  
**Outputs:** Chat and document upload flows functional  
**Dependencies:** Track 6, Tracks 4 & 5 (or mocks)  
**Mocks if blocked:** Stub ChatRepository with canned responses  
**Status:** ✅ Complete

---

## 6. Interface Specs Between Tracks

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check, no auth |
| GET | `/api/foods/items` | List saved food items (`?search=`) |
| GET | `/api/foods/items/{id}` | Get food item |
| POST | `/api/foods/items` | Create food item |
| PUT | `/api/foods/items/{id}` | Update food item |
| DELETE | `/api/foods/items/{id}` | Delete food item |
| GET | `/api/foods/logs` | Get food logs (`?date=YYYY-MM-DD`) |
| GET | `/api/foods/logs/summary` | Daily nutrition summary (`?date=`) |
| POST | `/api/foods/logs` | Log food consumption |
| DELETE | `/api/foods/logs/{id}` | Delete food log entry |
| GET | `/api/workouts/` | List workout logs (paged) |
| GET | `/api/workouts/{id}` | Get workout log |
| POST | `/api/workouts/` | Create workout log |
| PUT | `/api/workouts/{id}` | Update workout log |
| DELETE | `/api/workouts/{id}` | Delete workout log |
| GET | `/api/weight/` | List weight logs (paged) |
| GET | `/api/weight/latest` | Get most recent weight |
| POST | `/api/weight/` | Log weight |
| DELETE | `/api/weight/{id}` | Delete weight log |
| GET | `/api/documents/` | List uploaded documents |
| GET | `/api/documents/{id}` | Get document metadata |
| GET | `/api/documents/{id}/chunks` | Get document chunks |
| POST | `/api/documents/upload` | Upload document (multipart/form-data) |
| DELETE | `/api/documents/{id}` | Delete document + file |
| GET | `/api/targets/active` | Get active target |
| GET | `/api/targets/` | List all targets |
| POST | `/api/targets/` | Set new active target |
| DELETE | `/api/targets/{id}` | Delete target |
| GET | `/api/chat/history` | Get chat history (`?limit=50`) |
| POST | `/api/chat/` | Send message, get AI response |
| DELETE | `/api/chat/history` | Clear chat history |

### Key DTO Shapes

**POST /api/foods/items**
```json
{ "name": "Chicken Breast", "brand": null, "servingSizeGrams": 100,
  "calories": 165, "proteinGrams": 31, "carbsGrams": 0, "fatGrams": 3.6, "fiberGrams": 0 }
```

**POST /api/foods/logs**
```json
{ "foodItemId": 1, "servingsConsumed": 1.5, "meal": "Lunch", "notes": null, "loggedAt": null }
```

**POST /api/workouts/**
```json
{ "workoutType": "Push", "notes": null, "cardioDurationMinutes": null, "loggedAt": null,
  "exercises": [{ "name": "Bench Press", "sets": 4, "reps": 8, "weightKg": 80, "notes": null }] }
```

**POST /api/weight/**
```json
{ "weightKg": 85.5, "notes": null, "loggedAt": null }
```

**POST /api/targets/**
```json
{ "phase": "cut", "targetCalories": 2200, "targetProteinGrams": 180, "targetCarbsGrams": 220,
  "targetFatGrams": 65, "targetFiberGrams": 30, "targetWeightKg": 80 }
```

**POST /api/chat/**
```json
{ "message": "How am I doing today on protein?", "includeRecentLogs": true }
```

**POST /api/documents/upload** — `multipart/form-data`:
- `file`: file content (PDF, DOCX, TXT, image)
- `notes`: optional string

### Service Interfaces

```csharp
interface IModelService {
    Task<string> GenerateAsync(string systemPrompt, string userMessage, CancellationToken ct);
    Task<bool> IsAvailableAsync(CancellationToken ct);
}

interface IDocumentExtractionService {
    Task<IReadOnlyList<string>> ExtractChunksAsync(string filePath, string contentType, CancellationToken ct);
}

interface IRetrievalService {
    Task<IReadOnlyList<string>> RetrieveRelevantChunksAsync(string query, int maxChunks, CancellationToken ct);
}
```

---

## 7. Build Order & Dependency Graph

```
Track 1: Backend Foundation
    │
    ├── Track 2: DB Schema & Models
    │       │
    │       ├── Track 3: Logging APIs ──────────────────────┐
    │       │       │                                        │
    │       └── Track 4: File Upload & Extraction            │
    │               │                                        │
    │               └── Track 5: LLM Integration ◄───────────┘
    │
    └── Track 6: Android Foundation
            │
            ├── Track 7: Android Logging Screens
            │
            └── Track 8: Android Chat & Upload Screens
```

**Can start immediately (no dependencies):** Track 1, Track 6 (with mocked API)

**Parallel safe after Track 1:** Tracks 2, 6

**Parallel safe after Track 2:** Tracks 3, 4

**After Tracks 3 & 4:** Track 5

**After Track 6:** Tracks 7, 8 (in parallel)

**Integration order:**
1. Track 1 + Track 2 → Get migrations working
2. Track 3 + Track 4 in parallel → Get data APIs functional
3. Track 5 → LLM layer on top of data APIs
4. Track 6 → Build Android with mock API responses
5. Tracks 7+8 in parallel → Add real API calls once backend stable
6. End-to-end integration test: Android → backend → Ollama

---

## 8. Risks & Anti-Scope-Creep Guidance

### Key Risks

| Risk | Mitigation |
|------|-----------|
| Ollama not running on user's machine | Graceful offline fallback in ChatEndpoint; show clear error message in Android |
| Large PDF/DOCX fails extraction | Log and skip gracefully; background task failure doesn't block upload |
| Android base URL not configurable at runtime | Settings screen saves to DataStore; need hot-reload of Retrofit in v1.1 |
| SQLite write contention from background extraction task | Use scoped DbContext in background task (already implemented) |
| EF Core migrations drift | Always generate migration after schema change, never edit DB directly |
| Auth credentials hardcoded in APK | Stored in DataStore, not in code; user changes in Settings |

### What NOT to Build in v1

- ❌ Multi-user or multi-tenant support
- ❌ Cloud sync or remote storage
- ❌ Paid API calls (OpenAI, Anthropic, etc.)
- ❌ Vector database (Qdrant, Weaviate, Pinecone, etc.)
- ❌ Progress charts or graph visualizations
- ❌ OCR for nutrition labels (v1.1)
- ❌ Spreadsheet (.xlsx) text extraction
- ❌ Generic food database (USDA, Nutritionix, etc.)
- ❌ Push notifications
- ❌ Microservices, message queues, Docker-compose
- ❌ Billing, subscriptions, user management
- ❌ Automatic plan modification by AI
- ❌ AI-generated workout programs (AI assists, not prescribes)
- ❌ iOS app
- ❌ Web UI
- ❌ Barcode scanning

### Safety Guidelines (built into system prompt)

The AI is **explicitly constrained** to:
- Not prescribe aggressive calorie changes
- Not silently modify plans
- Mark all AI suggestions clearly with `[SUGGESTION]`
- Distinguish uploaded plan data from log data from suggestions
- Remind users it is not a doctor or dietitian
