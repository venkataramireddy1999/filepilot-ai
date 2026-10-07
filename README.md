# FilePilot AI

**Talk to your files.** FilePilot AI is a Spring Boot application that lets you manage a local folder using plain English. Behind a simple REST endpoint, an LLM decides which file operation to run and executes it through the **Model Context Protocol (MCP)**.

> "Create a file called `meeting-notes.md` with today's agenda."
> "Summarize everything in the `reports` folder."
> "Find all files that mention *invoice* and tell me what they contain."

---

## Features

- **Natural-language file management**: read, create, write and update files and folders by just asking.
- **MCP-powered tooling**: file operations are exposed to the model as MCP tools through the official [`@modelcontextprotocol/server-filesystem`](https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem) server, so there is no hand-written file-handling code to maintain.
- **Sandboxed by design**: the model can only touch the single directory you mount into the container (`/data/documents`).
- **Conversation memory**: each `userId` + `chatId` pair keeps its own sliding window of the last 10 messages, so follow-ups like *"now add a conclusion to it"* work.
- **Honest assistant**: the system prompt tells the model to verify each operation and never claim success unless the tool call actually succeeded.
- **One-command deployment**: ships with a Dockerfile that bundles the JRE and Node.js (needed to launch the MCP server).

---

## How it works

```
┌────────┐   POST /api/ai/chat   ┌──────────────────────────────────────────┐
│ Client │ ────────────────────▶ │            Spring Boot app               │
└────────┘                       │  AiController → AiService → ChatClient   │
                                 │                     │                    │
                                 │     chat memory ◀───┤                    │
                                 │     (userId:chatId) │                    │
                                 └─────────────────────┼────────────────────┘
                                                       │ tool calls
                          ┌────────────────────────────┴───────────┐
                          ▼                                        ▼
                 ┌─────────────────┐                    ┌────────────────────┐
                 │  OpenAI model   │                    │ MCP client (stdio) │
                 │ (plans actions) │                    └─────────┬──────────┘
                 └─────────────────┘                              │ spawns via npx
                                                       ┌──────────▼───────────┐
                                                       │ MCP filesystem server│
                                                       └──────────┬───────────┘
                                                                  ▼
                                                      /data/documents (mounted)
```

1. A client sends a message to `POST /api/ai/chat`.
2. Spring AI's `ChatClient` forwards it to the model together with the list of MCP tools and the conversation history.
3. The model decides which tool(s) to call. Spring AI executes them through the MCP client, which talks over **stdio** to the filesystem MCP server (launched with `npx`).
4. Tool results go back to the model, which writes the final answer returned to the client.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| AI integration | Spring AI 2.0.1 (`ChatClient`, MCP client, OpenAI starter) |
| Protocol | Model Context Protocol (stdio transport) |
| File tools | `@modelcontextprotocol/server-filesystem` (Node.js 22) |
| LLM provider | OpenAI |
| Build | Maven (wrapper included) |
| Packaging | Docker (`eclipse-temurin:21-jre` + Node.js 22) |

---

## Prerequisites

- JDK 21
- Docker
- An [OpenAI API key](https://platform.openai.com/api-keys)

To run without Docker you also need Node.js 22+ on your machine, because the app launches the MCP server with `npx`.

---

## Getting started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/filepilot-ai.git
cd filepilot-ai
```

### 2. Create a `.env` file

```env
OPENAI_API_KEY=sk-your-key-here
```

> Never commit this file. Add `.env` to your `.gitignore`.

### 3. Build the JAR

```bash
./mvnw clean package -DskipTests
```

On Windows use `mvnw.cmd clean package -DskipTests`.

### 4. Build the Docker image

```bash
docker build -t filepilot-ai .
```

The Dockerfile copies `target/*.jar`, so step 3 must run first.

### 5. Run the container

```bash
docker run --name filepilot-ai-container \
  -p 8080:8080 \
  --env-file .env \
  -v "D:/Documents:/data/documents" \
  filepilot-ai
```

Replace `D:/Documents` with the folder you want FilePilot to manage. Inside the container it is always available at `/data/documents`.

**Read-only mode:** append `:ro` to the volume (`-v "D:/Documents:/data/documents:ro"`) to let the assistant read, search and summarize files while making it impossible to change them. Create and update requests will fail in this mode, which makes it a good way to try the app safely first.

---

## Usage

### Endpoint

`POST /api/ai/chat`

| Field | Type | Description |
|---|---|---|
| `userId` | string, required | Identifies the user |
| `chatId` | string, required | Identifies the conversation; reuse it to keep context |
| `query` | string, required | What you want done, in plain English |

The response body is the assistant's reply as plain text. Validation errors (blank fields) return a `400` with a descriptive message.

### Example

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
        "userId": "rami",
        "chatId": "chat-1",
        "query": "Create a file named todo.txt with three tasks for this week"
      }'
```

Follow-up in the same conversation:

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
        "userId": "rami",
        "chatId": "chat-1",
        "query": "Add a fourth task: review pull requests"
      }'
```

### Things to try

- "List everything in the root folder."
- "Read `notes/ideas.md` and give me a 3-bullet summary."
- "Create a folder called `archive` and move `old-report.pdf` into it."
- "Search for files with *budget* in the name."

---

## Configuration

| Setting | Where | Notes |
|---|---|---|
| `OPENAI_API_KEY` | `.env` / environment | Required |
| Allowed directory | `src/main/resources/servers-configuration.json` | Defaults to `/data/documents`; the MCP server refuses access outside it |
| Chat memory size | `AiConfig.java` | `MessageWindowChatMemory` keeps the last 10 messages per conversation |
| System prompt | `AiConfig.java` | Controls the assistant's behavior and its "verify before claiming success" rule |
| Model | `application.properties` | Uses the Spring AI OpenAI default; override with `spring.ai.openai.chat.options.model` |

MCP server definition:

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/data/documents"]
    }
  }
}
```

---

## Project structure

```
filepilot-ai/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/dev/nonsyncbobbal/filepilot_ai/
    │   ├── FilepilotAiApplication.java   # Entry point
    │   ├── config/AiConfig.java          # ChatClient, system prompt, chat memory, MCP tools
    │   ├── controller/AiController.java  # POST /api/ai/chat
    │   ├── service/AiService.java        # Builds the conversation ID and calls the model
    │   └── dto/ChatRequest.java          # Validated request body
    └── resources/
        ├── application.properties
        └── servers-configuration.json    # MCP server definition
```

---

## Security notes

- **Scope the mount.** Only mount a folder you are comfortable letting an AI read and modify. Start with `:ro` or a test folder.
- **No authentication yet.** The API is open, so don't expose port 8080 to the internet as is. Put it behind an authenticated gateway or add Spring Security.
- **Verbose logging.** Debug logging for Spring AI advisors and Spring Web is enabled in `application.properties`, which prints prompts and file content to the logs. Turn it down before running with sensitive data.
- **Your data goes to OpenAI.** Contents of files the assistant reads are sent to the model provider as part of the conversation.

---

## Limitations

- Chat memory is in-memory, so it resets when the container restarts.
- The reference filesystem MCP server covers reading, creating, writing, editing, listing, searching and moving files. It does not expose a delete tool, so deleting files would need a custom or alternative MCP server.
- Best suited to text files; binary formats are not interpreted.

---

## Roadmap

- [ ] Persistent chat memory (database-backed)
- [ ] Authentication and per-user folders
- [ ] Streaming responses
- [ ] Support for additional MCP servers (Git, databases, web)
- [ ] Support for other model providers
- [ ] Simple web chat UI

---

## License

Add a license of your choice (for example MIT) before publishing.

---

Built by **Venkata Rami Reddy Bobbala** with Java, Spring AI and MCP.
