package jsui;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Simplified Java port of core helpers from t-sui/ui.ts.
 *
 * Provides HTML string builders, attribute helpers, basic form controls,
 * and Target/Skeleton utilities needed by server/data layers.
 */
public final class Ui {

    private Ui() {
    }

    public enum Swap {
        inline, outline, none, append, prepend
    }

    @Data
    @Accessors(chain = true, fluent = false)
    public static final class Attr {
        public String onclick;
        public String onchange;
        public String onsubmit;
        public String step;
        public String id;
        public String href;
        public String title;
        public String alt;

        public String type;
        public String clazz;
        public String style;
        public String name;
        public String value;
        public String checked;
        public String htmlFor;
        public String src;
        public String selected;
        public String pattern;
        public String placeholder;
        public String autocomplete;
        public String max;
        public String min;
        public String target;
        public Integer rows;
        public Integer cols;
        public Integer width;
        public Integer height;
        public Boolean disabled;
        public Boolean required;
        public Boolean readonly;

        public String form;

        public String dataAccordion;
        public String dataAccordionItem;
        public String dataAccordionContent;
        public String dataTabs;
        public String dataTabsIndex;
        public String dataTabsPanel;

        public static Attr of() {
            return new Attr();
        }

        public Attr id(String v) {
            this.id = v;
            return this;
        }

        public Attr href(String v) {
            this.href = v;
            return this;
        }

        public Attr alt(String v) {
            this.alt = v;
            return this;
        }

        public Attr title(String v) {
            this.title = v;
            return this;
        }

        public Attr src(String v) {
            this.src = v;
            return this;
        }

        public Attr htmlFor(String v) {
            this.htmlFor = v;
            return this;
        }

        public Attr type(String v) {
            this.type = v;
            return this;
        }

        public Attr clazz(String v) {
            this.clazz = v;
            return this;
        }

        public Attr style(String v) {
            this.style = v;
            return this;
        }

        public Attr onclick(String v) {
            this.onclick = v;
            return this;
        }

        public Attr onchange(String v) {
            this.onchange = v;
            return this;
        }

        public Attr onsubmit(String v) {
            this.onsubmit = v;
            return this;
        }

        public Attr value(String v) {
            this.value = v;
            return this;
        }

        public Attr checked(String v) {
            this.checked = v;
            return this;
        }

        public Attr selected(String v) {
            this.selected = v;
            return this;
        }

        public Attr name(String v) {
            this.name = v;
            return this;
        }

        public Attr placeholder(String v) {
            this.placeholder = v;
            return this;
        }

        public Attr autocomplete(String v) {
            this.autocomplete = v;
            return this;
        }

        public Attr pattern(String v) {
            this.pattern = v;
            return this;
        }

        public Attr cols(Integer v) {
            this.cols = v;
            return this;
        }

        public Attr rows(Integer v) {
            this.rows = v;
            return this;
        }

        public Attr width(Integer v) {
            this.width = v;
            return this;
        }

        public Attr height(Integer v) {
            this.height = v;
            return this;
        }

        public Attr min(String v) {
            this.min = v;
            return this;
        }

        public Attr max(String v) {
            this.max = v;
            return this;
        }

        public Attr target(String v) {
            this.target = v;
            return this;
        }

        public Attr step(String v) {
            this.step = v;
            return this;
        }

        public Attr required(boolean v) {
            this.required = v;
            return this;
        }

        public Attr disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public Attr readonly(boolean v) {
            this.readonly = v;
            return this;
        }

        public Attr form(String v) {
            this.form = v;
            return this;
        }

        public Attr dataAccordion(String v) {
            this.dataAccordion = v;
            return this;
        }
    }

    public static Attr targetAttr(Target target) {
        if (target == null || target.id == null) {
            return null;
        }
        return Attr.of().id(target.id);
    }

    @Data
    @NoArgsConstructor
    public static final class AOption {
        public String id;
        public String value;

        public AOption(String id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    public static final class Target {
        public final String id;
        public final Action Replace;
        public final Action Append;
        public final Action Prepend;
        public final Action Render;

        public Target(String id) {
            this.id = id;
            this.Replace = new Action(id, Swap.outline);
            this.Append = new Action(id, Swap.append);
            this.Prepend = new Action(id, Swap.prepend);
            this.Render = new Action(id, Swap.inline);
        }

        public String Skeleton(SkeletonType type) {
            if (type == SkeletonType.list)
                return Skeleton.List(this, 5);
            if (type == SkeletonType.component)
                return Skeleton.Component(this);
            if (type == SkeletonType.page)
                return Skeleton.Page(this);
            if (type == SkeletonType.form)
                return Skeleton.Form(this);
            return Skeleton.Default(this);
        }
    }

    public static final class Action {
        public final String id;
        public final Swap swap;

        public Action(String id, Swap swap) {
            this.id = id;
            this.swap = swap;
        }
    }

    public enum SkeletonType {
        list, component, page, form, def
    }

    public static final class Skeleton {
        private Skeleton() {
        }

        public static String Default(Target t) {
            return div("animate-pulse", targetAttr(t)).render(
                    div("bg-white dark:bg-gray-900 rounded-lg p-4 shadow").render(
                            div("bg-gray-200 h-5 rounded w-5/6 mb-2").render(),
                            div("bg-gray-200 h-5 rounded w-2/3 mb-2").render(),
                            div("bg-gray-200 h-5 rounded w-4/6").render()));
        }

        public static String List(Target t, int rows) {
            List<String> items = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                items.add(
                        div("flex items-center gap-3 mb-3").render(
                                div("bg-gray-200 rounded-full h-10 w-10").render(),
                                div("flex-1").render(
                                        div("bg-gray-200 h-4 rounded w-5/6 mb-2").render(),
                                        div("bg-gray-200 h-4 rounded w-3/6").render())));
            }
            return div("animate-pulse", targetAttr(t)).render(
                    div("bg-white dark:bg-gray-900 rounded-lg p-4 shadow").render(String.join("", items)));
        }

        public static String Component(Target t) {
            return div("animate-pulse", targetAttr(t)).render(
                    div("bg-white dark:bg-gray-900 rounded-lg p-4 shadow").render(
                            div("bg-gray-200 h-6 rounded w-2/5 mb-4").render(),
                            div("bg-gray-200 h-4 rounded w-full mb-2").render(),
                            div("bg-gray-200 h-4 rounded w-5/6 mb-2").render(),
                            div("bg-gray-200 h-4 rounded w-4/6").render()));
        }

        public static String Page(Target t) {
            String card = div("bg-white dark:bg-gray-900 rounded-lg p-4 shadow mb-4").render(
                    div("bg-gray-200 h-5 rounded w-2/5 mb-3").render(),
                    div("bg-gray-200 h-4 rounded w-full mb-2").render(),
                    div("bg-gray-200 h-4 rounded w-5/6 mb-2").render(),
                    div("bg-gray-200 h-4 rounded w-4/6").render());
            return div("animate-pulse", targetAttr(t)).render(
                    div("bg-gray-200 h-8 rounded w-1/3 mb-6").render(),
                    card,
                    card);
        }

        public static String Form(Target t) {
            String fieldShort = div("").render(
                    div("bg-gray-200 h-4 rounded w-3/6 mb-2").render(),
                    div("bg-gray-200 h-10 rounded w-full").render());
            String fieldArea = div("").render(
                    div("bg-gray-200 h-4 rounded w-2/6 mb-2").render(),
                    div("bg-gray-200 h-24 rounded w-full").render());
            String actions = div("flex justify-end gap-3 mt-6").render(
                    div("bg-gray-200 h-10 rounded w-24").render(),
                    div("bg-gray-200 h-10 rounded w-32").render());
            return div("animate-pulse", targetAttr(t)).render(
                    div("bg-white dark:bg-gray-900 rounded-lg p-4 shadow").render(
                            div("bg-gray-200 h-6 rounded w-2/5 mb-5").render(),
                            div("grid grid-cols-1 md:grid-cols-2 gap-4").render(
                                    div("").render(fieldShort),
                                    div("").render(fieldShort),
                                    div("").render(fieldArea),
                                    div("").render(fieldShort)),
                            actions));
        }
    }

    public static final String XS = " p-1";
    public static final String SM = " p-2";
    public static final String MD = " p-3";
    public static final String ST = " p-4";
    public static final String LG = " p-5";
    public static final String XL = " p-6";

    public static final String AREA = " cursor-pointer bg-white border border-gray-300 hover:border-blue-500 rounded-lg block w-full";
    public static final String INPUT = " cursor-pointer bg-white border border-gray-300 hover:border-blue-500 rounded-lg block w-full h-12";
    public static final String VALUE = " bg-white border border-gray-300 hover:border-blue-500 rounded-lg block h-12";
    public static final String BTN = " cursor-pointer font-bold text-center select-none";
    public static final String DISABLED = " cursor-text pointer-events-none bg-gray-50";

    public static final String Yellow = " bg-yellow-400 text-gray-800 hover:text-gray-200 hover:bg-yellow-600 font-bold border-gray-300 flex items-center justify-center";
    public static final String YellowOutline = " border border-yellow-500 text-yellow-600 hover:text-gray-700 hover:bg-yellow-500 flex items-center justify-center";
    public static final String Green = " bg-green-600 text-white hover:bg-green-700 checked:bg-green-600 border-gray-300 flex items-center justify-center";
    public static final String GreenOutline = " border border-green-500 text-green-500 hover:text-white hover:bg-green-600 flex items-center justify-center";
    public static final String Purple = " bg-purple-500 text-white hover:bg-purple-700 border-purple-500 flex items-center justify-center";
    public static final String PurpleOutline = " border border-purple-500 text-purple-500 hover:text-white hover:bg-purple-600 flex items-center justify-center";
    public static final String Blue = " bg-blue-800 text-white hover:bg-blue-700 border-gray-300 flex items-center justify-center";
    public static final String BlueOutline = " border border-blue-500 text-blue-600 hover:text-white hover:bg-blue-700 checked:bg-blue-700 flex items-center justify-center";
    public static final String Red = " bg-red-600 text-white hover:bg-red-800 border-gray-300 flex items-center justify-center";
    public static final String RedOutline = " border border-red-500 text-red-600 hover:text-white hover:bg-red-700 flex items-center justify-center";
    public static final String Gray = " bg-gray-600 text-white hover:bg-gray-800 focus:bg-gray-800 border-gray-300 flex items-center justify-center";
    public static final String GrayOutline = " border border-gray-300 text-black hover:text-white hover:bg-gray-700 flex items-center justify-center";
    public static final String White = " bg-white text-black hover:bg-gray-200 border-gray-200 flex items-center justify-center";
    public static final String WhiteOutline = " border border-white text-balck hover:text-black hover:bg-white flex items-center justify-center";

    public static final String space = "&nbsp;";

    public static String ThemeSwitcher(String css) {
        String id = "tsui_theme_" + RandomString(8);
        String sun = """
                <svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">\
                <path d="M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.8 1.42-1.42zm10.48 14.32l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM12 4V1h-0 0 0 0v3zm0 19v-3h0 0 0 0v3zM4 12H1v0 0 0 0h3zm19 0h-3v0 0 0 0h3zM6.76 19.16l-1.79 1.8 1.41 1.41 1.8-1.79-1.42-1.42zM19.16 6.76l1.8-1.79-1.41-1.41-1.8 1.79 1.41 1.41zM12 8a4 4 0 100 8 4 4 0 000-8z"/>\
                </svg>""";
        String moon = """
                <svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">\
                <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/>\
                </svg>""";
        String desktop = """
                <svg aria-hidden="true" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">\
                <path d="M3 4h18v12H3z"/><path d="M8 20h8v-2H8z"/>\
                </svg>""";

        String cssClasses = Classes(css);
        String btn = """
                <button id="%s" type="button" class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-gray-300 bg-white text-gray-700 hover:bg-gray-100 \
                dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-700 shadow-sm %s">\
                <span class="icon">%s</span>\
                <span class="label">Auto</span></button>"""
                .formatted(id, cssClasses, desktop);

        String script = """
                <script>(function(){var btn=document.getElementById("%s"); if(!btn) return;\
                var modes=["system","light","dark"]; function getPref(){ try { return localStorage.getItem("theme")||"system"; } catch(_) { return "system"; } }\
                function resolve(mode){ if(mode==="system"){ try { return (window.matchMedia && window.matchMedia("(prefers-color-scheme: %%d)").matches)?"dark":"light"; } catch(_) { return "light"; } } return mode; }\
                function setMode(mode){ try { if (typeof setTheme === "function") setTheme(mode); } catch(_){} }\
                function labelFor(mode){ return mode==="system"?"Auto":(mode.charAt(0).toUpperCase()+mode.slice(1)); }\
                function iconFor(effective){ if(effective==="dark"){ return `%s`; } if(effective==="light"){ return `%s`; } return `%s`; }\
                function render(){ var pref=getPref(); var eff=resolve(pref); var icon=iconFor(eff); var i=btn.querySelector(".icon"); if(i){ i.innerHTML=icon; } var l=btn.querySelector(".label"); if(l){ l.textContent=labelFor(pref); } }\
                render();\
                btn.addEventListener("click", function(){ var pref=getPref(); var idx=modes.indexOf(pref); var next=modes[(idx+1)%%modes.length]; setMode(next); render(); });\
                try { if (window.matchMedia){ window.matchMedia("(prefers-color-scheme: %%d)").addEventListener("change", function(){ if(getPref()==="system"){ render(); } }); } } catch(_){ }\
                })();</script>"""
                .formatted(id, moon, sun, desktop);

        return btn + script;
    }

    public static String Script(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isEmpty())
                sb.append(p);
        }
        return Trim("<script>" + sb + "</script>");
    }

    public static String Captcha(String siteKey, String securedHtml) {
        if (siteKey == null || siteKey.isEmpty()) {
            return div("flex items-center justify-center").render(div("").render(securedHtml));
        }
        String noteId = makeId();
        String hiddenId = makeId();
        String captchaId = makeId();

        String container = div("", Attr.of().id(captchaId).style("min-height:78px; min-width:304px;")).render();
        String hidden = div("absolute inset-0 flex items-center justify-center opacity-0 pointer-events-none",
                Attr.of().id(hiddenId)).render(securedHtml);
        String top = div("relative flex items-center justify-center").render(container, hidden);
        String note = div("text-xs border border-dashed border-black p-1 whitespace-wrap p-2 hidden",
                Attr.of().id(noteId)).render(
                        div("").render("Captcha not loaded, please add the following script to your html file."),
                        div("").render(
                                "&lt;script src=\"https://www.google.com/recaptcha/api.js\" async defer&gt;&lt;/script&gt;"));

        String js = """
                setTimeout(function(){var note=document.getElementById('%s');var captcha=document.getElementById('%s');var hidden=document.getElementById('%s');var loaded=window.grecaptcha||null;\
                if(loaded==null){setTimeout(function(){if(!window.grecaptcha){note.classList.remove('hidden');}},1200);}\
                else{loaded.ready(function(){loaded.render('%s',{sitekey:'%s',callback:function(){requestAnimationFrame(function(){captcha.style.visibility='hidden';hidden.classList.remove('opacity-0');hidden.classList.remove('pointer-events-none');});},\
                'expired-callback':function(){requestAnimationFrame(function(){captcha.style.visibility='visible';hidden.classList.add('opacity-0');hidden.classList.add('pointer-events-none');loaded.reset();});},\
                'error-callback':function(){requestAnimationFrame(function(){captcha.style.visibility='visible';hidden.classList.add('opacity-0');hidden.classList.add('pointer-events-none');loaded.reset();});}});});}\
                },300);"""
                .formatted(noteId, captchaId, hiddenId, captchaId, siteKey);

        return Normalize(div("").render(top, note) + Script(js));
    }

    public static String Captcha2() {
        String text = RandomString(6);
        String canvasId = "captchaCanvas_" + RandomString(8);
        String inputId = "captchaInput_" + RandomString(8);
        String hiddenId = "captchaVerified_" + RandomString(8);

        String canvas = canvas("", Attr.of().id(canvasId)
                .style("border:1px solid #ccc; width:100%; max-width:320px; height:96px;")).render();
        String input = input("w-full sm:w-auto flex-1 min-w-0",
                Attr.of().id(inputId).type("text").name("js_captcha_answer")
                        .placeholder("Enter text from image").required(true).autocomplete("off"));
        String hidden = input("", Attr.of().id(hiddenId).type("hidden").name("js_captcha_verified").value("false"));

        String escapedText = escapeJs(text);
        String js = """
                setTimeout(function(){var canvas=document.getElementById('%s');var ctx=canvas.getContext('2d');\
                var input=document.getElementById('%s');var hidden=document.getElementById('%s');var captchaText=%s;\
                function sizeCanvas(){var ratio=window.devicePixelRatio||1;var dw=Math.min(320,canvas.clientWidth||320);var dh=96;canvas.width=Math.floor(dw*ratio);canvas.height=Math.floor(dh*ratio);ctx.setTransform(ratio,0,0,ratio,0,0);canvas.style.width=dw+'px';canvas.style.height=dh+'px';}\
                function draw(){sizeCanvas();var w=canvas.clientWidth||320;var h=canvas.clientHeight||96;ctx.clearRect(0,0,w,h);ctx.fillStyle='#f0f0f0';ctx.fillRect(0,0,w,h);ctx.font='bold 24px Arial';ctx.textBaseline='middle';ctx.textAlign='center';\
                for(var i=0;i<captchaText.length;i++){var ch=captchaText[i];var x=(w/captchaText.length)*i+(w/captchaText.length)/2;var y=h/2+(Math.random()*10-5);ctx.save();ctx.translate(x,y);ctx.rotate((Math.random()*0.5-0.25));\
                ctx.fillStyle='rgb('+Math.floor(Math.random()*200)+','+Math.floor(Math.random()*200)+','+Math.floor(Math.random()*200)+')';ctx.fillText(ch,0,0);ctx.restore();}\
                for(var j=0;j<20;j++){ctx.beginPath();ctx.arc(Math.random()*w,Math.random()*h,Math.random()*2,0,Math.PI*2);ctx.fillStyle='rgba(0,0,0,0.3)';ctx.fill();}}\
                function validate(){if((input.value||'').toLowerCase()===captchaText.toLowerCase()){hidden.value='true';input.style.borderColor='green';}else{hidden.value='false';input.style.borderColor='red';}}\
                input.addEventListener('input', validate);draw();window.addEventListener('resize', draw);\
                },300);"""
                .formatted(canvasId, inputId, hiddenId, escapedText);

        return div("",
                Attr.of().style(
                        "display:flex;flex-wrap:wrap;align-items:center;gap:10px;margin-bottom:10px;width:100%;"))
                .render(canvas, input, hidden, Script(js));
    }

    private static String escapeJs(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r",
                "\\r");
    }

    /**
     * Server-side CAPTCHA session storage.
     */
    public static final class CaptchaSession {
        public final String text;
        public final long createdAt;
        public volatile int attempts;
        public volatile boolean solved;
        public final long expiresAt;
        public final int maxAttempts;

        CaptchaSession(String text, long createdAt, long expiresAt, int maxAttempts) {
            this.text = text;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.maxAttempts = maxAttempts;
            this.attempts = 0;
            this.solved = false;
        }

        boolean isExpired(long now) {
            return now > expiresAt;
        }
    }

    /**
     * Configurable CAPTCHA component with server-side session validation.
     * Uses builder pattern for configuration.
     */
    public static final class Captcha2Component {
        private String answerFieldName = "captcha_answer";
        private String sessionFieldName = "captcha_session";
        private String clientVerifiedFieldName = "captcha_client_verified";
        private int codeLength = DEFAULT_CAPTCHA_LENGTH;
        private long sessionLifetimeMillis = DEFAULT_CAPTCHA_LIFETIME_MS;
        private int attemptLimit = DEFAULT_CAPTCHA_ATTEMPTS;
        private Context.Callable onValidated;

        private Captcha2Component(Context.Callable onValidated) {
            this.onValidated = onValidated != null ? onValidated
                    : ctx -> div("text-green-600").render("Captcha validated successfully!");
        }

        public static Captcha2Component create(Context.Callable onValidated) {
            return new Captcha2Component(onValidated);
        }

        public Captcha2Component answerField(String name) {
            if (name != null && !name.isEmpty()) {
                this.answerFieldName = name;
            }
            return this;
        }

        public Captcha2Component sessionField(String name) {
            if (name != null && !name.isEmpty()) {
                this.sessionFieldName = name;
            }
            return this;
        }

        public Captcha2Component clientVerifiedField(String name) {
            if (name != null && !name.isEmpty()) {
                this.clientVerifiedFieldName = name;
            }
            return this;
        }

        public Captcha2Component length(int n) {
            if (n > 0) {
                this.codeLength = n;
            }
            return this;
        }

        public Captcha2Component lifetimeMillis(long millis) {
            if (millis > 0) {
                this.sessionLifetimeMillis = millis;
            }
            return this;
        }

        public Captcha2Component attempts(int limit) {
            if (limit > 0) {
                this.attemptLimit = limit;
            }
            return this;
        }

        public String render(Context ctx) {
            if (ctx == null || ctx.app == null) {
                return renderCaptchaError("Context or App is null");
            }

            // Generate secure session ID and captcha text
            String sessionId = generateSecureId("captcha_session_");
            String captchaText = generateSecureCaptchaText(codeLength);
            if (captchaText == null) {
                return renderCaptchaError("Error generating CAPTCHA text");
            }

            // Create and store session
            long now = System.currentTimeMillis();
            CaptchaSession session = new CaptchaSession(captchaText, now, now + sessionLifetimeMillis, attemptLimit);
            storeCaptchaSession(sessionId, session);

            String rootId = generateSecureId("captchaRoot_");
            String canvasId = generateSecureId("captchaCanvas_");
            String hiddenFieldId = generateSecureId("captchaVerified_");
            String containerId = generateSecureId("captchaContainer_");

            String successPath = "";
            if (onValidated != null) {
                Context.Callable callable = ctx.Callable(onValidated);
                if (callable != null) {
                    String path = ctx.app.pathOf(callable);
                    if (path != null && !path.isEmpty()) {
                        successPath = path;
                    }
                }
            }

            String defaultSuccess = div("text-green-600").render("Captcha validated successfully!");
            String escapedText = escapeJs(captchaText);
            String escapedSuccessPath = escapeJs(successPath);
            String escapedDefaultSuccess = escapeJs(defaultSuccess);

            String canvas = canvas("",
                    Attr.of().id(canvasId).style("border:1px solid #ccc;width:100%;max-width:320px;height:96px;"))
                    .render();
            String inputField = IText(answerFieldName, null).Class("w-full").ClassLabel("text-gray-600")
                    .ClassInput("w-full")
                    .Autocomplete("off").Required().Render("Enter text from image");
            String hiddenSession = Hidden(sessionFieldName, "hidden", sessionId);
            String hiddenVerified = input("",
                    Attr.of().id(hiddenFieldId).type("hidden").name(clientVerifiedFieldName).value("false"));

            String container = div("flex flex-col gap-3 w-72", Attr.of().id(containerId)).render(canvas, inputField);
            String root = div("flex flex-col items-start gap-3 w-full", Attr.of().id(rootId)).render(container,
                    hiddenSession, hiddenVerified);

            String js = """
                    setTimeout(function(){var root=document.getElementById('%s');var canvas=document.getElementById('%s');if(!canvas){return;}var ctx=canvas.getContext('2d');if(!ctx){return;}\
                    var input=document.querySelector('input[name="%s"]');var hiddenField=document.getElementById('%s');var container=document.getElementById('%s');\
                    var captchaText=%s;var successPath=%s;var defaultSuccess=%s;var solved=false;\
                    function injectSuccess(html){if(!root){return;}var output=(html&&html.trim())?html:defaultSuccess;root.innerHTML=output;}\
                    function sizeCanvas(){if(solved){return;}var ratio=window.devicePixelRatio||1;var displayWidth=Math.min(320,canvas.clientWidth||320);var displayHeight=96;\
                    canvas.width=Math.floor(displayWidth*ratio);canvas.height=Math.floor(displayHeight*ratio);ctx.setTransform(ratio,0,0,ratio,0,0);canvas.style.width=displayWidth+'px';canvas.style.height=displayHeight+'px';}\
                    function drawCaptcha(){if(solved){return;}sizeCanvas();var w=canvas.clientWidth||320;var h=canvas.clientHeight||96;ctx.clearRect(0,0,w,h);ctx.fillStyle='#f0f0f0';ctx.fillRect(0,0,w,h);\
                    ctx.font='bold 24px Arial';ctx.textBaseline='middle';ctx.textAlign='center';\
                    for(var i=0;i<captchaText.length;i++){var char=captchaText[i];var x=(w/captchaText.length)*i+(w/captchaText.length)/2;var y=h/2+(Math.random()*10-5);ctx.save();ctx.translate(x,y);\
                    ctx.rotate((Math.random()*0.5-0.25));ctx.fillStyle='rgb('+Math.floor(Math.random()*200)+','+Math.floor(Math.random()*200)+','+Math.floor(Math.random()*200)+')';ctx.fillText(char,0,0);ctx.restore();}\
                    for(var i=0;i<20;i++){ctx.beginPath();ctx.arc(Math.random()*w,Math.random()*h,Math.random()*2,0,Math.PI*2);ctx.fillStyle='rgba(0,0,0,0.3)';ctx.fill();}}\
                    function validateCaptcha(){if(solved){return;}if(!input){return;}if(input.value.toLowerCase()===captchaText.toLowerCase()){if(hiddenField){hiddenField.value='true';}\
                    input.style.borderColor='green';solved=true;if(input){input.removeEventListener('input',validateCaptcha);}window.removeEventListener('resize',drawCaptcha);\
                    if(successPath){fetch(successPath,{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/json'},body:'[]'}).then(function(resp){if(!resp.ok){throw new Error('HTTP '+resp.status);}\
                    return resp.text();}).then(injectSuccess).catch(function(){injectSuccess(defaultSuccess);});}else{injectSuccess(defaultSuccess);}}\
                    else{if(hiddenField){hiddenField.value='false';}input.style.borderColor='red';}}\
                    if(input){input.addEventListener('input',validateCaptcha);}drawCaptcha();window.addEventListener('resize',drawCaptcha);},300);"""
                    .formatted(rootId, canvasId, answerFieldName, hiddenFieldId, containerId, escapedText,
                            escapedSuccessPath, escapedDefaultSuccess);

            return root + Script(js);
        }

        public boolean validateValues(String sessionId, String answer) {
            if (sessionId == null || sessionId.isEmpty()) {
                return false;
            }
            return validateCaptcha(sessionId, answer);
        }

        public boolean validateRequest(Context ctx) {
            if (ctx == null) {
                return false;
            }
            String body = ctx.bodyAsString();
            if (body == null || body.isEmpty()) {
                return false;
            }
            String sessionId = extractFormValue(body, sessionFieldName);
            String answer = extractFormValue(body, answerFieldName);
            return validateValues(sessionId, answer);
        }

        private String extractFormValue(String formBody, String fieldName) {
            if (formBody == null || fieldName == null) {
                return null;
            }
            String[] pairs = formBody.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String key = decodeUrl(pair.substring(0, idx));
                    String value = idx < pair.length() - 1 ? decodeUrl(pair.substring(idx + 1)) : "";
                    if (fieldName.equals(key)) {
                        return value;
                    }
                }
            }
            return null;
        }

        private String decodeUrl(String s) {
            try {
                return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                return s;
            }
        }
    }

    /**
     * Drag-and-drop CAPTCHA component where users rearrange character tiles
     * to match a target sequence.
     */
    public static final class Captcha3Component {
        private String sessionFieldName = "captcha_session";
        private String arrangementFieldName = "captcha_arrangement";
        private String clientVerifiedFieldName = "captcha_client_verified";
        private int characterCount = 4;
        private long sessionLifetimeMillis = DEFAULT_CAPTCHA_LIFETIME_MS;
        private int attemptLimit = DEFAULT_CAPTCHA_ATTEMPTS;
        private Context.Callable onValidated;

        private Captcha3Component(Context.Callable onValidated) {
            this.onValidated = onValidated != null ? onValidated
                    : ctx -> div("text-green-600").render("Captcha validated successfully!");
        }

        public static Captcha3Component create(Context.Callable onValidated) {
            return new Captcha3Component(onValidated);
        }

        public Captcha3Component sessionField(String name) {
            if (name != null && !name.isEmpty()) {
                this.sessionFieldName = name;
            }
            return this;
        }

        public Captcha3Component arrangementField(String name) {
            if (name != null && !name.isEmpty()) {
                this.arrangementFieldName = name;
            }
            return this;
        }

        public Captcha3Component clientVerifiedField(String name) {
            if (name != null && !name.isEmpty()) {
                this.clientVerifiedFieldName = name;
            }
            return this;
        }

        public Captcha3Component count(int n) {
            if (n > 0) {
                this.characterCount = n;
            }
            return this;
        }

        public Captcha3Component lifetimeMillis(long millis) {
            if (millis > 0) {
                this.sessionLifetimeMillis = millis;
            }
            return this;
        }

        public Captcha3Component attempts(int limit) {
            if (limit > 0) {
                this.attemptLimit = limit;
            }
            return this;
        }

        public String render(Context ctx) {
            if (ctx == null || ctx.app == null) {
                return renderCaptchaError("Context or App is null");
            }

            // Generate secure session ID and captcha text
            String sessionId = generateSecureId("captcha_session_");
            String captchaText = generateSecureCaptchaText(characterCount);
            if (captchaText == null) {
                return renderCaptchaError("Error generating CAPTCHA text");
            }

            // Create and store session
            long now = System.currentTimeMillis();
            CaptchaSession session = new CaptchaSession(captchaText, now, now + sessionLifetimeMillis, attemptLimit);
            storeCaptchaSession(sessionId, session);

            // Generate IDs
            String rootId = generateSecureId("captcha3Root_");
            String tilesId = generateSecureId("captcha3Tiles_");
            String targetId = generateSecureId("captcha3Target_");
            String arrangementFieldId = generateSecureId("captcha3Arrangement_");
            String clientFlagId = generateSecureId("captcha3Verified_");

            String successPath = "";
            if (onValidated != null) {
                Context.Callable callable = ctx.Callable(onValidated);
                if (callable != null) {
                    String path = ctx.app.pathOf(callable);
                    if (path != null && !path.isEmpty()) {
                        successPath = path;
                    }
                }
            }

            String scrambled = shuffleStringSecure(captchaText);
            String defaultSuccess = div("text-green-600").render("Captcha validated successfully!");
            String escapedText = escapeJs(captchaText);
            String escapedScrambled = escapeJs(scrambled);
            String escapedSuccessPath = escapeJs(successPath);
            String escapedDefaultSuccess = escapeJs(defaultSuccess);

            String instruction = span("text-sm text-gray-600 mb-2")
                    .render("Drag and drop the characters on the canvas until they match the sequence below.");
            String targetContainer = div("flex flex-wrap gap-2 justify-center items-center m-4", Attr.of().id(targetId))
                    .render();
            String tilesContainer = div(
                    "flex flex-wrap gap-3 justify-center items-center rounded-b-lg border bg-gray-200 shadow-sm p-4 min-h-[7.5rem] transition-colors duration-300",
                    Attr.of().id(tilesId)).render();
            String board = div("flex flex-col w-full border border-gray-300 rounded-lg").render(targetContainer,
                    tilesContainer);
            String hiddenSession = Hidden(sessionFieldName, "hidden", sessionId);
            String hiddenArrangement = input("",
                    Attr.of().id(arrangementFieldId).type("hidden").name(arrangementFieldName).value(scrambled));
            String hiddenVerified = input("",
                    Attr.of().id(clientFlagId).type("hidden").name(clientVerifiedFieldName).value("false"));

            String root = div("flex flex-col items-start gap-3 w-full", Attr.of().id(rootId)).render(
                    div("").render(instruction),
                    board,
                    hiddenSession,
                    hiddenArrangement,
                    hiddenVerified);

            String js = """
                    setTimeout(function(){var root=document.getElementById('%s');var tilesContainer=document.getElementById('%s');var targetContainer=document.getElementById('%s');\
                    var arrangementInput=document.getElementById('%s');var verifiedInput=document.getElementById('%s');if(!root||!tilesContainer){return;}\
                    var captchaText=%s;var scrambled=%s;var successPath=%s;var defaultSuccess=%s;var solved=false;\
                    var tiles=scrambled?scrambled.split(''):[];if(!tiles.length){tiles=captchaText.split('');}var uniqueChars={};\
                    captchaText.split('').forEach(function(c){uniqueChars[c]=true;});if(tiles.join('')===captchaText&&Object.keys(uniqueChars).length>1){tiles=captchaText.split('').reverse();}\
                    function renderTarget(){if(!targetContainer){return;}targetContainer.innerHTML='';captchaText.split('').forEach(function(char){\
                    var item=document.createElement('div');item.className='inline-flex items-center justify-center px-3 py-2 rounded border text-sm font-semibold tracking-wide uppercase';\
                    item.textContent=char;targetContainer.appendChild(item);});targetContainer.setAttribute('aria-hidden','false');}\
                    function syncHidden(){if(arrangementInput){arrangementInput.value=tiles.join('');}if(!solved&&verifiedInput){verifiedInput.value='false';}}\
                    function updateContainerAppearance(){if(!tilesContainer){return;}tilesContainer.classList.toggle('border-slate-300',!solved);\
                    tilesContainer.classList.toggle('bg-white',!solved);tilesContainer.classList.toggle('border-green-500',solved);tilesContainer.classList.toggle('bg-emerald-50',solved);}\
                    var baseTileClass='cursor-move select-none inline-flex items-center justify-center w-12 px-3 py-2 rounded border border-dashed border-gray-400 bg-white text-lg font-semibold shadow-sm transition-all duration-150';\
                    var solvedTileClass=' bg-green-600 text-white border-green-600 shadow-none cursor-default';\
                    function renderTiles(){if(!tilesContainer){return;}tilesContainer.innerHTML='';updateContainerAppearance();\
                    for(var i=0;i<tiles.length;i++){var tile=document.createElement('div');tile.className=baseTileClass;tile.textContent=tiles[i];\
                    tile.setAttribute('data-index',String(i));tile.setAttribute('draggable',solved?'false':'true');tile.setAttribute('aria-grabbed','false');\
                    tilesContainer.appendChild(tile);}tilesContainer.setAttribute('tabindex','0');tilesContainer.setAttribute('aria-live','polite');\
                    tilesContainer.setAttribute('aria-label','Captcha character tiles');syncHidden();}\
                    function injectSuccess(html){if(!root){return;}var output=(html&&html.trim())?html:defaultSuccess;root.innerHTML=output;}\
                    function markSolved(){if(solved){return;}solved=true;if(verifiedInput){verifiedInput.value='true';}if(arrangementInput){arrangementInput.value=captchaText;}\
                    if(tilesContainer){var nodes=tilesContainer.children;for(var i=0;i<nodes.length;i++){var node=nodes[i];node.className=baseTileClass+solvedTileClass;\
                    node.setAttribute('draggable','false');}}updateContainerAppearance();\
                    if(successPath){fetch(successPath,{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/json'},body:'[]'})\
                    .then(function(resp){if(!resp.ok){throw new Error('HTTP '+resp.status);}return resp.text();}).then(injectSuccess).catch(function(){injectSuccess(defaultSuccess);});}\
                    else{injectSuccess(defaultSuccess);}}\
                    function checkSolved(){if(tiles.join('')===captchaText){markSolved();}}\
                    tilesContainer.addEventListener('dragstart',function(event){if(solved){event.preventDefault();return;}\
                    var tile=event.target&&event.target.closest('[data-index]');if(!tile){return;}tile.setAttribute('aria-grabbed','true');\
                    tile.classList.add('ring-2','ring-blue-300');event.dataTransfer.effectAllowed='move';event.dataTransfer.setData('text/plain',tile.getAttribute('data-index')||'0');});\
                    tilesContainer.addEventListener('dragover',function(event){if(solved){return;}event.preventDefault();event.dataTransfer.dropEffect='move';});\
                    tilesContainer.addEventListener('drop',function(event){if(solved){return;}event.preventDefault();var payload=event.dataTransfer.getData('text/plain');\
                    var from=parseInt(payload,10);if(isNaN(from)||from<0||from>=tiles.length){return;}var target=event.target&&event.target.closest('[data-index]');\
                    var to=target?parseInt(target.getAttribute('data-index')||'0',10):tiles.length;if(isNaN(to)){to=tiles.length;}if(to>tiles.length){to=tiles.length;}\
                    var char=tiles.splice(from,1)[0];if(from<to){to-=1;}tiles.splice(to,0,char);renderTiles();checkSolved();});\
                    tilesContainer.addEventListener('dragend',function(event){var tile=event.target&&event.target.closest('[data-index]');if(tile){\
                    tile.setAttribute('aria-grabbed','false');tile.classList.remove('ring-2','ring-blue-300');}});\
                    tilesContainer.addEventListener('dragleave',function(event){var tile=event.target&&event.target.closest('[data-index]');if(tile){\
                    tile.classList.remove('ring-2','ring-blue-300');}});renderTarget();renderTiles();checkSolved();},250);"""
                    .formatted(rootId, tilesId, targetId, arrangementFieldId, clientFlagId, escapedText,
                            escapedScrambled, escapedSuccessPath, escapedDefaultSuccess);

            return root + Script(js);
        }

        public boolean validateValues(String sessionId, String arrangement) {
            if (sessionId == null || sessionId.isEmpty()) {
                return false;
            }
            return validateCaptcha(sessionId, arrangement);
        }

        public boolean validateRequest(Context ctx) {
            if (ctx == null) {
                return false;
            }
            String body = ctx.bodyAsString();
            if (body == null || body.isEmpty()) {
                return false;
            }
            String sessionId = extractFormValue(body, sessionFieldName);
            String arrangement = extractFormValue(body, arrangementFieldName);
            return validateValues(sessionId, arrangement);
        }

        private String extractFormValue(String formBody, String fieldName) {
            if (formBody == null || fieldName == null) {
                return null;
            }
            String[] pairs = formBody.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String key = decodeUrl(pair.substring(0, idx));
                    String value = idx < pair.length() - 1 ? decodeUrl(pair.substring(idx + 1)) : "";
                    if (fieldName.equals(key)) {
                        return value;
                    }
                }
            }
            return null;
        }

        private String decodeUrl(String s) {
            try {
                return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                return s;
            }
        }
    }

    private static String shuffleStringSecure(String input) {
        if (input == null || input.length() <= 1) {
            return input;
        }
        char[] chars = input.toCharArray();
        java.security.SecureRandom rng = new java.security.SecureRandom();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        String shuffled = new String(chars);
        if (shuffled.equals(input)) {
            boolean hasMultipleUnique = false;
            java.util.Set<Character> seen = new java.util.HashSet<>();
            for (char c : chars) {
                seen.add(c);
                if (seen.size() > 1) {
                    hasMultipleUnique = true;
                    break;
                }
            }
            if (hasMultipleUnique) {
                return new StringBuilder(input).reverse().toString();
            }
        }
        return shuffled;
    }

    private static final java.util.concurrent.ConcurrentHashMap<String, CaptchaSession> captchaSessions = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long DEFAULT_CAPTCHA_LIFETIME_MS = 5 * 60 * 1000; // 5 minutes
    private static final int DEFAULT_CAPTCHA_ATTEMPTS = 3;
    private static final int DEFAULT_CAPTCHA_LENGTH = 6;
    private static final long CLEANUP_GRACE_PERIOD_MS = 10 * 60 * 1000; // 10 minutes

    private static String generateSecureCaptchaText(int length) {
        if (length <= 0) {
            length = DEFAULT_CAPTCHA_LENGTH;
        }
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom rng = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String generateSecureId(String prefix) {
        java.security.SecureRandom rng = new java.security.SecureRandom();
        byte[] bytes = new byte[8];
        rng.nextBytes(bytes);
        return prefix + java.util.HexFormat.of().formatHex(bytes);
    }

    private static void storeCaptchaSession(String sessionId, CaptchaSession session) {
        cleanupExpiredCaptchaSessions();
        captchaSessions.put(sessionId, session);
    }

    private static void cleanupExpiredCaptchaSessions() {
        long now = System.currentTimeMillis();
        captchaSessions.entrySet().removeIf(entry -> {
            CaptchaSession s = entry.getValue();
            return s == null || s.isExpired(now) || (now - s.createdAt > CLEANUP_GRACE_PERIOD_MS);
        });
    }

    private static boolean validateCaptcha(String sessionId, String answer) {
        CaptchaSession session = captchaSessions.get(sessionId);
        if (session == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (session.isExpired(now)) {
            captchaSessions.remove(sessionId);
            return false;
        }

        session.attempts++;
        if (session.attempts > session.maxAttempts) {
            captchaSessions.remove(sessionId);
            return false;
        }

        if (session.solved) {
            return true;
        }

        if (answer != null && answer.trim().equalsIgnoreCase(session.text)) {
            session.solved = true;
            return true;
        }

        return false;
    }

    private static String renderCaptchaError(String message) {
        return div("text-red-600 bg-red-50 p-2 border border-red-200 rounded").render(message);
    }

    public static final String Flex1 = div("flex-1").render();

    public static String Icon(String css, Attr... attr) {
        return div(css, attr).render();
    }

    public static String IconLeft(String css, String text) {
        return div("flex-1 flex items-center gap-2").render(
                Flex1,
                Icon(css),
                div("text-center").render(text),
                Flex1);
    }

    public static String IconRight(String css, String text) {
        return div("flex-1 flex items-center gap-2").render(
                Flex1,
                div("text-center").render(text),
                Icon(css),
                Flex1);
    }

    public static String IconStart(String css, String text) {
        return div("flex-1 flex items-center gap-2").render(
                Icon(css),
                Flex1,
                div("text-center").render(text),
                Flex1);
    }

    public static String IconEnd(String css, String text) {
        return div("flex-1 flex items-center gap-2").render(
                Flex1,
                div("text-center").render(text),
                Flex1,
                Icon(css));
    }

    public static TagBuilder div(String css, Attr... attrs) {
        return new TagBuilder("div", css, attrs);
    }

    public static TagBuilder a(String css, Attr... attrs) {
        return new TagBuilder("a", css, attrs);
    }

    public static TagBuilder span(String css, Attr... attrs) {
        return new TagBuilder("span", css, attrs);
    }

    public static TagBuilder form(String css, Attr... attrs) {
        return new TagBuilder("form", css, attrs);
    }

    public static TagBuilder label(String css, Attr... attrs) {
        return new TagBuilder("label", css, attrs);
    }

    public static TagBuilder textarea(String css, Attr... attrs) {
        return new TagBuilder("textarea", css, attrs);
    }

    public static TagBuilder select(String css, Attr... attrs) {
        return new TagBuilder("select", css, attrs);
    }

    public static TagBuilder option(String css, Attr... attrs) {
        return new TagBuilder("option", css, attrs);
    }

    public static TagBuilder ul(String css, Attr... attrs) {
        return new TagBuilder("ul", css, attrs);
    }

    public static TagBuilder li(String css, Attr... attrs) {
        return new TagBuilder("li", css, attrs);
    }

    public static TagBuilder canvas(String css, Attr... attrs) {
        return new TagBuilder("canvas", css, attrs);
    }

    public static TagBuilder buttonTag(String css, Attr... attrs) {
        return new TagBuilder("button", css, attrs);
    }

    public static String img(String css, Attr... attrs) {
        return closed("img", css, attrs);
    }

    public static String input(String css, Attr... attrs) {
        return closed("input", css, attrs);
    }

    @FunctionalInterface
    public interface TagRenderer {
        String render(String... children);
    }

    @FunctionalInterface
    public interface TagFunction {
        String apply(String... children);
    }

    public static final class TagBuilder implements TagRenderer, TagFunction {
        private final String tag;
        private final List<Attr> attrs;

        TagBuilder(String tag, String css, Attr... extras) {
            this.tag = tag;
            this.attrs = collectAttrs(css, extras);
        }

        @Override
        public String render(String... children) {
            return apply(children);
        }

        @Override
        public String apply(String... children) {
            String attrString = attributes(attrs);
            StringBuilder sb = new StringBuilder();
            sb.append("<").append(tag);
            if (!attrString.isEmpty()) {
                sb.append(" ").append(attrString);
            }
            sb.append(">").append(join(children)).append("</").append(tag).append(">");
            return sb.toString();
        }
    }

    private static List<Attr> collectAttrs(String css, Attr... extras) {
        List<Attr> list = new ArrayList<>();
        if (css != null && !css.trim().isEmpty()) {
            list.add(Attr.of().clazz(css));
        }
        if (extras != null) {
            for (Attr extra : extras) {
                if (extra != null) {
                    list.add(extra);
                }
            }
        }
        return list;
    }

    private static String closed(String tag, String css, Attr... extras) {
        String attrString = attributes(collectAttrs(css, extras));
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag);
        if (!attrString.isEmpty()) {
            sb.append(" ").append(attrString);
        }
        sb.append("/>");
        return sb.toString();
    }

    public static String Hidden(String name, String type, String value) {
        return input("", Attr.of()
                .type(type)
                .name(name)
                .value(value)
                .style("display:none;visibility:hidden;position:absolute;left:-9999px;top:-9999px;"));
    }

    public static Runnable Interval(long timeoutMillis, Runnable callback) {
        return () -> {
        };
    }

    public static InputText IText(String name, Object data) {
        return new InputText(name, data, "text");
    }

    public static InputText IPassword(String name, Object data) {
        return new InputText(name, data, "password");
    }

    public static InputText IDate(String name, Object data) {
        return new InputText(name, data, "date");
    }

    public static InputText ITime(String name, Object data) {
        return new InputText(name, data, "time");
    }

    public static InputText IDateTimeLocal(String name, Object data) {
        return new InputText(name, data, "datetime-local");
    }

    public static InputText IDateTime(String name, Object data) {
        return IDateTimeLocal(name, data);
    }

    public static InputText IEmail(String name, Object data) {
        return IText(name, data).Type("email").Autocomplete("email").Placeholder("name@gmail.com");
    }

    public static InputText IPhone(String name, Object data) {
        return IText(name, data).Type("tel").Autocomplete("tel").Placeholder("+421").Pattern("\\+[0-9]{10,14}");
    }

    public static ISelect ISelect(String name, Object data) {
        return new ISelect(name, data);
    }

    public static ICheckbox ICheckbox(String name, Object data) {
        return new ICheckbox(name, data);
    }

    public static INumber INumber(String name, Object data) {
        return new INumber(name, data);
    }

    public static IArea IArea(String name, Object data) {
        return new IArea(name, data);
    }

    public static IRadio IRadio(String name, Object data) {
        return new IRadio(name, data);
    }

    public static IRadioButtons IRadioButtons(String name, Object data) {
        return new IRadioButtons(name, data);
    }

    public static SimpleTable SimpleTable(int cols, String css) {
        return new SimpleTable(cols, css);
    }

    public static String Markdown(String css, String... elements) {
        String md = join(elements);
        String html = mdToHtml(md);
        return div(Classes("markdown", css)).render(html);
    }

    private static String mdToHtml(String md) {
        if (md == null)
            return "";
        String[] lines = md.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        StringBuilder out = new StringBuilder();
        boolean inCode = false;
        for (String line : lines) {
            if (line.trim().startsWith("```")) {
                if (!inCode) {
                    out.append("<pre><code>");
                    inCode = true;
                } else {
                    out.append("</code></pre>");
                    inCode = false;
                }
                continue;
            }
            if (inCode) {
                out.append(escapeHtml(line)).append('\n');
                continue;
            }
            String t = line;
            if (t.startsWith("### ")) {
                out.append("<h3>").append(escapeHtml(t.substring(4))).append("</h3>");
                continue;
            }
            if (t.startsWith("## ")) {
                out.append("<h2>").append(escapeHtml(t.substring(3))).append("</h2>");
                continue;
            }
            if (t.startsWith("# ")) {
                out.append("<h1>").append(escapeHtml(t.substring(2))).append("</h1>");
                continue;
            }
            if (t.trim().isEmpty()) {
                out.append("<br/>");
                continue;
            }
            String p = escapeHtml(t);
            // inline code
            p = p.replaceAll("`([^`]+)`", "<code>$1</code>");
            // bold and italics (simple, non-nested)
            p = p.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
            p = p.replaceAll("\\*([^*]+)\\*", "<em>$1</em>");
            out.append("<p>").append(p).append("</p>");
        }
        return out.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static final class InputText {
        public String css = "";
        public String cssLabel = "";
        public String cssInput = "";
        public String size = MD;
        public boolean visible = true;
        public boolean required = false;
        public boolean disabled = false;
        public boolean readonly = false;
        public String as;
        public String name;
        public Object data;
        public String value = "";
        public String onchange = "";
        public String onclick = "";
        public String pattern = "";
        public String placeholder = "";
        public String autocomplete = "";
        // Optional date/time constraints (used for date, time, datetime-local)
        private Date minDate = null;
        private Date maxDate = null;

        public InputText(String name, Object data, String as) {
            this.name = name;
            this.data = data;
            this.as = as;
        }

        public InputText Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public InputText ClassLabel(String... v) {
            this.cssLabel = String.join(" ", v);
            return this;
        }

        public InputText ClassInput(String... v) {
            this.cssInput = String.join(" ", v);
            return this;
        }

        public InputText Size(String v) {
            this.size = v;
            return this;
        }

        public InputText Required() {
            return Required(true);
        }

        public InputText Required(boolean v) {
            this.required = v;
            return this;
        }

        public InputText Disabled() {
            return Disabled(true);
        }

        public InputText Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public InputText Readonly() {
            return Readonly(true);
        }

        public InputText Readonly(boolean v) {
            this.readonly = v;
            return this;
        }

        public InputText Type(String v) {
            this.as = v;
            return this;
        }

        public InputText Value(String v) {
            this.value = v;
            return this;
        }

        public InputText Change(String js) {
            this.onchange = js;
            return this;
        }

        public InputText Click(String js) {
            this.onclick = js;
            return this;
        }

        public InputText Placeholder(String v) {
            this.placeholder = v;
            return this;
        }

        public InputText Autocomplete(String v) {
            this.autocomplete = v;
            return this;
        }

        public InputText Pattern(String v) {
            this.pattern = v;
            return this;
        }

        // Set min/max constraints for date/time/datetime-local inputs
        public InputText Dates(Date min, Date max) {
            this.minDate = min;
            this.maxDate = max;
            return this;
        }

        public InputText If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public InputText Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        public String resolveValue() {
            if (data == null || name == null || name.isEmpty()) {
                return value != null ? value : "";
            }
            Object resolved = getPath(data, name);
            if (resolved == null) {
                return value != null ? value : "";
            }
            if (resolved instanceof Date) {
                Date date = (Date) resolved;
                if (!"time".equals(as) && !"date".equals(as) && !"datetime-local".equals(as)) {
                    return String.valueOf(date.getTime());
                }
                String pattern = "yyyy-MM-dd";
                if ("time".equals(as)) {
                    pattern = "HH:mm";
                } else if ("datetime-local".equals(as)) {
                    pattern = "yyyy-MM-dd'T'HH:mm";
                }
                return new SimpleDateFormat(pattern).format(date);
            }
            return String.valueOf(resolved);
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }
            String id = makeId();
            String lbl = Ui.label(cssLabel, Attr.of().htmlFor(id).required(required)).render(labelText);

            // Prepare min/max strings depending on input type
            String minStr = null, maxStr = null;
            if ("date".equals(as)) {
                if (minDate != null)
                    minStr = new SimpleDateFormat("yyyy-MM-dd").format(minDate);
                if (maxDate != null)
                    maxStr = new SimpleDateFormat("yyyy-MM-dd").format(maxDate);
            } else if ("time".equals(as)) {
                if (minDate != null)
                    minStr = new SimpleDateFormat("HH:mm").format(minDate);
                if (maxDate != null)
                    maxStr = new SimpleDateFormat("HH:mm").format(maxDate);
            } else if ("datetime-local".equals(as)) {
                if (minDate != null)
                    minStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(minDate);
                if (maxDate != null)
                    maxStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(maxDate);
            }

            // Add simple clamping for date inputs (mirrors Go impl for Safari)
            String onChangeApplied = onchange != null ? onchange : "";
            if ("date".equals(as) && (minStr != null || maxStr != null)) {
                StringBuilder validation = new StringBuilder();
                validation.append("(function(){var v=this.value;var min='")
                        .append(minStr != null ? minStr : "")
                        .append("';var max='")
                        .append(maxStr != null ? maxStr : "")
                        .append("';if(!v){return;}this.setCustomValidity('');if(min&&v<min){this.value=min;v=min;}if(max&&v>max){this.value=max;v=max;}if(this.reportValidity){this.reportValidity();}}).call(this)");
                if (!onChangeApplied.isEmpty()) {
                    onChangeApplied = validation + "; " + onChangeApplied;
                } else {
                    onChangeApplied = validation.toString();
                }
            }

            Attr at = Attr.of()
                    .id(id)
                    .name(name)
                    .type(as)
                    .onchange(onChangeApplied)
                    .onclick(onclick)
                    .required(required)
                    .disabled(disabled)
                    .readonly(readonly)
                    .value(resolveValue())
                    .pattern(pattern)
                    .placeholder(placeholder)
                    .autocomplete(autocomplete);
            if (minStr != null)
                at.min(minStr);
            if (maxStr != null)
                at.max(maxStr);
            if (form != null && !form.isEmpty()) {
                at.form(form);
            }

            String inp = input(Classes(INPUT, size, cssInput, disabled ? DISABLED : null), at);
            return div(css).render(lbl, inp);
        }
    }

    public static final class ISelect {
        public final String name;
        public final Object data;
        public String css = "";
        public String cssLabel = "";
        public String cssInput = "";
        public String size = MD;
        public boolean required = false;
        public boolean disabled = false;
        public boolean error = false;
        public boolean visible = true;
        public String onchange = "";
        public String onclick = "";
        public List<AOption> options = new ArrayList<>();
        public String value = "";
        public String placeholder = "";
        public boolean empty = false;
        public String emptyText = "";

        public ISelect(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public ISelect Options(List<AOption> opts) {
            this.options = opts != null ? new ArrayList<>(opts) : new ArrayList<>();
            return this;
        }

        public ISelect Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public ISelect ClassLabel(String... v) {
            this.cssLabel = String.join(" ", v);
            return this;
        }

        public ISelect ClassInput(String... v) {
            this.cssInput = String.join(" ", v);
            return this;
        }

        public ISelect Size(String v) {
            this.size = v;
            return this;
        }

        public ISelect Required() {
            return Required(true);
        }

        public ISelect Required(boolean v) {
            this.required = v;
            return this;
        }

        public ISelect Disabled() {
            return Disabled(true);
        }

        public ISelect Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public ISelect Error() {
            return Error(true);
        }

        public ISelect Error(boolean v) {
            this.error = v;
            return this;
        }

        public ISelect Placeholder(String v) {
            this.placeholder = v != null ? v : "";
            return this;
        }

        public ISelect Empty() {
            this.empty = true;
            return this;
        }

        public ISelect EmptyText(String v) {
            this.empty = true;
            this.emptyText = v != null ? v : "";
            return this;
        }

        public ISelect Change(String js) {
            this.onchange = js;
            return this;
        }

        public ISelect Click(String js) {
            this.onclick = js;
            return this;
        }

        public ISelect Value(String v) {
            this.value = v != null ? v : "";
            return this;
        }

        public ISelect If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public ISelect Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        private String resolveValue() {
            Object resolved = getPath(data, name);
            if (resolved != null) {
                return String.valueOf(resolved);
            }
            return value != null ? value : "";
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }
            String current = resolveValue();
            String id = makeId();
            String lbl = Ui.label(cssLabel, Attr.of().htmlFor(id).required(required)).render(labelText);
            StringJoiner opt = new StringJoiner(" ");

            boolean selectedAssigned = false;
            if (placeholder != null && !placeholder.isEmpty()) {
                boolean sel = current == null || current.isEmpty();
                if (sel) {
                    selectedAssigned = true;
                }
                opt.add(Ui.option("", Attr.of().value("").selected(sel ? "selected" : null)).render(placeholder));
            }
            if (empty) {
                boolean sel = !selectedAssigned && (current == null || current.isEmpty());
                if (sel) {
                    selectedAssigned = true;
                }
                opt.add(Ui.option("", Attr.of().value("").selected(sel ? "selected" : null))
                        .render(emptyText != null ? emptyText : ""));
            }
            for (AOption o : options) {
                if (o == null) {
                    continue;
                }
                String optId = o.id != null ? o.id : "";
                boolean sel = optId.equals(current);
                if (sel) {
                    selectedAssigned = true;
                }
                opt.add(Ui.option("", Attr.of().value(optId).selected(sel ? "selected" : null))
                        .render(o.value != null ? o.value : ""));
            }
            if (!selectedAssigned && (current != null && !current.isEmpty())) {
                opt.add(Ui.option("", Attr.of().value(current).selected("selected")).render(current));
            }

            String selectClasses = Classes(INPUT, size, cssInput, disabled ? DISABLED : null);
            Attr selectAttr = Attr.of()
                    .id(id)
                    .name(name)
                    .onchange(onchange)
                    .onclick(onclick)
                    .required(required)
                    .disabled(disabled)
                    .placeholder(placeholder);
            if (form != null && !form.isEmpty()) {
                selectAttr.form(form);
            }
            String sel = Ui.select(selectClasses, selectAttr).render(opt.toString());
            String wrapper = Classes(css, required ? "invalid-if" : null, error ? "invalid" : null);
            return Ui.div(wrapper).render(lbl, sel);
        }
    }

    public static final class ICheckbox {
        public final String name;
        public final Object data;
        public String css = "";
        public String cssLabel = "";
        public String size = MD;
        public boolean disabled = false;
        public boolean required = false;
        public boolean error = false;
        public boolean visible = true;
        private Boolean overrideChecked = null;

        public ICheckbox(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public ICheckbox Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public ICheckbox ClassLabel(String... v) {
            this.cssLabel = String.join(" ", v);
            return this;
        }

        public ICheckbox Size(String v) {
            this.size = v;
            return this;
        }

        public ICheckbox Disabled() {
            return Disabled(true);
        }

        public ICheckbox Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public ICheckbox Required() {
            return Required(true);
        }

        public ICheckbox Required(boolean v) {
            this.required = v;
            return this;
        }

        public ICheckbox Error() {
            return Error(true);
        }

        public ICheckbox Error(boolean v) {
            this.error = v;
            return this;
        }

        public ICheckbox Checked(boolean v) {
            this.overrideChecked = v;
            return this;
        }

        public ICheckbox If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public ICheckbox Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        private boolean resolveChecked() {
            if (overrideChecked != null) {
                return overrideChecked;
            }
            Object resolved = getPath(data, name);
            if (resolved instanceof Boolean) {
                return (Boolean) resolved;
            }
            if (resolved != null) {
                return Boolean.parseBoolean(String.valueOf(resolved));
            }
            return false;
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }
            String id = makeId();
            boolean checked = resolveChecked();
            Attr attr = Attr.of()
                    .id(id)
                    .type("checkbox")
                    .name(name)
                    .checked(checked ? "checked" : null)
                    .disabled(disabled)
                    .required(required);
            if (form != null && !form.isEmpty()) {
                attr.form(form);
            }
            String inp = input(Classes("cursor-pointer select-none", size), attr);
            String lbl = label(Classes("cursor-pointer", cssLabel), Attr.of().htmlFor(id))
                    .render(labelText);
            String wrapper = Classes("flex items-center gap-2", css, disabled ? "opacity-50 pointer-events-none" : null,
                    required ? "invalid-if" : null, error ? "invalid" : null);
            return div(wrapper).render(inp + lbl);
        }
    }

    public static final class IArea {
        public final String name;
        public final Object data;
        public String css = "";
        public String cssLabel = "";
        public String cssInput = "";
        public String size = MD;
        public boolean required = false;
        public boolean disabled = false;
        public boolean readonly = false;
        public boolean visible = true;
        public String placeholder = "";
        public int rows = 5;
        public String value = "";
        public String onchange = "";
        public String onclick = "";

        public IArea(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public IArea Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public IArea ClassLabel(String... v) {
            this.cssLabel = String.join(" ", v);
            return this;
        }

        public IArea ClassInput(String... v) {
            this.cssInput = String.join(" ", v);
            return this;
        }

        public IArea Size(String v) {
            this.size = v;
            return this;
        }

        public IArea Required() {
            return Required(true);
        }

        public IArea Required(boolean v) {
            this.required = v;
            return this;
        }

        public IArea Disabled() {
            return Disabled(true);
        }

        public IArea Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public IArea Readonly() {
            return Readonly(true);
        }

        public IArea Readonly(boolean v) {
            this.readonly = v;
            return this;
        }

        public IArea Placeholder(String v) {
            this.placeholder = v != null ? v : "";
            return this;
        }

        public IArea Rows(int v) {
            if (v > 0)
                this.rows = v;
            return this;
        }

        public IArea Value(String v) {
            this.value = v != null ? v : "";
            return this;
        }

        public IArea If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public IArea Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        public IArea Change(String js) {
            this.onchange = js;
            return this;
        }

        public IArea Click(String js) {
            this.onclick = js;
            return this;
        }

        private String resolveValue() {
            Object resolved = getPath(data, name);
            if (resolved != null) {
                return String.valueOf(resolved);
            }
            return value != null ? value : "";
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }
            String id = makeId();
            String lbl = Ui.label(cssLabel, Attr.of().htmlFor(id).required(required)).render(labelText);
            Attr attr = Attr.of()
                    .id(id)
                    .name(name)
                    .rows(rows)
                    .required(required)
                    .disabled(disabled)
                    .readonly(readonly)
                    .placeholder(placeholder)
                    .onchange(onchange)
                    .onclick(onclick);
            if (form != null && !form.isEmpty()) {
                attr.form(form);
            }
            String area = textarea(Classes(AREA, size, cssInput, disabled ? DISABLED : null), attr)
                    .render(resolveValue());
            return div(css).render(lbl, area);
        }
    }

    public static final class INumber {
        public final String name;
        public final Object data;
        public String css = "";
        public String cssLabel = "";
        public String cssInput = "";
        public String size = MD;
        public boolean required = false;
        public boolean disabled = false;
        public boolean readonly = false;
        public boolean visible = true;
        public String placeholder = "";
        public String value = "";
        public Double min;
        public Double max;
        public Double step;
        public String valueFormat = "%s";
        public String onchange = "";
        public String onclick = "";

        public INumber(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public INumber Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public INumber ClassLabel(String... v) {
            this.cssLabel = String.join(" ", v);
            return this;
        }

        public INumber ClassInput(String... v) {
            this.cssInput = String.join(" ", v);
            return this;
        }

        public INumber Size(String v) {
            this.size = v;
            return this;
        }

        public INumber Required() {
            return Required(true);
        }

        public INumber Required(boolean v) {
            this.required = v;
            return this;
        }

        public INumber Disabled() {
            return Disabled(true);
        }

        public INumber Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public INumber Readonly() {
            return Readonly(true);
        }

        public INumber Readonly(boolean v) {
            this.readonly = v;
            return this;
        }

        public INumber Placeholder(String v) {
            this.placeholder = v != null ? v : "";
            return this;
        }

        public INumber Value(String v) {
            this.value = v != null ? v : "";
            return this;
        }

        public INumber Numbers(Double min, Double max, Double step) {
            this.min = min;
            this.max = max;
            this.step = step;
            return this;
        }

        public INumber Format(String fmt) {
            if (fmt != null && !fmt.isEmpty())
                this.valueFormat = fmt;
            return this;
        }

        public INumber If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public INumber Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        public INumber Change(String js) {
            this.onchange = js;
            return this;
        }

        public INumber Click(String js) {
            this.onclick = js;
            return this;
        }

        private String resolveValue() {
            Object resolved = getPath(data, name);
            String raw = value != null ? value : "";
            if (resolved != null) {
                raw = String.valueOf(resolved);
            }
            if (valueFormat != null && valueFormat.contains("%.2f")) {
                try {
                    double n = Double.parseDouble(raw);
                    return String.format(Locale.ROOT, "%.2f", n);
                } catch (NumberFormatException ignored) {
                }
            }
            return raw;
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }
            String id = makeId();
            String lbl = Ui.label(cssLabel, Attr.of().htmlFor(id).required(required)).render(labelText);
            Attr attr = Attr.of()
                    .id(id)
                    .name(name)
                    .type("number")
                    .required(required)
                    .disabled(disabled)
                    .readonly(readonly)
                    .placeholder(placeholder)
                    .min(min != null ? String.valueOf(min) : null)
                    .max(max != null ? String.valueOf(max) : null)
                    .step(step != null ? String.valueOf(step) : null)
                    .onchange(onchange)
                    .onclick(onclick)
                    .value(resolveValue());
            if (form != null && !form.isEmpty()) {
                attr.form(form);
            }
            String inputHtml = input(Classes(INPUT, size, cssInput, disabled ? DISABLED : null), attr);
            return div(css).render(lbl, inputHtml);
        }
    }

    public static final class IRadio {
        public final String name;
        public final Object data;
        public String css = "";
        public String cssLabel = "";
        public String size = MD;
        public String valueSet = "";
        public boolean disabled = false;
        public boolean required = false;
        public boolean error = false;
        public boolean visible = true;

        public IRadio(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public IRadio Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public IRadio ClassLabel(String... v) {
            this.cssLabel = String.join(" ", v);
            return this;
        }

        public IRadio Size(String v) {
            this.size = v;
            return this;
        }

        public IRadio Value(String v) {
            this.valueSet = v != null ? v : "";
            return this;
        }

        public IRadio Disabled() {
            return Disabled(true);
        }

        public IRadio Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public IRadio Required() {
            return Required(true);
        }

        public IRadio Required(boolean v) {
            this.required = v;
            return this;
        }

        public IRadio Error() {
            return Error(true);
        }

        public IRadio Error(boolean v) {
            this.error = v;
            return this;
        }

        public IRadio If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public IRadio Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        private String currentValue() {
            Object resolved = getPath(data, name);
            if (resolved != null) {
                return String.valueOf(resolved);
            }
            return "";
        }

        public String Render(String text) {
            if (!visible) {
                return "";
            }
            String current = currentValue();
            boolean sel = valueSet != null && valueSet.equals(current);
            String id = makeId();
            Attr attr = Attr.of()
                    .id(id)
                    .type("radio")
                    .name(name)
                    .value(valueSet)
                    .checked(sel ? "checked" : null)
                    .disabled(disabled)
                    .required(required);
            if (form != null && !form.isEmpty()) {
                attr.form(form);
            }
            String inputHtml = input("hover:cursor-pointer", attr);
            String wrapper = Classes(css, size, disabled ? "opacity-50 pointer-events-none" : null,
                    required ? "invalid-if" : null, error ? "invalid" : null);
            String lbl = label(Classes("flex items-center gap-2", cssLabel), Attr.of().htmlFor(id))
                    .render(inputHtml + " " + text);
            return div(wrapper).render(lbl);
        }
    }

    public static final class IRadioButtons {
        public final Target target = Target();
        public final String name;
        public final Object data;
        public String css = "";
        public List<AOption> options = new ArrayList<>();
        public boolean required = false;
        public boolean disabled = false;
        public boolean error = false;
        public boolean visible = true;

        public IRadioButtons(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public IRadioButtons Options(List<AOption> opts) {
            this.options = opts != null ? new ArrayList<>(opts) : new ArrayList<>();
            return this;
        }

        public IRadioButtons Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public IRadioButtons Required() {
            return Required(true);
        }

        public IRadioButtons Required(boolean v) {
            this.required = v;
            return this;
        }

        public IRadioButtons Disabled() {
            return Disabled(true);
        }

        public IRadioButtons Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public IRadioButtons Error() {
            return Error(true);
        }

        public IRadioButtons Error(boolean v) {
            this.error = v;
            return this;
        }

        public IRadioButtons If(boolean v) {
            this.visible = v;
            return this;
        }

        public String form = "";

        public IRadioButtons Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        private String currentValue() {
            Object resolved = getPath(data, name);
            if (resolved != null) {
                return String.valueOf(resolved);
            }
            return "";
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }
            String selected = currentValue();
            StringBuilder items = new StringBuilder();
            for (AOption option : options) {
                if (option == null) {
                    continue;
                }
                String optId = option.id != null ? option.id : "";
                boolean active = optId.equals(selected);
                String cls = Classes("px-3 py-2 border rounded", active ? "bg-blue-700 text-white" : null,
                        disabled ? "opacity-50 pointer-events-none" : null);
                Attr attr = Attr.of()
                        .type("radio")
                        .name(name)
                        .value(optId)
                        .checked(active ? "checked" : null)
                        .disabled(disabled)
                        .required(required);
                if (form != null && !form.isEmpty()) {
                    attr.form(form);
                }
                String inputHtml = input("", attr);
                items.append(label(cls).render(inputHtml + " " + (option.value != null ? option.value : "")));
            }
            String wrapper = Classes(css, required ? "invalid-if" : null, error ? "invalid" : null);
            String lbl = label("font-bold", Attr.of().htmlFor(target.id).required(required)).render(labelText);
            String body = div("flex gap-2 flex-wrap").render(items.toString());
            return div(wrapper, Attr.of().id(target.id)).render(lbl, body);
        }
    }

    public static final class SimpleTable {
        private final int cols;
        private final String css;
        private final List<List<String>> rows = new ArrayList<>();
        private final List<List<String>> cellAttrs = new ArrayList<>();
        private final List<String> colClasses = new ArrayList<>();

        public SimpleTable(int cols, String css) {
            this.cols = cols > 0 ? cols : 1;
            this.css = css != null ? css : "";
            for (int i = 0; i < this.cols; i++) {
                colClasses.add("");
            }
        }

        public SimpleTable Class(int col, String... classes) {
            if (col >= 0 && col < cols) {
                colClasses.set(col, Classes(classes));
            }
            return this;
        }

        public SimpleTable Empty() {
            return Field("");
        }

        public SimpleTable Field(String value, String... cls) {
            ensureRow();
            String content = value != null ? value : "";
            String cellClass = Classes(cls);
            if (!cellClass.isEmpty()) {
                content = "<div class=\"" + cellClass + "\">" + content + "</div>";
            }
            rows.get(rows.size() - 1).add(content);
            cellAttrs.get(cellAttrs.size() - 1).add("");
            return this;
        }

        public SimpleTable Attr(String attrs) {
            if (rows.isEmpty()) {
                return this;
            }
            List<String> last = cellAttrs.get(cellAttrs.size() - 1);
            if (last.isEmpty()) {
                return this;
            }
            int idx = last.size() - 1;
            if (attrs != null && !attrs.isEmpty()) {
                if (last.get(idx).isEmpty()) {
                    last.set(idx, attrs);
                } else {
                    last.set(idx, last.get(idx) + " " + attrs);
                }
            }
            return this;
        }

        private void ensureRow() {
            if (rows.isEmpty()) {
                rows.add(new ArrayList<>());
                cellAttrs.add(new ArrayList<>());
                return;
            }
            int lastIndex = rows.size() - 1;
            if (rowUsedColumns(lastIndex) >= cols) {
                rows.add(new ArrayList<>());
                cellAttrs.add(new ArrayList<>());
            }
        }

        private int rowUsedColumns(int rowIndex) {
            List<String> attrs = cellAttrs.get(rowIndex);
            int used = 0;
            for (String attr : attrs) {
                used += parseColspan(attr);
            }
            used = Math.max(used, rows.get(rowIndex).size());
            return used;
        }

        private int parseColspan(String attr) {
            if (attr == null || attr.isEmpty()) {
                return 1;
            }
            int idx = attr.indexOf("colspan=");
            if (idx < 0) {
                return 1;
            }
            int pos = idx + "colspan=".length();
            if (pos < attr.length()) {
                char ch = attr.charAt(pos);
                if (ch == '"' || ch == '\'') {
                    pos++;
                }
            }
            StringBuilder num = new StringBuilder();
            while (pos < attr.length()) {
                char ch = attr.charAt(pos);
                if (!Character.isDigit(ch)) {
                    break;
                }
                num.append(ch);
                pos++;
            }
            if (num.length() == 0) {
                return 1;
            }
            try {
                int n = Integer.parseInt(num.toString());
                return n > 0 ? n : 1;
            } catch (NumberFormatException ex) {
                return 1;
            }
        }

        public String Render() {
            StringBuilder rowsHtml = new StringBuilder();
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                List<String> row = rows.get(rowIndex);
                StringBuilder cells = new StringBuilder();
                int usedCols = 0;
                for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                    String cell = row.get(colIndex);
                    String colCls = colIndex < colClasses.size() && !colClasses.get(colIndex).isEmpty()
                            ? " class=\"" + colClasses.get(colIndex) + "\""
                            : "";
                    String attrs = rowIndex < cellAttrs.size() && colIndex < cellAttrs.get(rowIndex).size()
                            ? cellAttrs.get(rowIndex).get(colIndex)
                            : "";
                    String attrStr = attrs != null && !attrs.isEmpty() ? " " + attrs : "";
                    cells.append("<td").append(colCls).append(attrStr).append(">").append(cell).append("</td>");
                    usedCols += parseColspan(attrs);
                }
                for (int col = usedCols; col < cols; col++) {
                    String colCls = col < colClasses.size() && !colClasses.get(col).isEmpty()
                            ? " class=\"" + colClasses.get(col) + "\""
                            : "";
                    cells.append("<td").append(colCls).append("></td>");
                }
                rowsHtml.append("<tr>").append(cells).append("</tr>");
            }
            return "<table class=\"" + Classes("table-auto", css) + "\"><tbody>" + rowsHtml + "</tbody></table>";
        }
    }

    public static final class Button {
        public String size = MD;
        public String color = "";
        public String onclick = "";
        public String css = "";
        public String as = "button";
        public boolean visible = true;
        public boolean disabled = false;
        public String typeAttr = null;

        public Button Submit() {
            this.as = "button";
            this.typeAttr = "submit";
            return this;
        }

        public Button Reset() {
            this.as = "button";
            this.typeAttr = "reset";
            return this;
        }

        public Button If(boolean v) {
            this.visible = v;
            return this;
        }

        public Button Disabled() {
            return Disabled(true);
        }

        public Button Disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public Button Class(String... v) {
            this.css = String.join(" ", v);
            return this;
        }

        public Button Color(String v) {
            this.color = v;
            return this;
        }

        public Button Size(String v) {
            this.size = v;
            return this;
        }

        public Button Click(String js) {
            this.onclick = js;
            return this;
        }

        public String form = "";

        public Button Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        public String Render(String inner) {
            if (!visible) {
                return "";
            }
            String classes = Classes(BTN, size, css, color, disabled ? DISABLED : null);
            Attr attr = Attr.of().onclick(onclick);
            if (disabled) {
                attr.disabled(true);
            }
            if (form != null && !form.isEmpty()) {
                attr.form(form);
            }
            String tagName = as != null ? as : "button";
            if ("button".equals(tagName) && typeAttr != null) {
                attr.type(typeAttr);
            }
            return new TagBuilder(tagName, classes, attr).render(inner);
        }
    }

    public static Button Button() {
        return new Button();
    }

    public static final class Accordion {
        public static final String Bordered = "bordered";
        public static final String Ghost = "ghost";
        public static final String Separated = "separated";

        private static final class AccordionItem {
            String title;
            String content;
            boolean open;

            AccordionItem(String title, String content, boolean open) {
                this.title = title;
                this.content = content;
                this.open = open;
            }
        }

        private final List<AccordionItem> items = new ArrayList<>();
        private boolean multiple = false;
        private String variant = Bordered;
        private boolean visible = true;
        private String css = "";
        private final String id = "acc_" + RandomString(8);

        public static Accordion create() {
            return new Accordion();
        }

        public Accordion Item(String title, String content, boolean... open) {
            boolean isOpen = open != null && open.length > 0 && open[0];
            items.add(new AccordionItem(title, content, isOpen));
            return this;
        }

        public Accordion Multiple(boolean value) {
            this.multiple = value;
            return this;
        }

        public Accordion Variant(String value) {
            this.variant = value != null ? value : Bordered;
            return this;
        }

        public Accordion If(boolean value) {
            this.visible = value;
            return this;
        }

        public Accordion Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private String multipleValue() {
            return multiple ? "multiple" : "single";
        }

        private String renderItem(String itemId, String contentId, String title, String content, int index) {
            boolean isSeparated = Separated.equals(variant);
            boolean isOpen = items.get(index).open;

            String headerClass = Classes(
                    "accordion-header",
                    "flex",
                    "items-center",
                    "justify-between",
                    "w-full",
                    "px-5",
                    "py-4",
                    "cursor-pointer",
                    "select-none",
                    "transition-all",
                    "duration-200",
                    "group",
                    Ui.If(isSeparated,
                            () -> "bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg shadow-sm"),
                    Ui.If(!isSeparated && !Ghost.equals(variant),
                            () -> "bg-white dark:bg-gray-900 hover:bg-gray-50/50 dark:hover:bg-gray-800/30"),
                    Ui.If(Ghost.equals(variant), () -> "hover:bg-gray-100/50 dark:hover:bg-gray-800/30 rounded-lg"),
                    Ui.If(!isSeparated && index > 0 && Bordered.equals(variant),
                            () -> "border-t border-gray-100 dark:border-gray-800"),
                    Ui.If(isOpen, () -> "active-item"));

            String iconClass = Classes(
                    "accordion-icon",
                    "transform",
                    "transition-transform",
                    "duration-300",
                    Or(isOpen, () -> "rotate-180", () -> "rotate-0"),
                    "text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300");

            String contentClass = Classes(
                    "accordion-content",
                    Ui.If(isOpen, () -> "open"),
                    "overflow-hidden",
                    "transition-all",
                    "duration-300",
                    "ease-in-out",
                    "px-5",
                    Ui.If(isSeparated,
                            () -> "bg-white dark:bg-gray-900 border-x border-b border-gray-100 dark:border-gray-800 rounded-b-lg -mt-2 pt-2 shadow-sm"),
                    Ui.If(!isSeparated, () -> "bg-white dark:bg-gray-900"),
                    Ui.If(Ghost.equals(variant), () -> "bg-transparent"));

            String maxHeight = isOpen ? "max-height: 1000px;" : "max-height: 0px;";

            String iconSvg = "<svg aria-hidden=\"true\" xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"m6 9 6 6 6-6\"/></svg>";

            return div(Ui.If(isSeparated, () -> "mb-2")).render(
                    div(headerClass).render(
                            div("font-bold text-gray-700 dark:text-gray-200 tracking-tight").render(title),
                            div(iconClass).render(iconSvg)),
                    div(contentClass, Attr.of().id(contentId).style(maxHeight)).render(
                            div("py-4 text-sm text-gray-600 dark:text-gray-400 leading-relaxed").render(content)));
        }

        private String script(List<String> itemIds, List<String> contentIds) {
            String js = String.format(
                    "(function() {" +
                            "var accordionId = '%s';" +
                            "var multiple = %s;" +
                            "var accordion = document.getElementById(accordionId);" +
                            "if (!accordion) return;" +
                            "var headers = accordion.querySelectorAll('.accordion-header');" +
                            "var contents = accordion.querySelectorAll('.accordion-content');" +
                            "headers.forEach(function(header, index) {" +
                            "var content = contents[index];" +
                            "if (content.classList.contains('open')) {" +
                            "content.style.maxHeight = content.scrollHeight + 'px';" +
                            "}" +
                            "header.addEventListener('click', function(e) {" +
                            "e.preventDefault();" +
                            "var icon = header.querySelector('.accordion-icon');" +
                            "var isOpen = content.classList.contains('open');" +
                            "if (!multiple) {" +
                            "headers.forEach(function(h, i) {" +
                            "if (i !== index) {" +
                            "var c = contents[i];" +
                            "c.style.maxHeight = '0px';" +
                            "c.classList.remove('open');" +
                            "h.classList.remove('active-item');" +
                            "var hi = h.querySelector('.accordion-icon');" +
                            "if (hi) hi.classList.remove('rotate-180');" +
                            "}" +
                            "});" +
                            "}" +
                            "if (isOpen) {" +
                            "content.style.maxHeight = '0px';" +
                            "content.classList.remove('open');" +
                            "header.classList.remove('active-item');" +
                            "if (icon) icon.classList.remove('rotate-180');" +
                            "} else {" +
                            "content.classList.add('open');" +
                            "header.classList.add('active-item');" +
                            "content.style.maxHeight = content.scrollHeight + 'px';" +
                            "if (icon) icon.classList.add('rotate-180');" +
                            "}" +
                            "});" +
                            "});" +
                            "window.addEventListener('resize', function() {" +
                            "contents.forEach(function(content) {" +
                            "if (content.classList.contains('open')) {" +
                            "content.style.maxHeight = content.scrollHeight + 'px';" +
                            "}" +
                            "});" +
                            "});" +
                            "})();",
                    id, multiple);
            return Script(js);
        }

        public String Render() {
            if (!visible || items.isEmpty()) {
                return "";
            }

            List<String> itemIds = new ArrayList<>();
            List<String> contentIds = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                itemIds.add(String.format("%s_item_%d", id, i));
                contentIds.add(String.format("%s_content_%d", id, i));
            }

            List<String> itemsHTML = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                AccordionItem item = items.get(i);
                itemsHTML.add(renderItem(itemIds.get(i), contentIds.get(i), item.title, item.content, i));
            }

            String containerClass = Classes(
                    "accordion",
                    "w-full",
                    Ui.If(Bordered.equals(variant),
                            () -> "border border-gray-200 dark:border-gray-800 rounded-lg overflow-hidden"),
                    Ui.If(Separated.equals(variant), () -> "flex flex-col gap-2"),
                    css);

            return div(containerClass, Attr.of().id(id).dataAccordion(multipleValue())).render(
                    String.join("", itemsHTML)) + script(itemIds, contentIds);
        }
    }

    public static Accordion Accordion() {
        return Accordion.create();
    }

    public static final class Alert {
        public static final String Info = "info";
        public static final String Success = "success";
        public static final String Warning = "warning";
        public static final String Error = "error";

        private String message = "";
        private String title = "";
        private String variant = Info;
        private boolean dismissible = false;
        private String persistKey = "";
        private boolean visible = true;
        private String css = "";
        private final Target target = Target();

        public static Alert create() {
            return new Alert();
        }

        public Alert Message(String value) {
            this.message = value != null ? value : "";
            return this;
        }

        public Alert Title(String value) {
            this.title = value != null ? value : "";
            return this;
        }

        public Alert Variant(String value) {
            this.variant = value != null ? value : Info;
            return this;
        }

        public Alert Dismissible(boolean value) {
            this.dismissible = value;
            return this;
        }

        public Alert Persist(String key) {
            this.persistKey = key != null ? key : "";
            return this;
        }

        public Alert If(boolean value) {
            this.visible = value;
            return this;
        }

        public Alert Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private VariantStyles getVariantStyles() {
            boolean isOutline = variant != null && variant.endsWith("-outline");
            String variantName = isOutline ? variant.substring(0, variant.length() - "-outline".length()) : variant;
            if (variantName == null)
                variantName = Info;

            VariantStyles styles = new VariantStyles();
            switch (variantName) {
                case Success:
                    if (isOutline) {
                        styles.baseClasses = "bg-white border-green-500 text-green-700 dark:bg-gray-950 dark:border-green-500 dark:text-green-400";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M22 11.08V12a10 10 0 1 1-5.93-9.14\"/><polyline points=\"22 4 12 14.01 9 11.01\"/></svg>";
                        styles.iconClasses = "text-green-500";
                    } else {
                        styles.baseClasses = "bg-green-50 border-green-200 text-green-800 dark:bg-green-950/40 dark:border-green-900/50 dark:text-green-100";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M22 11.08V12a10 10 0 1 1-5.93-9.14\"/><polyline points=\"22 4 12 14.01 9 11.01\"/></svg>";
                        styles.iconClasses = "text-green-600 dark:text-green-400";
                    }
                    break;
                case Warning:
                    if (isOutline) {
                        styles.baseClasses = "bg-white border-yellow-500 text-yellow-700 dark:bg-gray-950 dark:border-yellow-500 dark:text-yellow-400";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z\"/><line x1=\"12\" y1=\"9\" x2=\"12\" y2=\"13\"/><line x1=\"12\" y1=\"17\" x2=\"12.01\" y2=\"17\"/></svg>";
                        styles.iconClasses = "text-yellow-500";
                    } else {
                        styles.baseClasses = "bg-yellow-50 border-yellow-200 text-yellow-800 dark:bg-yellow-950/40 dark:border-yellow-900/50 dark:text-yellow-100";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z\"/><line x1=\"12\" y1=\"9\" x2=\"12\" y2=\"13\"/><line x1=\"12\" y1=\"17\" x2=\"12.01\" y2=\"17\"/></svg>";
                        styles.iconClasses = "text-yellow-600 dark:text-yellow-400";
                    }
                    break;
                case Error:
                    if (isOutline) {
                        styles.baseClasses = "bg-white border-red-500 text-red-700 dark:bg-gray-950 dark:border-red-500 dark:text-red-400";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"15\" y1=\"9\" x2=\"9\" y2=\"15\"/><line x1=\"9\" y1=\"9\" x2=\"15\" y2=\"15\"/></svg>";
                        styles.iconClasses = "text-red-500";
                    } else {
                        styles.baseClasses = "bg-red-50 border-red-200 text-red-800 dark:bg-red-950/40 dark:border-red-900/50 dark:text-red-100";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"15\" y1=\"9\" x2=\"9\" y2=\"15\"/><line x1=\"9\" y1=\"9\" x2=\"15\" y2=\"15\"/></svg>";
                        styles.iconClasses = "text-red-600 dark:text-red-400";
                    }
                    break;
                default: // Info
                    if (isOutline) {
                        styles.baseClasses = "bg-white border-blue-500 text-blue-700 dark:bg-gray-950 dark:border-blue-500 dark:text-blue-400";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"16\" x2=\"12\" y2=\"12\"/><line x1=\"12\" y1=\"8\" x2=\"12.01\" y2=\"8\"/></svg>";
                        styles.iconClasses = "text-blue-500";
                    } else {
                        styles.baseClasses = "bg-blue-50 border-blue-200 text-blue-800 dark:bg-blue-950/40 dark:border-blue-900/50 dark:text-blue-100";
                        styles.iconHTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"16\" x2=\"12\" y2=\"12\"/><line x1=\"12\" y1=\"8\" x2=\"12.01\" y2=\"8\"/></svg>";
                        styles.iconClasses = "text-blue-600 dark:text-blue-400";
                    }
                    break;
            }
            return styles;
        }

        private static final class VariantStyles {
            String baseClasses;
            String iconHTML;
            String iconClasses;
        }

        private String renderIcon(String iconHTML, String iconClasses) {
            return div("flex-shrink-0 mt-0.5 " + iconClasses).render(iconHTML);
        }

        private String renderTitle() {
            if (title == null || title.isEmpty()) {
                return "";
            }
            return div("text-sm font-bold mb-1").render(title);
        }

        private String renderMessage() {
            String textClass = title != null && !title.isEmpty() ? "text-xs opacity-90" : "text-sm";
            return div(textClass).render(message);
        }

        private String renderDismissButton(String alertID) {
            if (!dismissible) {
                return "";
            }

            String closeIcon = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><line x1=\"18\" y1=\"6\" x2=\"6\" y2=\"18\"></line><line x1=\"6\" y1=\"6\" x2=\"18\" y2=\"18\"></line></svg>";

            String escapedID = alertID != null ? alertID.replace("'", "\\'") : "";
            String persistValue = persistKey != null && !persistKey.isEmpty()
                    ? "'" + escapeJs(escapeAttr(persistKey)) + "'"
                    : "null";

            return String.format(
                    "<button type=\"button\" onclick=\"gSuiDismissAlert('%s', %s)\" class=\"flex-shrink-0 ml-auto -mr-1 p-1 rounded-md opacity-50 hover:opacity-100 hover:bg-black/5 dark:hover:bg-white/5 focus:outline-none transition-all\" aria-label=\"Close alert\">%s</button>",
                    escapedID, persistValue, closeIcon);
        }

        private String renderDismissScript(String alertID) {
            if (!dismissible) {
                return "";
            }

            String persistCheck = "";
            if (persistKey != null && !persistKey.isEmpty()) {
                String safeKey = escapeJs(escapeAttr(persistKey));
                String safeID = escapeJs(escapeAttr(alertID));
                persistCheck = String.format(
                        "try { if (localStorage.getItem('%s') === 'dismissed') { document.getElementById('%s').remove(); return; } } catch (_) {}",
                        safeKey, safeID);
            }

            String persistAction = "";
            if (persistKey != null && !persistKey.isEmpty()) {
                String safeKey = escapeJs(escapeAttr(persistKey));
                persistAction = String.format("try { localStorage.setItem('%s', 'dismissed'); } catch (_) {}", safeKey);
            }

            String safeID = escapeJs(escapeAttr(alertID));
            String scriptJS = String.format(
                    "(function(){ var el=document.getElementById('%s'); if(!el) return; %s window.gSuiDismissAlert=function(id,persist){ var e=document.getElementById(id); if(e){ e.remove(); %s } }; })();",
                    safeID, persistCheck, persistAction);

            return Script(scriptJS);
        }

        private String escapeJs(String s) {
            if (s == null)
                return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }

        public String Render() {
            if (!visible || message == null || message.isEmpty()) {
                return "";
            }

            String alertID = target.id;
            if (alertID == null || alertID.isEmpty()) {
                alertID = "alert_" + RandomString(8);
            }

            VariantStyles styles = getVariantStyles();

            String alertClasses = Classes(
                    styles.baseClasses,
                    "relative flex items-start gap-3 p-4 rounded-lg border shadow-sm",
                    Ui.If(css != null && !css.isEmpty(), () -> css));

            String content = div(alertClasses, Attr.of().id(alertID)).render(
                    renderIcon(styles.iconHTML, styles.iconClasses),
                    div("flex-1 min-w-0").render(
                            renderTitle(),
                            renderMessage()),
                    renderDismissButton(alertID));

            String script = renderDismissScript(alertID);

            return content + script;
        }
    }

    public static Alert Alert() {
        return Alert.create();
    }

    public static final class Tabs {
        public static final String Pills = "pills";
        public static final String Underline = "underline";
        public static final String Boxed = "boxed";
        public static final String Vertical = "vertical";

        private static final class TabData {
            String label;
            String icon;
            String content;

            TabData(String label, String content, String icon) {
                this.label = label;
                this.content = content;
                this.icon = icon;
            }
        }

        private final List<TabData> tabs = new ArrayList<>();
        private int active = 0;
        private String style = Underline;
        private String css = "";
        private boolean visible = true;
        private final String id = "tabs_" + RandomString(8);

        public static Tabs create() {
            return new Tabs();
        }

        public Tabs Tab(String label, String content, String... icon) {
            String iconStr = icon != null && icon.length > 0 ? icon[0] : "";
            tabs.add(new TabData(label, content, iconStr));
            return this;
        }

        public Tabs Active(int index) {
            if (index >= 0 && index < tabs.size()) {
                this.active = index;
            }
            return this;
        }

        public Tabs Style(String value) {
            if (value != null && (Pills.equals(value) || Underline.equals(value) || Boxed.equals(value)
                    || Vertical.equals(value))) {
                this.style = value;
            } else {
                this.style = Underline;
            }
            return this;
        }

        public Tabs If(boolean value) {
            this.visible = value;
            return this;
        }

        public Tabs Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private String getButtonClass(boolean isActive) {
            String baseClass = "cursor-pointer font-bold transition-all duration-200 focus:outline-none text-sm whitespace-nowrap flex items-center justify-center";

            switch (style) {
                case Pills:
                    String pillsActive = "bg-blue-600 text-white shadow-md shadow-blue-500/20";
                    String pillsInactive = "bg-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800/50";
                    return Classes(baseClass, isActive ? pillsActive : pillsInactive, "rounded-lg px-4 py-2");
                case Underline:
                    String underlineActive = "text-blue-600 border-b-2 border-blue-600 dark:text-blue-400 dark:border-blue-400";
                    String underlineInactive = "text-gray-500 border-b-2 border-transparent hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:border-gray-600";
                    return Classes(baseClass, isActive ? underlineActive : underlineInactive, "px-4 py-2.5 -mb-px");
                case Boxed:
                    String boxedActive = "bg-white text-blue-600 shadow-sm dark:bg-gray-800 dark:text-blue-400 rounded-md";
                    String boxedInactive = "text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200";
                    return Classes(baseClass, isActive ? boxedActive : boxedInactive, "px-4 py-1.5 flex-1");
                case Vertical:
                    String verticalActive = "bg-blue-50 text-blue-700 border-r-2 border-blue-600 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-400";
                    String verticalInactive = "text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-800 border-r-2 border-transparent";
                    return Classes(baseClass, isActive ? verticalActive : verticalInactive,
                            "px-4 py-3 text-left rounded-l-md");
                default:
                    return Classes(baseClass, "px-4 py-2");
            }
        }

        private String renderTabButtons(List<String> buttonIDs, List<String> panelIDs) {
            StringBuilder builder = new StringBuilder();

            String wrapperClass = "flex overflow-x-auto scrollbar-hide ";
            switch (style) {
                case Pills:
                    wrapperClass += "gap-2 mb-4";
                    break;
                case Underline:
                    wrapperClass += "border-b border-gray-200 dark:border-gray-800 mb-4";
                    break;
                case Boxed:
                    wrapperClass += "gap-0 mb-4 border border-gray-200 dark:border-gray-800 rounded-lg overflow-hidden p-1 bg-gray-50/50 dark:bg-gray-950/30";
                    break;
                case Vertical:
                    wrapperClass = "flex flex-col gap-1 min-w-[12rem]";
                    break;
            }

            builder.append(String.format("<div class=\"%s\" role=\"tablist\">", escapeAttr(wrapperClass)));

            for (int i = 0; i < tabs.size(); i++) {
                TabData tab = tabs.get(i);
                boolean isActive = i == active;
                String buttonClass = getButtonClass(isActive);
                String ariaSelected = isActive ? "true" : "false";
                String ariaControls = panelIDs.get(i);
                String tabIndex = isActive ? "0" : "-1";

                builder.append(String.format(
                        "<button id=\"%s\" class=\"%s\" data-tabs-index=\"%d\" role=\"tab\" aria-selected=\"%s\" aria-controls=\"%s\" tabindex=\"%s\">",
                        escapeAttr(buttonIDs.get(i)),
                        escapeAttr(buttonClass),
                        i,
                        escapeAttr(ariaSelected),
                        escapeAttr(ariaControls),
                        escapeAttr(tabIndex)));

                if (tab.icon != null && !tab.icon.isEmpty()) {
                    builder.append(String.format("<span class=\"mr-2\">%s</span>", tab.icon));
                }
                builder.append(String.format("<span>%s</span>", tab.label));
                builder.append("</button>");
            }

            builder.append("</div>");
            return builder.toString();
        }

        private String renderTabPanels(List<String> panelIDs) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < tabs.size(); i++) {
                TabData tab = tabs.get(i);
                boolean isActive = i == active;
                String hiddenAttr = isActive ? "" : "hidden=\"\"";
                String labelledBy = id + "_btn_" + i;

                String panelClass = Classes(
                        "tab-panel",
                        Ui.If(!isActive, () -> "hidden opacity-0"),
                        Ui.If(isActive, () -> "opacity-100"),
                        "transition-opacity duration-300 ease-in-out");

                builder.append(String.format(
                        "<div id=\"%s\" class=\"%s\" data-tabs-panel=\"%d\" role=\"tabpanel\" aria-labelledby=\"%s\" %s>",
                        escapeAttr(panelIDs.get(i)),
                        escapeAttr(panelClass),
                        i,
                        escapeAttr(labelledBy),
                        hiddenAttr));
                builder.append(tab.content);
                builder.append("</div>");
            }

            return builder.toString();
        }

        private String renderJavaScript(List<String> buttonIDs, List<String> panelIDs) {
            String activeClasses, inactiveClasses;
            switch (style) {
                case Pills:
                    activeClasses = "bg-blue-600 text-white shadow-md shadow-blue-500/20";
                    inactiveClasses = "bg-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800/50";
                    break;
                case Underline:
                    activeClasses = "text-blue-600 border-b-2 border-blue-600 dark:text-blue-400 dark:border-blue-400";
                    inactiveClasses = "text-gray-500 border-b-2 border-transparent hover:text-gray-700 hover:border-gray-300 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:border-gray-600";
                    break;
                case Boxed:
                    activeClasses = "bg-white text-blue-600 shadow-sm dark:bg-gray-800 dark:text-blue-400 rounded-md";
                    inactiveClasses = "text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200";
                    break;
                case Vertical:
                    activeClasses = "bg-blue-50 text-blue-700 border-r-2 border-blue-600 dark:bg-blue-900/20 dark:text-blue-400 dark:border-blue-400";
                    inactiveClasses = "text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-800 border-r-2 border-transparent";
                    break;
                default:
                    activeClasses = "text-blue-600";
                    inactiveClasses = "text-gray-600";
                    break;
            }

            String safeID = escapeJs(escapeAttr(id));
            String safeActiveClasses = escapeJs(activeClasses);
            String safeInactiveClasses = escapeJs(inactiveClasses);

            String js = String.format(
                    "(function(){" +
                            "var container=document.getElementById('%s');" +
                            "if(!container)return;" +
                            "var buttons=container.querySelectorAll('button[data-tabs-index]');" +
                            "var panels=container.querySelectorAll('div[data-tabs-panel]');" +
                            "var activeClasses='%s';" +
                            "var inactiveClasses='%s';" +
                            "function setActiveTab(index){" +
                            "buttons.forEach(function(btn){" +
                            "var idx=parseInt(btn.getAttribute('data-tabs-index'));" +
                            "if(idx===index){" +
                            "btn.setAttribute('aria-selected','true');" +
                            "inactiveClasses.split(' ').filter(function(c){return c;}).forEach(function(c){btn.classList.remove(c);});"
                            +
                            "activeClasses.split(' ').filter(function(c){return c;}).forEach(function(c){btn.classList.add(c);});"
                            +
                            "btn.tabIndex=0;" +
                            "}else{" +
                            "btn.setAttribute('aria-selected','false');" +
                            "activeClasses.split(' ').filter(function(c){return c;}).forEach(function(c){btn.classList.remove(c);});"
                            +
                            "inactiveClasses.split(' ').filter(function(c){return c;}).forEach(function(c){btn.classList.add(c);});"
                            +
                            "btn.tabIndex=-1;" +
                            "}" +
                            "});" +
                            "panels.forEach(function(panel){" +
                            "var idx=parseInt(panel.getAttribute('data-tabs-panel'));" +
                            "if(idx===index){" +
                            "panel.classList.remove('hidden');" +
                            "panel.removeAttribute('hidden');" +
                            "setTimeout(function(){" +
                            "panel.classList.remove('opacity-0');" +
                            "panel.classList.add('opacity-100');" +
                            "},10);" +
                            "panel.setAttribute('aria-hidden','false');" +
                            "}else{" +
                            "panel.classList.add('hidden','opacity-0');" +
                            "panel.setAttribute('hidden','');" +
                            "panel.classList.remove('opacity-100');" +
                            "panel.setAttribute('aria-hidden','true');" +
                            "}" +
                            "});" +
                            "container.setAttribute('data-tabs-active',index);" +
                            "}" +
                            "buttons.forEach(function(btn){" +
                            "btn.addEventListener('click',function(){" +
                            "var index=parseInt(this.getAttribute('data-tabs-index'));" +
                            "setActiveTab(index);" +
                            "});" +
                            "btn.addEventListener('keydown',function(e){" +
                            "var currentIndex=parseInt(container.getAttribute('data-tabs-active'));" +
                            "if(e.key==='ArrowRight'||e.key==='ArrowDown'){" +
                            "var newIndex=(currentIndex+1)%%buttons.length;" +
                            "buttons[newIndex].focus();" +
                            "setActiveTab(newIndex);" +
                            "e.preventDefault();" +
                            "}else if(e.key==='ArrowLeft'||e.key==='ArrowUp'){" +
                            "var newIndex=(currentIndex-1+buttons.length)%%buttons.length;" +
                            "buttons[newIndex].focus();" +
                            "setActiveTab(newIndex);" +
                            "e.preventDefault();" +
                            "}" +
                            "});" +
                            "});" +
                            "setActiveTab(%d);" +
                            "})();",
                    safeID, safeActiveClasses, safeInactiveClasses, active);
            return Script(js);
        }

        public String Render() {
            if (!visible || tabs.isEmpty()) {
                return "";
            }

            List<String> buttonIDs = new ArrayList<>();
            List<String> panelIDs = new ArrayList<>();
            for (int i = 0; i < tabs.size(); i++) {
                String suffix = RandomString(6);
                buttonIDs.add(String.format("%s_btn_%d_%s", id, i, suffix));
                panelIDs.add(String.format("%s_panel_%d_%s", id, i, suffix));
            }

            StringBuilder builder = new StringBuilder();

            builder.append(
                    "<style>.scrollbar-hide::-webkit-scrollbar{display:none}.scrollbar-hide{-ms-overflow-style:none;scrollbar-width:none}</style>");

            boolean isVertical = Vertical.equals(style);
            String containerClass = Classes(
                    "w-full",
                    Ui.If(isVertical, () -> "flex flex-col md:flex-row gap-6"),
                    css);

            builder.append(String.format("<div id=\"%s\" class=\"%s\" data-tabs-active=\"%d\" data-tabs-style=\"%s\">",
                    escapeAttr(id),
                    escapeAttr(containerClass),
                    active,
                    escapeAttr(style)));

            builder.append(renderTabButtons(buttonIDs, panelIDs));

            if (isVertical) {
                builder.append("<div class=\"flex-1\">");
            }

            builder.append(renderTabPanels(panelIDs));

            if (isVertical) {
                builder.append("</div>");
            }

            builder.append("</div>");

            builder.append(renderJavaScript(buttonIDs, panelIDs));

            return builder.toString();
        }
    }

    public static Tabs Tabs() {
        return Tabs.create();
    }

    public static final class Progress {
        private int value = 0;
        private String color = "bg-blue-600";
        private List<String> gradient = new ArrayList<>();
        private boolean striped = false;
        private boolean animated = false;
        private boolean indeterminate = false;
        private String size = "md";
        private String label = "";
        private String labelPosition = "inside";
        private String css = "";
        private boolean visible = true;

        public static Progress create() {
            return new Progress();
        }

        public Progress Value(int percent) {
            this.value = Math.max(0, Math.min(100, percent));
            return this;
        }

        public Progress Color(String value) {
            this.color = value != null ? value : "bg-blue-600";
            return this;
        }

        public Progress Gradient(String... colors) {
            this.gradient = colors != null ? new ArrayList<>(java.util.Arrays.asList(colors)) : new ArrayList<>();
            return this;
        }

        public Progress Size(String value) {
            this.size = value != null ? value : "md";
            return this;
        }

        public Progress Striped(boolean value) {
            this.striped = value;
            return this;
        }

        public Progress Animated(boolean value) {
            this.animated = value;
            return this;
        }

        public Progress Indeterminate(boolean value) {
            this.indeterminate = value;
            return this;
        }

        public Progress Label(String value) {
            this.label = value != null ? value : "";
            return this;
        }

        public Progress LabelPosition(String value) {
            this.labelPosition = value != null ? value : "inside";
            return this;
        }

        public Progress If(boolean value) {
            this.visible = value;
            return this;
        }

        public Progress Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private String getHeightClass() {
            switch (size) {
                case "xs":
                    return "h-1";
                case "sm":
                    return "h-1.5";
                case "md":
                    return "h-2.5";
                case "lg":
                    return "h-4";
                case "xl":
                    return "h-6";
                default:
                    return "h-2";
            }
        }

        private String getAnimationStyle() {
            List<String> styles = new ArrayList<>();
            if ((animated && striped) || indeterminate) {
                styles.add("@keyframes progress-stripes{0%{background-position:1rem 0}100%{background-position:0 0}}");
                styles.add("@keyframes progress-indeterminate{0%{left:-33%;}100%{left:100%;}}");
                styles.add(
                        ".animate-progress-indeterminate{position:absolute; animation: progress-indeterminate 1.8s infinite cubic-bezier(0.65, 0.815, 0.735, 0.395);}");
            }
            if (styles.isEmpty()) {
                return "";
            }
            return "<style id=\"__progress-anim__\">" + String.join("", styles) + "</style>";
        }

        public String Render() {
            if (!visible) {
                return "";
            }

            String heightClass = getHeightClass();
            String containerClasses = Classes(
                    "w-full",
                    "overflow-hidden",
                    "bg-gray-200/50",
                    "dark:bg-gray-800/50",
                    "rounded-full",
                    heightClass,
                    css);

            List<String> barClasses = new ArrayList<>();
            barClasses.add("h-full");
            barClasses.add("rounded-full");

            if (gradient.isEmpty()) {
                barClasses.add(color);
            }

            if (!indeterminate) {
                barClasses.add("transition-all");
                barClasses.add("duration-500");
                barClasses.add("ease-out");
            } else {
                barClasses.add("w-1/3");
                barClasses.add("animate-progress-indeterminate");
                barClasses.add(color);
            }

            StringBuilder barStyle = new StringBuilder();
            if (!indeterminate) {
                barStyle.append(String.format("width: %d%%", value));
            }

            if (!gradient.isEmpty()) {
                barStyle.append("; background: linear-gradient(90deg, ").append(String.join(", ", gradient))
                        .append(")");
            }

            if (striped) {
                barStyle.append(
                        "; background-image: linear-gradient(45deg, rgba(255,255,255,.15) 25%, transparent 25%, transparent 50%, rgba(255,255,255,.15) 50%, rgba(255,255,255,.15) 75%, transparent 75%, transparent); background-size: 1rem 1rem");
            }

            if (animated && striped && !indeterminate) {
                barStyle.append("; animation: progress-stripes 1s linear infinite");
            }

            String barHTML = String.format("<div class=\"%s\" style=\"%s\"></div>",
                    Classes(barClasses.toArray(new String[0])), barStyle.toString());

            String labelHTML = "";
            if (label != null && !label.isEmpty() && !indeterminate) {
                if ("inside".equals(labelPosition)) {
                    labelHTML = String.format(
                            "<div class=\"absolute inset-0 flex items-center justify-center text-[10px] font-bold text-white mix-blend-difference pointer-events-none\">%s</div>",
                            escapeAttr(label));
                } else {
                    return div("flex flex-col gap-1.5").render(
                            div("flex justify-between items-center text-xs font-semibold").render(
                                    span("").render(label),
                                    span("text-gray-500").render(value + "%")),
                            div(containerClasses, Attr.of().style("position: relative;")).render(barHTML));
                }
            }

            String container = div(containerClasses, Attr.of().style("position: relative;"))
                    .render(barHTML + labelHTML);
            return container + getAnimationStyle();
        }
    }

    public static Progress ProgressBar() {
        return Progress.create();
    }

    public static Progress ProgressWithLabel(int percent) {
        return Progress.create().Value(percent).Label(percent + "%");
    }

    public static final class Badge {
        private String text = "";
        private String color = "gray";
        private boolean dot = false;
        private String icon = "";
        private String size = "md";
        private boolean rounded = true;
        private boolean visible = true;
        private String css = "";
        private List<Attr> attrs = new ArrayList<>();

        public static Badge create(Attr... attr) {
            Badge b = new Badge();
            if (attr != null) {
                b.attrs = new ArrayList<>(java.util.Arrays.asList(attr));
            }
            return b;
        }

        public Badge Text(String value) {
            this.text = value != null ? value : "";
            return this;
        }

        public Badge Color(String value) {
            this.color = value != null ? value : "gray";
            return this;
        }

        public Badge Dot() {
            this.dot = true;
            return this;
        }

        public Badge Icon(String html) {
            this.icon = html != null ? html : "";
            return this;
        }

        public Badge Size(String value) {
            this.size = value != null ? value : "md";
            return this;
        }

        public Badge Square() {
            this.rounded = false;
            return this;
        }

        public Badge If(boolean value) {
            this.visible = value;
            return this;
        }

        public Badge Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private String getColorClasses(String colorName, boolean isOutline, boolean isSoft) {
            switch (colorName) {
                case "red":
                    if (isOutline)
                        return "bg-transparent text-red-600 border border-red-600 dark:text-red-400 dark:border-red-400";
                    if (isSoft)
                        return "bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-300 border border-red-200/50 dark:border-red-800/50";
                    return "bg-red-600 text-white dark:bg-red-700 dark:text-red-100";
                case "green":
                    if (isOutline)
                        return "bg-transparent text-green-600 border border-green-600 dark:text-green-400 dark:border-green-400";
                    if (isSoft)
                        return "bg-green-50 text-green-700 dark:bg-green-950/40 dark:text-green-300 border border-green-200/50 dark:border-green-800/50";
                    return "bg-green-600 text-white dark:bg-green-700 dark:text-green-100";
                case "blue":
                    if (isOutline)
                        return "bg-transparent text-blue-600 border border-blue-600 dark:text-blue-400 dark:border-blue-400";
                    if (isSoft)
                        return "bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300 border border-blue-200/50 dark:border-blue-800/50";
                    return "bg-blue-600 text-white dark:bg-blue-700 dark:text-blue-100";
                case "yellow":
                    if (isOutline)
                        return "bg-transparent text-yellow-600 border border-yellow-600 dark:text-yellow-400 dark:border-yellow-400";
                    if (isSoft)
                        return "bg-yellow-50 text-yellow-700 dark:bg-yellow-950/40 dark:text-yellow-300 border border-yellow-200/50 dark:border-yellow-800/50";
                    return "bg-yellow-500 text-gray-900 dark:bg-yellow-600 dark:text-gray-100";
                case "purple":
                    if (isOutline)
                        return "bg-transparent text-purple-600 border border-purple-600 dark:text-purple-400 dark:border-purple-400";
                    if (isSoft)
                        return "bg-purple-50 text-purple-700 dark:bg-purple-950/40 dark:text-purple-300 border border-purple-200/50 dark:border-purple-800/50";
                    return "bg-purple-600 text-white dark:bg-purple-700 dark:text-white";
                case "gray":
                    if (isOutline)
                        return "bg-transparent text-gray-600 border border-gray-600 dark:text-gray-400 dark:border-gray-400";
                    if (isSoft)
                        return "bg-gray-100 text-gray-700 dark:bg-gray-800/60 dark:text-gray-300 border border-gray-200 dark:border-gray-700";
                    return "bg-gray-600 text-white dark:bg-gray-700 dark:text-gray-100";
                default:
                    if (color != null && color.startsWith("bg-")) {
                        return color;
                    }
                    return "bg-gray-600 text-white dark:bg-gray-700 dark:text-gray-100";
            }
        }

        public String Render() {
            if (!visible) {
                return "";
            }

            boolean isOutline = color != null && color.endsWith("-outline");
            boolean isSoft = color != null && color.endsWith("-soft");
            String colorName = color != null ? color.replace("-outline", "").replace("-soft", "") : "gray";

            if (dot) {
                String baseClass = "inline-flex items-center justify-center rounded-full";
                String sizeClass = "h-2 w-2";
                if ("lg".equals(size)) {
                    sizeClass = "h-3 w-3";
                } else if ("sm".equals(size)) {
                    sizeClass = "h-1.5 w-1.5";
                }
                String colorClass = getColorClasses(colorName, isOutline, isSoft);
                return span(Classes(baseClass, sizeClass, colorClass, css), attrs.toArray(new Attr[0])).render();
            }

            String roundedClass = rounded ? "rounded-full" : "rounded-md";
            String sizeClass = "px-2 py-0.5 text-[10px] h-5";
            if ("lg".equals(size)) {
                sizeClass = "px-3 py-1 text-xs h-6";
            } else if ("sm".equals(size)) {
                sizeClass = "px-1.5 py-0 text-[9px] h-4";
            }

            String baseClass = "inline-flex items-center justify-center font-bold tracking-wide uppercase";
            String colorClass = getColorClasses(colorName, isOutline, isSoft);

            String content = text;
            if (icon != null && !icon.isEmpty()) {
                String iconSize = "sm".equals(size) ? "w-2.5 h-2.5" : "w-3 h-3";
                content = String.format("<span class=\"%s mr-1 flex items-center justify-center\">%s</span>%s",
                        iconSize, icon, text);
            }

            return span(Classes(baseClass, sizeClass, roundedClass, colorClass, css), attrs.toArray(new Attr[0]))
                    .render(content);
        }
    }

    public static Badge Badge(Attr... attr) {
        return Badge.create(attr);
    }

    public static final class Tooltip {
        private String content = "";
        private String position = "top";
        private String variant = "dark";
        private int delay = 200;
        private boolean visible = true;
        private String css = "";

        public static Tooltip create() {
            return new Tooltip();
        }

        public Tooltip Content(String value) {
            this.content = value != null ? value : "";
            return this;
        }

        public Tooltip Position(String value) {
            this.position = value != null ? value : "top";
            return this;
        }

        public Tooltip Variant(String value) {
            this.variant = value != null ? value : "dark";
            return this;
        }

        public Tooltip Delay(int ms) {
            this.delay = ms;
            return this;
        }

        public Tooltip If(boolean value) {
            this.visible = value;
            return this;
        }

        public Tooltip Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private PositionClasses getPositionClasses() {
            PositionClasses pc = new PositionClasses();
            switch (position) {
                case "bottom":
                    pc.tooltipClasses = "left-1/2 -translate-x-1/2 top-full mt-2.5";
                    pc.arrowClasses = "absolute left-1/2 -translate-x-1/2 -top-1 w-2 h-2 rotate-45";
                    break;
                case "left":
                    pc.tooltipClasses = "right-full top-1/2 -translate-y-1/2 mr-2.5";
                    pc.arrowClasses = "absolute right-0 top-1/2 -translate-y-1/2 translate-x-1 w-2 h-2 rotate-45";
                    break;
                case "right":
                    pc.tooltipClasses = "left-full top-1/2 -translate-y-1/2 ml-2.5";
                    pc.arrowClasses = "absolute left-0 top-1/2 -translate-y-1/2 -translate-x-1 w-2 h-2 rotate-45";
                    break;
                default: // "top"
                    pc.tooltipClasses = "left-1/2 -translate-x-1/2 bottom-full mb-2.5";
                    pc.arrowClasses = "absolute left-1/2 -translate-x-1/2 -bottom-1 w-2 h-2 rotate-45";
                    break;
            }
            return pc;
        }

        private static final class PositionClasses {
            String tooltipClasses;
            String arrowClasses;
        }

        private String getVariantClasses() {
            switch (variant) {
                case "light":
                    return "bg-white text-gray-800 border border-gray-100 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-700 shadow-lg";
                case "blue":
                    return "bg-blue-600 text-white shadow-lg shadow-blue-500/20";
                case "green":
                    return "bg-green-600 text-white shadow-lg shadow-green-500/20";
                case "red":
                    return "bg-red-600 text-white shadow-lg shadow-red-500/20";
                case "yellow":
                    return "bg-yellow-500 text-gray-900 shadow-lg shadow-yellow-500/20";
                default: // "dark"
                    return "bg-gray-900 text-white dark:bg-white dark:text-gray-900 shadow-lg";
            }
        }

        private String getArrowColor() {
            switch (variant) {
                case "light":
                    return "bg-white dark:bg-gray-800 border-l border-t border-gray-100 dark:border-gray-700";
                case "blue":
                    return "bg-blue-600";
                case "green":
                    return "bg-green-600";
                case "red":
                    return "bg-red-600";
                case "yellow":
                    return "bg-yellow-500";
                default: // "dark"
                    return "bg-gray-900 dark:bg-white";
            }
        }

        private String renderArrow(String arrowClasses) {
            return String.format("<div class=\"%s %s\"></div>", arrowClasses, getArrowColor());
        }

        private String renderTooltipScript(String tooltipID) {
            return Script(
                    "(function() {" +
                            "window.gSuiTooltipTimers = window.gSuiTooltipTimers || {};" +
                            "window.gSuiShowTooltip = window.gSuiShowTooltip || function(id) {" +
                            "var tt = document.getElementById(id);" +
                            "if (!tt) return;" +
                            "if (window.gSuiTooltipTimers[id]) {" +
                            "clearTimeout(window.gSuiTooltipTimers[id]);" +
                            "}" +
                            "var delay = parseInt(tt.getAttribute('data-tooltip-delay')) || 0;" +
                            "window.gSuiTooltipTimers[id] = setTimeout(function() {" +
                            "tt.classList.remove('opacity-0', 'invisible', 'scale-95');" +
                            "tt.classList.add('opacity-100', 'visible', 'scale-100');" +
                            "}, delay);" +
                            "};" +
                            "window.gSuiHideTooltip = window.gSuiHideTooltip || function(id) {" +
                            "var tt = document.getElementById(id);" +
                            "if (!tt) return;" +
                            "if (window.gSuiTooltipTimers[id]) {" +
                            "clearTimeout(window.gSuiTooltipTimers[id]);" +
                            "}" +
                            "tt.classList.remove('opacity-100', 'visible', 'scale-100');" +
                            "tt.classList.add('opacity-0', 'invisible', 'scale-95');" +
                            "};" +
                            "})();");
        }

        public String Render(String wrappedHTML) {
            if (!visible || content == null || content.isEmpty()) {
                return wrappedHTML != null ? wrappedHTML : "";
            }

            String tooltipID = "tt_" + RandomString(8);
            PositionClasses pc = getPositionClasses();
            String variantClasses = getVariantClasses();

            String tooltipClasses = Classes(
                    "absolute z-[100]",
                    "px-2.5 py-1.5",
                    "text-[11px] font-bold leading-none whitespace-nowrap",
                    "rounded-md shadow-lg",
                    "opacity-0 scale-95",
                    "invisible",
                    "transition-all duration-200 ease-out",
                    "pointer-events-none",
                    pc.tooltipClasses,
                    variantClasses,
                    css);

            String arrow = renderArrow(pc.arrowClasses);
            String wrapperClasses = Classes("relative", "inline-block");

            String safeID = escapeAttr(tooltipID);
            String tooltipHTML = String.format(
                    "<div id=\"%s\" class=\"%s\" data-tooltip-delay=\"%d\">%s%s</div>",
                    escapeAttr(tooltipID),
                    escapeAttr(tooltipClasses),
                    delay,
                    escapeAttr(content),
                    arrow);

            String wrapper = String.format(
                    "<div class=\"%s\" onmouseenter=\"gSuiShowTooltip('%s')\" onmouseleave=\"gSuiHideTooltip('%s')\">%s%s</div>",
                    escapeAttr(wrapperClasses),
                    safeID,
                    safeID,
                    wrappedHTML != null ? wrappedHTML : "",
                    tooltipHTML);

            return wrapper + renderTooltipScript(tooltipID);
        }
    }

    public static Tooltip Tooltip() {
        return Tooltip.create();
    }

    public static final class Card {
        public static final String Bordered = "bordered";
        public static final String Shadowed = "shadowed";
        public static final String Flat = "flat";
        public static final String Glass = "glass";

        private String header = "";
        private String body = "";
        private String footer = "";
        private String image = "";
        private String imageAlt = "";
        private String variant = Shadowed;
        private String css = "";
        private String padding = "p-6";
        private boolean hover = false;
        private boolean compact = false;
        private boolean visible = true;

        public static Card create() {
            return new Card();
        }

        public Card Header(String html) {
            this.header = html != null ? html : "";
            return this;
        }

        public Card Body(String html) {
            this.body = html != null ? html : "";
            return this;
        }

        public Card Footer(String html) {
            this.footer = html != null ? html : "";
            return this;
        }

        public Card Image(String src, String alt) {
            this.image = src != null ? src : "";
            this.imageAlt = alt != null ? alt : "";
            return this;
        }

        public Card Padding(String value) {
            this.padding = value != null ? value : "p-6";
            return this;
        }

        public Card Hover(boolean value) {
            this.hover = value;
            return this;
        }

        public Card Compact(boolean value) {
            this.compact = value;
            if (value) {
                this.padding = "p-4";
            }
            return this;
        }

        public Card Variant(String value) {
            this.variant = value != null ? value : Shadowed;
            return this;
        }

        public Card If(boolean value) {
            this.visible = value;
            return this;
        }

        public Card Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        public String Render() {
            if (!visible) {
                return "";
            }

            List<String> baseClasses = new ArrayList<>();
            baseClasses.add("bg-white");
            baseClasses.add("dark:bg-gray-900");
            baseClasses.add("rounded-xl");
            baseClasses.add("overflow-hidden");

            switch (variant) {
                case Bordered:
                    baseClasses.add("border");
                    baseClasses.add("border-gray-200");
                    baseClasses.add("dark:border-gray-800");
                    break;
                case Shadowed:
                    baseClasses.add("shadow-sm");
                    baseClasses.add("border");
                    baseClasses.add("border-gray-100");
                    baseClasses.add("dark:border-gray-800/50");
                    break;
                case Flat:
                    break;
                case Glass:
                    baseClasses = new ArrayList<>();
                    baseClasses.add("bg-white/70");
                    baseClasses.add("dark:bg-gray-900/70");
                    baseClasses.add("backdrop-blur-md");
                    baseClasses.add("rounded-xl");
                    baseClasses.add("overflow-hidden");
                    baseClasses.add("border");
                    baseClasses.add("border-white/20");
                    baseClasses.add("dark:border-gray-800/50");
                    break;
                default:
                    baseClasses.add("shadow-sm");
                    baseClasses.add("border");
                    baseClasses.add("border-gray-100");
                    baseClasses.add("dark:border-gray-800/50");
                    break;
            }

            if (hover) {
                baseClasses.add("transition-all duration-300 hover:shadow-lg hover:-translate-y-1");
            }

            if (css != null && !css.isEmpty()) {
                baseClasses.add(css);
            }

            String cardClass = Classes(baseClasses.toArray(new String[0]));
            List<String> sections = new ArrayList<>();

            if (image != null && !image.isEmpty()) {
                String height = compact ? "h-32" : "h-48";
                sections.add(String.format("<img src=\"%s\" alt=\"%s\" class=\"w-full %s object-cover\">",
                        escapeAttr(image), escapeAttr(imageAlt), height));
            }

            if (header != null && !header.isEmpty()) {
                String headerPadding = compact ? "px-4 py-3" : "px-6 py-4";
                String headerHtml = div(Classes(headerPadding, "border-b", "border-gray-100/80",
                        "dark:border-gray-800/80", "bg-gray-50/30", "dark:bg-gray-800/30")).render(header);
                sections.add(headerHtml);
            }

            if (body != null && !body.isEmpty()) {
                String bodyHtml = div(Classes(padding)).render(body);
                sections.add(bodyHtml);
            }

            if (footer != null && !footer.isEmpty()) {
                String footerPadding = compact ? "px-4 py-3" : "px-6 py-4";
                String footerHtml = div(Classes(footerPadding, "border-t", "border-gray-100/80",
                        "dark:border-gray-800/80", "bg-gray-50/30", "dark:bg-gray-800/30")).render(footer);
                sections.add(footerHtml);
            }

            return div(cardClass).render(String.join("", sections));
        }
    }

    public static Card Card() {
        return Card.create();
    }

    public static final class Dropdown {
        private static final class DropdownItem {
            String label;
            String onclick;
            String icon;
            String variant;
            boolean isDivider;
            boolean isHeader;

            DropdownItem(String label, String onclick, String icon, String variant, boolean isDivider,
                    boolean isHeader) {
                this.label = label;
                this.onclick = onclick;
                this.icon = icon;
                this.variant = variant;
                this.isDivider = isDivider;
                this.isHeader = isHeader;
            }
        }

        private String trigger = "";
        private List<DropdownItem> items = new ArrayList<>();
        private String position = "bottom-left";
        private boolean visible = true;
        private String css = "";
        private final Target target = Target();

        public static Dropdown create() {
            return new Dropdown();
        }

        public Dropdown Trigger(String html) {
            this.trigger = html != null ? html : "";
            return this;
        }

        public Dropdown Item(String label, String onclick, String... icon) {
            String iconStr = icon != null && icon.length > 0 ? icon[0] : "";
            items.add(new DropdownItem(label, onclick, iconStr, "default", false, false));
            return this;
        }

        public Dropdown Danger(String label, String onclick, String... icon) {
            String iconStr = icon != null && icon.length > 0 ? icon[0] : "";
            items.add(new DropdownItem(label, onclick, iconStr, "danger", false, false));
            return this;
        }

        public Dropdown Header(String label) {
            items.add(new DropdownItem(label, "", "", "default", false, true));
            return this;
        }

        public Dropdown Divider() {
            items.add(new DropdownItem("", "", "", "default", true, false));
            return this;
        }

        public Dropdown Position(String value) {
            this.position = value != null ? value : "bottom-left";
            return this;
        }

        public Dropdown If(boolean value) {
            this.visible = value;
            return this;
        }

        public Dropdown Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        private String getPositionClasses() {
            switch (position) {
                case "bottom-right":
                    return "right-0 top-full mt-2 origin-top-right";
                case "top-left":
                    return "left-0 bottom-full mb-2 origin-bottom-left";
                case "top-right":
                    return "right-0 bottom-full mb-2 origin-bottom-right";
                default: // "bottom-left"
                    return "left-0 top-full mt-2 origin-top-left";
            }
        }

        private String renderItems() {
            if (items.isEmpty()) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            for (DropdownItem item : items) {
                if (item.isDivider) {
                    builder.append("<div class=\"border-t border-gray-100 dark:border-gray-800 my-1.5 mx-2\"></div>");
                } else if (item.isHeader) {
                    builder.append(String.format(
                            "<div class=\"px-4 py-1.5 text-[10px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest\">%s</div>",
                            escapeAttr(item.label)));
                } else {
                    builder.append(renderItem(item.label, item.onclick, item.icon, item.variant));
                }
            }
            return builder.toString();
        }

        private String renderItem(String label, String onclick, String icon, String variant) {
            String itemClass = Classes(
                    "flex", "items-center", "gap-2", "w-full", "text-left", "px-3", "py-2", "mx-1",
                    "w-[calc(100%-0.5rem)]", "text-sm", "font-bold", "cursor-pointer", "rounded-md",
                    "transition-all", "duration-150", "whitespace-nowrap");

            if ("danger".equals(variant)) {
                itemClass = Classes(itemClass,
                        "text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/30");
            } else {
                itemClass = Classes(itemClass,
                        "text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800");
            }

            String iconHTML = "<span class=\"w-5 h-5 flex-shrink-0 flex items-center justify-center opacity-70\">";
            if (icon != null && !icon.isEmpty()) {
                iconHTML += icon;
            }
            iconHTML += "</span>";

            return String.format(
                    "<button class=\"%s\" onclick=\"%s\">%s<span class=\"flex-1\">%s</span></button>",
                    escapeAttr(itemClass),
                    escapeAttr(onclick != null ? onclick : ""),
                    iconHTML,
                    escapeAttr(label != null ? label : ""));
        }

        private String renderScript(String dropdownID, String triggerID) {
            String safeTriggerID = escapeJs(escapeAttr(triggerID));
            String safeDropdownID = escapeJs(escapeAttr(dropdownID));
            String scriptJS = String.format(
                    "(function(){" +
                            "var t=document.getElementById('%s');if(!t)return;" +
                            "var d=document.getElementById('%s');if(!d)return;" +
                            "var o=false;" +
                            "function show(){" +
                            "o=true;" +
                            "d.classList.remove('hidden');" +
                            "setTimeout(function(){" +
                            "d.classList.remove('opacity-0','scale-95');" +
                            "d.classList.add('opacity-100','scale-100');" +
                            "},10);" +
                            "}" +
                            "function hide(){" +
                            "o=false;" +
                            "d.classList.remove('opacity-100','scale-100');" +
                            "d.classList.add('opacity-0','scale-95');" +
                            "setTimeout(function(){" +
                            "if(!o)d.classList.add('hidden');" +
                            "},200);" +
                            "}" +
                            "t.addEventListener('click',function(e){" +
                            "e.stopPropagation();" +
                            "if(o)hide();else show();" +
                            "});" +
                            "document.addEventListener('click',function(){if(o)hide();});" +
                            "document.addEventListener('keydown',function(e){if(e.key==='Escape'&&o)hide();});" +
                            "})();",
                    safeTriggerID, safeDropdownID);
            return Script(scriptJS);
        }

        public String Render() {
            if (!visible || trigger == null || trigger.isEmpty()) {
                return "";
            }

            String dropdownID = "dropdown_" + target.id;
            String triggerID = "dropdown_trigger_" + target.id;

            String positionClasses = getPositionClasses();
            String menuClasses = Classes(
                    "absolute z-50",
                    "min-w-[12rem]",
                    "bg-white",
                    "dark:bg-gray-900",
                    "border",
                    "border-gray-200",
                    "dark:border-gray-800",
                    "rounded-xl",
                    "shadow-xl",
                    "py-1.5",
                    "hidden",
                    "opacity-0 scale-95 origin-top",
                    "transition-all duration-200 ease-out",
                    positionClasses,
                    css);

            String itemsHTML = renderItems();
            String menuHTML = String.format("<div id=\"%s\" class=\"%s\">%s</div>", escapeAttr(dropdownID),
                    escapeAttr(menuClasses), itemsHTML);
            String triggerWrapper = String.format("<div id=\"%s\" class=\"relative inline-block\">%s%s</div>",
                    escapeAttr(triggerID), trigger, menuHTML);
            return triggerWrapper + renderScript(dropdownID, triggerID);
        }
    }

    public static Dropdown Dropdown() {
        return Dropdown.create();
    }

    public static final class StepProgress {
        private int current = 0;
        private int total = 1;
        private String color = "bg-blue-500";
        private String size = "md";
        private String css = "";
        private boolean visible = true;

        public static StepProgress create(int current, int total) {
            StepProgress sp = new StepProgress();
            if (current < 0)
                current = 0;
            if (total < 1)
                total = 1;
            if (current > total)
                current = total;
            sp.current = current;
            sp.total = total;
            return sp;
        }

        public StepProgress Current(int value) {
            if (value < 0)
                value = 0;
            if (value > total)
                value = total;
            this.current = value;
            return this;
        }

        public StepProgress Total(int value) {
            if (value < 1)
                value = 1;
            this.total = value;
            if (current > value) {
                current = value;
            }
            return this;
        }

        public StepProgress Color(String value) {
            this.color = value != null ? value : "bg-blue-500";
            return this;
        }

        public StepProgress Size(String value) {
            this.size = value != null ? value : "md";
            return this;
        }

        public StepProgress Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        public StepProgress If(boolean value) {
            this.visible = value;
            return this;
        }

        private String getHeightClass() {
            switch (size) {
                case "xs":
                    return "h-0.5";
                case "sm":
                    return "h-1";
                case "md":
                    return "h-1.5";
                case "lg":
                    return "h-2";
                case "xl":
                    return "h-3";
                default:
                    return "h-1";
            }
        }

        public String Render() {
            if (!visible) {
                return "";
            }

            double percent = (double) current / (double) total * 100.0;
            String heightClass = getHeightClass();

            String containerClasses = Classes(
                    "w-full",
                    "bg-gray-200",
                    "dark:bg-gray-700",
                    "rounded-full",
                    "overflow-hidden",
                    heightClass,
                    css);

            String barClasses = Classes(
                    "h-full",
                    color,
                    "rounded-full",
                    "transition-all",
                    "duration-300",
                    "flex-shrink-0");

            String bar = String.format("<div class=\"%s\" style=\"width: %.0f%%;\"></div>", barClasses, percent);
            String container = String.format("<div class=\"%s\">%s</div>", containerClasses, bar);
            String label = String.format(
                    "<div class=\"text-sm font-medium text-gray-500 dark:text-gray-400 mb-1\">Step %d of %d</div>",
                    current, total);
            return label + container;
        }
    }

    public static StepProgress StepProgress(int current, int total) {
        return StepProgress.create(current, total);
    }

    public static final class ALabel {
        private String id = "";
        private String css = "text-sm";
        private String cssLabel = "";
        private boolean required = false;
        private boolean disabled = false;

        public static ALabel create(Attr target) {
            ALabel lbl = new ALabel();
            if (target != null && target.id != null) {
                lbl.id = target.id;
            }
            return lbl;
        }

        public ALabel Required(boolean value) {
            this.required = value;
            return this;
        }

        public ALabel Disabled(boolean value) {
            this.disabled = value;
            return this;
        }

        public ALabel Class(String... value) {
            List<String> classes = new ArrayList<>();
            if (css != null && !css.isEmpty()) {
                classes.addAll(java.util.Arrays.asList(css.split(" ")));
            }
            if (value != null) {
                classes.addAll(java.util.Arrays.asList(value));
            }
            this.css = String.join(" ", classes);
            return this;
        }

        public ALabel ClassLabel(String... value) {
            List<String> classes = new ArrayList<>();
            if (cssLabel != null && !cssLabel.isEmpty()) {
                classes.addAll(java.util.Arrays.asList(cssLabel.split(" ")));
            }
            if (value != null) {
                classes.addAll(java.util.Arrays.asList(value));
            }
            this.cssLabel = String.join(" ", classes);
            return this;
        }

        public String Render(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }

            String labelHtml = label(cssLabel, Attr.of().htmlFor(id)).render(text);
            String requiredStar = If(required && !disabled, () -> span("ml-1 text-red-700").render("*"));
            return div(Classes(css, "relative")).render(labelHtml, requiredStar);
        }
    }

    public static ALabel Label(Attr target) {
        return ALabel.create(target);
    }

    public static final class FormInstance {
        public final String FormId;
        public final Attr OnSubmit;

        private FormInstance(String formId, Attr onSubmit) {
            this.FormId = formId;
            this.OnSubmit = onSubmit;
        }

        public static FormInstance create(Attr onSubmit) {
            return new FormInstance("i" + RandomString(15), onSubmit);
        }

        public InputText Text(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IText(name, d).Form(FormId);
        }

        public IArea Area(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IArea(name, d).Form(FormId);
        }

        public InputText Password(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IPassword(name, d).Form(FormId);
        }

        public INumber Number(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return INumber(name, d).Form(FormId);
        }

        public ISelect Select(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return ISelect(name, d).Form(FormId);
        }

        public ICheckbox Checkbox(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return ICheckbox(name, d).Form(FormId);
        }

        public IRadio Radio(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IRadio(name, d).Form(FormId);
        }

        public IRadioButtons RadioButtons(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IRadioButtons(name, d).Form(FormId);
        }

        public IRadioDiv RadioDiv(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IRadioDiv(name, d).Form(FormId);
        }

        public InputText Date(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IDate(name, d).Form(FormId);
        }

        public InputText Time(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return ITime(name, d).Form(FormId);
        }

        public InputText DateTime(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IDateTime(name, d).Form(FormId);
        }

        public InputText Phone(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IPhone(name, d).Form(FormId);
        }

        public InputText Email(String name, Object... data) {
            Object d = data != null && data.length > 0 ? data[0] : null;
            return IEmail(name, d).Form(FormId);
        }

        public String Hidden(String name, Object value, Attr... attr) {
            List<Attr> attrs = new ArrayList<>();
            if (attr != null) {
                attrs.addAll(java.util.Arrays.asList(attr));
            }
            attrs.add(
                    Attr.of().name(name).type("hidden").value(value != null ? String.valueOf(value) : "").form(FormId));
            return input("hidden", attrs.toArray(new Attr[0]));
        }

        public Button Button() {
            return Ui.Button().Form(FormId);
        }

        public String Render() {
            return form("hidden", Attr.of().id(FormId), OnSubmit).render();
        }
    }

    public static FormInstance FormNew(Attr onSubmit) {
        return FormInstance.create(onSubmit);
    }

    public static final class Table<T> {
        private final List<String> heads = new ArrayList<>();
        private final List<Slot<T>> slots = new ArrayList<>();
        private String css = "";

        @FunctionalInterface
        public interface SlotFunction<T> {
            String apply(T item);
        }

        private static final class Slot<T> {
            final SlotFunction<T> slot;
            final String cls;

            Slot(SlotFunction<T> slot, String cls) {
                this.slot = slot;
                this.cls = cls;
            }
        }

        public static <T> Table<T> create(String cls) {
            Table<T> t = new Table<>();
            t.css = cls != null ? cls : "";
            return t;
        }

        public Table<T> Head(String value, String cls) {
            String headHtml = String.format("<th class=\"%s\">%s</th>", escapeAttr(cls != null ? cls : ""),
                    escapeHtml(value != null ? value : ""));
            heads.add(headHtml);
            return this;
        }

        public Table<T> HeadHTML(String value, String cls) {
            String headHtml = String.format("<th class=\"%s\">%s</th>", escapeAttr(cls != null ? cls : ""),
                    value != null ? value : "");
            heads.add(headHtml);
            return this;
        }

        public Table<T> Field(SlotFunction<T> slot, String cls) {
            slots.add(new Slot<>(slot, cls != null ? cls : ""));
            return this;
        }

        public Table<T> FieldText(SlotFunction<T> slot, String cls) {
            SlotFunction<T> safeSlot = item -> escapeHtml(slot != null ? slot.apply(item) : "");
            slots.add(new Slot<>(safeSlot, cls != null ? cls : ""));
            return this;
        }

        public String Render(List<T> data) {
            if (data == null) {
                data = new ArrayList<>();
            }

            StringBuilder headsBuilder = new StringBuilder();
            for (String head : heads) {
                headsBuilder.append(head);
            }

            StringBuilder rowsBuilder = new StringBuilder();
            for (T row : data) {
                rowsBuilder.append("<tr>");
                for (Slot<T> slot : slots) {
                    String cellContent = slot.slot != null ? slot.slot.apply(row) : "";
                    rowsBuilder.append(String.format("<td class=\"%s\">%s</td>", escapeAttr(slot.cls), cellContent));
                }
                rowsBuilder.append("</tr>");
            }

            return String.format(
                    "<div><table class=\"table-auto %s\"><thead><tr>%s</tr></thead><tbody>%s</tbody></table></div>",
                    escapeAttr(css), headsBuilder.toString(), rowsBuilder.toString());
        }
    }

    public static <T> Table<T> Table(String cls) {
        return Table.create(cls);
    }

    public static IRadioDiv IRadioDiv(String name, Object... data) {
        Object temp = data != null && data.length > 0 ? data[0] : null;
        return new IRadioDiv(name, temp);
    }

    public static final class IRadioDiv {
        private final String name;
        private final Object data;
        private String css = "";
        private String cssLabel = "";
        private String onchange = "";
        private String buttonInactive = "";
        private String buttonActive = "border border-blue-400";
        private String button = "cursor-pointer grid rounded-xl";
        private final Target target = Target();
        private List<AOption> options = new ArrayList<>();
        private boolean disabled = false;
        private boolean required = false;
        private boolean visible = true;
        private String radioPosition = "absolute bottom-4 right-4";

        public IRadioDiv(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        public IRadioDiv Options(List<AOption> opts) {
            this.options = opts != null ? new ArrayList<>(opts) : new ArrayList<>();
            return this;
        }

        public IRadioDiv Class(String... value) {
            this.css = String.join(" ", value);
            return this;
        }

        public IRadioDiv ClassLabel(String... value) {
            this.cssLabel = String.join(" ", value);
            return this;
        }

        public IRadioDiv Required(boolean... value) {
            this.required = value != null && value.length > 0 ? value[0] : true;
            return this;
        }

        public IRadioDiv Disabled(boolean... value) {
            this.disabled = value != null && value.length > 0 ? value[0] : true;
            return this;
        }

        public IRadioDiv Change(String action) {
            this.onchange = action;
            return this;
        }

        public IRadioDiv RadioPosition(String cls) {
            this.radioPosition = cls != null ? cls : "absolute bottom-4 right-4";
            return this;
        }

        public IRadioDiv If(boolean value) {
            this.visible = value;
            return this;
        }

        public String form = "";

        public IRadioDiv Form(String formId) {
            this.form = formId != null ? formId : "";
            return this;
        }

        private String currentValue() {
            Object resolved = getPath(data, name);
            return resolved != null ? String.valueOf(resolved) : "";
        }

        public String Render(String labelText) {
            if (!visible) {
                return "";
            }

            String value = currentValue();
            String hiddenInput = input("",
                    Attr.of().type("hidden").name(name).value(value).onchange(onchange != null ? onchange : ""));

            List<String> cardItems = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                AOption option = options.get(i);
                if (option == null)
                    continue;

                String radioID = target.id + "_" + i;
                boolean checked = option.id != null && option.id.equals(value);
                String checkedAttr = checked ? "checked" : null;

                String cardClass = Classes(
                        "relative",
                        button,
                        Ui.If(disabled, () -> "opacity-50 pointer-events-none"),
                        Or(checked, () -> buttonActive, () -> buttonInactive));

                String onClickJs = String.format(
                        "var cards=document.querySelectorAll('[target=%s]');" +
                                "cards.forEach(function(card){" +
                                "card.classList.value='%s';" +
                                "var radio=card.querySelector('input[type=\"radio\"]');" +
                                "if(radio)radio.checked=false;" +
                                "});" +
                                "event.currentTarget.classList.value='%s';" +
                                "var radio=event.currentTarget.querySelector('input[type=\"radio\"]');" +
                                "if(radio){" +
                                "radio.checked=true;" +
                                "var el=document.getElementById('%s');" +
                                "if(el!=null){" +
                                "el.value='%s';" +
                                "el.dispatchEvent(new Event('change'));" +
                                "}" +
                                "}",
                        target.id,
                        Classes("relative", button, buttonInactive),
                        Classes("relative", button, buttonActive),
                        target.id,
                        escapeJs(escapeAttr(option.id != null ? option.id : "")));

                Attr radioAttr = Attr.of()
                        .type("radio")
                        .id(radioID)
                        .name(name)
                        .value(option.id != null ? option.id : "")
                        .checked(checkedAttr)
                        .required(required)
                        .disabled(disabled)
                        .onchange(String.format(
                                "var el=document.getElementById('%s');if(el==null)return;el.value='%s';el.dispatchEvent(new Event('change'));",
                                target.id, escapeJs(escapeAttr(option.id != null ? option.id : ""))));
                if (form != null && !form.isEmpty()) {
                    radioAttr.form(form);
                }
                String radioInput = input("hover:cursor-pointer " + radioPosition, radioAttr);

                String cardContent = div(cardClass, Attr.of().target(target.id).onclick(onClickJs)).render(
                        option.value != null ? option.value : "",
                        radioInput);
                cardItems.add(cardContent);
            }

            String labelHtml = "";
            if (labelText != null && !labelText.isEmpty()) {
                labelHtml = Label(Attr.of().id(target.id))
                        .Class(cssLabel)
                        .ClassLabel("text-gray-600")
                        .Required(required)
                        .Render(labelText);
            }

            return div(css).render(
                    labelHtml,
                    hiddenInput,
                    div("w-full flex flex-col gap-4").render(cardItems.toArray(new String[0])));
        }
    }

    public static String Trim(String s) {
        if (s == null)
            return "";
        String x = s;
        x = x.replaceAll("(?s)/\\*.*?\\*/", "");
        x = x.replaceAll("(?s)<!--.*?-->", "");
        // Strip full-line // comments (avoid breaking inline URLs like http://)
        x = x.replaceAll("(?m)^[ \t]*//.*$", "");
        // Collapse newlines/tabs, then long spaces
        x = x.replaceAll("[\t\n]+", "");
        x = x.replaceAll("\\s{4,}", " ");
        return x.trim();
    }

    public static String Normalize(String s) {
        if (s == null)
            return "";
        String x = s.replace("\"", "&quot;");
        x = x.replaceAll("(?s)/\\*.*?\\*/", "");
        x = x.replaceAll("(?s)<!--.*?-->", "");
        x = x.replaceAll("(?m)^[ \t]*//.*$", "");
        x = x.replaceAll("[\t\n]+", "");
        x = x.replaceAll("\\s{4,}", " ");
        return x.trim();
    }

    public static String Classes(String... values) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                joiner.add(v.trim());
            }
        }
        return Trim(joiner.toString());
    }

    // Helper: conditional string (if condition is true, return value from supplier,
    // else empty)
    public static String If(boolean cond, java.util.function.Supplier<String> value) {
        return cond ? (value != null ? value.get() : "") : "";
    }

    public static String Or(boolean cond, java.util.function.Supplier<String> value,
            java.util.function.Supplier<String> other) {
        return cond ? (value != null ? value.get() : "") : (other != null ? other.get() : "");
    }

    public static Target Target() {
        return new Target(makeId());
    }

    public static String makeId() {
        return "i" + RandomString(15);
    }

    public static String RandomString(int n) {
        if (n <= 0) {
            return "";
        }
        final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(n);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            sb.append(letters.charAt(rnd.nextInt(letters.length())));
        }
        return sb.toString();
    }

    private static String join(String... children) {
        StringBuilder sb = new StringBuilder();
        if (children != null) {
            for (String child : children) {
                if (child == null || child.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(child);
            }
        }
        return sb.toString();
    }

    private static String attributes(List<Attr> attrs) {
        List<String> out = new ArrayList<>();
        if (attrs != null) {
            for (Attr a : attrs) {
                if (a == null) {
                    continue;
                }
                add(out, "id", a.id);
                add(out, "href", a.href);
                add(out, "alt", a.alt);
                add(out, "title", a.title);
                add(out, "src", a.src);
                add(out, "for", a.htmlFor);
                add(out, "type", a.type);
                add(out, "class", a.clazz);
                add(out, "style", a.style);
                add(out, "onclick", a.onclick);
                add(out, "onchange", a.onchange);
                add(out, "onsubmit", a.onsubmit);
                if (a.value != null)
                    add(out, "value", a.value);
                add(out, "checked", a.checked);
                add(out, "selected", a.selected);
                add(out, "name", a.name);
                add(out, "placeholder", a.placeholder);
                add(out, "autocomplete", a.autocomplete);
                add(out, "pattern", a.pattern);
                if (a.cols != null)
                    add(out, "cols", String.valueOf(a.cols));
                if (a.rows != null)
                    add(out, "rows", String.valueOf(a.rows));
                if (a.width != null)
                    add(out, "width", String.valueOf(a.width));
                if (a.height != null)
                    add(out, "height", String.valueOf(a.height));
                add(out, "min", a.min);
                add(out, "max", a.max);
                add(out, "target", a.target);
                add(out, "step", a.step);
                add(out, "form", a.form);
                if (Boolean.TRUE.equals(a.required))
                    out.add("required=\"required\"");
                if (Boolean.TRUE.equals(a.disabled))
                    out.add("disabled=\"disabled\"");
                if (Boolean.TRUE.equals(a.readonly))
                    out.add("readonly=\"readonly\"");
                add(out, "data-accordion", a.dataAccordion);
                add(out, "data-accordion-item", a.dataAccordionItem);
                add(out, "data-accordion-content", a.dataAccordionContent);
                add(out, "data-tabs", a.dataTabs);
                add(out, "data-tabs-index", a.dataTabsIndex);
                add(out, "data-tabs-panel", a.dataTabsPanel);
            }
        }
        return String.join(" ", out);
    }

    private static void add(List<String> out, String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        out.add(key + "=\"" + escapeAttr(value) + "\"");
    }

    private static String escapeAttr(String v) {
        if (v == null)
            return "";
        String x = v;
        x = x.replace("&", "&amp;");
        x = x.replace("\"", "&quot;");
        x = x.replace("<", "&lt;");
        x = x.replace(">", "&gt;");
        return x;
    }

    private static Object getPath(Object data, String path) {
        if (data == null || path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
                continue;
            }
            if (current instanceof List) {
                int idx = parseIndex(part);
                List<?> list = (List<?>) current;
                current = idx >= 0 && idx < list.size() ? list.get(idx) : null;
                continue;
            }
            if (current.getClass().isArray()) {
                int idx = parseIndex(part);
                current = idx >= 0 && idx < Array.getLength(current) ? Array.get(current, idx) : null;
                continue;
            }
            current = readField(current, part);
        }
        return current;
    }

    private static int parseIndex(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static Object readField(Object current, String fieldName) {
        try {
            Field field = current.getClass().getField(fieldName);
            return field.get(current);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                Field field = current.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(current);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return null;
            }
        }
    }
}
