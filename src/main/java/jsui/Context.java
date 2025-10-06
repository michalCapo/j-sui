package jsui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java skeleton of t-sui/ui.server.ts Context with attribute-generating helpers.
 *
 * For server integration, wire this to your HTTP handling and evaluate
 * callables manually. The client-side JS helpers referenced here are placeholders.
 */
public final class Context {
    public final App app;
    public final String sessionID;
    public final String method;
    public final String path;
    public final Map<String, String> headers;
    public final byte[] body;
    public final Map<String, String> query;
    public final String queryString;
    public final List<String> append = new ArrayList<>();
    private final PatchSender patchSender;
    private final long pageGeneration;

    public Context(App app, String sessionID) {
        this(app, sessionID, "GET", "/", Collections.emptyMap(), new byte[0], Collections.emptyMap(), "", null);
    }

    public Context(App app, String sessionID, String method, String path, Map<String, String> headers, byte[] body) {
        this(app, sessionID, method, path, headers, body, Collections.emptyMap(), "", null);
    }

    public Context(App app, String sessionID, String method, String path, Map<String, String> headers, byte[] body, Map<String, String> query, String queryString, PatchSender patchSender) {
        this.app = app;
        this.sessionID = sessionID != null ? sessionID : "";
        this.method = method != null ? method : "GET";
        this.path = path != null ? path : "/";
        this.headers = headers != null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
        this.body = body != null ? body.clone() : new byte[0];
        this.query = query != null ? Collections.unmodifiableMap(new HashMap<>(query)) : Collections.emptyMap();
        this.queryString = queryString != null ? queryString : "";
        this.patchSender = patchSender;
        this.pageGeneration = app != null ? app.currentSessionGeneration(this.sessionID) : 0L;
    }

    // Populate a POJO from request body (supports form posts)
    public <T> void Body(T output) {
        if (output == null) {
            return;
        }
        if (body == null || body.length == 0) {
            return;
        }
        String contentType = header("content-type");
        if (contentType != null && !contentType.contains("application/x-www-form-urlencoded")) {
            return;
        }
        Map<String, List<String>> form = parseForm(bodyAsString());
        if (form.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : form.entrySet()) {
            applyValue(output, entry.getKey(), entry.getValue());
        }
    }

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public String header(String name) {
        if (name == null) {
            return null;
        }
        return headers.get(name.toLowerCase());
    }

    public String query(String key) { return query.get(key); }

    public Callable Callable(Callable method) { return app.Callable(method); }
    public Callable Action(String uid, Callable action) { return app.Action(uid, action); }

    // Compose client-side submit/post handlers (placeholder JS strings)
    public String Post(String as, Ui.Swap swap, Action action) {
        String path = app.pathOf(action.method);
        if (path == null || path.isEmpty()) path = "/__not_registered";
        String tgt = action.target != null ? action.target.id : "";
        // lightweight noop-friendly script hook
        String js = "(function(e){try{if(window.__post){return __post('" + (as!=null?as:"") + "','" + path + "','" + swap + "','" + tgt + "',e);} }catch(_){ } return false;})(event)";
        return Ui.Normalize(js);
    }

    public CallBuilder Call(Callable method, Object... values) {
        final Callable callable = this.Callable(method);
        final Context self = this;
        return new CallBuilder() {
            @Override public String Render(Ui.Attr target) { return self.Post("POST", Ui.Swap.inline, new Action(callable, target, values)); }
            @Override public String Replace(Ui.Attr target) { return self.Post("POST", Ui.Swap.outline, new Action(callable, target, values)); }
            @Override public String Append(Ui.Attr target) { return self.Post("POST", Ui.Swap.append, new Action(callable, target, values)); }
            @Override public String Prepend(Ui.Attr target) { return self.Post("POST", Ui.Swap.prepend, new Action(callable, target, values)); }
            @Override public String None() { return self.Post("POST", Ui.Swap.none, new Action(callable, null, values)); }
        };
    }

    public SubmitBuilder Submit(Callable method, Object... values) {
        final Callable callable = this.Callable(method);
        final Context self = this;
        return new SubmitBuilder() {
            @Override public Ui.Attr Render(Ui.Attr target) { return Ui.Attr.of().onsubmit(self.Post("FORM", Ui.Swap.inline, new Action(callable, target, values))); }
            @Override public Ui.Attr Replace(Ui.Attr target) { return Ui.Attr.of().onsubmit(self.Post("FORM", Ui.Swap.outline, new Action(callable, target, values))); }
            @Override public Ui.Attr Append(Ui.Attr target) { return Ui.Attr.of().onsubmit(self.Post("FORM", Ui.Swap.append, new Action(callable, target, values))); }
            @Override public Ui.Attr Prepend(Ui.Attr target) { return Ui.Attr.of().onsubmit(self.Post("FORM", Ui.Swap.prepend, new Action(callable, target, values))); }
            @Override public Ui.Attr None() { return Ui.Attr.of().onsubmit(self.Post("FORM", Ui.Swap.none, new Action(callable, null, values))); }
        };
    }

    public Ui.Attr Load(String href) { return Ui.Attr.of().onclick(Ui.Normalize("__load(\"" + href + "\")")); }
    public String Reload() { return Ui.Normalize("<script>window.location.reload();</script>"); }
    public String Redirect(String href) { return Ui.Normalize("<script>window.location.href='" + href + "';</script>"); }

    // Translate message using simple java.text.MessageFormat ({0}, {1}, ...)
    public String Translate(String message, Object... val) {
        if (message == null) return "";
        try {
            return java.text.MessageFormat.format(message, val);
        } catch (IllegalArgumentException ex) {
            return message;
        }
    }

    public void Success(String message) { append.add(messageBox(message, "bg-green-700 text-white")); }
    public void Error(String message) { append.add(messageBox(message, "bg-red-700 text-white")); }
    public void Info(String message) { append.add(messageBox(message, "bg-blue-700 text-white")); }

    public void Patch(Ui.Action target, String html) {
        Patch(target, html, null);
    }

    public void Patch(Ui.Action target, String html, Runnable clear) {
        if (target == null || target.id == null || target.id.isEmpty() || html == null) return;
        String swap = target.swap != null ? target.swap.name() : Ui.Swap.inline.name();
        if (clear != null && app != null) {
            app.registerClear(sessionID, target.id, clear);
        }
        if (patchSender != null) {
            String json = "{\"type\":\"patch\",\"id\":\"" + Ui.Normalize(target.id) + "\",\"swap\":\"" + swap + "\",\"html\":\"" + Ui.Normalize(html) + "\"}";
            try {
                patchSender.send(sessionID, json);
            } catch (Exception ex) {
                // fallback to inline script on failure
                append.add(patchScriptInline(target.id, swap, html));
            }
        } else {
            append.add(patchScriptInline(target.id, swap, html));
        }
    }

    public void Defer(Ui.Action target, Callable job) { Defer(target, job, null); }

    public void Defer(Ui.Action target, Callable job, Runnable clear) {
        if (job == null || target == null) return;
        Thread t = new Thread(() -> {
            try {
                String result = job.handle(this);
                if (result != null) {
                    if (app != null && app.currentSessionGeneration(sessionID) != pageGeneration) {
                        return;
                    }
                    Patch(target, result);
                }
            } catch (Exception ignored) { }
        }, "jsui-defer");
        // Register clear to interrupt the worker and run user cleanup
        if (app != null) {
            app.registerClear(sessionID, target.id, () -> {
                try { t.interrupt(); } catch (Throwable ignored) { }
                if (clear != null) { try { clear.run(); } catch (Throwable ignored) { } }
            });
        }
        t.start();
    }

    // Repeat: periodically invoke job and patch the target with the returned HTML until cleared.
    public void Repeat(Ui.Action target, long intervalMillis, Callable job) { Repeat(target, intervalMillis, job, null); }

    public void Repeat(Ui.Action target, long intervalMillis, Callable job, Runnable clear) {
        if (job == null || target == null) return;
        final long delay = Math.max(50L, intervalMillis);
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Stop if user navigated (generation changed)
                    if (app != null && app.currentSessionGeneration(sessionID) != pageGeneration) {
                        break;
                    }
                    String html = job.handle(this);
                    if (html != null) Patch(target, html);
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {
                }
            }
        }, "jsui-repeat");
        if (app != null) {
            app.registerClear(sessionID, target.id, () -> {
                try { t.interrupt(); } catch (Throwable ignored) { }
                if (clear != null) { try { clear.run(); } catch (Throwable ignored) { } }
            });
        }
        t.start();
    }

    // Delay: run job once after a delay and patch the target; auto-clears if target becomes invalid
    public void Delay(Ui.Action target, long delayMillis, Callable job) { Delay(target, delayMillis, job, null); }

    public void Delay(Ui.Action target, long delayMillis, Callable job, Runnable clear) {
        if (job == null || target == null) return;
        final long wait = Math.max(0L, delayMillis);
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(wait);
                if (Thread.currentThread().isInterrupted()) return;
                if (app != null && app.currentSessionGeneration(sessionID) != pageGeneration) {
                    return;
                }
                String html = job.handle(this);
                if (html != null) Patch(target, html);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
            }
        }, "jsui-delay");
        if (app != null) {
            app.registerClear(sessionID, target.id, () -> {
                try { t.interrupt(); } catch (Throwable ignored) { }
                if (clear != null) { try { clear.run(); } catch (Throwable ignored) { } }
            });
        }
        t.start();
    }

    // Trigger a client-side download using a data: URL; appends a script to the response.
    public void DownloadAs(InputStream stream, String contentType, String name) throws IOException {
        if (stream == null) return;
        try (java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream()) {
            byte[] tmp = new byte[8192];
            int r;
            while ((r = stream.read(tmp)) != -1) buf.write(tmp, 0, r);
            DownloadAs(buf.toByteArray(), contentType, name);
        }
    }

    public void DownloadAs(byte[] content, String contentType, String name) {
        if (content == null) return;
        String ct = (contentType == null || contentType.isEmpty()) ? "application/octet-stream" : contentType;
        String filename = (name == null || name.isEmpty()) ? "download" : name;
        String base64 = java.util.Base64.getEncoder().encodeToString(content);
        String href = "data:" + ct + ";base64," + base64;
        String js = "(function(){var a=document.createElement('a');a.href='" + Ui.Normalize(href) +
                "';a.download='" + Ui.Normalize(filename) + "';document.body.appendChild(a);a.click();setTimeout(function(){try{document.body.removeChild(a);}catch(_){}} ,0);} )();";
        append.add(Ui.Script(js));
    }

    private String patchScriptInline(String id, String swap, String html) {
        String safe = Ui.Normalize(html);
        String s = "(function(){var el=document.getElementById('" + Ui.Normalize(id) + "'); if(!el) return;" +
                "var h=\"" + safe + "\";" +
                "switch('" + swap + "'){" +
                "case 'outline': el.outerHTML=h; break;" +
                "case 'append': el.insertAdjacentHTML('beforeend', h); break;" +
                "case 'prepend': el.insertAdjacentHTML('afterbegin', h); break;" +
                "default: el.innerHTML=h; } })();";
        return Ui.Script(s);
    }

    // Sender used by Server to deliver patches to a session (via WS)
    public interface PatchSender { void send(String sessionId, String message) throws Exception; }

    private String messageBox(String message, String color) {
        String id = "__messages__";
        String inner = Ui.div("rounded px-3 py-2 shadow " + color).render(Ui.span("").render(message));
        return Ui.Normalize(Ui.div("fixed bottom-4 right-4 z-50", Ui.Attr.of().id(id)).render(inner));
    }

    private Map<String, List<String>> parseForm(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> out = new HashMap<>();
        String[] parts = raw.split("&");
        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            String key;
            String value;
            int idx = part.indexOf('=');
            if (idx >= 0) {
                key = part.substring(0, idx);
                value = part.substring(idx + 1);
            } else {
                key = part;
                value = "";
            }
            key = decode(key);
            value = decode(value);
            if (key.isEmpty()) {
                continue;
            }
            out.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return out;
    }

    private String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    private void applyValue(Object target, String path, List<String> values) {
        if (target == null || path == null || path.isEmpty()) {
            return;
        }
        String[] segments = path.split("\\.");
        Object current = target;
        for (int i = 0; i < segments.length - 1; i++) {
            current = ensureNested(current, segments[i]);
            if (current == null) {
                return;
            }
        }
        String fieldName = segments[segments.length - 1];
        Field field = findField(current.getClass(), fieldName);
        if (field == null) {
            return;
        }
        field.setAccessible(true);
        String last = values != null && !values.isEmpty() ? values.get(values.size() - 1) : "";
        try {
            Class<?> type = field.getType();
            if (type == String.class) {
                field.set(current, last);
            } else if (type == boolean.class || type == Boolean.class) {
                field.set(current, parseBoolean(last));
            } else if (type == int.class || type == Integer.class) {
                field.set(current, parseInt(last));
            } else if (type == long.class || type == Long.class) {
                field.set(current, parseLong(last));
            } else if (type == double.class || type == Double.class) {
                field.set(current, parseDouble(last));
            } else if (type == float.class || type == Float.class) {
                field.set(current, (float) parseDouble(last));
            } else {
                // Fallback: attempt to set directly
                field.set(current, last);
            }
        } catch (IllegalAccessException ex) {
            // ignore assignment errors
        }
    }

    private Object ensureNested(Object target, String fieldName) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        field.setAccessible(true);
        try {
            Object value = field.get(target);
            if (value != null) {
                return value;
            }
            Class<?> type = field.getType();
            Object created = null;
            try {
                created = type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                return null;
            }
            field.set(target, created);
            return created;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("on") || normalized.equals("yes");
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return 0.0d;
        }
    }

    // Types ------------------------------------------------------------------
    public interface Callable { String handle(Context ctx) throws Exception; }

    public interface CallBuilder {
        String Render(Ui.Attr target);
        String Replace(Ui.Attr target);
        String Append(Ui.Attr target);
        String Prepend(Ui.Attr target);
        String None();
    }

    public interface SubmitBuilder {
        Ui.Attr Render(Ui.Attr target);
        Ui.Attr Replace(Ui.Attr target);
        Ui.Attr Append(Ui.Attr target);
        Ui.Attr Prepend(Ui.Attr target);
        Ui.Attr None();
    }

    public static final class Action {
        public final Callable method;
        public final Ui.Attr target;
        public final Object[] values;
        public Action(Callable method, Ui.Attr target, Object... values) {
            this.method = method; this.target = target; this.values = values;
        }
    }
}
