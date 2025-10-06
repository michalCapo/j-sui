# j-sui

Server‑rendered UI for Java: interactive pages with server actions and DOM/WebSocket patches—no frontend framework.

> Status: experimental. APIs may change while things settle.


## Why

- Keep UI logic on the server in Java.
- Render plain HTML strings with Tailwind classes (via CDN) and minimal JS helpers.
- Patch only the parts of the page that change (inner/outer/append/prepend).
- Use WebSockets for live patches when available, fall back to inline scripts.
- No SPA build tools, frameworks, or client state management.


## Features

- Server-rendered HTML components via `jsui.Ui` string builders.
- `Context.Call` and `Context.Submit` generate client-side handlers for actions.
- `Ui.Target` + swap modes: `inline`, `outline`, `append`, `prepend`, `none`.
- Async helpers: `Defer`, `Repeat`, `Delay` for background work and polling.
- Minimal HTTP + WebSocket server in `jsui.Server` (pure Java SE sockets).
- Form helpers with basic binding from `application/x-www-form-urlencoded`.
- Theming utility with `Ui.ThemeSwitcher` and dark-mode CSS overrides.
- Static assets from classpath and favicon helpers in `jsui.App`.


## Quick Start

Prerequisites: JDK 17+

Compile and run the included showcase app:

```bash
# From repo root
javac -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes jsui.examples.Main
# Open http://localhost:1422
```

You should see a navigation bar with multiple demo pages (inputs, tables, radio buttons, append/prepend, deferred jobs, markdown, and more).


## Minimal Example

```java
import jsui.App;
import jsui.Context;
import jsui.Server;
import jsui.Ui;

public class Hello {
  public static void main(String[] args) throws Exception {
    App app = new App("en");

    // A simple page that patches a counter target
    app.Page("/", ctx -> {
      Ui.Target t = Ui.Target();
      String inc = ctx.Call(_ -> {
        // Replace the target with a new value
        return Ui.div("text-2xl font-bold", Ui.targetAttr(t))
                 .render("Count: " + (System.currentTimeMillis() % 100));
      }).Replace(Ui.targetAttr(t));

      String body = Ui.div("p-6 space-y-4").render(
        Ui.div("text-xl font-semibold").render("Hello from j-sui"),
        Ui.div("", Ui.targetAttr(t)).render("Count: 0"),
        Ui.Button().Color(Ui.Blue).OnClick(inc).Render("Increment")
      );
      return app.HTML("Hello", "bg-gray-100 min-h-screen", body);
    });

    Server.builder(app).httpPort(1422).start();
  }
}
```


## Core Concepts

- `App`
  - Composes pages and actions, holds HTML head and theming helpers.
  - Serves static assets from classpath and sets favicon.
- `Context`
  - Request context and helpers for `Call`, `Submit`, `Load`, `Redirect`, messages, patch scheduling.
  - Binds form data to POJOs for `application/x-www-form-urlencoded` posts via `ctx.Body(model)`.
- `Ui`
  - HTML builders (e.g., `div`, `span`, `a`, `img`) that return strings.
  - Form controls (`IText`, `INumber`, `IArea`, `ISelect`, `ICheckbox`, `IRadio`, `IRadioButtons`, dates, etc.).
  - `Target` with `.Replace/.Append/.Prepend/.Render` swaps and skeletons.
  - Utilities: class merging, ID generation, minimal scripts (`__post`, `__applySwap`, theme helper).
- `Server`
  - Lightweight HTTP and WebSocket server implemented with Java networking primitives.
  - Delivers HTML pages, evaluates registered `Context.Callable` handlers, and broadcasts patches.


## Patching Model

- Generate event handlers with `ctx.Call(Callable)` and `ctx.Submit(Callable)`.
- Choose how to apply changes with swap modes:
  - `inline`: replace target element inner HTML.
  - `outline`: replace the whole element.
  - `append`/`prepend`: insert HTML around the existing content.
  - `none`: run without targeting a specific element.
- Use `ctx.Patch(target, html)` from server-side jobs, or schedule with `Defer`, `Repeat`, `Delay`.


## Theming

- Tailwind v2 via CDN is injected by default in `App`.
- `Ui.ThemeSwitcher("")` renders a client-side theme toggle (light/dark/system) and minimal dark CSS overrides.


## Assets and Favicons

Serve resources from your classpath and wire a favicon:

```java
app.AssetsFromClasspath("/assets", "public/assets", 86400);
app.FaviconFromClasspath("/assets", "public/favicon.ico", 86400);
```

Place files under `src/main/resources/public/assets` and reach them at `/assets/...`.


## Running the Showcase

The repo includes a multi-page showcase with forms, inputs, tables, skeletons, and async examples.

Entry point: `src/main/java/jsui/examples/Main.java`.

```bash
javac -d target/classes $(find src/main/java -name "*.java")
java -cp target/classes jsui.examples.Main
# Navigate to http://localhost:1422
```


## Notes

- This is a simplified Java port inspired by the t-sui/g-sui approach.
- No external server framework is required; everything runs on Java SE.
- For production, consider fronting with a reverse proxy and tightening headers.


## License

MIT — see `LICENSE`.

