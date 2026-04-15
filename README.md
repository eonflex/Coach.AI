# Coach.AI

A locally-hosted AI diet and fitness coach. Track food, workouts, and weight. Upload your plans. Ask your AI coach anything — all running on your own hardware with no cloud dependencies.

## Overview

- **Backend**: ASP.NET Core 8 (Minimal APIs) + SQLite + EF Core
- **Android app**: Kotlin + Jetpack Compose
- **AI**: Ollama (local LLM, default: llama3)
- **Auth**: Single-user HTTP Basic Auth
- **Retrieval**: Document upload + keyword-based context injection

## Quick Start

### Backend

**Prerequisites**: [.NET 8 SDK](https://dotnet.microsoft.com/download), [Ollama](https://ollama.ai)

```bash
# Start Ollama and pull a model
ollama pull llama3

# Run the backend
cd backend/src/CoachAI.Api
dotnet run
# API available at http://localhost:5000
# Swagger UI at http://localhost:5000/swagger
```

Default credentials: `coach` / `changeme` (change in `appsettings.json`)

### Android App

Open the `android/` directory in Android Studio and build/run on device or emulator.

In the app, go to **Settings** and set:
- Server URL: `http://<your-server-ip>:5000` (use `http://10.0.2.2:5000` for emulator)
- Username / Password: matching your `appsettings.json`

## Features (v1)

- 🥗 **Food logging** — save custom foods, log meals, view daily macro summary
- 🏋️ **Workout logging** — log exercises with sets/reps/weight, cardio duration
- ⚖️ **Weight tracking** — date-stamped weight history
- 📄 **Document upload** — upload PDFs, Word docs, text notes as plans
- 🎯 **Targets** — set cut/maintain/bulk phase with calorie + macro targets
- 🤖 **AI chat** — chat with an AI coach grounded in your logs, targets, and uploaded plans (requires Ollama)

## Architecture

See [`docs/architecture.md`](docs/architecture.md) for the full architecture, API contracts, work decomposition, and build order.

## Project Structure

```
Coach.AI/
├── docs/               # Architecture docs
├── backend/
│   ├── src/CoachAI.Api/    # ASP.NET Core backend
│   └── tests/CoachAI.Api.Tests/
└── android/
    └── app/            # Android (Kotlin + Compose)
```

## Safety Note

Coach.AI is **not a doctor or dietitian**. The AI will not autonomously modify plans or prescribe aggressive calorie changes. All AI suggestions are clearly marked. Always consult a qualified professional for medical or dietary advice.
