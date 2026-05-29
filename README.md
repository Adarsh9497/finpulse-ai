# 📊 FinPulse AI — Agentic Financial Intelligence System

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue.svg?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/Vector%20Store-PGVector%2016-blue?style=for-the-badge&logo=postgresql)](https://github.com/pgvector/pgvector)
[![OpenAI](https://img.shields.io/badge/LLM-GPT--4o--mini-blueviolet?style=for-the-badge&logo=openai)](https://openai.com/)

**FinPulse AI** is an enterprise-grade, agentic financial intelligence engine built on Spring Boot and Spring AI. It enables high-fidelity financial document ingestion, structured chunking, vector embeddings generation, and strict, trace-grounded Retrieval-Augmented Generation (RAG) query resolution. 

It is designed specifically for financial analysts and operations teams who require zero-hallucination responses extracted from complex reports (10-K filings, earnings calls, quarterly statements) with explicit page and source file citations.

---

## ✨ Key Features

*   **⚡ Automated Vector Ingestion**: Splits long, unstructured financial narratives and tables into optimized semantic chunks (500 tokens, 50 token overlap) utilizing `TokenTextSplitter`.
*   **🧠 Semantics-Driven Retrieval**: Uses OpenAI's high-performance `text-embedding-3-small` (1536-dimensions) to generate embedding vectors stored directly in PostgreSQL using `pgvector`.
*   **🔒 Strict Zero-Hallucination Guardrails**: Includes precise system prompts and a similarity threshold cut-off (`0.5`) to guarantee that responses are built strictly from matching financial context or fall back gracefully.
*   **🏷️ Citation-Backed Answers**: All responses cite the specific source document and page numbers from which the financial data was extracted.
*   **🐳 Immediate Local Provisioning**: Built-in support for **Spring Boot Docker Compose**, instantly starting up a PostgreSQL database with the `pgvector` extension upon booting the application.

---

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Runtime** | Java OpenJDK | 21 | High-performance, modern JVM features |
| **Framework** | Spring Boot | 3.4.1 | Reactive and enterprise core dependency injection |
| **AI Layer** | Spring AI (BOM) | 1.0.0 | High-level abstraction for vector store and chat clients |
| **LLM Gateway** | OpenAI | `gpt-4o-mini` | Cost-effective and fast reasoning / text synthesis |
| **Embeddings** | OpenAI Embeddings | `text-embedding-3-small` | Semantic search vectors (1536 dims) |
| **Vector Store** | PostgreSQL + pgvector | 16 | Relational persistence & multi-dimensional search |
| **Infrastructure** | Docker Compose | pg16-vector | Containerized PostgreSQL automation |
| **Testing** | JUnit 5 + Testcontainers | 3.4.1 | Reproducible database container integration tests |

---

## 📐 Architecture & Data Flow

The project is structured around clean Spring Boot architectural layers:

```text
finpulse-ai/
├── compose.yml                         # Runs pgvector-enabled PostgreSQL 16
├── pom.xml                             # Core project configuration & dependency BOMs
└── src/
    └── main/
        ├── java/com/finpulse/finpulse_ai/
        │   ├── FinpulseAiApplication.java    # Application Bootstrapper
        │   ├── controller/
        │   │   └── ChatController.java       # Exposes the /api/v1/chat and /api/v1/ingest endpoints
        │   ├── dto/                          # Immutable API Request/Response records
        │   └── service/
        │       ├── IngestionService.java     # Text splitting, embedding generation, and DB storage
        │       ├── SemanticSearchService.java # Threshold-filtered cosine-similarity vector queries
        │       └── ChatService.java          # Context formatting, system prompts, and RAG execution
        └── resources/
            └── application.properties        # Application configurations (models, ports, database credentials)
```

### Ingestion Flow
1. Text is sent to `/api/v1/ingest` along with metadata (`sourceFile`, `pageNumber`, `chunkType`).
2. `IngestionService` converts the payload into Spring AI `Document` models.
3. `TokenTextSplitter` segments the text into ~500 token windows with a 50-token overlap to maintain contextual continuity.
4. Chunks are sent to the OpenAI embedding endpoint, transformed into vectors, and saved within a `vector_store` table in PostgreSQL.

### Query / RAG Flow
1. Questions are sent to `/api/v1/chat`.
2. `SemanticSearchService` performs a cosine similarity lookup against PostgreSQL (`COSINE_DISTANCE` metric, threshold `0.5`).
3. Matching text chunks are formatted as citation blocks (`[Page X | filename]`).
4. A strict system prompt forces the LLM (`gpt-4o-mini`) to answer *only* based on the context. If no context matches, the system returns a safe, pre-configured response: *"I could not find this information in the uploaded documents."*

---

## 🚀 Getting Started

### Prerequisites
*   **Java**: JDK 21+ installed and configured.
*   **Docker Desktop**: Ensure Docker is running locally (required to spin up the PostgreSQL vector database).
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
Run the Maven spring-boot plugin directly from the project directory. The Spring Docker Compose integration will automatically detect and start your local PostgreSQL vector container:

```bash
./mvnw spring-boot:run
```

Once booted, the application will be listening on: **`http://localhost:8201`**

---

## 🔌 API Reference

### 1. Ingest Document Content
Ingests raw text or financial tables from a specific source file and page index.

*   **Endpoint**: `POST /api/v1/ingest`
*   **Headers**: `Content-Type: application/json`
*   **Payload Example**:
    ```json
    {
      "text": "For the fiscal year ended December 31, 2025, FinPulse Inc. reports total operating revenue of $148.5 million, up 12% year-over-year. Operating income stood at $34.2 million, showing strong margin expansion due to platform automation.",
      "sourceFile": "finpulse_annual_report_2025.pdf",
      "pageNumber": 14,
      "chunkType": "TEXT"
    }
    ```

*   **cURL Command**:
    ```bash
    curl -X POST http://localhost:8201/api/v1/ingest \
      -H "Content-Type: application/json" \
      -d '{
        "text": "For the fiscal year ended December 31, 2025, FinPulse Inc. reports total operating revenue of $148.5 million, up 12% year-over-year. Operating income stood at $34.2 million, showing strong margin expansion due to platform automation.",
        "sourceFile": "finpulse_annual_report_2025.pdf",
        "pageNumber": 14,
        "chunkType": "TEXT"
      }'
    ```

*   **Response**:
    ```json
    {
      "chunksStored": 1,
      "sourceFile": "finpulse_annual_report_2025.pdf"
    }
    ```

---

### 2. Grounded Chat Query
Asks a financial query. The server performs semantic search, builds the grounded context, and executes the RAG pipeline.

*   **Endpoint**: `POST /api/v1/chat`
*   **Headers**: `Content-Type: application/json`
*   **Payload Example**:
    ```json
    {
      "message": "What was the operating revenue for FinPulse Inc in fiscal year 2025 and did margins expand?"
    }
    ```

*   **cURL Command**:
    ```bash
    curl -X POST http://localhost:8201/api/v1/chat \
      -H "Content-Type: application/json" \
      -d '{
        "message": "What was the operating revenue for FinPulse Inc in fiscal year 2025 and did margins expand?"
      }'
    ```

*   **Response**:
    ```json
    {
      "answer": "According to the financial reports, FinPulse Inc. reported a total operating revenue of $148.5 million for the fiscal year ended December 31, 2025. This represents a 12% increase year-over-year. Additionally, there was strong margin expansion due to platform automation, with operating income reaching $34.2 million. [Page 14 | finpulse_annual_report_2025.pdf]"
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

For a deep dive into the service design principles, full sequence diagrams, strict conventions (e.g., records for immutability, similarity guardrails), and upcoming multi-agent orchestrator integrations, please refer to the onboarding blueprints in:
👉 **[agent.md](./agent.md)**
