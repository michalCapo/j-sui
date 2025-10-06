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

/**
 * Simplified Java port of core helpers from t-sui/ui.ts.
 *
 * Provides HTML string builders, attribute helpers, basic form controls,
 * and Target/Skeleton utilities needed by server/data layers.
 */
public final class Ui {

    private Ui() {
    }

    // ---------------------------------------------------------------------
    // Shared models

    public enum Swap {
        inline, outline, none, append, prepend
    }

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

        public Attr() {
        }

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
    }

    public static Attr targetAttr(Target target) {
        if (target == null || target.id == null) {
            return null;
        }
        return Attr.of().id(target.id);
    }

    public static final class AOption {
        public String id;
        public String value;

        public AOption() {
        }

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

    // ---------------------------------------------------------------------
    // Style constants

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
        String sun = "<svg aria-hidden=\"true\" xmlns=\"http://www.w3.org/2000/svg\" class=\"h-4 w-4\" fill=\"currentColor\" viewBox=\"0 0 24 24\"><path d=\"M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.8 1.42-1.42zm10.48 14.32l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM12 4V1h-0 0 0 0v3zm0 19v-3h0 0 0 0v3zM4 12H1v0 0 0 0h3zm19 0h-3v0 0 0 0h3zM6.76 19.16l-1.79 1.8 1.41 1.41 1.8-1.79-1.42-1.42zM19.16 6.76l1.8-1.79-1.41-1.41-1.8 1.79 1.41 1.41zM12 8a4 4 0 100 8 4 4 0 000-8z\"/></svg>";
        String moon = "<svg aria-hidden=\"true\" xmlns=\"http://www.w3.org/2000/svg\" class=\"h-4 w-4\" fill=\"currentColor\" viewBox=\"0 0 24 24\"><path d=\"M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z\"/></svg>";
        String desktop = "<svg aria-hidden=\"true\" xmlns=\"http://www.w3.org/2000/svg\" class=\"h-4 w-4\" fill=\"currentColor\" viewBox=\"0 0 24 24\"><path d=\"M3 4h18v12H3z\"/><path d=\"M8 20h8v-2H8z\"/></svg>";

        StringBuilder btn = new StringBuilder();
        btn.append("<button id=\"").append(id).append("\" type=\"button\" class=\"")
                .append("inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-gray-300 bg-white text-gray-700 hover:bg-gray-100 ")
                .append("dark:bg-gray-800 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-700 shadow-sm ")
                .append(Classes(css)).append("\">");
        btn.append("<span class=\"icon\">").append(desktop).append("</span>");
        btn.append("<span class=\"label\">Auto</span></button>");

        StringBuilder script = new StringBuilder();
        script.append("<script>(function(){");
        script.append("var btn=document.getElementById(\"").append(id).append("\"); if(!btn) return;");
        script.append(
                "var modes=[\"system\",\"light\",\"dark\"]; function getPref(){ try { return localStorage.getItem(\"theme\")||\"system\"; } catch(_) { return \"system\"; } }");
        script.append(
                "function resolve(mode){ if(mode===\"system\"){ try { return (window.matchMedia && window.matchMedia(\"(prefers-color-scheme: dark)\").matches)?\"dark\":\"light\"; } catch(_) { return \"light\"; } } return mode; }");
        script.append(
                "function setMode(mode){ try { if (typeof setTheme === \"function\") setTheme(mode); } catch(_){} }");
        script.append(
                "function labelFor(mode){ return mode===\"system\"?\"Auto\":(mode.charAt(0).toUpperCase()+mode.slice(1)); }");
        script.append("function iconFor(effective){ if(effective===\"dark\"){ return `").append(moon)
                .append("`; } if(effective===\"light\"){ return `").append(sun).append("`; } return `")
                .append(desktop).append("`; }");
        script.append(
                "function render(){ var pref=getPref(); var eff=resolve(pref); var icon=iconFor(eff); var i=btn.querySelector(\".icon\"); if(i){ i.innerHTML=icon; } var l=btn.querySelector(\".label\"); if(l){ l.textContent=labelFor(pref); } }");
        script.append("render();");
        script.append(
                "btn.addEventListener(\"click\", function(){ var pref=getPref(); var idx=modes.indexOf(pref); var next=modes[(idx+1)%modes.length]; setMode(next); render(); });");
        script.append(
                "try { if (window.matchMedia){ window.matchMedia(\"(prefers-color-scheme: dark)\").addEventListener(\"change\", function(){ if(getPref()===\"system\"){ render(); } }); } } catch(_){ }");
        script.append("})();</script>");

        return btn.toString() + script.toString();
    }

    // Simple helper to embed inline scripts safely
    public static String Script(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isEmpty())
                sb.append(p);
        }
        return Trim("<script>" + sb + "</script>");
    }

    // Google reCAPTCHA integration (client-side render). When siteKey is empty,
    // renders only the secured fallback markup.
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

        String js = "setTimeout(function(){" +
                "var note=document.getElementById('" + noteId + "');" +
                "var captcha=document.getElementById('" + captchaId + "');" +
                "var hidden=document.getElementById('" + hiddenId + "');" +
                "var loaded=window.grecaptcha||null;" +
                "if(loaded==null){setTimeout(function(){if(!window.grecaptcha){note.classList.remove('hidden');}},1200);}"
                +
                "else{loaded.ready(function(){loaded.render('" + captchaId + "',{sitekey:'" + siteKey
                + "',callback:function(){requestAnimationFrame(function(){captcha.style.visibility='hidden';hidden.classList.remove('opacity-0');hidden.classList.remove('pointer-events-none');});},"
                +
                "'expired-callback':function(){requestAnimationFrame(function(){captcha.style.visibility='visible';hidden.classList.add('opacity-0');hidden.classList.add('pointer-events-none');loaded.reset();});},"
                +
                "'error-callback':function(){requestAnimationFrame(function(){captcha.style.visibility='visible';hidden.classList.add('opacity-0');hidden.classList.add('pointer-events-none');loaded.reset();});}});});}"
                +
                "},300);";

        return Normalize(div("").render(top, note) + Script(js));
    }

    // Lightweight client-side CAPTCHA (for demos only). Use server-side validation
    // by checking the hidden field 'js_captcha_verified'.
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

        String js = String.join("",
                "setTimeout(function(){",
                "var canvas=document.getElementById('", canvasId, "');",
                "var ctx=canvas.getContext('2d');",
                "var input=document.getElementById('", inputId, "');",
                "var hidden=document.getElementById('", hiddenId, "');",
                "var captchaText=", escapeJs(text), ";",
                "function sizeCanvas(){var ratio=window.devicePixelRatio||1;var dw=Math.min(320,canvas.clientWidth||320);var dh=96;canvas.width=Math.floor(dw*ratio);canvas.height=Math.floor(dh*ratio);ctx.setTransform(ratio,0,0,ratio,0,0);canvas.style.width=dw+'px';canvas.style.height=dh+'px';}",
                "function draw(){sizeCanvas();var w=canvas.clientWidth||320;var h=canvas.clientHeight||96;ctx.clearRect(0,0,w,h);ctx.fillStyle='#f0f0f0';ctx.fillRect(0,0,w,h);ctx.font='bold 24px Arial';ctx.textBaseline='middle';ctx.textAlign='center';for(var i=0;i<captchaText.length;i++){var ch=captchaText[i];var x=(w/captchaText.length)*i+(w/captchaText.length)/2;var y=h/2+(Math.random()*10-5);ctx.save();ctx.translate(x,y);ctx.rotate((Math.random()*0.5-0.25));ctx.fillStyle='rgb('+Math.floor(Math.random()*200)+','+Math.floor(Math.random()*200)+','+Math.floor(Math.random()*200)+')';ctx.fillText(ch,0,0);ctx.restore();}for(var j=0;j<20;j++){ctx.beginPath();ctx.arc(Math.random()*w,Math.random()*h,Math.random()*2,0,Math.PI*2);ctx.fillStyle='rgba(0,0,0,0.3)';ctx.fill();}}",
                "function validate(){if((input.value||'').toLowerCase()===captchaText.toLowerCase()){hidden.value='true';input.style.borderColor='green';}else{hidden.value='false';input.style.borderColor='red';}}",
                "input.addEventListener('input', validate);draw();window.addEventListener('resize', draw);",
                "},300);");

        return div("",
                Attr.of().style(
                        "display:flex;flex-wrap:wrap;align-items:center;gap:10px;margin-bottom:10px;width:100%;"))
                .render(canvas, input, hidden, Script(js));
    }

    private static String escapeJs(String html) {
        return "\"" + html.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
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

    // ---------------------------------------------------------------------
    // Tag builders

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

    public static final class TagBuilder {
        private final String tag;
        private final List<Attr> attrs;

        TagBuilder(String tag, String css, Attr... extras) {
            this.tag = tag;
            this.attrs = collectAttrs(css, extras);
        }

        public String render(String... children) {
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

    // ---------------------------------------------------------------------
    // Form helpers

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

    // Convenience wrappers
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

    // ---------------------------------------------------------------------
    // Markdown (very small subset converter)

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
                if (minDate != null) minStr = new SimpleDateFormat("yyyy-MM-dd").format(minDate);
                if (maxDate != null) maxStr = new SimpleDateFormat("yyyy-MM-dd").format(maxDate);
            } else if ("time".equals(as)) {
                if (minDate != null) minStr = new SimpleDateFormat("HH:mm").format(minDate);
                if (maxDate != null) maxStr = new SimpleDateFormat("HH:mm").format(maxDate);
            } else if ("datetime-local".equals(as)) {
                if (minDate != null) minStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(minDate);
                if (maxDate != null) maxStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(maxDate);
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
            if (minStr != null) at.min(minStr);
            if (maxStr != null) at.max(maxStr);

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

        public String Render(String inner) {
            if (!visible) {
                return "";
            }
            String classes = Classes(BTN, size, css, color, disabled ? DISABLED : null);
            Attr attr = Attr.of().onclick(onclick);
            if (disabled) {
                attr.disabled(true);
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

    // ---------------------------------------------------------------------
    // Utilities

    public static String Trim(String s) {
        if (s == null)
            return "";
        String x = s;
        // Strip JS/TS block comments and HTML comments first
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
        // Keep in sync with Trim for comment removal
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
                if (Boolean.TRUE.equals(a.required))
                    out.add("required=\"required\"");
                if (Boolean.TRUE.equals(a.disabled))
                    out.add("disabled=\"disabled\"");
                if (Boolean.TRUE.equals(a.readonly))
                    out.add("readonly=\"readonly\"");
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

    // ---------------------------------------------------------------------
    // Data helpers

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
