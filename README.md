# 📊 FinPulse AI — Enterprise Financial Intelligence System

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue.svg?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-ai)
[![RabbitMQ](https://img.shields.io/badge/Message_Broker-RabbitMQ-ff6600.svg?style=for-the-badge&logo=rabbitmq)](https://www.rabbitmq.com/)
[![PostgreSQL](https://img.shields.io/badge/Vector%20Store-PGVector%2016-blue?style=for-the-badge&logo=postgresql)](https://github.com/pgvector/pgvector)
[![OpenAI](https://img.shields.io/badge/LLM-GPT--4o--mini-blueviolet?style=for-the-badge&logo=openai)](https://openai.com/)

**FinPulse AI** is an enterprise-grade, agentic financial intelligence engine built on Spring Boot and Spring AI. It enables high-fidelity multi-format document ingestion (PDFs, CSVs, etc.), asynchronous background processing, structured chunking, vector embeddings generation, and strict, trace-grounded Retrieval-Augmented Generation (RAG) query resolution. 

It is designed specifically for financial analysts and operations teams who require zero-hallucination responses extracted from complex reports (10-K filings, earnings calls, quarterly statements) with explicit source file citations.

---

## ✨ Key Features

*   **⚡ Asynchronous Multi-Format Ingestion**: Supports direct uploads of PDFs, Word Docs, and CSVs. Uses **Apache Tika** for parsing and **RabbitMQ** for processing heavy documents in the background without blocking the UI.
*   **🧠 Semantics-Driven Retrieval**: Uses OpenAI's high-performance `text-embedding-3-small` (1536-dimensions) to generate embedding vectors stored directly in PostgreSQL using `pgvector`.
*   **🎨 Premium Glassmorphism UI**: Features a beautiful, responsive, dark-mode single-page application built with Vanilla HTML/JS/CSS served right from the Spring Boot static resources.
*   **🔒 Strict Zero-Hallucination Guardrails**: Includes precise system prompts and a similarity threshold cut-off (`0.5`) to guarantee that responses are built strictly from matching financial context or fall back gracefully.
*   **🐳 Immediate Local Provisioning**: Built-in support for **Spring Boot Docker Compose**, instantly starting up a PostgreSQL database (with `pgvector`) and RabbitMQ upon booting the application.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Runtime** | Java OpenJDK | 21 | High-performance, modern JVM features |
| **Framework** | Spring Boot | 3.4.1 | Reactive and enterprise core dependency injection |
| **AI Layer** | Spring AI (BOM) | 1.0.0 | High-level abstraction for vector store and chat clients |
| **Document Parser**| Apache Tika | 1.0.0 | Native parsing of complex file formats (PDF/CSV/DOCX) |
| **Message Broker** | RabbitMQ | 3.x | Decoupled, asynchronous background ingestion tasks |
| **LLM Gateway** | OpenAI | `gpt-4o-mini` | Cost-effective and fast reasoning / text synthesis |
| **Embeddings** | OpenAI Embeddings | `text-embedding-3-small` | Semantic search vectors (1536 dims) |
| **Vector Store** | PostgreSQL + pgvector | 16 | Relational persistence & multi-dimensional search |
| **UI Frontend** | Vanilla HTML/CSS/JS | - | Glassmorphism SPA served via Spring Web |

---

## 📐 Architecture & Data Flow

### Ingestion Flow (Asynchronous)
1. A user uploads a multi-part file (e.g., PDF) via the UI to `/api/v1/ingest`.
2. The `ChatController` saves the file temporarily and drops a `FileIngestionMessage` onto the **RabbitMQ Exchange**, instantly returning a `202 Accepted` to the UI.
3. The `IngestionService` (`@RabbitListener`) picks up the job in the background.
4. **Apache Tika** extracts text from the document natively.
5. `TokenTextSplitter` segments the text into ~500 token windows with a 50-token overlap.
6. Chunks are sent to the OpenAI embedding endpoint, transformed into vectors, and saved within a `vector_store` table in PostgreSQL.
7. The temporary file is cleaned up.

### Query / RAG Flow (Synchronous)
1. User submits a question via the Chat UI to `/api/v1/chat`.
2. `SemanticSearchService` performs a cosine similarity lookup against PostgreSQL (`COSINE_DISTANCE` metric, threshold `0.5`).
3. Matching text chunks are formatted as citation blocks (`[Page X | filename]`).
4. A strict system prompt forces the LLM (`gpt-4o-mini`) to answer *only* based on the context. 

---

## 🚀 Getting Started

### Prerequisites
*   **Java**: JDK 21+ installed and configured.
*   **Docker Desktop**: Ensure Docker is running locally (required to spin up the PostgreSQL vector database and RabbitMQ).
*   **OpenAI API Key**: A valid key to generate embeddings and execute chat completions.

### Configuration
1. Export your OpenAI API key in your terminal session:
   ```bash
   export OPENAI_API_KEY="your-actual-api-key-here"
   ```

2. Review the core variables in `src/main/resources/application.properties` (the port is pre-configured to `8201`):
   ```properties
   server.port=8201
   spring.ai.openai.api-key=${OPENAI_API_KEY}
   spring.ai.openai.chat.options.model=gpt-4o-mini
   spring.ai.openai.embedding.options.model=text-embedding-3-small
   ```

### Running Locally
Run the Maven spring-boot plugin directly from the project directory. The Spring Docker Compose integration will automatically detect and start your local PostgreSQL and RabbitMQ containers!

```bash
./mvnw spring-boot:run
```

Once booted, open your browser and navigate to the premium frontend UI: **`http://localhost:8201`**

---

## 🔌 API Reference

### 1. Ingest Document Content (Async)
Uploads a document for asynchronous parsing and embedding.

*   **Endpoint**: `POST /api/v1/ingest`
*   **Content-Type**: `multipart/form-data`
*   **Form Data**:
    * `file`: (File) The PDF, CSV, or DOCX file to upload.
    * `chunkType`: (String) "TEXT" or "TABLE"

*   **Response**:
    ```json
    {
      "message": "File queued for async processing.",
      "sourceFile": "finpulse_annual_report_2025.pdf"
    }
    ```

### 2. Grounded Chat Query
Asks a financial query. The server performs semantic search and executes the RAG pipeline.

*   **Endpoint**: `POST /api/v1/chat`
*   **Content-Type**: `application/json`
*   **Payload Example**:
    ```json
    {
      "message": "What was the operating revenue for FinPulse Inc in fiscal year 2025 and did margins expand?"
    }
    ```

*   **Response**:
    ```json
    {
      "answer": "According to the financial reports, FinPulse Inc. reported a total operating revenue of $148.5 million for the fiscal year ended December 31, 2025... [Page N/A | finpulse_annual_report_2025.pdf]"
    }
    ```

---

## 🧪 Testing

The codebase includes integration tests leveraging **Testcontainers** to verify database connectivity, table initialization, and repository interactions inside isolated postgres containers.

To run the full suite of unit and integration tests:

```bash
./mvnw clean test
```

---

## 🗺️ Roadmap & Conventions

For a deep dive into the service design principles, full sequence diagrams, strict conventions, and upcoming orchestrator integrations, please refer to the onboarding blueprints in:
👉 **[agent.md](./agent.md)**
