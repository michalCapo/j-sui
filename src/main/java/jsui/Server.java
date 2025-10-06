package jsui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Minimal HTTP and WebSocket server built with Java SE networking primitives.
 *
 * Designed to serve the {@link App} HTML output and evaluate registered
 * {@link Context.Callable} actions without relying on third-party libraries.
 */
public final class Server implements AutoCloseable {

    private static final String SESSION_COOKIE = "jsui_session";

    private final App app;
    private final HttpService httpService;
    private final WebSocketService webSocketService;

    private Server(App app, HttpService httpService, WebSocketService webSocketService) {
        this.app = app;
        this.httpService = httpService;
        this.webSocketService = webSocketService;
    }

    /**
     * Creates a new server instance with the supplied builder.
     */
    public static Builder builder(App app) {
        return new Builder(app);
    }

    /** Starts the HTTP (and optional WebSocket) services. */
    public void start() {
        httpService.start();
        if (webSocketService != null)
            webSocketService.start();
    }

    /** Blocks the calling thread until the server is shut down. */
    public void join() throws InterruptedException {
        if (webSocketService != null) {
            webSocketService.join();
        }
        httpService.join();
    }

    /** Broadcasts a text frame to all connected WebSocket clients if enabled. */
    public void broadcast(String message) throws IOException {
        if (webSocketService != null) {
            webSocketService.broadcast(message);
        }
    }

    public App app() {
        return app;
    }

    public int httpPort() {
        return httpService.port();
    }

    public Integer websocketPort() {
        return webSocketService != null ? webSocketService.port() : null;
    }

    @Override
    public void close() throws IOException {
        IOException error = null;
        try {
            httpService.close();
        } catch (IOException ex) {
            error = ex;
        }
        if (webSocketService != null) {
            try {
                webSocketService.close();
            } catch (IOException ex) {
                if (error == null) {
                    error = ex;
                }
            }
        }
        if (error != null) {
            throw error;
        }
    }

    // ---------------------------------------------------------------------
    // Builder

    public static final class Builder {
        private final App app;
        private int httpPort = 8080;
        private int httpBacklog = 50;
        private String httpHost = "0.0.0.0";
        private boolean enableWebSocket = true;
        private WebSocketListener wsListener = WebSocketListener.noop();
        private Integer websocketPort = null; // if null, defaults to httpPort+1

        private Duration shutdownTimeout = Duration.ofSeconds(5);

        private Builder(App app) {
            this.app = Objects.requireNonNull(app, "app");
        }

        public Builder httpPort(int port) {
            this.httpPort = port;
            return this;
        }

        public Builder httpBacklog(int backlog) {
            this.httpBacklog = backlog;
            return this;
        }

        public Builder httpHost(String host) {
            this.httpHost = host;
            return this;
        }

        public Builder shutdownTimeout(Duration timeout) {
            this.shutdownTimeout = timeout;
            return this;
        }

        /** Enable or disable WebSocket support. */
        public Builder enableWebSocket(boolean enable) {
            this.enableWebSocket = enable;
            return this;
        }

        /** Set the port for the WebSocket server. Defaults to httpPort+1. */
        public Builder websocketPort(int port) {
            this.websocketPort = port;
            return this;
        }

        /** Provide a listener for WebSocket events. */
        public Builder websocketListener(WebSocketListener listener) {
            this.wsListener = listener != null ? listener : WebSocketListener.noop();
            return this;
        }

        /** Builds and starts the server. */
        public Server start() throws IOException {
            // Use a single HTTP port for both HTTP and WebSocket (upgrade handled in
            // HttpService)
            HttpService http = new HttpService(app, new InetSocketAddress(httpHost, httpPort), httpBacklog,
                    shutdownTimeout);
            if (enableWebSocket) {
                // Route Context.Patch messages through HttpService's session map
                http.setPatchSender((sessionId, message) -> http.sendToSession(sessionId, message));
                // Client boot script: connect WS to the same host:port as the page
                final String wsBoot = "(function(){try{" +
                        "var url=(location.protocol==='https:'?'wss://':'ws://')+location.host+'/';" +
                        "var bannerId='jsui_offline_banner';" +
                        "function show(){var el=document.getElementById(bannerId);if(!el){el=document.createElement('div');el.id=bannerId;el.className='fixed top-3 left-3 z-50';el.innerHTML='<div class\\\"px-4 py-2 rounded-full bg-red-500 text-white shadow-lg ring-1 ring-white/20 backdrop-blur flex items-center gap-3\\\"><span class\\\"font-bold\\\">Offline</span><span class\\\"opacity-90\\\">Trying to reconnect…</span></div>';document.body.appendChild(el);}else{el.style.display='';}}"
                        +
                        "function hide(){var el=document.getElementById(bannerId);if(el){el.style.display='none';}}" +
                        "function markSeen(id){try{window.__jsuiSeen=window.__jsuiSeen||{};window.__jsuiSeen[id]=true;}catch(_){}}"
                        +
                        "function wasSeen(id){try{return !!(window.__jsuiSeen&&window.__jsuiSeen[id]);}catch(_){return false;}}"
                        +
                        "function handlePatch(msg){try{var id=String(msg.id||'');var el=document.getElementById(id);if(!el){if(wasSeen(id)){try{ws&&ws.readyState===1&&ws.send(JSON.stringify({type:'invalid',id:id}));}catch(_){}}return;}markSeen(id);var html=String(msg.html||'');try{var tpl=document.createElement('template');tpl.innerHTML=html;var scripts=tpl.content.querySelectorAll('script');for(var i=0;i<scripts.length;i++){var s=document.createElement('script');s.textContent=scripts[i].textContent;document.body.appendChild(s);} }catch(_){ } if(msg.swap==='outline'){el.outerHTML=html;}else if(msg.swap==='append'){el.insertAdjacentHTML('beforeend',html);}else if(msg.swap==='prepend'){el.insertAdjacentHTML('afterbegin',html);}else{el.innerHTML=html;}}catch(_){}}"
                        +
                        "function connect(d){setTimeout(function(){ws=new WebSocket(url);ws.onopen=function(){hide();try{ws.send(JSON.stringify({type:'ping'}));}catch(_){}};ws.onmessage=function(ev){try{var m=JSON.parse(ev.data);if(m.type==='patch'){handlePatch(m);}else if(m.type==='ping'){try{ws.send(JSON.stringify({type:'pong'}));}catch(_){}}}catch(_){}};ws.onerror=function(){try{ws.close();}catch(_){}};ws.onclose=function(){show();connect(Math.min((d||250)*2,5000));};},d||0);} var ws; if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',function(){connect(0);});}else{connect(0);}"
                        +
                        "}catch(_){}})();";
                app.HTMLHead.add("<script>" + wsBoot + "</script>");
            }
            // Construct Server with no separate WS service
            Server server = new Server(app, http, null);
            server.start();
            return server;
        }
    }

    // ---------------------------------------------------------------------
    // HTTP implementation

    private static final class HttpService implements Closeable {
        private final App app;
        private final InetSocketAddress address;
        private final int backlog;
        private final Duration shutdownTimeout;
        private final ExecutorService workers = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "jsui-http-worker");
            t.setDaemon(true);
            return t;
        });
        private final Thread acceptThread;
        private volatile boolean running;
        private ServerSocket serverSocket;
        private Context.PatchSender patchSender;
        private final Set<WebSocketConnection> wsConnections = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final Map<String, Set<WebSocketConnection>> wsBySession = new ConcurrentHashMap<>();

        void setPatchSender(Context.PatchSender sender) {
            this.patchSender = sender;
        }

        void sendToSession(String sessionId, String message) throws IOException {
            if (sessionId == null || sessionId.isEmpty())
                return;
            Set<WebSocketConnection> set = wsBySession.get(sessionId);
            if (set == null)
                return;
            for (WebSocketConnection c : set) {
                if (c.isOpen()) {
                    c.sendText(message);
                }
            }
        }

        HttpService(App app, InetSocketAddress address, int backlog, Duration shutdownTimeout) {
            this.app = app;
            this.address = address;
            this.backlog = backlog;
            this.shutdownTimeout = shutdownTimeout != null ? shutdownTimeout : Duration.ofSeconds(5);
            this.acceptThread = new Thread(this::acceptLoop, "jsui-http-accept");
            this.acceptThread.setDaemon(true);
        }

        int port() {
            return serverSocket != null ? serverSocket.getLocalPort() : address.getPort();
        }

        void start() {
            if (running) {
                return;
            }
            running = true;
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(address, backlog);
            } catch (IOException ex) {
                running = false;
                throw new RuntimeException("Failed to bind HTTP server", ex);
            }
            acceptThread.start();
        }

        void join() throws InterruptedException {
            acceptThread.join();
        }

        private void acceptLoop() {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    workers.submit(() -> handle(socket));
                } catch (SocketException ex) {
                    if (running) {
                        ex.printStackTrace();
                    }
                } catch (IOException ex) {
                    if (running) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        private void handle(Socket socket) {
            try (Socket autoClose = socket) {
                socket.setTcpNoDelay(true);
                InputStream rawIn = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(rawIn, StandardCharsets.UTF_8));
                OutputStream rawOut = socket.getOutputStream();
                String requestLine = reader.readLine();
                if (requestLine == null || requestLine.isEmpty()) {
                    sendPlain(rawOut, 400, "Bad Request", "missing request line", null, false);
                    return;
                }
                String[] parts = requestLine.split(" ");
                if (parts.length < 2) {
                    sendPlain(rawOut, 400, "Bad Request", "invalid request line", null, false);
                    return;
                }
                String method = parts[0].toUpperCase(Locale.ROOT);
                String path = parts[1];
                String queryString = "";
                int queryIndex = path.indexOf('?');
                if (queryIndex >= 0) {
                    queryString = path.substring(queryIndex + 1);
                    path = path.substring(0, queryIndex);
                }
                Map<String, String> headers = readHeaders(reader);

                // Same-port WebSocket upgrade
                String upgrade = headers.getOrDefault("upgrade", "");
                if ("websocket".equalsIgnoreCase(upgrade)) {
                    handleWebSocket(socket, headers, rawIn);
                    return;
                }

                byte[] body = readBody(rawIn, headers, method);
                Session session = resolveSession(headers);
                Map<String, String> query = parseQuery(queryString);
                if (!"GET".equals(method) && !"POST".equals(method)) {
                    sendPlain(rawOut, 405, "Method Not Allowed", method + " unsupported", session, session.newSession);
                    return;
                }

                if ("GET".equals(method)) {
                    // Serve classpath assets if configured and path matches a mount
                    try {
                        App.ResolvedAsset asset = app.resolveAsset(path);
                        if (asset != null) {
                            sendAsset(rawOut, asset, session, session.newSession);
                            return;
                        }
                    } catch (Exception ignore) {
                    }
                    // Clear background workers from previous navigations before starting a new page
                    try {
                        app.ClearSessionTargets(session.sessionId);
                    } catch (Throwable ignore) {
                    }
                    // Bump navigation generation so old Repeat threads can self-terminate
                    try {
                        app.bumpSessionGeneration(session.sessionId);
                    } catch (Throwable ignore) {
                    }
                    Context ctx = new Context(app, session.sessionId, method, path, headers, body, query, queryString,
                            patchSender);
                    try {
                        String result = app.invoke(path, ctx);
                        if (result == null) {
                            sendPlain(rawOut, 404, "Not Found", "route not found", session, session.newSession);
                            return;
                        }
                        String payload = result;
                        if (!ctx.append.isEmpty()) {
                            StringBuilder sb = new StringBuilder(payload);
                            for (String extra : ctx.append) {
                                if (extra != null && !extra.isEmpty()) {
                                    sb.append(extra);
                                }
                            }
                            payload = sb.toString();
                        }
                        respondHtml(rawOut, payload, session, session.newSession);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        sendPlain(rawOut, 500, "Internal Server Error", ex.getMessage(), session, session.newSession);
                    }
                    return;
                }

                if (path.startsWith("/act/") || path.startsWith("/call/")) {
                    Context ctx = new Context(app, session.sessionId, method, path, headers, body, query, queryString,
                            patchSender);
                    try {
                        String result = app.invoke(path, ctx);
                        if (result == null) {
                            sendPlain(rawOut, 404, "Not Found", "route not found", session, session.newSession);
                            return;
                        }
                        String payload = result;
                        if (!ctx.append.isEmpty()) {
                            StringBuilder sb = new StringBuilder(payload);
                            for (String extra : ctx.append) {
                                if (extra != null && !extra.isEmpty()) {
                                    sb.append(extra);
                                }
                            }
                            payload = sb.toString();
                        }
                        respondHtml(rawOut, payload, session, session.newSession);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        sendPlain(rawOut, 500, "Internal Server Error", ex.getMessage(), session, session.newSession);
                    }
                    return;
                }

                sendPlain(rawOut, 404, "Not Found", "path not found", session, session.newSession);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void handleWebSocket(Socket socket, Map<String, String> headers, InputStream in) throws IOException {
            OutputStream out = socket.getOutputStream();
            String key = headers.get("sec-websocket-key");
            if (key == null || key.isEmpty()) {
                return;
            }
            String cookieHeader = headers.getOrDefault("cookie", "");
            String sessionId = null;
            if (!cookieHeader.isEmpty()) {
                String[] cookies = cookieHeader.split(";\\s*");
                for (String cookie : cookies) {
                    int idx = cookie.indexOf('=');
                    if (idx > 0) {
                        String name = cookie.substring(0, idx).trim();
                        String value = cookie.substring(idx + 1).trim();
                        if (SESSION_COOKIE.equals(name)) {
                            sessionId = value;
                            break;
                        }
                    }
                }
            }
            String acceptKey = wsHandshakeResponse(key);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write("HTTP/1.1 101 Switching Protocols\r\n");
            writer.write("Upgrade: websocket\r\n");
            writer.write("Connection: Upgrade\r\n");
            writer.write("Sec-WebSocket-Accept: ");
            writer.write(acceptKey);
            writer.write("\r\n\r\n");
            writer.flush();

            WebSocketConnection connection = new WebSocketConnection(socket);
            wsConnections.add(connection);
            if (sessionId != null && !sessionId.isEmpty()) {
                wsBySession.computeIfAbsent(sessionId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                        .add(connection);
            }
            readWsFrames(connection, in, sessionId);
        }

        private void readWsFrames(WebSocketConnection connection, InputStream in, String sessionId) {
            try {
                while (true) {
                    int b1 = in.read();
                    if (b1 == -1)
                        break;
                    int b2 = in.read();
                    if (b2 == -1)
                        break;
                    int opcode = b1 & 0x0F;
                    boolean masked = (b2 & 0x80) != 0;
                    long payloadLength = b2 & 0x7F;
                    if (payloadLength == 126) {
                        payloadLength = wsReadExtendedLength(in, 2);
                    } else if (payloadLength == 127) {
                        payloadLength = wsReadExtendedLength(in, 8);
                    }
                    byte[] maskKey = new byte[4];
                    if (masked) {
                        wsReadFully(in, maskKey);
                    }
                    if (payloadLength > Integer.MAX_VALUE) {
                        throw new IOException("Frame too large");
                    }
                    byte[] payload = new byte[(int) payloadLength];
                    wsReadFully(in, payload);
                    if (masked) {
                        for (int i = 0; i < payload.length; i++) {
                            payload[i] = (byte) (payload[i] ^ maskKey[i % 4]);
                        }
                    }
                    if (opcode == 0x8) { // close
                        connection.close();
                        break;
                    }
                    if (opcode == 0x9) { // ping -> pong
                        connection.sendControl(0xA, payload);
                        continue;
                    }
                    if (opcode == 0x1) { // text
                        String text = new String(payload, StandardCharsets.UTF_8);
                        try {
                            String msg = text != null ? text : "";
                            if (msg.contains("\"type\":\"ping\"")) {
                                try {
                                    connection.sendText("{\"type\":\"pong\"}");
                                } catch (IOException ignored) {
                                }
                                continue;
                            }
                            if (msg.contains("\"type\":\"invalid\"")) {
                                String id = "";
                                int i = msg.indexOf("\"id\":\"");
                                if (i >= 0) {
                                    int start = i + 6;
                                    int end = msg.indexOf('"', start);
                                    if (end > start)
                                        id = msg.substring(start, end);
                                }
                                if (sessionId != null && !sessionId.isEmpty() && id != null && !id.isEmpty()) {
                                    app.triggerClear(sessionId, id);
                                }
                                continue;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (IOException ex) {
                // ignore
            } finally {
                wsConnections.remove(connection);
                for (Set<WebSocketConnection> set : wsBySession.values()) {
                    set.remove(connection);
                }
                try {
                    connection.close();
                } catch (IOException ignore) {
                }
            }
        }

        private long wsReadExtendedLength(InputStream in, int bytes) throws IOException {
            byte[] data = new byte[bytes];
            wsReadFully(in, data);
            long length = 0;
            for (int i = 0; i < bytes; i++) {
                length = (length << 8) | (data[i] & 0xFF);
            }
            return length;
        }

        private void wsReadFully(InputStream in, byte[] buffer) throws IOException {
            int offset = 0;
            int remaining = buffer.length;
            while (remaining > 0) {
                int read = in.read(buffer, offset, remaining);
                if (read == -1) {
                    throw new IOException("Unexpected end of stream");
                }
                offset += read;
                remaining -= read;
            }
        }

        private String wsHandshakeResponse(String key) throws IOException {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                String acceptSeed = key.trim() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                byte[] hash = digest.digest(acceptSeed.getBytes(StandardCharsets.ISO_8859_1));
                return Base64.getEncoder().encodeToString(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new IOException("SHA-1 not available", ex);
            }
        }

        private Map<String, String> readHeaders(BufferedReader reader) throws IOException {
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                int idx = line.indexOf(':');
                if (idx > 0) {
                    String name = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                    String value = line.substring(idx + 1).trim();
                    headers.put(name, value);
                }
            }
            return headers;
        }

        private byte[] readBody(InputStream in, Map<String, String> headers, String method) throws IOException {
            if (!"POST".equals(method)) {
                return new byte[0];
            }
            String lenHeader = headers.get("content-length");
            if (lenHeader == null) {
                return new byte[0];
            }
            int len;
            try {
                len = Integer.parseInt(lenHeader);
            } catch (NumberFormatException ex) {
                return new byte[0];
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(len);
            byte[] chunk = new byte[1024];
            int remaining = len;
            while (remaining > 0) {
                int read = in.read(chunk, 0, Math.min(chunk.length, remaining));
                if (read == -1) {
                    break;
                }
                buffer.write(chunk, 0, read);
                remaining -= read;
            }
            return buffer.toByteArray();
        }

        private Map<String, String> parseQuery(String queryString) {
            if (queryString == null || queryString.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, String> params = new HashMap<>();
            String[] parts = queryString.split("&");
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                int idx = part.indexOf('=');
                String key = idx >= 0 ? part.substring(0, idx) : part;
                String value = idx >= 0 ? part.substring(idx + 1) : "";
                params.put(urlDecode(key), urlDecode(value));
            }
            return params;
        }

        private String urlDecode(String value) {
            try {
                return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (Exception ex) {
                return value;
            }
        }

        private Session resolveSession(Map<String, String> headers) {
            String cookieHeader = headers.getOrDefault("cookie", "");
            if (!cookieHeader.isEmpty()) {
                String[] cookies = cookieHeader.split(";\\s*");
                for (String cookie : cookies) {
                    int idx = cookie.indexOf('=');
                    if (idx <= 0) {
                        continue;
                    }
                    String name = cookie.substring(0, idx).trim();
                    String value = cookie.substring(idx + 1).trim();
                    if (SESSION_COOKIE.equals(name)) {
                        return new Session(value, false);
                    }
                }
            }
            return new Session(UUID.randomUUID().toString(), true);
        }

        private void respondHtml(OutputStream out, String value, Session session, boolean setCookie)
                throws IOException {
            byte[] body = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
            sendResponse(out, 200, "OK", "text/html; charset=UTF-8", body, session, setCookie);
        }

        private void sendPlain(OutputStream out, int status, String statusText, String message, Session session,
                boolean setCookie) throws IOException {
            String payload = message != null ? message : "";
            sendResponse(out, status, statusText, "text/plain; charset=UTF-8", payload.getBytes(StandardCharsets.UTF_8),
                    session, setCookie);
        }

        private void sendResponse(OutputStream out, int status, String statusText, String contentType, byte[] body,
                Session session, boolean setCookie) throws IOException {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write("HTTP/1.1 ");
            writer.write(Integer.toString(status));
            writer.write(' ');
            writer.write(statusText);
            writer.write("\r\n");
            writer.write("Content-Length: ");
            writer.write(Integer.toString(body.length));
            writer.write("\r\n");
            writer.write("Content-Type: ");
            writer.write(contentType);
            writer.write("\r\n");
            writer.write("Connection: close\r\n");
            if (setCookie && session != null) {
                writer.write("Set-Cookie: ");
                writer.write(SESSION_COOKIE);
                writer.write('=');
                writer.write(session.sessionId);
                writer.write("; Path=/; HttpOnly\r\n");
            }
            writer.write("\r\n");
            writer.flush();
            out.write(body);
            out.flush();
        }

        private void sendAsset(OutputStream out, App.ResolvedAsset asset, Session session, boolean setCookie)
                throws IOException {
            byte[] body;
            try (InputStream in = asset.stream; ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                byte[] tmp = new byte[8192];
                int read;
                while ((read = in.read(tmp)) != -1)
                    buf.write(tmp, 0, read);
                body = buf.toByteArray();
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Length: ");
            writer.write(Integer.toString(body.length));
            writer.write("\r\n");
            writer.write("Content-Type: ");
            writer.write(asset.contentType != null ? asset.contentType : "application/octet-stream");
            writer.write("\r\n");
            if (asset.maxAgeSeconds > 0) {
                writer.write("Cache-Control: public, max-age=" + asset.maxAgeSeconds + "\r\n");
            }
            writer.write("Connection: close\r\n");
            if (setCookie && session != null) {
                writer.write("Set-Cookie: ");
                writer.write(SESSION_COOKIE);
                writer.write('=');
                writer.write(session.sessionId);
                writer.write("; Path=/; HttpOnly\r\n");
            }
            writer.write("\r\n");
            writer.flush();
            out.write(body);
            out.flush();
        }

        @Override
        public void close() throws IOException {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
            workers.shutdownNow();
            try {
                workers.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        private static final class Session {
            final String sessionId;
            final boolean newSession;

            Session(String sessionId, boolean newSession) {
                this.sessionId = sessionId;
                this.newSession = newSession;
            }
        }
    }

    // ---------------------------------------------------------------------
    // WebSocket implementation

    public interface WebSocketListener {
        default void onOpen(WebSocketConnection connection) {
        }

        default void onMessage(WebSocketConnection connection, String message) {
        }

        default void onClose(WebSocketConnection connection) {
        }

        default void onError(WebSocketConnection connection, Throwable error) {
            error.printStackTrace();
        }

        static WebSocketListener noop() {
            return new WebSocketListener() {
            };
        }
    }

    public static final class WebSocketConnection implements Closeable {
        private final Socket socket;
        private final OutputStream out;
        private volatile boolean open = true;

        WebSocketConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.out = socket.getOutputStream();
        }

        public synchronized void sendText(String message) throws IOException {
            if (!open) {
                throw new IOException("connection closed");
            }
            byte[] payload = message != null ? message.getBytes(StandardCharsets.UTF_8) : new byte[0];
            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            frame.write(0x81); // FIN + text frame
            int length = payload.length;
            if (length <= 125) {
                frame.write(length);
            } else if (length <= 65535) {
                frame.write(126);
                frame.write((length >>> 8) & 0xFF);
                frame.write(length & 0xFF);
            } else {
                frame.write(127);
                long len = length;
                for (int i = 7; i >= 0; i--) {
                    frame.write((int) (len >>> (8 * i)) & 0xFF);
                }
            }
            frame.write(payload);
            out.write(frame.toByteArray());
            out.flush();
        }

        synchronized void sendControl(int opcode, byte[] payload) throws IOException {
            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            frame.write(0x80 | (opcode & 0x0F));
            int length = payload != null ? payload.length : 0;
            if (length <= 125) {
                frame.write(length);
            } else if (length <= 65535) {
                frame.write(126);
                frame.write((length >>> 8) & 0xFF);
                frame.write(length & 0xFF);
            } else {
                frame.write(127);
                long len = length;
                for (int i = 7; i >= 0; i--) {
                    frame.write((int) (len >>> (8 * i)) & 0xFF);
                }
            }
            if (length > 0) {
                frame.write(payload);
            }
            out.write(frame.toByteArray());
            out.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            if (!open) {
                return;
            }
            try {
                sendControl(0x8, new byte[0]);
            } finally {
                open = false;
                socket.close();
            }
        }

        boolean isOpen() {
            return open && !socket.isClosed();
        }
    }

    private static final class WebSocketService implements Closeable {
        private final InetSocketAddress address;
        private final WebSocketListener listener;
        private final Duration shutdownTimeout;
        private final ExecutorService workers = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "jsui-ws-worker");
            t.setDaemon(true);
            return t;
        });
        private final Thread acceptThread;
        private final Set<WebSocketConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final Map<String, Set<WebSocketConnection>> bySession = new ConcurrentHashMap<>();
        private volatile boolean running;
        private ServerSocket serverSocket;

        WebSocketService(InetSocketAddress address, WebSocketListener listener, Duration shutdownTimeout) {
            this.address = address;
            this.listener = listener != null ? listener : WebSocketListener.noop();
            this.shutdownTimeout = shutdownTimeout != null ? shutdownTimeout : Duration.ofSeconds(5);
            this.acceptThread = new Thread(this::acceptLoop, "jsui-ws-accept");
            this.acceptThread.setDaemon(true);
        }

        int port() {
            return serverSocket != null ? serverSocket.getLocalPort() : address.getPort();
        }

        void start() {
            if (running) {
                return;
            }
            running = true;
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(address);
            } catch (IOException ex) {
                running = false;
                throw new RuntimeException("Failed to bind WebSocket server", ex);
            }
            acceptThread.start();
        }

        void join() throws InterruptedException {
            acceptThread.join();
        }

        void broadcast(String message) throws IOException {
            for (WebSocketConnection connection : connections) {
                if (connection.isOpen()) {
                    connection.sendText(message);
                }
            }
        }

        void sendToSession(String sessionId, String message) throws IOException {
            if (sessionId == null || sessionId.isEmpty())
                return;
            Set<WebSocketConnection> set = bySession.get(sessionId);
            if (set == null)
                return;
            for (WebSocketConnection c : set) {
                if (c.isOpen()) {
                    c.sendText(message);
                }
            }
        }

        String findSessionId(WebSocketConnection connection) {
            for (Map.Entry<String, Set<WebSocketConnection>> e : bySession.entrySet()) {
                if (e.getValue().contains(connection)) {
                    return e.getKey();
                }
            }
            return null;
        }

        private void acceptLoop() {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    workers.submit(() -> handle(socket));
                } catch (SocketException ex) {
                    if (running) {
                        ex.printStackTrace();
                    }
                } catch (IOException ex) {
                    if (running) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        private void handle(Socket socket) {
            try (Socket autoClose = socket) {
                socket.setTcpNoDelay(true);
                InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                OutputStream out = socket.getOutputStream();

                String requestLine = reader.readLine();
                if (requestLine == null || requestLine.isEmpty()) {
                    return;
                }
                Map<String, String> headers = new HashMap<>();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    int idx = line.indexOf(':');
                    if (idx > 0) {
                        String name = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                        String value = line.substring(idx + 1).trim();
                        headers.put(name, value);
                    }
                }

                if (!"websocket".equalsIgnoreCase(headers.getOrDefault("upgrade", ""))) {
                    return;
                }
                String key = headers.get("sec-websocket-key");
                if (key == null || key.isEmpty()) {
                    return;
                }
                // try extract session id from Cookie header
                String cookieHeader = headers.getOrDefault("cookie", "");
                String sessionId = null;
                if (!cookieHeader.isEmpty()) {
                    String[] cookies = cookieHeader.split(";\\s*");
                    for (String cookie : cookies) {
                        int idx = cookie.indexOf('=');
                        if (idx > 0) {
                            String name = cookie.substring(0, idx).trim();
                            String value = cookie.substring(idx + 1).trim();
                            if (SESSION_COOKIE.equals(name)) {
                                sessionId = value;
                                break;
                            }
                        }
                    }
                }
                String acceptKey = handshakeResponse(key);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write("HTTP/1.1 101 Switching Protocols\r\n");
                writer.write("Upgrade: websocket\r\n");
                writer.write("Connection: Upgrade\r\n");
                writer.write("Sec-WebSocket-Accept: ");
                writer.write(acceptKey);
                writer.write("\r\n\r\n");
                writer.flush();

                WebSocketConnection connection = new WebSocketConnection(socket);
                connections.add(connection);
                if (sessionId != null && !sessionId.isEmpty()) {
                    bySession.computeIfAbsent(sessionId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                            .add(connection);
                }
                listener.onOpen(connection);
                readFrames(connection, in);
            } catch (IOException ex) {
                listener.onError(null, ex);
            }
        }

        private void readFrames(WebSocketConnection connection, InputStream in) {
            try {
                while (true) {
                    int b1 = in.read();
                    if (b1 == -1) {
                        break;
                    }
                    int b2 = in.read();
                    if (b2 == -1) {
                        break;
                    }
                    int opcode = b1 & 0x0F;
                    boolean masked = (b2 & 0x80) != 0;
                    long payloadLength = b2 & 0x7F;
                    if (payloadLength == 126) {
                        payloadLength = readExtendedLength(in, 2);
                    } else if (payloadLength == 127) {
                        payloadLength = readExtendedLength(in, 8);
                    }
                    byte[] maskKey = new byte[4];
                    if (masked) {
                        readFully(in, maskKey);
                    }
                    if (payloadLength > Integer.MAX_VALUE) {
                        throw new IOException("Frame too large");
                    }
                    byte[] payload = new byte[(int) payloadLength];
                    readFully(in, payload);
                    if (masked) {
                        for (int i = 0; i < payload.length; i++) {
                            payload[i] = (byte) (payload[i] ^ maskKey[i % 4]);
                        }
                    }
                    if (opcode == 0x8) { // close
                        connection.close();
                        break;
                    }
                    if (opcode == 0x9) { // ping -> pong
                        connection.sendControl(0xA, payload);
                        continue;
                    }
                    if (opcode == 0x1) { // text
                        String text = new String(payload, StandardCharsets.UTF_8);
                        listener.onMessage(connection, text);
                    }
                }
            } catch (IOException ex) {
                listener.onError(connection, ex);
            } finally {
                connections.remove(connection);
                for (Set<WebSocketConnection> set : bySession.values()) {
                    set.remove(connection);
                }
                try {
                    connection.close();
                } catch (IOException ignore) {
                    // ignore
                }
                listener.onClose(connection);
            }
        }

        private long readExtendedLength(InputStream in, int bytes) throws IOException {
            byte[] data = new byte[bytes];
            readFully(in, data);
            long length = 0;
            for (int i = 0; i < bytes; i++) {
                length = (length << 8) | (data[i] & 0xFF);
            }
            return length;
        }

        @Override
        public void close() throws IOException {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
            for (WebSocketConnection connection : connections) {
                if (connection.isOpen()) {
                    connection.close();
                }
            }
            workers.shutdownNow();
            try {
                workers.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        private static String handshakeResponse(String key) throws IOException {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                String acceptSeed = key.trim() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                byte[] hash = digest.digest(acceptSeed.getBytes(StandardCharsets.ISO_8859_1));
                return Base64.getEncoder().encodeToString(hash);
            } catch (NoSuchAlgorithmException ex) {
                throw new IOException("SHA-1 not available", ex);
            }
        }

        private static void readFully(InputStream in, byte[] buffer) throws IOException {
            int offset = 0;
            int remaining = buffer.length;
            while (remaining > 0) {
                int read = in.read(buffer, offset, remaining);
                if (read == -1) {
                    throw new IOException("Unexpected end of stream");
                }
                offset += read;
                remaining -= read;
            }
        }
    }
}
