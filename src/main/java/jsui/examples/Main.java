package jsui.examples;

import java.io.IOException;
import java.util.List;
import jsui.App;
import jsui.Context;
import jsui.Server;
import jsui.Ui;
import jsui.examples.pages.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class Main {

    private record Route(String path, String title) {
    }

    @FunctionalInterface
    public interface Page {
        String render(Context ctx) throws Exception;
    }

    private static final List<Route> ROUTES = List.of(
            new Route("/", "Showcase"),
            new Route("/icons", "Icons"),
            new Route("/button", "Button"),
            new Route("/text", "Text"),
            new Route("/password", "Password"),
            new Route("/number", "Number"),
            new Route("/date", "Date & Time"),
            new Route("/area", "Textarea"),
            new Route("/select", "Select"),
            new Route("/checkbox", "Checkbox"),
            new Route("/radio", "Radio"),
            new Route("/form", "Form"),
            new Route("/table", "Table"),
            new Route("/captcha", "Captcha"),
            new Route("/others", "Others"),
            new Route("/shared", "Shared"),
            new Route("/collate", "Collate"),
            new Route("/append", "Append/Prepend"),
            new Route("/clock", "Clock"),
            new Route("/deferred", "Deferred"),
            new Route("/spa", "SPA"),
            new Route("/markdown", "Markdown"));

    /**
     * Start the server on the specified port. Used for testing.
     *
     * @param port the port to listen on
     * @return the started Server instance
     * @throws IOException if an I/O error occurs
     */
    public static Server startServer(int port) throws IOException {
        return startServer(port, new App("en"));
    }

    /**
     * Start the server on the specified port with a custom App instance. Used for
     * testing.
     *
     * @param port the port to listen on
     * @param app  the App instance to use
     * @return the started Server instance
     * @throws IOException if an I/O error occurs
     */
    public static Server startServer(int port, App app) throws IOException {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128">\
                <rect width="128" height="128" rx="24" ry="24" fill="#2563eb" stroke="#1e40af" stroke-width="6"/>\
                <text x="50%" y="56%" dominant-baseline="middle" text-anchor="middle" font-size="80" font-weight="700" font-family="Arial, Helvetica, sans-serif" fill="#ffffff">UI</text></svg>""";
        String favicon = """
                <link rel="icon" type="image/svg+xml" sizes="any" href="data:image/svg+xml,%s">"""
                .formatted(java.net.URLEncoder.encode(svg, java.nio.charset.StandardCharsets.UTF_8));
        app.HTMLHead.add(favicon);
        app.HTMLHead.add(
                "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\" integrity=\"sha512-SfTiTlX6kk+qitfevl/7LibUOeJWlt9rbyDn92a1DqWOw9vWG2MFoays0sgObmWazO5BQPiFucnnEAjpAB+/Sw==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />");

        app.Page("/", layout(app, "Showcase", ShowcasePage::render));
        app.Page("/icons", layout(app, "Icons", IconsPage::render));
        app.Page("/button", layout(app, "Button", ButtonPage::render));
        app.Page("/text", layout(app, "Text", TextPage::render));
        app.Page("/password", layout(app, "Password", PasswordPage::render));
        app.Page("/number", layout(app, "Number", NumberPage::render));
        app.Page("/date", layout(app, "Date & Time", DatePage::render));
        app.Page("/area", layout(app, "Textarea", AreaPage::render));
        app.Page("/select", layout(app, "Select", SelectPage::render));
        app.Page("/checkbox", layout(app, "Checkbox", CheckboxPage::render));
        app.Page("/radio", layout(app, "Radio", RadioPage::render));
        app.Page("/form", layout(app, "Form", FormPage::render));
        app.Page("/table", layout(app, "Table", TablePage::render));
        app.Page("/captcha", layout(app, "Captcha", CaptchaPage::render));
        app.Page("/others", layout(app, "Others", OthersPage::render));
        app.Page("/shared", layout(app, "Shared", SharedPage::render));
        app.Page("/collate", layout(app, "Collate", CollatePage::render));
        app.Page("/append", layout(app, "Append / Prepend", AppendPage::render));
        app.Page("/clock", layout(app, "Clock", ClockPage::render));
        app.Page("/deferred", layout(app, "Deferred", DeferredOnlyPage::render));
        app.Page("/spa", layout(app, "SPA", SpaPage::render));
        app.Page("/markdown", layout(app, "Markdown", MarkdownPage::render));

        app.debug(true);

        return Server.builder(app)
                .httpPort(port)
                .start();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = startServer(1422);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (IOException ignored) {
            }
        }));

        System.out.println("Showcase running at http://localhost:" + server.httpPort());
        new java.util.concurrent.CountDownLatch(1).await();
    }

    private static Context.Callable layout(App app, String title, Page page) {
        return ctx -> {
            String current = ctx.path != null ? ctx.path.split("\\?")[0].toLowerCase() : "/";
            StringBuilder links = new StringBuilder();
            for (Route route : ROUTES) {
                String base = "px-2 py-1 rounded text-sm whitespace-nowrap";
                boolean active = route.path().equalsIgnoreCase(current);
                String cls = active
                        ? base + " bg-blue-700 text-white hover:bg-blue-600"
                        : base + " hover:bg-gray-200";
                Ui.Attr load = ctx.Load(route.path());
                Ui.Attr attr = Ui.Attr.of().href(route.path());
                if (load != null && load.onclick != null) {
                    attr.onclick(load.onclick);
                }
                String anchor = Ui.a(cls, attr).render(route.title());
                if (links.length() > 0) {
                    links.append(' ');
                }
                links.append(anchor);
            }
            String nav = Ui.div("bg-white shadow mb-6").render(
                    Ui.div("max-w-5xl mx-auto px-4 py-2 flex items-center gap-2").render(
                            Ui.div("flex flex-wrap gap-1 mt-2 md:mt-0").render(links.toString()),
                            Ui.div("flex-1").render(),
                            Ui.ThemeSwitcher("")));
            String content = page.render(ctx);
            return app.HTML(title, "bg-gray-100 min-h-screen", nav + Ui.div("max-w-5xl mx-auto px-2").render(content));
        };
    }
}
