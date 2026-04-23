# Low-Level Architecture

## Backend package map

```text
com.aeon.documentrag.backend
├── config
├── controller
├── dto
├── entity
│   └── type
├── exception
├── mapper
├── repository
└── service
```

## Important classes

### `DocumentController`

- Accepts multipart uploads
- Lists document records
- Deletes document records and vector chunks

### `ChatController`

- Accepts chat prompts
- Returns generated answer plus citations
- Exposes conversation read/delete endpoints

### `DocumentService`

- Validates file types
- Stores uploaded files
- Runs Tika extraction
- Calls chunking service
- Writes chunk documents to ChromaDB
- Keeps H2 metadata synchronized

### `DocumentChunkingService`

- Builds Spring AI `TokenTextSplitter` from config
- Applies metadata enrichment
- Generates deterministic chunk IDs per document

### `RagChatService`

- Builds a `SearchRequest`
- Retrieves similar chunks from ChromaDB
- Calls Spring AI `QuestionAnswerAdvisor`
- Persists user and assistant messages
- Returns citations for transparency

### `ConversationService`

- Creates or validates conversation IDs
- Persists messages
- Renders bounded conversation history for prompting

## Persistence model

### `DocumentRecordEntity`

Tracks:

- upload identity
- source filename
- local storage path
- checksum
- chunk count
- indexing status
- failure reason

### `ConversationEntity`

Tracks:

- conversation identity
- derived title
- creation/update timestamps

### `ConversationMessageEntity`

Tracks:

- message role
- message body
- creation timestamp
- conversation ownership

## Library detail

### Spring Boot

- REST layer, dependency injection, validation, JPA, actuator

### Lombok

- `@RequiredArgsConstructor` for constructor injection in controllers and services
- `@Getter`, `@Setter`, and `@NoArgsConstructor` to keep JPA entities concise while preserving service-layer construction
- `@UtilityClass` for mapper helpers
- `@Slf4j` for lightweight backend diagnostics

### Spring AI

- `ChatClient` for model interaction
- `QuestionAnswerAdvisor` for RAG context injection
- `TokenTextSplitter` for chunking
- Chroma vector store starter
- Ollama model starter

### Apache Tika

- Text extraction for office and document formats without format-specific controller code

### Springdoc OpenAPI

- Swagger UI and generated API schema

### Angular

- Standalone component architecture
- `HttpClient` services for backend integration
- Signal-based local UI state

## Endpoint detail

### `POST /api/v1/documents/ingest`

Request:

- `multipart/form-data`
- field name: `files`

Response:

- upload count
- indexed document metadata list

### `GET /api/v1/documents`

Response:

- all document metadata records ordered by newest first

### `DELETE /api/v1/documents/{documentId}`

Behavior:

- deletes chunk IDs from ChromaDB
- deletes local stored file
- deletes H2 metadata record

### `POST /api/v1/chat/messages`

Request JSON:

```json
{
  "conversationId": "optional-existing-id",
  "prompt": "Summarize the uploaded policy",
  "topK": 5
}
```

Response JSON:

```json
{
  "conversationId": "uuid",
  "answer": "Grounded answer...",
  "citations": [
    {
      "chunkId": "doc-1-chunk-1",
      "documentId": "doc-1",
      "sourceFileName": "policy.pdf",
      "chunkIndex": 1,
      "excerpt": "Relevant text..."
    }
  ],
  "respondedAt": "2026-04-22T22:00:00Z"
}
```

## Chunking configuration

Defined in `application.yml`:

- `app.rag.chunk-size`
- `app.rag.min-chunk-size-chars`
- `app.rag.min-chunk-length-to-embed`
- `app.rag.max-num-chunks`
- `app.rag.keep-separator`

This keeps chunk sizing configurable without code changes.

## Runtime assumptions

- ChromaDB is reachable at `http://localhost:8000`
- Ollama is reachable at `http://localhost:11434`
- Default chat model: `llama3.2`
- Default embedding model: `nomic-embed-text`
