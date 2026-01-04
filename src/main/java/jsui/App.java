package jsui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
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

    private final java.util.concurrent.ConcurrentHashMap<String, Callable> routes = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Callable, String> reverse = new java.util.concurrent.ConcurrentHashMap<>();

    private boolean debugEnabled = false;
    private boolean smoothNav = false;

    static final class sessRec {
        volatile long lastSeen;
        final java.util.concurrent.ConcurrentHashMap<String, Runnable> targets = new java.util.concurrent.ConcurrentHashMap<>();
        volatile long generation;
    }

    final java.util.concurrent.ConcurrentHashMap<String, sessRec> sessions = new java.util.concurrent.ConcurrentHashMap<>();

    static final class AssetCfg {
        final String mountPath;
        final String resourceRoot;
        final long maxAgeSeconds;

        AssetCfg(String mountPath, String resourceRoot, long maxAgeSeconds) {
            this.mountPath = mountPath;
            this.resourceRoot = resourceRoot;
            this.maxAgeSeconds = maxAgeSeconds;
        }
    }

    final List<AssetCfg> assets = new ArrayList<>();

    public enum TailwindMode {
        CDN, // Default: load from jsdelivr CDN
        NONE, // No Tailwind (custom CSS only)
        SELF_HOSTED // Load from /assets/tailwind.js
    }

    private TailwindMode tailwindMode = TailwindMode.CDN;

    private long sessionTtlMillis = 30 * 60 * 1000L;
    private long cleanupIntervalMillis = 5 * 60 * 1000L;
    private volatile boolean cleanupEnabled = true;
    private Thread cleanupThread;

    public App(String defaultLanguage) {
        this.contentId = Ui.Target();
        this.Language = defaultLanguage != null ? defaultLanguage : "en";
        HTMLHead.add("<meta charset=\"UTF-8\">");
        HTMLHead.add("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        HTMLHead.add("""
                <style>html{scroll-behavior:smooth}.invalid,select:invalid,textarea:invalid,input:invalid{border-bottom-width:2px;border-bottom-color:red;border-bottom-style:dashed}\
                @media (max-width:768px){input[type=\"date\"]{max-width:100%!important;width:100%!important;min-width:0!important;box-sizing:border-box!important;overflow:hidden!important}\
                input[type=\"date\"]::-webkit-datetime-edit{max-width:100%!important;overflow:hidden!important}}</style>""");

        addTailwindScript();

        HTMLHead.add("""
                <style id="jsui-dark-overrides">html.dark{color-scheme:dark}.dark body{color:#e5e7eb}html.dark.bg-white,html.dark.bg-gray-100{background-color:#111827!important}\
                .dark .bg-white,.dark .bg-gray-50,.dark .bg-gray-100{background-color:#111827!important}.dark .text-black,.dark .text-gray-900,.dark .text-gray-800,.dark .text-gray-700,\
                .dark .text-gray-600,.dark .text-gray-500{color:#e5e7eb!important}.dark .text-gray-400,.dark .text-gray-300{color:#d1d5db!important}.dark .border-gray-100,.dark .border-gray-200,.dark .border-gray-300{border-color:#374151!important}\
                .dark input,.dark select,.dark textarea{color:#e5e7eb!important;background-color:#1f2937!important}.dark input::placeholder,.dark textarea::placeholder{color:#9ca3af!important}\
                .dark .hover\\:bg-gray-200:hover{background-color:#374151!important}</style>""");

        String coreJs = loadResource("jsui/jsui-core.js");
        if (coreJs != null) {
            HTMLHead.add("""
                    <script>%s</script>
                    """.formatted(coreJs));
        } else {
            HTMLHead.add("""
                    <script>(function(){function apply(mode){try{localStorage.setItem('theme',mode);}catch(e){}var root=document.documentElement;\
                    var eff=(mode==='system')?((window.matchMedia&&window.matchMedia('(prefers-color-scheme: dark)').matches)?'dark':'light'):mode;\
                    if(eff==='dark'){root.classList.add('dark');}else{root.classList.remove('dark');}};window.setTheme=apply;\
                    try{apply(localStorage.getItem('theme')||'system');}catch(_){apply('light');}})();</script>""");
            HTMLHead.add(
                    """
                            <script>(function(){
                                window.__applySwap=function(id,swap,html){
                                    try{
                                        var el=document.getElementById(id);
                                        if(!el)return;
                                        switch(swap){
                                            case 'outline':el.outerHTML=html;break;
                                            case 'append':el.insertAdjacentHTML('beforeend',html);break;
                                            case 'prepend':el.insertAdjacentHTML('afterbegin',html);break;
                                            default:el.innerHTML=html;
                                        }
                                    }catch(_){}
                                };
                                window.__post=function(as,path,swap,id,e){
                                    try{
                                        var opts={method:'POST'};
                                        if(as==='FORM'){
                                            var f=null;
                                            if(e&&e.target){
                                                if(e.target.tagName==='FORM'){f=e.target;}
                                                else if(e.target.closest&&e.target.closest('form')){f=e.target.closest('form');}
                                                else if(e.submitter&&e.submitter.form){f=e.submitter.form;}
                                            }
                                            if(!f||f.tagName!=='FORM'){return false;}
                                            var fd=new FormData(f);
                                            var pairs=[];
                                            fd.forEach(function(v,k){
                                                pairs.push(encodeURIComponent(k)+'='+encodeURIComponent(v));
                                            });
                                            opts.headers={'content-type':'application/x-www-form-urlencoded;charset=UTF-8'};
                                            opts.body=pairs.join('&');
                                        }
                                        fetch(path,opts).then(function(r){
                                            return r.text();
                                        }).then(function(t){
                                            __applySwap(id,swap,t);
                                        }).catch(function(){});
                                        return false;
                                    }catch(_){return false;}
                                };
                                var __loader=(function(){
                                    var S={count:0,t:0,el:null};
                                    function build(){
                                        var overlay=document.createElement('div');
                                        overlay.className='fixed inset-0 z-50 flex items-center justify-center transition-opacity opacity-0';
                                        try{overlay.style.backdropFilter='blur(3px)';}catch(_){}
                                        try{overlay.style.webkitBackdropFilter='blur(3px)';}catch(_){}
                                        try{overlay.style.background='rgba(255,255,255,0.28)';}catch(_){}
                                        try{overlay.style.pointerEvents='auto';}catch(_){}
                                        var badge=document.createElement('div');
                                        badge.className='absolute top-3 left-3 flex items-center gap-2 rounded-full px-3 py-1 text-white shadow-lg ring-1 ring-white/30';
                                        badge.style.background='linear-gradient(135deg, #6366f1, #22d3ee)';
                                        var dot=document.createElement('span');
                                        dot.className='inline-block h-2.5 w-2.5 rounded-full bg-white/95 animate-pulse';
                                        var label=document.createElement('span');
                                        label.className='font-semibold tracking-wide';
                                        label.textContent='Loading…';
                                        var sub=document.createElement('span');
                                        sub.className='ml-1 text-white/85 text-xs';
                                        sub.textContent='Please wait';
                                        sub.style.color='rgba(255,255,255,0.9)';
                                        badge.appendChild(dot);
                                        badge.appendChild(label);
                                        badge.appendChild(sub);
                                        overlay.appendChild(badge);
                                        document.body.appendChild(overlay);
                                        try{
                                            requestAnimationFrame(function(){
                                                overlay.style.opacity='1';
                                            });
                                        }catch(_){}
                                        return overlay;
                                    }
                                    function start(){
                                        S.count=S.count+1;
                                        if(S.el!=null){return{stop:stop};}
                                        if(S.t){return{stop:stop};}
                                        S.t=setTimeout(function(){
                                            S.t=0;
                                            if(S.el==null){S.el=build();}
                                        },120);
                                        return{stop:stop};
                                    }
                                    function stop(){
                                        if(S.count>0){S.count=S.count-1;}
                                        if(S.count!==0){return;}
                                        if(S.t){
                                            try{clearTimeout(S.t);}catch(_){}
                                            S.t=0;
                                        }
                                        if(S.el){
                                            var el=S.el;
                                            S.el=null;
                                            try{el.style.opacity='0';}catch(_){}
                                            setTimeout(function(){
                                                try{
                                                    if(el&&el.parentNode){
                                                        el.parentNode.removeChild(el);
                                                    }
                                                }catch(_){}
                                            },160);
                                        }
                                        return{start:start};
                                    }
                                    return{start:start};
                                })();
                                window.__error=function(message){
                                    (function(){
                                        try{
                                            var box=document.getElementById('__messages__');
                                            if(box==null){
                                                box=document.createElement('div');
                                                box.id='__messages__';
                                                box.style.position='fixed';
                                                box.style.top='0';
                                                box.style.right='0';
                                                box.style.padding='8px';
                                                box.style.zIndex='9999';
                                                box.style.pointerEvents='none';
                                                document.body.appendChild(box);
                                            }
                                            var n=document.getElementById('__error_toast__');
                                            if(!n){
                                                n=document.createElement('div');
                                                n.id='__error_toast__';
                                                n.style.display='flex';
                                                n.style.alignItems='center';
                                                n.style.gap='10px';
                                                n.style.padding='12px 16px';
                                                n.style.margin='8px';
                                                n.style.borderRadius='12px';
                                                n.style.minHeight='44px';
                                                n.style.minWidth='340px';
                                                n.style.maxWidth='340px';
                                                n.style.background='#fee2e2';
                                                n.style.color='#991b1b';
                                                n.style.border='1px solid #fecaca';
                                                n.style.borderLeft='4px solid #dc2626';
                                                n.style.boxShadow='0 6px 18px rgba(0,0,0,0.08)';
                                                n.style.fontWeight='600';
                                                n.style.pointerEvents='auto';
                                                var dot=document.createElement('span');
                                                dot.style.width='10px';
                                                dot.style.height='10px';
                                                dot.style.borderRadius='9999px';
                                                dot.style.background='#dc2626';
                                                n.appendChild(dot);
                                                var span=document.createElement('span');
                                                span.id='__error_text__';
                                                n.appendChild(span);
                                                var btn=document.createElement('button');
                                                btn.textContent='Reload';
                                                btn.style.background='#991b1b';
                                                btn.style.color='#fff';
                                                btn.style.border='none';
                                                btn.style.padding='6px 10px';
                                                btn.style.borderRadius='8px';
                                                btn.style.cursor='pointer';
                                                btn.style.fontWeight='700';
                                                btn.onclick=function(){
                                                    try{window.location.reload();}catch(_){}
                                                };
                                                n.appendChild(btn);
                                                box.appendChild(n);
                                            }
                                            var spanText=document.getElementById('__error_text__');
                                            if(spanText){
                                                spanText.textContent=message||'Something went wrong ...';
                                            }
                                        }catch(_){
                                            try{
                                                alert(message||'Something went wrong ...');
                                            }catch(__){}
                                        }
                                    })();
                                };
                                window.__load=function(href){
                                    try{
                                        if(typeof event!=='undefined'&&event&&event.preventDefault){
                                            event.preventDefault();
                                        }
                                    }catch(e1){}
                                    var loaderTimer=null;
                                    var loaderStarted=false;
                                    var L=null;
                                    loaderTimer=setTimeout(function(){
                                        if(!loaderStarted){
                                            loaderStarted=true;
                                            try{
                                                L=(function(){
                                                    try{return __loader.start();}catch(e2){return{stop:function(){}};}
                                                })();
                                            }catch(e3){}
                                        }
                                    },50);
                                    fetch(href,{method:'GET'}).then(function(resp){
                                        if(!resp.ok){throw new Error('HTTP '+resp.status);}
                                        return resp.text();
                                    }).then(function(html){
                                        if(loaderTimer){
                                            clearTimeout(loaderTimer);
                                            loaderTimer=null;
                                        }
                                        if(loaderStarted&&L){
                                            try{L.stop();}catch(e4){}
                                        }
                                        var parser=new DOMParser();
                                        var doc=parser.parseFromString(html,'text/html');
                                        document.title=doc.title;
                                        document.body.innerHTML=doc.body.innerHTML;
                                        var scripts=[...doc.body.querySelectorAll('script'),...doc.head.querySelectorAll('script')];
                                        for(var i=0;i<scripts.length;i++){
                                            var newScript=document.createElement('script');
                                            newScript.textContent=scripts[i].textContent;
                                            document.body.appendChild(newScript);
                                        }
                                        window.history.pushState({},doc.title,href);
                                    }).catch(function(e5){
                                        if(loaderTimer){
                                            clearTimeout(loaderTimer);
                                            loaderTimer=null;
                                        }
                                        if(loaderStarted&&L){
                                            try{L.stop();}catch(e6){}
                                        }
                                        try{
                                            __error('Something went wrong ...');
                                        }catch(e7){}
                                    });
                                };
                            })();</script>
                            """);
        }
    }

    /** Configures Tailwind mode. */
    public App tailwind(TailwindMode mode) {
        this.tailwindMode = mode;
        return this;
    }

    private void addTailwindScript() {
        switch (tailwindMode) {
            case CDN:
                HTMLHead.add("<script src=\"https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4\"></script>");
                break;
            case SELF_HOSTED:
                HTMLHead.add("<script src=\"/assets/tailwind.js\"></script>");
                break;
            case NONE:
                break;
        }
    }

    private static String loadResource(String path) {
        try (InputStream is = App.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null)
                return null;
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] tmp = new byte[8192];
            int read;
            while ((read = is.read(tmp)) != -1) {
                buf.write(tmp, 0, read);
            }
            return buf.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            return null;
        }
    }

    public void debug(boolean enable) {
        this.debugEnabled = enable;
    }

    private void log(String m) {
        if (debugEnabled)
            System.out.println("""
                    tsui - %s
                    """.formatted(m));
    }

    /** Sets the session TTL in milliseconds. Minimum 1 minute. */
    public App sessionTtl(long ttlMillis) {
        this.sessionTtlMillis = Math.max(60_000L, ttlMillis);
        return this;
    }

    /** Sets the session cleanup interval in milliseconds. */
    public App sessionCleanupInterval(long intervalMillis) {
        this.cleanupIntervalMillis = Math.max(30_000L, intervalMillis);
        return this;
    }

    /** Starts the session cleanup daemon thread. */
    public synchronized void startSessionCleanup() {
        if (cleanupThread != null && cleanupThread.isAlive())
            return;
        cleanupEnabled = true;
        cleanupThread = new Thread(() -> {
            while (cleanupEnabled) {
                try {
                    Thread.sleep(cleanupIntervalMillis);
                    long now = System.currentTimeMillis();
                    sessions.entrySet().removeIf(entry -> {
                        sessRec rec = entry.getValue();
                        if (now - rec.lastSeen > sessionTtlMillis) {
                            for (Runnable r : rec.targets.values()) {
                                try {
                                    r.run();
                                } catch (Throwable ignored) {
                                }
                            }
                            return true;
                        }
                        return false;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "jsui-session-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /** Stops the session cleanup daemon thread. */
    public synchronized void stopSessionCleanup() {
        cleanupEnabled = false;
        if (cleanupThread != null) {
            cleanupThread.interrupt();
        }
    }

    void registerClear(String sessionId, String targetId, Runnable clear) {
        if (sessionId == null || sessionId.isEmpty() || targetId == null || targetId.isEmpty() || clear == null)
            return;
        sessRec rec = sessions.computeIfAbsent(sessionId, k -> new sessRec());
        rec.lastSeen = System.currentTimeMillis();
        rec.targets.put(targetId, clear);
    }

    void triggerClear(String sessionId, String targetId) {
        if (sessionId == null || sessionId.isEmpty() || targetId == null || targetId.isEmpty())
            return;
        sessRec rec = sessions.get(sessionId);
        if (rec == null)
            return;
        Runnable fn = rec.targets.remove(targetId);
        if (fn != null) {
            try {
                fn.run();
            } catch (Throwable ignore) {
            }
        }
    }

    /**
     * Clears and triggers all registered target cleanup callbacks for a session.
     * Use this when starting a new page load to terminate background workers
     * (Repeat/Delay/Defer) from the previous page.
     */
    public void ClearSessionTargets(String sessionId) {
        if (sessionId == null || sessionId.isEmpty())
            return;
        sessRec rec = sessions.get(sessionId);
        if (rec == null)
            return;
        java.util.List<String> keys = new java.util.ArrayList<>(rec.targets.keySet());
        for (String key : keys) {
            Runnable fn = rec.targets.remove(key);
            if (fn != null) {
                try {
                    fn.run();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    /** Increments and returns the navigation generation for a session. */
    public long bumpSessionGeneration(String sessionId) {
        if (sessionId == null || sessionId.isEmpty())
            return 0L;
        sessRec rec = sessions.computeIfAbsent(sessionId, k -> new sessRec());
        rec.lastSeen = System.currentTimeMillis();
        rec.generation = rec.generation + 1;
        return rec.generation;
    }

    /** Returns the current navigation generation for a session. */
    public long currentSessionGeneration(String sessionId) {
        if (sessionId == null || sessionId.isEmpty())
            return 0L;
        sessRec rec = sessions.get(sessionId);
        return rec != null ? rec.generation : 0L;
    }

    public String HTMLBody(String cls) {
        if (cls == null || cls.isEmpty())
            cls = "bg-gray-200";
        String head = String.join(" ", HTMLHead);
        return String.join(" ",
                "<!DOCTYPE html>",
                """
                        <html lang="%s" class="%s">
                        """.formatted(Language, cls),
                """
                          <head>%s</head>
                        """.formatted(head),
                """
                          <body id="%s" class="relative"></body>
                        """.formatted(contentId.id),
                "</html>");
    }

    public String HTML(String title, String bodyClass, String body) {
        String cls = Ui.Classes(bodyClass);
        if (cls == null || cls.isEmpty()) {
            cls = "bg-gray-200";
        }
        StringBuilder head = new StringBuilder();
        head.append("<title>");
        head.append(title != null ? HtmlUtils.escape(Ui.Trim(title)) : "");
        head.append("</title>");
        for (String item : HTMLHead) {
            if (item != null) {
                head.append(item);
            }
        }
        if (smoothNav) {
            head.append(__smoothnav());
        }
        String titleEscaped = title != null ? HtmlUtils.escape(Ui.Trim(title)) : "";
        String headStr = head.toString();
        String bodyContent = body != null ? body : "";
        return Ui.Trim("""
                <!DOCTYPE html>
                <html lang="%s" class="%s">
                <head>%s%s</head>
                <body id="%s" class="relative">%s</body>
                </html>""".formatted(Language, cls, "<title>" + titleEscaped + "</title>", headStr, contentId.id, bodyContent));
    }

    /** Enables or disables smooth client-side navigation. */
    public App smoothNav(boolean enable) {
        this.smoothNav = enable;
        return this;
    }

    /** Returns true if smooth navigation is enabled. */
    public boolean isSmoothNavEnabled() {
        return this.smoothNav;
    }

    private String __smoothnav() {
        return Ui.Script("""
                (function(){try{if(window.__jsuiSmoothNavInit){return;}window.__jsuiSmoothNavInit=true;\
                function isInternalLink(href){if(!href)return false;if(href.startsWith('#'))return false;if(href.startsWith('javascript:'))return false;\
                if(href.startsWith('data:')||href.startsWith('mailto:'))return false;\
                if(href.startsWith('http://')||href.startsWith('https://')){try{var linkUrl=new URL(href,window.location.href);return linkUrl.origin===window.location.origin;}catch(_){return false;}}\
                return true;}document.addEventListener('click',function(e){var link=e.target.closest('a');if(!link)return;var href=link.getAttribute('href');if(!href)return;\
                if(link.target&&link.target!=='_self')return;if(link.download)return;var onclickAttr=link.getAttribute('onclick');if(onclickAttr&&onclickAttr.trim().length>0)return;\
                if(!isInternalLink(href))return;e.preventDefault();try{__load(href);}catch(_){window.location.href=href;}},true);}catch(_){}})();""");
    }

    /**
     * Serve static files from the application classpath.
     * Example: AssetsFromClasspath("/assets", "public/assets", 86400)
     * will serve GET /assets/FILE by reading classpath resource
     * "public/assets/FILE".
     */
    public void AssetsFromClasspath(String mountPath, String resourceRoot, long maxAgeSeconds) {
        if (mountPath == null || mountPath.isEmpty() || resourceRoot == null || resourceRoot.isEmpty())
            return;
        String m = normalizePath(mountPath);
        if ("/".equals(m))
            return;
        assets.add(new AssetCfg(m, resourceRoot, Math.max(0, maxAgeSeconds)));
    }

    /** Adds a favicon link using a data: URL. */
    public void FaviconDataUrl(String dataUrl, long maxAgeSeconds) {
        if (dataUrl == null || dataUrl.isEmpty())
            return;
        HTMLHead.add("""
                <link rel="icon" href="%s" />
                """.formatted(Ui.Trim(dataUrl)));
    }

    /**
     * Adds a favicon served from classpath via mounted assets.
     * Example: FaviconFromClasspath("/assets", "public/favicon.ico", 86400)
     */
    public void FaviconFromClasspath(String mountPath, String classpathResource, long maxAgeSeconds) {
        if (mountPath == null || classpathResource == null)
            return;
        String m = normalizePath(mountPath);
        String pathOnly = m.endsWith("/") ? m.substring(0, m.length() - 1) : m;
        int slash = classpathResource.lastIndexOf('/') + 1;
        String root = slash > 0 ? classpathResource.substring(0, slash) : "";
        if (!root.isEmpty())
            AssetsFromClasspath(m, root, maxAgeSeconds);
        String name = classpathResource.substring(slash);
        String href = "%s/%s".formatted(pathOnly, name);
        HTMLHead.add("""
                <link rel="icon" href="%s" />
                """.formatted(Ui.Trim(href)));
    }

    // Asset resolution (used by Server)
    static final class ResolvedAsset {
        final InputStream stream;
        final String contentType;
        final long maxAgeSeconds;

        ResolvedAsset(InputStream s, String ct, long age) {
            stream = s;
            contentType = ct;
            maxAgeSeconds = age;
        }
    }

    ResolvedAsset resolveAsset(String path) {
        if (path == null || path.isEmpty())
            return null;
        for (AssetCfg cfg : assets) {
            if (!path.startsWith("%s/".formatted(cfg.mountPath)) && !path.equals(cfg.mountPath))
                continue;
            String rel = path.substring(cfg.mountPath.length());
            if (rel.startsWith("/"))
                rel = rel.substring(1);
            if (rel.isEmpty())
                return null;
            String resource = cfg.resourceRoot.endsWith("/") ? "%s%s".formatted(cfg.resourceRoot, rel)
                    : "%s/%s".formatted(cfg.resourceRoot, rel);
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
        if (f.endsWith(".css"))
            return "text/css; charset=utf-8";
        if (f.endsWith(".js"))
            return "application/javascript; charset=utf-8";
        if (f.endsWith(".json"))
            return "application/json; charset=utf-8";
        if (f.endsWith(".svg"))
            return "image/svg+xml";
        if (f.endsWith(".png"))
            return "image/png";
        if (f.endsWith(".jpg") || f.endsWith(".jpeg"))
            return "image/jpeg";
        if (f.endsWith(".gif"))
            return "image/gif";
        if (f.endsWith(".webp"))
            return "image/webp";
        if (f.endsWith(".ico"))
            return "image/x-icon";
        if (f.endsWith(".woff2"))
            return "font/woff2";
        if (f.endsWith(".woff"))
            return "font/woff";
        if (f.endsWith(".ttf"))
            return "font/ttf";
        if (f.endsWith(".map"))
            return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    public Callable Callable(Callable method) {
        if (method == null) {
            return null;
        }
        if (!reverse.containsKey(method)) {
            register("/call/%s".formatted(UUID.randomUUID()), method);
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
        register("/act/%s".formatted(uid), callable);
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
        log("invoke %s".formatted(path));
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
            normalized = "/%s".formatted(normalized);
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * PWA configuration for manifest and service worker.
     */
    public static final class PWAConfig {
        public String name;
        public String shortName;
        public String description;
        public String themeColor = "#2563eb";
        public String backgroundColor = "#ffffff";
        public String startUrl = "/";
        public String display = "standalone";
        public String orientation = "portrait";

        public static final class Icon {
            public String src;
            public String sizes;
            public String type;
            public String purpose; // "any", "maskable", "monochrome"

            public Icon(String src, String sizes, String type) {
                this.src = src;
                this.sizes = sizes;
                this.type = type;
            }

            public Icon purpose(String p) {
                this.purpose = p;
                return this;
            }
        }

        public final List<Icon> icons = new ArrayList<>();

        /** @deprecated Use icons list instead. */
        @Deprecated
        public String icon;

        public PWAConfig addIcon(String src, String sizes, String type) {
            icons.add(new Icon(src, sizes, type));
            return this;
        }

        public PWAConfig addIcon(Icon icon) {
            icons.add(icon);
            return this;
        }
    }

    private PWAConfig pwaConfig = null;

    public static final class CSPConfig {
        public String defaultSrc = "'self'";
        public String scriptSrc = "'self' 'unsafe-inline' https://cdn.jsdelivr.net";
        public String styleSrc = "'self' 'unsafe-inline' https://cdn.jsdelivr.net";
        public String imgSrc = "'self' data: https:";
        public String fontSrc = "'self' https:";
        public String connectSrc = "'self' wss: ws:";
        public String frameAncestors = "'self'";
        public boolean enabled = false;
    }

    private final CSPConfig cspConfig = new CSPConfig();

    /** Configures Content Security Policy. */
    public App csp(java.util.function.Consumer<CSPConfig> configurer) {
        configurer.accept(cspConfig);
        cspConfig.enabled = true;
        return this;
    }

    /** Returns the CSP header value or null if disabled. */
    public String cspHeaderValue() {
        if (!cspConfig.enabled)
            return null;
        return String.join("; ",
                "default-src %s".formatted(cspConfig.defaultSrc),
                "script-src %s".formatted(cspConfig.scriptSrc),
                "style-src %s".formatted(cspConfig.styleSrc),
                "img-src %s".formatted(cspConfig.imgSrc),
                "font-src %s".formatted(cspConfig.fontSrc),
                "connect-src %s".formatted(cspConfig.connectSrc),
                "frame-ancestors %s".formatted(cspConfig.frameAncestors));
    }

    /**
     * Configures PWA manifest and service worker.
     * 
     * @param name      Full application name
     * @param shortName Short name for home screen
     * @param icon      Path to icon (192x192 or larger PNG/SVG)
     * @return This App instance for chaining
     */
    public App PWA(String name, String shortName, String icon) {
        PWAConfig config = new PWAConfig();
        config.name = name != null ? name : "App";
        config.shortName = shortName != null ? shortName : config.name;
        config.icon = icon;
        this.pwaConfig = config;
        return this;
    }

    /**
     * Sets PWA configuration with full options.
     */
    public App PWA(PWAConfig config) {
        this.pwaConfig = config;
        return this;
    }

    /**
     * Generates and returns the web app manifest JSON.
     * Should be served at /manifest.json or similar.
     */
    public String Manifest() {
        if (pwaConfig == null) {
            return null;
        }
        String name = escapeJson(pwaConfig.name != null ? pwaConfig.name : "App");
        String shortName = escapeJson(pwaConfig.shortName != null ? pwaConfig.shortName : pwaConfig.name);
        String startUrl = escapeJson(pwaConfig.startUrl != null ? pwaConfig.startUrl : "/");
        String display = escapeJson(pwaConfig.display != null ? pwaConfig.display : "standalone");
        String orientation = escapeJson(pwaConfig.orientation != null ? pwaConfig.orientation : "portrait");
        String themeColor = escapeJson(pwaConfig.themeColor != null ? pwaConfig.themeColor : "#2563eb");
        String backgroundColor = escapeJson(pwaConfig.backgroundColor != null ? pwaConfig.backgroundColor : "#ffffff");

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(name).append("\",");
        sb.append("\"short_name\":\"").append(shortName).append("\",");
        if (pwaConfig.description != null && !pwaConfig.description.isEmpty()) {
            sb.append("\"description\":\"").append(escapeJson(pwaConfig.description)).append("\",");
        }
        sb.append("""
                "start_url":"%s","display":"%s","orientation":"%s","theme_color":"%s","background_color":"%s","icons":[\
                """.formatted(startUrl, display, orientation, themeColor, backgroundColor));

        List<PWAConfig.Icon> iconList = pwaConfig.icons.isEmpty() && pwaConfig.icon != null
                ? List.of(
                        new PWAConfig.Icon(pwaConfig.icon, "192x192", detectIconType(pwaConfig.icon)),
                        new PWAConfig.Icon(pwaConfig.icon, "512x512", detectIconType(pwaConfig.icon)))
                : pwaConfig.icons;

        for (int i = 0; i < iconList.size(); i++) {
            PWAConfig.Icon icon = iconList.get(i);
            if (i > 0)
                sb.append(",");
            String src = escapeJson(icon.src);
            String sizes = escapeJson(icon.sizes);
            String type = escapeJson(icon.type);
            String purpose = icon.purpose != null ? ",\"purpose\":\"" + escapeJson(icon.purpose) + "\"" : "";
            sb.append("""
                    {"src":"%s","sizes":"%s","type":"%s"%s}\
                    """.formatted(src, sizes, type, purpose));
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String detectIconType(String path) {
        if (path == null)
            return "image/png";
        String lower = path.toLowerCase();
        if (lower.endsWith(".svg"))
            return "image/svg+xml";
        if (lower.endsWith(".ico"))
            return "image/x-icon";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return "image/jpeg";
        if (lower.endsWith(".webp"))
            return "image/webp";
        return "image/png";
    }

    /**
     * Generates and returns the service worker JavaScript.
     * Should be served at /sw.js or similar.
     */
    public String ServiceWorker() {
        if (pwaConfig == null) {
            return null;
        }
        return """
                (function(){
                    const CACHE_NAME='jsui-v1';
                    const urlsToCache=[
                        '/',
                        '/manifest.json'
                    ];
                    self.addEventListener('install',function(event){
                        event.waitUntil(
                            caches.open(CACHE_NAME).then(function(cache){
                                return cache.addAll(urlsToCache);
                            })
                        );
                    });
                    self.addEventListener('fetch',function(event){
                        event.respondWith(
                            caches.match(event.request).then(function(response){
                                if(response){
                                    return response;
                                }
                                return fetch(event.request).then(function(response){
                                    if(!response||response.status!==200||response.type!=='basic'){
                                        return response;
                                    }
                                    var responseToCache=response.clone();
                                    caches.open(CACHE_NAME).then(function(cache){
                                        cache.put(event.request,responseToCache);
                                    });
                                    return response;
                                });
                            })
                        );
                    });
                    self.addEventListener('activate',function(event){
                        event.waitUntil(
                            caches.keys().then(function(cacheNames){
                                return Promise.all(
                                    cacheNames.map(function(cacheName){
                                        if(cacheName!==CACHE_NAME){
                                            return caches.delete(cacheName);
                                        }
                                    })
                                );
                            })
                        );
                    });
                })();
                """;
    }

    /**
     * Adds PWA manifest link and service worker registration to HTMLHead.
     * Call this after configuring PWA.
     */
    public void EnablePWA() {
        if (pwaConfig == null) {
            return;
        }
        HTMLHead.add("<link rel=\"manifest\" href=\"/manifest.json\">");
        HTMLHead.add("""
                <meta name="theme-color" content="%s">
                """.formatted(pwaConfig.themeColor != null ? pwaConfig.themeColor : "#2563eb"));
        HTMLHead.add("""
                <script>
                    if('serviceWorker'in navigator){
                        window.addEventListener('load',function(){
                            navigator.serviceWorker.register('/sw.js').then(function(reg){
                                console.log('Service Worker registered:',reg);
                            }).catch(function(err){
                                console.log('Service Worker registration failed:',err);
                            });
                        });
                    }
                </script>
                """);
    }

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
