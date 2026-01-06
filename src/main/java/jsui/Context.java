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
 * Java skeleton of t-sui/ui.server.ts Context with attribute-generating
 * helpers.
 *
 * For server integration, wire this to your HTTP handling and evaluate
 * callables manually. The client-side JS helpers referenced here are
 * placeholders.
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

    public Context(App app, String sessionID, String method, String path, Map<String, String> headers, byte[] body,
            Map<String, String> query, String queryString, PatchSender patchSender) {
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

    public String query(String key) {
        return query.get(key);
    }

    public Callable Callable(Callable method) {
        return app.Callable(method);
    }

    public Callable Action(String uid, Callable action) {
        return app.Action(uid, action);
    }

    public String Post(String as, ui.Swap swap, Action action) {
        String path = app.pathOf(action.method);
        if (path == null || path.isEmpty())
            path = "/__not_registered";
        String tgt = action.target != null ? action.target.id : "";
        String normalizedAs = as != null ? as : "";
        String js = """
                try{if(event&&event.preventDefault)event.preventDefault();}catch(_){}return window.__post?__post('%s','%s','%s','%s',event):false;"""
                .formatted(normalizedAs, path, swap, tgt);
        return ui.Normalize(js);
    }

    public CallBuilder Call(Callable method, Object... values) {
        final Callable callable = this.Callable(method);
        final Context self = this;
        return new CallBuilder() {
            @Override
            public String Render(ui.Attr target) {
                return self.Post("POST", ui.Swap.inline, new Action(callable, target, values));
            }

            @Override
            public String Replace(ui.Attr target) {
                return self.Post("POST", ui.Swap.outline, new Action(callable, target, values));
            }

            @Override
            public String Append(ui.Attr target) {
                return self.Post("POST", ui.Swap.append, new Action(callable, target, values));
            }

            @Override
            public String Prepend(ui.Attr target) {
                return self.Post("POST", ui.Swap.prepend, new Action(callable, target, values));
            }

            @Override
            public String None() {
                return self.Post("POST", ui.Swap.none, new Action(callable, null, values));
            }
        };
    }

    public SubmitBuilder Submit(Callable method, Object... values) {
        final Callable callable = this.Callable(method);
        final Context self = this;
        return new SubmitBuilder() {
            @Override
            public ui.Attr Render(ui.Attr target) {
                return ui.Attr.of().onsubmit(self.Post("FORM", ui.Swap.inline, new Action(callable, target, values)));
            }

            @Override
            public ui.Attr Replace(ui.Attr target) {
                return ui.Attr.of().onsubmit(self.Post("FORM", ui.Swap.outline, new Action(callable, target, values)));
            }

            @Override
            public ui.Attr Append(ui.Attr target) {
                return ui.Attr.of().onsubmit(self.Post("FORM", ui.Swap.append, new Action(callable, target, values)));
            }

            @Override
            public ui.Attr Prepend(ui.Attr target) {
                return ui.Attr.of().onsubmit(self.Post("FORM", ui.Swap.prepend, new Action(callable, target, values)));
            }

            @Override
            public ui.Attr None() {
                return ui.Attr.of().onsubmit(self.Post("FORM", ui.Swap.none, new Action(callable, null, values)));
            }
        };
    }

    public ui.Attr Load(String href) {
        return ui.Attr.of().onclick(ui.Normalize("__load(\"%s\")".formatted(href)));
    }

    public String Reload() {
        return ui.Normalize("<script>window.location.reload();</script>");
    }

    public String Redirect(String href) {
        return ui.Normalize("<script>window.location.href='%s';</script>".formatted(href));
    }

    public String Translate(String message, Object... val) {
        if (message == null)
            return "";
        try {
            return java.text.MessageFormat.format(message, val);
        } catch (IllegalArgumentException ex) {
            return message;
        }
    }

    public void Success(String message) {
        displayMessage(message, "bg-green-700 text-white");
    }

    public void Error(String message) {
        displayMessage(message, "bg-red-700 text-white");
    }

    public void Info(String message) {
        displayMessage(message, "bg-blue-700 text-white");
    }

    /**
     * Shows an error toast with a Reload button.
     */
    public void ErrorReload(String message) {
        displayError(message);
    }

    public void Patch(ui.Action target, String html) {
        Patch(target, html, null);
    }

    public void Patch(ui.Action target, String html, Runnable clear) {
        if (target == null || target.id == null || target.id.isEmpty() || html == null)
            return;

        String swap = target.swap != null ? target.swap.name() : ui.Swap.inline.name();
        if (clear != null && app != null) {
            app.registerClear(sessionID, target.id, clear);
        }
        if (patchSender != null) {
            String json = "{\"type\":\"patch\",\"id\":\"%s\",\"swap\":\"%s\",\"html\":\"%s\"}"
                    .formatted(ui.Normalize(target.id), swap, ui.EscapeJson(html));
            try {
                patchSender.send(sessionID, json);
                return;
            } catch (Exception ex) {
                // WebSocket not connected yet or other error.
                // For background threads (Repeat/Delay/Defer), the loop will retry.
                // For synchronous rendering, fall through to inline script below.
            }
        }
        // Fallback: inline script for synchronous HTTP responses
        // (This is ignored for background threads since the response has already been
        // sent)
        append.add(patchScriptInline(target.id, swap, html));
    }

    public void Defer(ui.Action target, Callable job) {
        Defer(target, job, null);
    }

    public void Defer(ui.Action target, Callable job, Runnable clear) {
        if (job == null || target == null)
            return;
        Thread t = new Thread(() -> {
            try {
                String result = job.handle(this);
                if (result != null) {
                    if (app != null && app.currentSessionGeneration(sessionID) != pageGeneration) {
                        return;
                    }
                    Patch(target, result);
                }
            } catch (Exception ignored) {
            }
        }, "jsui-defer");
        if (app != null) {
            app.registerClear(sessionID, target.id, () -> {
                try {
                    t.interrupt();
                } catch (Throwable ignored) {
                }
                if (clear != null) {
                    try {
                        clear.run();
                    } catch (Throwable ignored) {
                    }
                }
            });
        }
        t.start();
    }

    public void Repeat(ui.Action target, long intervalMillis, Callable job) {
        Repeat(target, intervalMillis, job, null);
    }

    public void Repeat(ui.Action target, long intervalMillis, Callable job, Runnable clear) {
        if (job == null || target == null)
            return;

        final long delay = Math.max(50L, intervalMillis);
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (app != null && app.currentSessionGeneration(sessionID) != pageGeneration) {
                        break;
                    }
                    String html = job.handle(this);
                    if (html != null)
                        Patch(target, html);
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
                try {
                    t.interrupt();
                } catch (Throwable ignored) {
                }
                if (clear != null) {
                    try {
                        clear.run();
                    } catch (Throwable ignored) {
                    }
                }
            });
        }
        t.start();
    }

    public void Delay(ui.Action target, long delayMillis, Callable job) {
        Delay(target, delayMillis, job, null);
    }

    public void Delay(ui.Action target, long delayMillis, Callable job, Runnable clear) {
        if (job == null || target == null)
            return;

        final long wait = Math.max(0L, delayMillis);

        Runnable r = () -> {
            try {
                Thread.sleep(wait);
                if (Thread.currentThread().isInterrupted())
                    return;
                if (app != null && app.currentSessionGeneration(sessionID) != pageGeneration) {
                    return;
                }
                String html = job.handle(this);
                if (html != null)
                    Patch(target, html);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
            }
        };

        Thread t = new Thread(r, "jsui-delay");

        if (app != null) {
            app.registerClear(sessionID, target.id, () -> {
                try {
                    t.interrupt();
                } catch (Throwable ignored) {
                }
                if (clear != null) {
                    try {
                        clear.run();
                    } catch (Throwable ignored) {
                    }
                }
            });
        }

        t.start();
    }

    public void DownloadAs(InputStream stream, String contentType, String name) throws IOException {
        if (stream == null)
            return;
        try (java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream()) {
            byte[] tmp = new byte[8192];
            int r;
            while ((r = stream.read(tmp)) != -1)
                buf.write(tmp, 0, r);
            DownloadAs(buf.toByteArray(), contentType, name);
        }
    }

    public void DownloadAs(byte[] content, String contentType, String name) {
        if (content == null)
            return;
        String ct = (contentType == null || contentType.isEmpty()) ? "application/octet-stream" : contentType;
        String filename = (name == null || name.isEmpty()) ? "download" : name;
        String base64 = java.util.Base64.getEncoder().encodeToString(content);
        String href = "data:" + ct + ";base64," + base64;
        String normalizedHref = ui.Normalize(href);
        String normalizedFilename = ui.Normalize(filename);
        String js = """
                (function(){var a=document.createElement('a');a.href='%s';a.download='%s';document.body.appendChild(a);a.click();\
                setTimeout(function(){try{document.body.removeChild(a);}catch(_){}},0);})();"""
                .formatted(normalizedHref, normalizedFilename);
        append.add(ui.Script(js));
    }

    private String patchScriptInline(String id, String swap, String html) {
        String safe = ui.Normalize(html);
        String normalizedId = ui.Normalize(id);
        String s = """
                (function(){var el=document.getElementById('%s');if(!el)return;var h="%s";switch('%s'){\
                case 'outline':el.outerHTML=h;break;\
                case 'append':el.insertAdjacentHTML('beforeend',h);break;\
                case 'prepend':el.insertAdjacentHTML('afterbegin',h);break;\
                default:el.innerHTML=h;}})();"""
                .formatted(normalizedId, safe, swap);
        return ui.Script(s);
    }

    public interface PatchSender {
        void send(String sessionId, String message) throws Exception;
    }

    private void displayMessage(String message, String color) {
        String escapedMessage = escapeJs(message != null ? message : "");
        String escapedColor = escapeJs(color != null ? color : "");
        String script = ui
                .Script("""
                        (function(){try{var box=document.getElementById('__messages__');if(!box){box=document.createElement('div');box.id='__messages__';\
                        box.style.position='fixed';box.style.top='0';box.style.right='0';box.style.padding='8px';box.style.zIndex='9999';box.style.pointerEvents='none';document.body.appendChild(box);}\
                        var n=document.createElement('div');n.style.display='flex';n.style.alignItems='center';n.style.gap='10px';n.style.padding='12px 16px';\
                        n.style.margin='8px';n.style.borderRadius='12px';n.style.minHeight='44px';n.style.minWidth='340px';n.style.maxWidth='340px';\
                        n.style.boxShadow='0 6px 18px rgba(0,0,0,0.08)';n.style.border='1px solid';var C=%s;\
                        var isGreen=C.indexOf('green')>=0;var isRed=C.indexOf('red')>=0;var accent=isGreen?'#16a34a':(isRed?'#dc2626':'#4f46e5');\
                        if(isGreen){n.style.background='#dcfce7';n.style.color='#166534';n.style.borderColor='#bbf7d0';}\
                        else if(isRed){n.style.background='#fee2e2';n.style.color='#991b1b';n.style.borderColor='#fecaca';}\
                        else{n.style.background='#eef2ff';n.style.color='#3730a3';n.style.borderColor='#e0e7ff';}\
                        n.style.borderLeft='4px solid '+accent;var dot=document.createElement('span');dot.style.width='10px';dot.style.height='10px';\
                        dot.style.borderRadius='9999px';dot.style.background=accent;var t=document.createElement('span');t.textContent=%s;\
                        n.appendChild(dot);n.appendChild(t);box.appendChild(n);setTimeout(function(){try{box.removeChild(n);}catch(_){}},5000);}catch(_){}})();"""
                        .formatted(escapedColor, escapedMessage));
        append.add(script);
    }

    private void displayError(String message) {
        String escapedMessage = escapeJs(message != null ? message : "");
        String script = ui
                .Script("""
                        (function(){try{var box=document.getElementById('__messages__');if(!box){box=document.createElement('div');box.id='__messages__';\
                        box.style.position='fixed';box.style.top='0';box.style.right='0';box.style.padding='8px';box.style.zIndex='9999';box.style.pointerEvents='none';document.body.appendChild(box);}\
                        var n=document.createElement('div');n.style.display='flex';n.style.alignItems='center';n.style.gap='10px';n.style.padding='12px 16px';\
                        n.style.margin='8px';n.style.borderRadius='12px';n.style.minHeight='44px';n.style.minWidth='340px';n.style.maxWidth='340px';\
                        n.style.background='#fee2e2';n.style.color='#991b1b';n.style.border='1px solid #fecaca';n.style.borderLeft='4px solid #dc2626';\
                        n.style.boxShadow='0 6px 18px rgba(0,0,0,0.08)';n.style.fontWeight='600';n.style.pointerEvents='auto';\
                        var dot=document.createElement('span');dot.style.width='10px';dot.style.height='10px';dot.style.borderRadius='9999px';dot.style.background='#dc2626';\
                        var t=document.createElement('span');t.textContent=%s;\
                        var btn=document.createElement('button');btn.textContent='Reload';btn.style.background='#991b1b';btn.style.color='#fff';\
                        btn.style.border='none';btn.style.padding='6px 10px';btn.style.borderRadius='8px';btn.style.cursor='pointer';btn.style.fontWeight='700';\
                        btn.onclick=function(){try{window.location.reload();}catch(_){}};n.appendChild(dot);n.appendChild(t);n.appendChild(btn);box.appendChild(n);\
                        setTimeout(function(){try{if(n&&n.parentNode){n.parentNode.removeChild(n);}}catch(_){}},88000);}catch(_){}})();"""
                        .formatted(escapedMessage));
        append.add(script);
    }

    private String escapeJs(String s) {
        if (s == null)
            return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
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
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("on")
                || normalized.equals("yes");
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
    public interface Callable {
        String handle(Context ctx) throws Exception;
    }

    public interface CallBuilder {
        String Render(ui.Attr target);

        String Replace(ui.Attr target);

        String Append(ui.Attr target);

        String Prepend(ui.Attr target);

        String None();
    }

    public interface SubmitBuilder {
        ui.Attr Render(ui.Attr target);

        ui.Attr Replace(ui.Attr target);

        ui.Attr Append(ui.Attr target);

        ui.Attr Prepend(ui.Attr target);

        ui.Attr None();
    }

    public static final class Action {
        public final Callable method;
        public final ui.Attr target;
        public final Object[] values;

        public Action(Callable method, ui.Attr target, Object... values) {
            this.method = method;
            this.target = target;
            this.values = values;
        }
    }
}
