# AGENTS.md - j-sui Project Guide

## Build Commands

**Project**: Maven-based Java 21 project with JUnit 5

```bash
# Compile project
mvn compile

# Run all tests
mvn test

# Run single test class
mvn test -Dtest=ClassName

# Run single test method
mvn test -Dtest=ClassName#methodName

# Package JAR
mvn package

# Clean build
mvn clean compile
```

**Quick start (without Maven)**:
```bash
javac -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes jsui.examples.Main
```

## Code Style Guidelines

### Imports
- Group imports: java.* first, then third-party, then project imports (jsui.*)
- Use wildcard imports judiciously (standard library OK, minimize for third-party)
- Blank line between import groups

### Class Design
- Use `final` for immutable classes (App, Server, Ui, Data, Context)
- Private constructors for utility classes (Ui, Data, HtmlUtils)
- Static factory methods for component creation: `Ui.IText()`, `Ui.Button()`
- Builder pattern for complex objects: `Server.builder()`, `Ui.Button().Color()`
- Use Lombok `@Data` with `@Accessors(chain = true, fluent = false)` for mutable DTOs

### Naming Conventions
- Classes: `PascalCase` (e.g., `App`, `Server`, `Context`)
- Methods: `camelCase` (e.g., `handle()`, `render()`, `invoke()`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `BOOL`, `DATES`, `DEFAULT_CAPTCHA_LENGTH`)
- Interfaces: `PascalCase` with single method names (e.g., `Callable`, `RenderRow`, `Export`)
- Private helper classes: lowerCamelCase (e.g., `sessRec`, `AssetCfg` inside App)

### Type Safety & Generics
- Use generics for reusable components: `CollateModel<T>`, `LoadResult<T>`
- Type parameters: Single uppercase letter `T`, `R`, `E`
- Wildcards sparingly; prefer bounded types when possible

### Error Handling
- Log exceptions with debug mode: `if (debugEnabled) System.out.println()`
- Silent catch for non-critical paths: `catch (Throwable ignored) {}`
- Return empty collections/defaults instead of null: `Collections.emptyList()`
- Check null before operations: `if (value == null || value.isEmpty())`
- Parse failures return defaults: `parseInt(value, fallback)`, `0L`, `0.0d`

### Threading & Concurrency
- Use `ConcurrentHashMap` for thread-safe maps
- Daemon threads for background workers: `new Thread(r, "name"); t.setDaemon(true)`
- Use `volatile` for visibility: `volatile long generation`
- Register cleanup callbacks: `app.registerClear(sessionId, targetId, clear)`
- Interrupt-safe loops: check `Thread.currentThread().isInterrupted()`

### HTML/UI Generation
- String-based HTML building via `Ui.TagBuilder` pattern
- Tailwind CSS classes inline: `"bg-white rounded-lg p-4"`
- Auto-generated IDs: `Ui.Target()` creates unique IDs
- Swap modes: `inline`, `outline`, `append`, `prepend`, `none`
- Escape user content: `HtmlUtils.escape()` for HTML, `Ui.Normalize()` for attributes
- Use `Ui.Classes()` to merge class strings, filtering nulls

### Form Handling
- Bind to POJOs via reflection: `ctx.Body(formModel)`
- Nested properties with dot notation: `"Filter.0.Field"`, `"Filter.0.Bool"`
- Date format: `"yyyy-MM-dd"` for form inputs, `SimpleDateFormat` for parsing
- Boolean parsing accepts: "true"/"1"/"on"/"yes" (case-insensitive)

### Constants & Configuration
- Define sizes as static fields: `Ui.XS`, `Ui.SM`, `Ui.MD`, `Ui.LG`, `Ui.XL`
- Color constants: `Ui.Blue`, `Ui.Green`, `Ui.Red`, `Ui.Purple`
- CSS constants for input styles: `Ui.INPUT`, `Ui.AREA`, `Ui.BTN`, `Ui.DISABLED`

### Functional Patterns
- Use lambdas for callbacks: `ctx.Call(c -> { return html; })`
- Functional interfaces: `Context.Callable`, `RenderRow<T>`, `Export<T>`, `Loader<T>`
- Records for simple data: `record Route(String path, String title) {}`
- Method references: `ShowcasePage::render`, `Ui.Target()`

### File & I/O
- Use try-with-resources: `try (InputStream is = ...)`
- Buffer sizes: 8192 bytes for reads
- Close resources silently on cleanup: `catch (IOException ignored) {}`
- Assets from classpath: `getClass().getClassLoader().getResourceAsStream(path)`

### WebSocket & Server
- Session cookie: `"jsui_session"`
- Security headers: CSP, X-Frame-Options, Permissions-Policy
- Patch format: `{"type":"patch","id":"<id>","swap":"<mode>","html":"<html>"}`
- Ping/pong for keepalive

### Testing (JUnit 5)
- Test classes in `src/test/java`
- Use `@Test` annotations
- Playwright for browser tests if needed

### Lombok Usage
- `@Data`: getters, setters, equals, hashCode, toString
- `@NoArgsConstructor`: no-arg constructor
- `@Accessors(chain = true, fluent = false)`: enable chaining with . setters
- Scope: `provided` for compile-time generation

## Dependencies (pom.xml)
- Java 21 (maven.compiler.release)
- Lombok 1.18.30 (provided scope)
- JUnit Jupiter 5.11.4 (test scope)
- Playwright 1.49.0 (browser testing)
- Apache POI 5.2.5 (Excel export)

## Key Patterns

### Action Handlers
```java
app.Page("/path", ctx -> {
    String action = ctx.Call(c -> {
        // Logic here
        return Ui.div("").render("Updated");
    }).Replace(targetAttr);
    return body + action;
});
```

### Target-based Patching
```java
Ui.Target t = Ui.Target();
ctx.Patch(t.Render, "<div>Content</div>"); // or .Replace/.Append/.Prepend
```

### Form Binding
```java
FormData data = new FormData();
ctx.Body(data); // Populates fields from POST body
```

### Async Operations
```java
ctx.Defer(target, job);        // Run once after response
ctx.Repeat(target, delay, job); // Run every N milliseconds
ctx.Delay(target, delay, job);  // Run once after N milliseconds
```

## Code Structure

- `jsui.App`: Page composition, HTML head, session management, routing
- `jsui.Context`: Request context, form binding, action builders, patching
- `jsui.Ui`: HTML builders, form controls, targets, utilities
- `jsui.Data`: Collate table, filtering, sorting, Excel export
- `jsui.Server`: HTTP/WebSocket server, request handling
- `jsui.HtmlUtils`: Escaping utilities

## Notes

- No external server framework (uses Java SE sockets)
- Tailwind CSS via CDN (configurable)
- WebSocket for real-time patches with inline fallback
- Experimental: APIs may change
- UTF-8 encoding for all text I/O
- Thread-safe for concurrent request handling
