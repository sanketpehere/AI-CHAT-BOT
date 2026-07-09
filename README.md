# 🤖 AI Chat Bot

A lightweight, reactive Spring Boot application that streams real-time AI responses directly to the client, mimicking the ChatGPT typing experience. 

It is designed to be beginner-friendly while utilizing modern Java concepts, and is powered by the **Google Gemini API** via the OpenAI Java SDK compatibility layer.

## 🛠️ Tech Stack

* **Java 21:** Utilizing modern language features like records.
* **Spring Boot 3 (WebFlux):** For reactive, non-blocking asynchronous streaming.
* **OpenAI Java SDK:** Official client configured to hit Gemini's OpenAI-compatible endpoints.
* **Spring Dotenv:** For secure local environment variable management.
* **Maven:** Dependency and build management.

## ✨ Key Features

* **Real-time Streaming:** Uses Server-Sent Events (SSE) to stream text chunks instantly.
* **Cost & Size Guards:** Validates and limits input length to prevent excessive API usage.
* **Zero-Cost Caching:** Automatically caches repeated prompts in memory for instant replies.
* **Standardized Error Handling:** Uses Spring's `ProblemDetail` to catch crashes and return clean, standardized JSON errors.
* **Mock Mode:** A toggleable offline mode (`app.mock-mode=true`) to test the UI without consuming API credits.

## 📡 API Endpoints

### 1. Health Check
Returns a simple welcome message to verify the server is running.
* **URL:** `/`
* **Method:** `GET`
* **Test:** `curl http://localhost:8080/`

### 2. Stream Chat
Submits a prompt to the AI and streams the generated response back in real-time.
* **URL:** `/api/chat/stream`
* **Method:** `GET`
* **Query Parameters:** `prompt` (String) - The question you want to ask.
* **Test (Terminal):**
  ```bash
  curl -N "http://localhost:8080/api/chat/stream?prompt=Tell%20me%20a%20joke"
