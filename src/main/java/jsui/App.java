package jsui;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jsui.Context.Callable;

/**
 * Java skeleton of t-sui/ui.server.ts App with minimal utilities.
 *
 * This port focuses on composing HTML and generating client-side handlers
 * (as attributes). It does not embed an HTTP server or WebSockets.
 */
public final class App {
    public final Ui.Target contentId;
    public String Language;
    public final List<String> HTMLHead = new ArrayList<>();

    private final Map<String, Callable> routes = new HashMap<>();
    private final Map<Callable, String> reverse = new HashMap<>();

    private boolean debugEnabled = false;

    // Session store for per-target clear callbacks
    static final class sessRec {
        volatile long lastSeen;
        final java.util.concurrent.ConcurrentHashMap<String, Runnable> targets = new java.util.concurrent.ConcurrentHashMap<>();
        volatile long generation;
    }
    final java.util.concurrent.ConcurrentHashMap<String, sessRec> sessions = new java.util.concurrent.ConcurrentHashMap<>();

    // Static assets (served from classpath) configuration
    static final class AssetCfg {
        final String mountPath;     // e.g. "/assets"
        final String resourceRoot;  // e.g. "public/assets" in classpath
        final long maxAgeSeconds;
        AssetCfg(String mountPath, String resourceRoot, long maxAgeSeconds) {
            this.mountPath = mountPath;
            this.resourceRoot = resourceRoot;
            this.maxAgeSeconds = maxAgeSeconds;
        }
    }
    final List<AssetCfg> assets = new ArrayList<>();

    public App(String defaultLanguage) {
        this.contentId = Ui.Target();
        this.Language = defaultLanguage != null ? defaultLanguage : "en";
        // minimal defaults
        HTMLHead.add("<meta charset=\"UTF-8\">");
        HTMLHead.add("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        // Base styles and Tailwind (align with g-sui defaults)
        HTMLHead.add("<style>html{scroll-behavior:smooth}.invalid,select:invalid,textarea:invalid,input:invalid{border-bottom-width:2px;border-bottom-color:red;border-bottom-style:dashed}@media (max-width:768px){input[type=\\\"date\\\"]{max-width:100%!important;width:100%!important;min-width:0!important;box-sizing:border-box!important;overflow:hidden!important}input[type=\\\"date\\\"]::-webkit-datetime-edit{max-width:100%!important;overflow:hidden!important}}</style>");
        HTMLHead.add("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.2.19/tailwind.min.css\" integrity=\"sha512-wnea99uKIC3TJF7v4eKk4Y+lMz2Mklv18+r4na2Gn1abDRPPOeef95xTzdwGD9e6zXJBteMIhZ1+68QC5byJZw==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />");
        // Dark-mode overrides so Tailwind v2 classes look good in dark
        HTMLHead.add("<style id=\"jsui-dark-overrides\">html.dark{color-scheme:dark}.dark body{color:#e5e7eb}html.dark.bg-white,html.dark.bg-gray-100{background-color:#111827!important}.dark .bg-white,.dark .bg-gray-50,.dark .bg-gray-100{background-color:#111827!important}.dark .text-black,.dark .text-gray-900,.dark .text-gray-800,.dark .text-gray-700,.dark .text-gray-600,.dark .text-gray-500{color:#e5e7eb!important}.dark .text-gray-400,.dark .text-gray-300{color:#d1d5db!important}.dark .border-gray-100,.dark .border-gray-200,.dark .border-gray-300{border-color:#374151!important}.dark input,.dark select,.dark textarea{color:#e5e7eb!important;background-color:#1f2937!important}.dark input::placeholder,.dark textarea::placeholder{color:#9ca3af!important}.dark .hover\\:bg-gray-200:hover{background-color:#374151!important}</style>");
        // Minimal theme helper to support Ui.ThemeSwitcher
        HTMLHead.add("<script>(function(){function apply(mode){try{localStorage.setItem('theme',mode);}catch(e){}var root=document.documentElement;var eff=(mode==='system')?((window.matchMedia&&window.matchMedia('(prefers-color-scheme: dark)').matches)?'dark':'light'):mode;if(eff==='dark'){root.classList.add('dark');}else{root.classList.remove('dark');}};window.setTheme=apply;try{apply(localStorage.getItem('theme')||'system');}catch(_){apply('light');}})();</script>");
        // Minimal __post and __applySwap to support Context.Call/Submit
        HTMLHead.add("<script>(function(){\n" +
                "window.__applySwap=function(id,swap,html){try{var el=document.getElementById(id);if(!el)return;switch(swap){case 'outline':el.outerHTML=html;break;case 'append':el.insertAdjacentHTML('beforeend',html);break;case 'prepend':el.insertAdjacentHTML('afterbegin',html);break;default:el.innerHTML=html;}}catch(_){}};\n" +
                "window.__post=function(as,path,swap,id,e){try{var opts={method:'POST'};if(as==='FORM'){var f=e&&e.target? (e.target.closest&&e.target.closest('form'))||e.target : null; if(!f||f.tagName!=='FORM'){return false;} var fd=new FormData(f); var pairs=[]; fd.forEach(function(v,k){pairs.push(encodeURIComponent(k)+'='+encodeURIComponent(v));}); opts.headers={'content-type':'application/x-www-form-urlencoded;charset=UTF-8'}; opts.body=pairs.join('&');} return fetch(path,opts).then(function(r){return r.text()}).then(function(t){__applySwap(id,swap,t);}).catch(function(){}),false;}catch(_){return false;}};\n" +
                "window.__load=function(href){try{window.location.href=href;}catch(_){}};\n" +
                "})();</script>");
    }

    public void debug(boolean enable) {
        this.debugEnabled = enable;
    }

    private void log(String m) {
        if (debugEnabled)
            System.out.println("tsui - " + m);
    }

    void registerClear(String sessionId, String targetId, Runnable clear) {
        if (sessionId == null || sessionId.isEmpty() || targetId == null || targetId.isEmpty() || clear == null) return;
        sessRec rec = sessions.computeIfAbsent(sessionId, k -> new sessRec());
        rec.lastSeen = System.currentTimeMillis();
        rec.targets.put(targetId, clear);
    }

    void triggerClear(String sessionId, String targetId) {
        if (sessionId == null || sessionId.isEmpty() || targetId == null || targetId.isEmpty()) return;
        sessRec rec = sessions.get(sessionId);
        if (rec == null) return;
        Runnable fn = rec.targets.remove(targetId);
        if (fn != null) {
            try { fn.run(); } catch (Throwable ignore) { }
        }
    }

    /**
     * Clears and triggers all registered target cleanup callbacks for a session.
     * Use this when starting a new page load to terminate background workers
     * (Repeat/Delay/Defer) from the previous page.
     */
    public void ClearSessionTargets(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return;
        sessRec rec = sessions.get(sessionId);
        if (rec == null) return;
        // Snapshot keys to avoid concurrent modification while callbacks may remove entries
        java.util.List<String> keys = new java.util.ArrayList<>(rec.targets.keySet());
        for (String key : keys) {
            Runnable fn = rec.targets.remove(key);
            if (fn != null) {
                try { fn.run(); } catch (Throwable ignore) { }
            }
        }
    }

    /** Increments and returns the navigation generation for a session. */
    public long bumpSessionGeneration(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return 0L;
        sessRec rec = sessions.computeIfAbsent(sessionId, k -> new sessRec());
        rec.lastSeen = System.currentTimeMillis();
        rec.generation = rec.generation + 1;
        return rec.generation;
    }

    /** Returns the current navigation generation for a session. */
    public long currentSessionGeneration(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return 0L;
        sessRec rec = sessions.get(sessionId);
        return rec != null ? rec.generation : 0L;
    }

    public String HTMLBody(String cls) {
        if (cls == null || cls.isEmpty())
            cls = "bg-gray-200";
        String head = String.join(" ", HTMLHead);
        return String.join(" ",
                "<!DOCTYPE html>",
                "<html lang=\"" + Language + "\" class=\"" + cls + "\">",
                "  <head>" + head + "</head>",
                "  <body id=\"" + contentId.id + "\" class=\"relative\"></body>",
                "</html>");
    }

    public String HTML(String title, String bodyClass, String body) {
        String cls = Ui.Classes(bodyClass);
        if (cls == null || cls.isEmpty()) {
            cls = "bg-gray-200";
        }
        StringBuilder head = new StringBuilder();
        head.append("<title>");
        head.append(title != null ? Ui.Trim(title) : "");
        head.append("</title>");
        for (String item : HTMLHead) {
            if (item != null) {
                head.append(item);
            }
        }
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"").append(Language).append("\" class=\"").append(cls).append("\">");
        html.append("<head>").append(head).append("</head>");
        html.append("<body id=\"").append(contentId.id).append("\" class=\"relative\">");
        if (body != null) {
            html.append(body);
        }
        html.append("</body>");
        html.append("</html>");
        return Ui.Trim(html.toString());
    }

    // ---------------------------------------------------------------------
    // Assets & Favicon helpers (classpath-based)

    /**
     * Serve static files from the application classpath.
     * Example: AssetsFromClasspath("/assets", "public/assets", 86400)
     * will serve GET /assets/FILE by reading classpath resource "public/assets/FILE".
     */
    public void AssetsFromClasspath(String mountPath, String resourceRoot, long maxAgeSeconds) {
        if (mountPath == null || mountPath.isEmpty() || resourceRoot == null || resourceRoot.isEmpty()) return;
        String m = normalizePath(mountPath);
        // Do not allow "/" to be mounted as assets
        if ("/".equals(m)) return;
        assets.add(new AssetCfg(m, resourceRoot, Math.max(0, maxAgeSeconds)));
    }

    /** Adds a favicon link using a data: URL. */
    public void FaviconDataUrl(String dataUrl, long maxAgeSeconds) {
        if (dataUrl == null || dataUrl.isEmpty()) return;
        HTMLHead.add("<link rel=\"icon\" href=\"" + Ui.Trim(dataUrl) + "\" />");
    }

    /**
     * Adds a favicon served from classpath via mounted assets.
     * Example: FaviconFromClasspath("/assets", "public/favicon.ico", 86400)
     */
    public void FaviconFromClasspath(String mountPath, String classpathResource, long maxAgeSeconds) {
        if (mountPath == null || classpathResource == null) return;
        String m = normalizePath(mountPath);
        String pathOnly = m.endsWith("/") ? m.substring(0, m.length()-1) : m;
        // Ensure mount is registered (best effort)
        int slash = classpathResource.lastIndexOf('/') + 1;
        String root = slash > 0 ? classpathResource.substring(0, slash) : "";
        if (!root.isEmpty()) AssetsFromClasspath(m, root, maxAgeSeconds);
        String name = classpathResource.substring(slash);
        String href = pathOnly + "/" + name;
        HTMLHead.add("<link rel=\"icon\" href=\"" + Ui.Trim(href) + "\" />");
    }

    // Asset resolution (used by Server)
    static final class ResolvedAsset {
        final InputStream stream;
        final String contentType;
        final long maxAgeSeconds;
        ResolvedAsset(InputStream s, String ct, long age) { stream = s; contentType = ct; maxAgeSeconds = age; }
    }

    ResolvedAsset resolveAsset(String path) {
        if (path == null || path.isEmpty()) return null;
        for (AssetCfg cfg : assets) {
            if (!path.startsWith(cfg.mountPath + "/") && !path.equals(cfg.mountPath)) continue;
            String rel = path.substring(cfg.mountPath.length());
            if (rel.startsWith("/")) rel = rel.substring(1);
            if (rel.isEmpty()) return null;
            String resource = cfg.resourceRoot.endsWith("/") ? (cfg.resourceRoot + rel) : (cfg.resourceRoot + "/" + rel);
            InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
            if (in != null) {
                String ct = contentTypeOf(rel);
                return new ResolvedAsset(in, ct, cfg.maxAgeSeconds);
            }
        }
        return null;
    }

    private static String contentTypeOf(String filename) {
        String f = filename == null ? "" : filename.toLowerCase();
        if (f.endsWith(".css")) return "text/css; charset=utf-8";
        if (f.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (f.endsWith(".json")) return "application/json; charset=utf-8";
        if (f.endsWith(".svg")) return "image/svg+xml";
        if (f.endsWith(".png")) return "image/png";
        if (f.endsWith(".jpg") || f.endsWith(".jpeg")) return "image/jpeg";
        if (f.endsWith(".gif")) return "image/gif";
        if (f.endsWith(".webp")) return "image/webp";
        if (f.endsWith(".ico")) return "image/x-icon";
        if (f.endsWith(".woff2")) return "font/woff2";
        if (f.endsWith(".woff")) return "font/woff";
        if (f.endsWith(".ttf")) return "font/ttf";
        if (f.endsWith(".map")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    // Route/Callable registration
    public Callable Callable(Callable method) {
        if (method == null) {
            return null;
        }
        if (!reverse.containsKey(method)) {
            register("/call/" + UUID.randomUUID(), method);
        }
        return method;
    }

    public Callable Page(String path, Callable callable) {
        if (callable == null) {
            return null;
        }
        register(path, callable);
        return callable;
    }

    public Callable Action(String uid, Callable callable) {
        if (callable == null) {
            return null;
        }
        if (uid == null || uid.isEmpty()) {
            uid = UUID.randomUUID().toString();
        }
        register("/act/" + uid, callable);
        return callable;
    }

    public String pathOf(Callable c) {
        return reverse.get(c);
    }

    public Callable routeForPath(String path) {
        return routes.get(normalizePath(path));
    }

    public String invoke(String path, Context ctx) throws Exception {
        Callable callable = routeForPath(path);
        if (callable == null) {
            return null;
        }
        log("invoke " + path);
        return callable.handle(ctx);
    }

    private void register(String path, Callable callable) {
        if (path == null || path.isEmpty() || callable == null) {
            return;
        }
        String normalized = normalizePath(path);
        routes.put(normalized, callable);
        reverse.put(callable, normalized);
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String normalized = path.trim();
        if (normalized.isEmpty()) {
            return "/";
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
