package jsui.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jsui.App;
import jsui.Context;
import jsui.Data;
import jsui.Server;
import jsui.Ui;

public final class Main {
    private Main() {
    }

    private record Route(String path, String title) {
    }

    @FunctionalInterface
    private interface Page {
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
            new Route("/table", "Table"),
            new Route("/others", "Others"),
            new Route("/collate", "Collate"),
            new Route("/append", "Append/Prepend"),
            new Route("/clock", "Clock"),
            new Route("/deferred", "Deferred"),
            new Route("/markdown", "Markdown"));

    public static void main(String[] args) throws IOException, InterruptedException {
        App app = new App("en");
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 128 128\"><rect width=\"128\" height=\"128\" rx=\"24\" ry=\"24\" fill=\"#2563eb\" stroke=\"#1e40af\" stroke-width=\"6\"/><text x=\"50%\" y=\"56%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-size=\"80\" font-weight=\"700\" font-family=\"Arial, Helvetica, sans-serif\" fill=\"#ffffff\">UI</text></svg>";
        String favicon = "<link rel=\"icon\" type=\"image/svg+xml\" sizes=\"any\" href=\"data:image/svg+xml,"
                + java.net.URLEncoder.encode(svg, java.nio.charset.StandardCharsets.UTF_8) + "\">";
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
        app.Page("/table", layout(app, "Table", TablePage::render));
        app.Page("/others", layout(app, "Others", OthersPage::render));
        app.Page("/collate", layout(app, "Collate", CollatePage::render));
        app.Page("/append", layout(app, "Append / Prepend", AppendPage::render));
        app.Page("/clock", layout(app, "Clock", ClockPage::render));
        app.Page("/deferred", layout(app, "Deferred", DeferredOnlyPage::render));
        app.Page("/markdown", layout(app, "Markdown", MarkdownPage::render));

        app.debug(true);

        Server server = Server.builder(app)
                .httpPort(1422)
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.close();
            } catch (IOException ignored) {
            }
        }));

        System.out.println("Showcase running at http://localhost:" + server.httpPort());
        // Block the main thread indefinitely instead of reading from System.in
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

    private static final class ShowcasePage {
        static String render(Context ctx) {
            DemoForm form = new DemoForm();
            return render(ctx, form, null);
        }

        private static final Ui.Target demoTarget = Ui.Target();

        private static String actionSubmit(Context ctx) {
            DemoForm form = new DemoForm();
            ctx.Body(form);
            ctx.Success("Form submitted successfully");
            return render(ctx, form, null);
        }

        private static String render(Context ctx, DemoForm form, String error) {
            List<Ui.AOption> countries = List.of(
                    new Ui.AOption("", "Select..."),
                    new Ui.AOption("USA", "USA"),
                    new Ui.AOption("Slovakia", "Slovakia"),
                    new Ui.AOption("Germany", "Germany"),
                    new Ui.AOption("Japan", "Japan"));
            List<Ui.AOption> genders = List.of(
                    new Ui.AOption("male", "Male"),
                    new Ui.AOption("female", "Female"),
                    new Ui.AOption("other", "Other"));

            String errHtml = "";
            if (error != null) {
                errHtml = Ui.div("text-red-600 p-4 rounded text-center border-4 border-red-600 bg-white").render(error);
            }

            return Ui.div("max-w-full sm:max-w-6xl mx-auto flex flex-col gap-6 w-full").render(
                    Ui.div("text-3xl font-bold").render("Component Showcase"),
                    Ui.div("grid gap-4 sm:gap-6 items-start w-full", Ui.targetAttr(demoTarget)).render(
                            Ui.form("flex flex-col gap-4 bg-white p-6 rounded-lg shadow w-full",
                                    Ui.targetAttr(demoTarget),
                                    ctx.Submit(ShowcasePage::actionSubmit).Replace(Ui.targetAttr(demoTarget))).render(
                                            Ui.div("text-xl font-bold").render("Component Showcase Form"),
                                            errHtml,
                                            Ui.IText("Name", form).Required(true).Render("Name"),
                                            Ui.IEmail("Email", form).Required(true).Render("Email"),
                                            Ui.IPhone("Phone", form).Render("Phone"),
                                            Ui.IPassword("Password", null).Required(true).Render("Password"),
                                            Ui.INumber("Age", form).Numbers(0.0, 120.0, 1.0).Render("Age"),
                                            Ui.INumber("Price", form).Format("%.2f").Render("Price (USD)"),
                                            Ui.IArea("Bio", form).Rows(4).Render("Short Bio"),
                                            Ui.div("block sm:hidden").render(
                                                    Ui.div("text-sm font-bold").render("Gender"),
                                                    Ui.IRadio("Gender", form).Value("male").Render("Male"),
                                                    Ui.IRadio("Gender", form).Value("female").Render("Female"),
                                                    Ui.IRadio("Gender", form).Value("other").Render("Other")),
                                            Ui.div("hidden sm:block overflow-x-auto").render(
                                                    Ui.IRadioButtons("Gender", form).Options(genders).Render("Gender")),
                                            Ui.ISelect("Country", form).Options(countries).Placeholder("Select...")
                                                    .Render("Country"),
                                            Ui.ICheckbox("Agree", form).Required().Render("I agree to the terms"),
                                            Ui.IDate("BirthDate", form).Render("Birth Date"),
                                            Ui.ITime("AlarmTime", form).Render("Alarm Time"),
                                            Ui.IDateTime("Meeting", form).Render("Meeting (Local)"),
                                            Ui.div("flex gap-2 mt-2").render(
                                                    Ui.Button().Submit().Color(Ui.Blue).Class("rounded")
                                                            .Render("Submit"),
                                                    Ui.Button().Reset().Color(Ui.Gray).Class("rounded")
                                                            .Render("Reset")))));
        }

        private static final class DemoForm {
            public String Name = "";
            public String Email = "";
            public String Phone = "";
            public String Password = "";
            public double Age = 0;
            public double Price = 0;
            public String Bio = "";
            public String Gender = "";
            public String Country = "";
            public boolean Agree = false;
            public java.util.Date BirthDate = new java.util.Date();
            public java.util.Date AlarmTime = new java.util.Date();
            public java.util.Date Meeting = new java.util.Date();
        }
    }

    private static final class ButtonPage {
        static String render(Context ctx) {
            String[][] sizeDefs = {
                    { Ui.XS, "Extra small" },
                    { Ui.SM, "Small" },
                    { Ui.MD, "Medium (default)" },
                    { Ui.ST, "Standard" },
                    { Ui.LG, "Large" },
                    { Ui.XL, "Extra large" },
            };

            String[][] solid = {
                    { Ui.Blue, "Blue" },
                    { Ui.Green, "Green" },
                    { Ui.Red, "Red" },
                    { Ui.Purple, "Purple" },
                    { Ui.Yellow, "Yellow" },
                    { Ui.Gray, "Gray" },
                    { Ui.White, "White" },
            };

            String[][] outline = {
                    { Ui.BlueOutline, "Blue (outline)" },
                    { Ui.GreenOutline, "Green (outline)" },
                    { Ui.RedOutline, "Red (outline)" },
                    { Ui.PurpleOutline, "Purple (outline)" },
                    { Ui.YellowOutline, "Yellow (outline)" },
                    { Ui.GrayOutline, "Gray (outline)" },
                    { Ui.WhiteOutline, "White (outline)" },
            };

            StringBuilder colorsGrid = new StringBuilder();
            for (String[] entry : solid) {
                colorsGrid.append(
                        Ui.Button()
                                .Color(entry[0])
                                .Class("rounded w-full")
                                .Render(entry[1]));
            }
            for (String[] entry : outline) {
                colorsGrid.append(
                        Ui.Button()
                                .Color(entry[0])
                                .Class("rounded w-full")
                                .Render(entry[1]));
            }
            String colors = Ui.div("grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2")
                    .render(colorsGrid.toString());

            StringBuilder sizes = new StringBuilder();
            for (String[] entry : sizeDefs) {
                sizes.append(example(
                        entry[1],
                        Ui.Button()
                                .Size(entry[0])
                                .Class("rounded")
                                .Color(Ui.Blue)
                                .Render("Click me")));
            }
            String sizesGrid = Ui.div("flex flex-col gap-2").render(sizes.toString());

            String basics = Ui.div("flex flex-col gap-2").render(
                    example("Button", Ui.Button().Class("rounded").Color(Ui.Blue).Render("Click me")),
                    example("Button — disabled",
                            Ui.Button().Disabled().Class("rounded").Color(Ui.Blue).Render("Unavailable")),
                    example("Button as link",
                            Ui.a(Ui.Classes(Ui.BTN, Ui.MD, "rounded", Ui.Blue),
                                    Ui.Attr.of().href("https://example.com")).render("Visit example.com")),
                    example("Submit button (visual)",
                            Ui.Button().Submit().Class("rounded").Color(Ui.Green).Render("Submit")),
                    example("Reset button (visual)",
                            Ui.Button().Reset().Class("rounded").Color(Ui.Gray).Render("Reset")));

            String colorsCard = card("Colors (solid and outline)", colors);
            String sizesCard = card("Sizes", sizesGrid);
            String basicsCard = card("Basics", basics);

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Button"),
                    Ui.div("text-gray-600")
                            .render("Common button states and variations. Clicks here are for visual demo only."),
                    basicsCard,
                    colorsCard,
                    sizesCard);
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String example(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4 w-full").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    control);
        }
    }

    private static final class IconsPage {
        static String render(Context ctx) {
            Ui.TagBuilder row = Ui.div("flex items-center gap-3 bg-white border rounded p-4");
            String basic = row.render(Ui.Icon("w-6 h-6 bg-gray-400 rounded"), Ui.div("flex-1").render("Basic icon"));
            String start = row.render(Ui.IconStart("w-6 h-6 bg-gray-400 rounded", "Start aligned icon"));
            String left = row.render(Ui.IconLeft("w-6 h-6 bg-blue-600 rounded", "Centered with icon left"));
            String right = row.render(Ui.IconRight("w-6 h-6 bg-green-600 rounded", "Centered with icon right"));
            String end = row.render(Ui.IconEnd("w-6 h-6 bg-purple-600 rounded", "End-aligned icon"));
            String card = Ui.div("").render(
                    Ui.div("flex flex-col gap-3").render(basic, start, left, right, end));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Icons"),
                    Ui.div("text-gray-600").render("Icon positioning helpers and layouts."),
                    card);
        }
    }

    private static final class AppendPage {
        static String render(Context ctx) {
            Ui.Target target = Ui.Target();
            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Append / Prepend"),
                    Ui.div("text-gray-600").render("Demonstrates appending and prepending items to a container."),
                    Ui.div("bg-white p-6 rounded-lg shadow flex items-center gap-3").render(
                            Ui.Button().Color(Ui.Green).Class("rounded")
                                    .Click(ctx.Call(AppendPage::doPrepend).Prepend(Ui.targetAttr(target)))
                                    .Render("Prepend"),
                            Ui.Button().Color(Ui.Purple).Class("rounded")
                                    .Click(ctx.Call(AppendPage::doAppend).Append(Ui.targetAttr(target)))
                                    .Render("Append")),
                    Ui.div("bg-white p-6 rounded-lg shadow min-h-20 space-y-2", Ui.targetAttr(target)).render());
        }

        private static String doAppend(Context ctx) {
            return Ui.div("px-3 py-2 rounded bg-blue-50 border").render("Appended at " + java.time.LocalTime.now());
        }

        private static String doPrepend(Context ctx) {
            return Ui.div("px-3 py-2 rounded bg-green-50 border").render("Prepended at " + java.time.LocalTime.now());
        }
    }

    private static final class ClockPage {
        static String render(Context ctx) {
            Ui.Target target = Ui.Target();

            // Start with current time immediately
            String currentTime = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String timeBody = Ui.div("text-4xl font-mono tracking-widest").render(currentTime);
            String clockContent = Ui.div("flex items-baseline gap-3").render(timeBody,
                    Ui.div("text-gray-500").render("Live server time"));

            // Use inline swap so the target id persists across patches
            ctx.Repeat(target.Render, 1000, c -> {
                String time = java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                String body = Ui.div("text-4xl font-mono tracking-widest").render(time);
                return Ui.div("flex items-baseline gap-3").render(
                        body,
                        Ui.div("text-gray-500").render("Live server time"));
            });

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Clock"),
                    Ui.div("text-gray-600").render("Updates every second via live patches."),
                    Ui.div("bg-white p-6 rounded-lg shadow", Ui.targetAttr(target)).render(clockContent));
        }
    }

    private static final class DeferredOnlyPage {
        static String render(Context ctx) {
            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Deferred"),
                    Ui.div("text-gray-600").render("Shows skeleton replaced with async content via live patches."),
                    Ui.div("bg-white p-6 rounded-lg shadow").render(
                            OthersPage.DeferredComponent.render(ctx)));
        }
    }

    private static final class MarkdownPage {
        static String render(Context ctx) {
            String md = String.join("\n",
                    "# Markdown Demo",
                    "",
                    "This page shows the Ui.Markdown wrapper.",
                    "",
                    "## Features",
                    "- Headings (#, ##, ###)",
                    "- Paragraphs and line breaks",
                    "- Inline code like `console.log('hi')`",
                    "- Bold **strong** and *emphasis*",
                    "",
                    "### Code Block",
                    "```",
                    "function greet(name) {",
                    "  return 'Hello, ' + name;",
                    "}",
                    "```",
                    "",
                    "That's it!");

            String body = Ui.Markdown("prose max-w-none", md);
            return Ui.div("max-w-full sm:max-w-3xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Markdown"),
                    Ui.div("bg-white p-6 rounded-lg shadow").render(body));
        }
    }

    private static final class TextPage {
        static String render(Context ctx) {
            TextData data = new TextData();
            data.Name = "John Doe";

            String basics = Ui.div("flex flex-col gap-2").render(
                    row("Default", Ui.IText("Name", data).Render("Name")),
                    row("With placeholder", Ui.IText("X", null).Placeholder("Type your name").Render("Your name")),
                    row("Required field", Ui.IText("Y", null).Required().Render("Required field")),
                    row("Readonly", Ui.IText("Y2", null).Readonly().Value("Read-only value").Render("Readonly field")),
                    row("Disabled", Ui.IText("Z", null).Disabled().Placeholder("Cannot type").Render("Disabled")),
                    row("With preset value", Ui.IText("Preset", null).Value("Preset text").Render("Preset")));

            String styling = Ui.div("flex flex-col gap-2").render(
                    row("Wrapper .Class()",
                            Ui.IText("C1", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                    row("Label .ClassLabel()",
                            Ui.IText("C2", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                    row("Input .ClassInput()",
                            Ui.IText("C3", null).ClassInput("bg-blue-50").Render("Custom input background")),
                    row("Size: XS", Ui.IText("S1", null).Size(Ui.XS).Render("XS")),
                    row("Size: MD (default)", Ui.IText("S2", null).Size(Ui.MD).Render("MD")),
                    row("Size: XL", Ui.IText("S3", null).Size(Ui.XL).Render("XL")));

            String behavior = Ui.div("flex flex-col gap-2").render(
                    row("Autocomplete", Ui.IText("Auto", null).Autocomplete("name").Render("Name (autocomplete)")),
                    row("Pattern (email-like)",
                            Ui.IText("Pattern", null).Type("email").Pattern("[^@]+@[^@]+\\.[^@]+")
                                    .Placeholder("user@example.com").Render("Email")),
                    row("Type switch (password)", Ui.IText("PassLike", null).Type("password").Render("Password-like")),
                    row("Change handler (console.log)",
                            Ui.IText("Change", null).Change("console.log('changed', this && this.value)")
                                    .Render("On change, log value")),
                    row("Click handler (console.log)",
                            Ui.IText("Click", null).Click("console.log('clicked input')").Render("On click, log")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Text input"),
                    Ui.div("text-gray-600").render("Common features supported by text-like inputs."),
                    card("Basics & states", basics),
                    card("Styling", styling),
                    card("Behavior & attributes", behavior));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String row(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    control);
        }

        private static final class TextData {
            String Name;
        }
    }

    private static final class PasswordPage {
        static String render(Context ctx) {
            String basics = Ui.div("flex flex-col gap-2").render(
                    row("Default", Ui.IPassword("P1", null).Render("Password")),
                    row("With placeholder", Ui.IPassword("P2", null).Placeholder("••••••••").Render("Password")),
                    row("Required", Ui.IPassword("P3", null).Required().Render("Password (required)")),
                    row("Readonly", Ui.IPassword("P4", null).Readonly().Value("secret").Render("Readonly password")),
                    row("Disabled", Ui.IPassword("P5", null).Disabled().Render("Password (disabled)")),
                    row("Preset value", Ui.IPassword("P6", null).Value("topsecret").Render("Preset value")),
                    row("Type switched to text (visible)",
                            Ui.IPassword("P7", null).Type("text").Value("visible value").Render("As text")));

            String styling = Ui.div("flex flex-col gap-2").render(
                    row("Wrapper .Class()",
                            Ui.IPassword("C1", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                    row("Label .ClassLabel()",
                            Ui.IPassword("C2", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                    row("Input .ClassInput()",
                            Ui.IPassword("C3", null).ClassInput("bg-blue-50").Render("Custom input background")),
                    row("Size: XS", Ui.IPassword("S1", null).Size(Ui.XS).Render("XS")),
                    row("Size: XL", Ui.IPassword("S2", null).Size(Ui.XL).Render("XL")));

            String behavior = Ui.div("flex flex-col gap-2").render(
                    row("Autocomplete (new-password)",
                            Ui.IPassword("A1", null).Autocomplete("new-password").Render("New password")),
                    row("Pattern (min 8 chars)",
                            Ui.IPassword("A2", null).Pattern(".{8,}").Placeholder("at least 8 characters")
                                    .Render("Min length pattern")),
                    row("Change handler (console.log)",
                            Ui.IPassword("A3", null).Change("console.log('changed pw', this && this.value)")
                                    .Render("On change, log")),
                    row("Click handler (console.log)",
                            Ui.IPassword("A4", null).Click("console.log('clicked pw')").Render("On click, log")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Password"),
                    Ui.div("text-gray-600").render("Common features and states for password inputs."),
                    card("Basics & states", basics),
                    card("Styling", styling),
                    card("Behavior & attributes", behavior));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String row(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    control);
        }
    }

    private static final class NumberPage {
        static String render(Context ctx) {
            NumberData data = new NumberData();
            data.Age = 30;
            data.Price = 19.9;

            String basics = Ui.div("flex flex-col gap-2").render(
                    row("Integer with range/step", Ui.INumber("Age", data).Numbers(0.0, 120.0, 1.0).Render("Age")),
                    row("Float formatted (%.2f)", Ui.INumber("Price", data).Format("%.2f").Render("Price")),
                    row("Required", Ui.INumber("Req", null).Required().Render("Required")),
                    row("Readonly", Ui.INumber("RO", null).Readonly().Value("42").Render("Readonly")),
                    row("Disabled", Ui.INumber("D", null).Disabled().Render("Disabled")),
                    row("Placeholder", Ui.INumber("PH", null).Placeholder("0..100").Render("Number")));

            String styling = Ui.div("flex flex-col gap-2").render(
                    row("Wrapper .Class()",
                            Ui.INumber("C", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                    row("Label .ClassLabel()",
                            Ui.INumber("CL", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                    row("Input .ClassInput()",
                            Ui.INumber("CI", null).ClassInput("bg-blue-50").Render("Custom input background")),
                    row("Size: LG", Ui.INumber("S", null).Size(Ui.LG).Render("Large size")));

            String behavior = Ui.div("flex flex-col gap-2").render(
                    row("Change handler (console.log)",
                            Ui.INumber("Change", null).Change("console.log('changed', this && this.value)")
                                    .Render("On change, log")),
                    row("Click handler (console.log)",
                            Ui.INumber("Click", null).Click("console.log('clicked number')").Render("On click, log")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Number input"),
                    Ui.div("text-gray-600").render("Ranges, formatting, and common attributes."),
                    card("Basics & states", basics),
                    card("Styling", styling),
                    card("Behavior & attributes", behavior));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String row(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    Ui.div("w-64").render(control));
        }

        private static final class NumberData {
            double Age;
            double Price;
        }
    }

    private static final class DatePage {
        static String render(Context ctx) {
            DateData data = new DateData();
            data.Birth = new java.util.Date();

            String basics = Ui.div("flex flex-col gap-2").render(
                    row("Date", Ui.IDate("Birth", data).Render("Birth date")),
                    row("Time", Ui.ITime("Alarm", null).Render("Alarm")),
                    row("DateTime", Ui.IDateTime("Meeting", null).Render("Meeting time")),
                    row("Required date", Ui.IDate("Req", null).Required().Render("Required date")),
                    row("Readonly time", Ui.ITime("RO", null).Readonly().Value("10:00").Render("Readonly time")),
                    row("Disabled datetime", Ui.IDateTime("D", null).Disabled().Render("Disabled datetime")));

            String styling = Ui.div("flex flex-col gap-2").render(
                    row("Wrapper .Class()",
                            Ui.IDate("C", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                    row("Label .ClassLabel()",
                            Ui.ITime("CL", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                    row("Input .ClassInput()",
                            Ui.IDateTime("CI", null).ClassInput("bg-blue-50").Render("Custom input background")),
                    row("Size: ST", Ui.IDate("S", null).Size(Ui.ST).Render("Standard size")));

            String behavior = Ui.div("flex flex-col gap-2").render(
                    row("Change handler (console.log)",
                            Ui.IDate("Change", null).Change("console.log('changed', this && this.value)")
                                    .Render("On change, log")),
                    row("Click handler (console.log)", Ui.IDateTime("Click", null)
                            .Click("console.log('clicked datetime')").Render("On click, log")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Date, Time, DateTime"),
                    Ui.div("text-gray-600").render("Common attributes across temporal inputs."),
                    card("Basics & states", basics),
                    card("Styling", styling),
                    card("Behavior & attributes", behavior));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String row(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    Ui.div("w-64").render(control));
        }

        private static final class DateData {
            java.util.Date Birth;
        }
    }

    private static final class AreaPage {
        static String render(Context ctx) {
            AreaData data = new AreaData();
            data.Bio = "Short text";

            String basics = Ui.div("flex flex-col gap-2").render(
                    row("Default", Ui.IArea("Bio", data).Rows(3).Render("Bio")),
                    row("Placeholder", Ui.IArea("P", null).Placeholder("Tell us something").Rows(3).Render("Your bio")),
                    row("Required", Ui.IArea("R", null).Required().Rows(3).Render("Required")),
                    row("Readonly", Ui.IArea("RO", null).Readonly().Value("Read-only text").Rows(3).Render("Readonly")),
                    row("Disabled", Ui.IArea("D", null).Disabled().Rows(3).Render("Disabled")),
                    row("With preset value",
                            Ui.IArea("V", null).Value("Initial text value").Rows(3).Render("With value")));

            String styling = Ui.div("flex flex-col gap-2").render(
                    row("Wrapper .Class()",
                            Ui.IArea("C", null).Class("p-2 rounded bg-yellow-50").Rows(3).Render("Styled wrapper")),
                    row("Label .ClassLabel()",
                            Ui.IArea("CL", null).ClassLabel("text-purple-700 font-bold").Rows(3)
                                    .Render("Custom label")),
                    row("Input .ClassInput()",
                            Ui.IArea("CI", null).ClassInput("bg-blue-50").Rows(3).Render("Custom input background")),
                    row("Size: XL", Ui.IArea("S", null).Size(Ui.XL).Rows(3).Render("XL size")));

            String behavior = Ui.div("flex flex-col gap-2").render(
                    row("Change handler (console.log)",
                            Ui.IArea("Change", null).Change("console.log('changed', this && this.value)").Rows(3)
                                    .Render("On change, log")),
                    row("Click handler (console.log)", Ui.IArea("Click", null).Click("console.log('clicked textarea')")
                            .Rows(3).Render("On click, log")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Textarea"),
                    Ui.div("text-gray-600").render("Common features supported by textarea."),
                    card("Basics & states", basics),
                    card("Styling", styling),
                    card("Behavior & attributes", behavior));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String row(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    control);
        }

        private static final class AreaData {
            String Bio;
        }
    }

    private static final class SelectPage {
        static String render(Context ctx) {
            List<Ui.AOption> opts = List.of(
                    new Ui.AOption("", "Select..."),
                    new Ui.AOption("one", "One"),
                    new Ui.AOption("two", "Two"),
                    new Ui.AOption("three", "Three"));
            List<Ui.AOption> optsNoPlaceholder = List.of(
                    new Ui.AOption("one", "One"),
                    new Ui.AOption("two", "Two"),
                    new Ui.AOption("three", "Three"));

            SelectData data = new SelectData();
            data.Country = "";

            String basics = Ui.div("flex flex-col gap-2").render(
                    example("Default", Ui.ISelect("Country", data).Options(opts).Render("Country")),
                    example("Placeholder",
                            Ui.ISelect("Country", data).Options(opts).Placeholder("Pick one").Render("Choose")));

            String validation = Ui.div("flex flex-col gap-2").render(
                    example("Error state",
                            Ui.ISelect("Err", null).Options(opts).Placeholder("Please select").Error()
                                    .Render("Invalid")),
                    example("Required + empty",
                            Ui.ISelect("Z", null).Options(opts).Empty().Required().Render("Required")),
                    example("Disabled", Ui.ISelect("Y", null).Options(opts).Disabled().Render("Disabled")));

            String variants = Ui.div("flex flex-col gap-2").render(
                    example("No placeholder + <empty>", Ui.ISelect("Country", data).Options(optsNoPlaceholder)
                            .EmptyText("<empty>").Render("Choose")));

            String sizes = Ui.div("flex flex-col gap-2").render(
                    example("Small (SM)",
                            Ui.ISelect("Country", data).Options(opts).Size(Ui.SM).ClassLabel("text-sm")
                                    .Render("Country")),
                    example("Extra small (XS)", Ui.ISelect("Country", data).Options(opts).Size(Ui.XS)
                            .ClassLabel("text-sm").Render("Country")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Select"),
                    Ui.div("text-gray-600").render("Select input variations, validation, and sizing."),
                    card("Basics", basics),
                    card("Validation", validation),
                    card("Variants", variants),
                    card("Sizes", sizes));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String example(String label, String control) {
            return Ui.div("flex items-center justify-between gap-4 w-full").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    Ui.div("flex items-center gap-3").render(Ui.div("w-64").render(control)));
        }

        private static final class SelectData {
            String Country;
        }
    }

    private static final class CheckboxPage {
        static String render(Context ctx) {
            CheckboxData data = new CheckboxData();
            data.Agree = true;

            String basics = Ui.div("flex flex-col gap-2").render(
                    example("Default", Ui.ICheckbox("Agree", data).Render("I agree")),
                    example("Required", Ui.ICheckbox("Terms", null).Required().Render("Accept terms")),
                    example("Unchecked", Ui.ICheckbox("X", null).Render("Unchecked")),
                    example("Disabled", Ui.ICheckbox("D", null).Disabled().Render("Disabled")));

            String sizes = Ui.div("flex flex-col gap-2").render(
                    example("Small (SM)", Ui.ICheckbox("S", null).Size(Ui.SM).Render("Small")),
                    example("Extra small (XS)", Ui.ICheckbox("XS", null).Size(Ui.XS).Render("Extra small")));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Checkbox"),
                    Ui.div("text-gray-600").render("Checkbox states, sizes, and required validation."),
                    card("Basics", basics),
                    card("Sizes", sizes));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static String example(String label, String control) {
            return Ui.div("flex items-start justify-between gap-4 w-full").render(
                    Ui.div("text-sm text-gray-600").render(label),
                    control);
        }

        private static final class CheckboxData {
            boolean Agree;
        }
    }

    private static final class RadioPage {
        static String render(Context ctx) {
            List<Ui.AOption> genders = List.of(
                    new Ui.AOption("male", "Male"),
                    new Ui.AOption("female", "Female"),
                    new Ui.AOption("other", "Other"));

            RadioData selected = new RadioData();
            selected.Gender = "male";

            String singleRadios = Ui.div("flex flex-col gap-2").render(
                    Ui.IRadio("Gender", selected).Value("male").Render("Male"),
                    Ui.IRadio("Gender", selected).Value("female").Render("Female"),
                    Ui.IRadio("Gender", selected).Value("other").Render("Other"));

            String radiosDefault = Ui.IRadioButtons("Group", null).Options(genders).Render("Gender");
            Group2Data group2 = new Group2Data();
            group2.Group2 = "female";
            String radiosWithSelected = Ui.IRadioButtons("Group2", group2).Options(genders).Render("Gender");

            String validation = Ui.div("flex flex-col gap-2").render(
                    Ui.div("text-sm text-gray-700").render("Required group (no selection)"),
                    Ui.IRadioButtons("ReqGroup", null).Options(genders).Required().Render("Gender (required)"),
                    Ui.div("text-sm text-gray-700").render("Required standalone radios (no selection)"),
                    Ui.div("flex flex-col gap-1").render(
                            Ui.IRadio("ReqSingle", null).Required().Value("a").Render("Option A"),
                            Ui.IRadio("ReqSingle", null).Required().Value("b").Render("Option B"),
                            Ui.IRadio("ReqSingle", null).Required().Value("c").Render("Option C")));

            String sizes = Ui.div("flex flex-col gap-2").render(
                    Ui.IRadio("SizesA", null).Value("a").Render("Default"),
                    Ui.IRadio("SizesB", null).Size(Ui.SM).ClassLabel("text-sm").Value("b").Render("Small (SM)"),
                    Ui.IRadio("SizesC", null).Size(Ui.XS).ClassLabel("text-sm").Value("c").Render("Extra small (XS)"));

            String disabled = Ui.div("flex flex-col gap-2").render(
                    Ui.IRadio("DisA", null).Disabled().Value("a").Render("Disabled A"),
                    Ui.IRadio("DisB", null).Disabled().Value("b").Render("Disabled B"));

            return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                    Ui.div("text-3xl font-bold").render("Radio"),
                    Ui.div("text-gray-600")
                            .render("Single radio inputs and grouped radio buttons with a selected state."),
                    card("Standalone radios (with selection)", singleRadios),
                    card("Radio buttons group (no selection)", radiosDefault),
                    card("Radio buttons group (with selection)", radiosWithSelected),
                    card("Sizes", sizes),
                    card("Validation", validation),
                    card("Disabled", disabled));
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                    Ui.div("text-sm font-bold text-gray-700").render(title),
                    body);
        }

        private static final class RadioData {
            String Gender;
        }

        private static final class Group2Data {
            String Group2;
        }
    }

    private static final class TablePage {
        static String render(Context ctx) {
            Ui.SimpleTable table = Ui.SimpleTable(4, "w-full table-auto");
            table.Class(0, "text-left font-bold p-2 border-b border-gray-200")
                    .Class(1, "text-left p-2 border-b border-gray-200")
                    .Class(2, "text-left p-2 border-b border-gray-200")
                    .Class(3, "text-right p-2 border-b border-gray-200");
            table.Field("ID").Field("Name").Field("Email").Field("Actions");
            table.Field("1").Field("John Doe").Field("john@example.com").Field(
                    Ui.Button().Class("px-3 py-1 rounded").Color(Ui.Blue).Render("View"));
            table.Field("2").Field("Jane Roe").Field("jane@example.com").Field(
                    Ui.Button().Class("px-3 py-1 rounded").Color(Ui.Green).Render("Edit"));
            table.Field("Notice", "text-blue-700 font-semibold text-center").Attr("colspan=\"4\"");
            table.Field("3").Field("No Email User").Empty().Field(
                    Ui.Button().Class("px-3 py-1 rounded").Color(Ui.Gray).Render("Disabled"));
            table.Field("Span 2", "text-center").Attr("colspan=\"2\"")
                    .Field("Right side").Attr("colspan=\"2\"");
            table.Empty().Field("Span across 3 columns").Attr("colspan=\"3\"");
            String tableCard = card("Basic", table.Render());

            Ui.SimpleTable t2 = Ui.SimpleTable(4, "w-full table-auto");
            t2.Class(0, "p-2 border-b border-gray-200")
                    .Class(1, "p-2 border-b border-gray-200")
                    .Class(2, "p-2 border-b border-gray-200")
                    .Class(3, "p-2 border-b border-gray-200");
            t2.Field("Full-width notice", "text-blue-700 font-semibold").Attr("colspan=\"4\"");
            t2.Field("Left span 2").Attr("colspan=\"2\"")
                    .Field("Right span 2").Attr("colspan=\"2\"");
            t2.Field("Span 3").Attr("colspan=\"3\"").Field("End");
            String t2Card = card("Colspan", t2.Render());

            Ui.SimpleTable t3 = Ui.SimpleTable(3, "w-full table-auto");
            t3.Class(0, "text-left p-2 border-b border-gray-200")
                    .Class(1, "text-right p-2 border-b border-gray-200")
                    .Class(2, "text-right p-2 border-b border-gray-200");
            t3.Field("Item").Field("Qty").Field("Amount");
            t3.Field("Apples").Field("3").Field("$6.00");
            t3.Field("Oranges").Field("2").Field("$5.00");
            t3.Field("Total", "font-semibold").Attr("colspan=\"2\"")
                    .Field("$11.00", "font-semibold");
            String t3Card = card("Column Classes & Totals", t3.Render());

            return Ui.div("flex flex-col gap-4").render(
                    Ui.div("text-3xl font-bold").render("Table"),
                    Ui.div("text-gray-600").render("SimpleTable with column classes, colspans, and totals."),
                    tableCard,
                    t2Card,
                    t3Card);
        }

        private static String card(String title, String body) {
            return Ui.div("bg-white rounded shadow p-4 border border-gray-200 overflow-hidden").render(
                    Ui.div("text-lg font-bold").render(title),
                    body);
        }
    }

    private static final class OthersPage {
        static String render(Context ctx) throws Exception {
            String helloCard = simpleCard("Hello", HelloComponent.render(ctx));
            String counterCard = simpleCard("Counter", Ui.div("flex gap-4 items-center p-4 border rounded").render(
                    CounterComponent.render(ctx, 4),
                    Ui.Flex1,
                    CounterComponent.render(ctx, 6)));
            String loginCard = simpleCard("Login", LoginComponent.render(ctx));

            return Ui.div("max-w-full sm:max-w-6xl mx-auto flex flex-col gap-6 w-full").render(
                    Ui.div("text-3xl font-bold").render("Others"),
                    Ui.div("text-gray-600").render("Miscellaneous demos: Hello, Counter, Login, and icon helpers."),
                    Ui.div("grid grid-cols-1 md:grid-cols-2 gap-4").render(
                            helloCard,
                            counterCard,
                            loginCard));
        }

        private static String simpleCard(String title, String body) {
            return Ui.div("bg-white p-6 rounded-lg shadow w-full").render(
                    Ui.div("text-lg font-bold mb-3").render(title),
                    body);
        }

        private static final class HelloComponent {
            private static final String BUTTON_CLASS = "rounded whitespace-nowrap";

            static String render(Context ctx) {
                return Ui.div("gap-4 border rounded p-4").render(
                        Ui.div("grid grid-cols-2 justify-start gap-4 items-center").render(
                                Ui.Button()
                                        .Color(Ui.GreenOutline)
                                        .Class(BUTTON_CLASS)
                                        .Click(ctx.Call(HelloComponent::sayHello).None())
                                        .Render("with ok"),

                                Ui.Button()
                                        .Color(Ui.RedOutline)
                                        .Class(BUTTON_CLASS)
                                        .Click(ctx.Call(HelloComponent::sayError).None())
                                        .Render("with error"),

                                Ui.Button()
                                        .Color(Ui.BlueOutline)
                                        .Class(BUTTON_CLASS)
                                        .Click(ctx.Call(HelloComponent::sayDelay).None())
                                        .Render("with delay"),

                                Ui.Button()
                                        .Color(Ui.YellowOutline)
                                        .Class(BUTTON_CLASS)
                                        .Click(ctx.Call(HelloComponent::sayCrash).None())
                                        .Render("with crash")));
            }

            private static String sayHello(Context ctx) {
                ctx.Success("Hello");
                return "";
            }

            private static String sayDelay(Context ctx) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                ctx.Info("Information");
                return "";
            }

            private static String sayError(Context ctx) {
                ctx.Error("Hello error");
                return "";
            }

            private static String sayCrash(Context ctx) {
                throw new RuntimeException("Hello again");
            }
        }

        private static final class CounterComponent {
            static String render(Context ctx, int start) {
                CounterModel model = new CounterModel();
                model.Count = start;
                return renderCounter(ctx, model);
            }

            private static String decrement(Context ctx) {
                CounterModel model = new CounterModel();
                ctx.Body(model);
                model.Count--;
                if (model.Count < 0) {
                    model.Count = 0;
                }
                return renderCounter(ctx, model);
            }

            private static String increment(Context ctx) {
                CounterModel model = new CounterModel();
                ctx.Body(model);
                model.Count++;
                return renderCounter(ctx, model);
            }

            private static String renderCounter(Context ctx, CounterModel model) {
                Ui.Target target = Ui.Target();
                return Ui.div("flex gap-2 items-center bg-purple-500 rounded text-white p-px", Ui.targetAttr(target))
                        .render(
                                Ui.Button()
                                        .Click(ctx.Call(CounterComponent::decrement, model)
                                                .Replace(Ui.targetAttr(target)))
                                        .Class("rounded-l px-5")
                                        .Render("-"),
                                Ui.div("text-2xl").render(String.valueOf(model.Count)),
                                Ui.Button()
                                        .Class("rounded-r px-5")
                                        .Click(ctx.Call(CounterComponent::increment, model)
                                                .Replace(Ui.targetAttr(target)))
                                        .Render("+"));
            }

            private static final class CounterModel {
                public int Count;
            }
        }

        private static final class LoginComponent {
            private static final Ui.Target target = Ui.Target();

            static String render(Context ctx) {
                return render(ctx, new LoginForm(), null);
            }

            private static String submit(Context ctx) {
                LoginForm form = new LoginForm();
                ctx.Body(form);
                if (!"user".equals(form.Name) || !"password".equals(form.Password)) {
                    return render(ctx, form, "Invalid credentials");
                }
                return Ui.div("text-green-600 max-w-md p-8 text-center font-bold rounded-lg bg-white shadow-xl")
                        .render("Success");
            }

            private static String render(Context ctx, LoginForm form, String error) {
                String errHtml = "";
                if (error != null) {
                    errHtml = Ui.div("text-red-600 p-4 rounded text-center border-4 border-red-600 bg-white")
                            .render(error);
                }
                return Ui.form("border flex flex-col gap-4 max-w-md bg-white p-8 rounded-lg shadow-xl",
                        Ui.targetAttr(target), ctx.Submit(LoginComponent::submit).Replace(Ui.targetAttr(target)))
                        .render(
                                errHtml,
                                Ui.IText("Name", form).Required().Render("Name"),
                                Ui.IPassword("Password", form).Required().Render("Password"),
                                Ui.Button().Submit().Color(Ui.Blue).Class("rounded").Render("Login"));
            }

            private static final class LoginForm {
                public String Name = "";
                public String Password = "";
            }
        }

        private static final class DeferredComponent {
            static String render(Context ctx) {
                Ui.Target target = Ui.Target();
                // One-shot delayed patch using the new helper; auto-cancellable on navigation
                ctx.Delay(target.Replace, 1000, c -> Ui.div("bg-white rounded-lg shadow p-4").render(
                        Ui.div("text-green-700 font-semibold").render("Deferred content ready"),
                        Ui.div("text-gray-600")
                                .render("This content was rendered asynchronously and delivered via live patch.")));
                return Ui.div("space-y-4", Ui.targetAttr(target)).render(
                        target.Skeleton(Ui.SkeletonType.component));
            }
        }
    }

    private static final class CollatePage {
        static String render(Context ctx) throws Exception {
            seed();

            Data.TQuery init = new Data.TQuery();
            init.Limit = 10;
            init.Offset = 0;
            init.Order = "createdat desc";
            init.Search = "";
            init.Filter = new ArrayList<>();

            Data.CollateModel<Row> collate = Data.Collate(init, CollatePage::load);
            collate.setFilter(buildFilters());
            collate.setSort(buildSort());
            collate.setExcel(buildExcel());
            collate.Row((row, index) -> renderRow(row));

            String body = Ui.div("flex flex-col gap-4").render(
                    Ui.div("text-3xl font-bold").render("Data Collation"),
                    Ui.div("text-gray-600 mb-2")
                            .render("Search, sort, filter, and paging over an in-memory dataset of 100 rows."),
                    collate.Render(ctx));

            return Ui.div("flex flex-col gap-4").render(body);
        }

        private static String renderRow(Row r) {
            String created = new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.CreatedAt);
            return Ui.div("bg-white rounded border border-gray-200 p-3 flex items-center gap-3").render(
                    Ui.div("w-12 text-right font-mono text-gray-500").render("#" + r.ID),
                    Ui.div("flex-1").render(
                            Ui.div("font-semibold").render(
                                    r.Name + Ui.space
                                            + Ui.div("inline text-gray-500 text-sm").render("(" + r.Role + ")")),
                            Ui.div("text-gray-600 text-sm").render(r.Email + " · " + r.City)),
                    Ui.div("text-gray-500 text-sm").render(created),
                    Ui.div("ml-2").render(
                            Ui.Button()
                                    .Class("w-20 text-center px-2 py-1 rounded")
                                    .Color(r.Active ? Ui.Green : Ui.Gray)
                                    .Render(r.Active ? "Active" : "Inactive")));
        }

        private static Data.LoadResult<Row> load(Data.TQuery query) {
            List<Row> list = new ArrayList<>();
            for (Row r : DB) {
                list.add(r.copy());
            }
            int total = list.size();

            String search = query.Search != null ? query.Search.trim() : "";
            if (!search.isEmpty()) {
                String needle = Data.NormalizeForSearch(search);
                list.removeIf(r -> {
                    String hay = Data.NormalizeForSearch(r.Name) + " " + Data.NormalizeForSearch(r.Email) + " "
                            + Data.NormalizeForSearch(r.City);
                    return !hay.contains(needle);
                });
            }

            if (query.Filter != null) {
                for (Data.TField f : query.Filter) {
                    if (f == null)
                        continue;
                    if (f.As == Data.BOOL && f.Bool) {
                        list.removeIf(r -> !r.Active);
                    } else if (f.As == Data.SELECT && f.Value != null && !f.Value.isEmpty()) {
                        String v = f.Value.toLowerCase();
                        list.removeIf(r -> !r.Role.equalsIgnoreCase(v));
                    } else if (f.As == Data.DATES && f.Dates != null) {
                        if (f.Dates.From != null && f.Dates.From.getTime() > 0) {
                            long from = truncateStart(f.Dates.From).getTime();
                            list.removeIf(r -> r.CreatedAt.getTime() < from);
                        }
                        if (f.Dates.To != null && f.Dates.To.getTime() > 0) {
                            long to = truncateEnd(f.Dates.To).getTime();
                            list.removeIf(r -> r.CreatedAt.getTime() > to);
                        }
                    }
                }
            }

            sort(list, query.Order != null ? query.Order : "createdat desc");

            int filtered = list.size();
            int offset = query.Offset > 0 ? query.Offset : 0;
            int limit = query.Limit > 0 ? query.Limit : 10;
            if (offset > filtered) {
                offset = 0;
            }
            int toIndex = Math.min(offset + limit, filtered);
            List<Row> page = list.subList(offset, toIndex);

            Data.LoadResult<Row> result = new Data.LoadResult<>();
            result.total = total;
            result.filtered = filtered;
            result.data.addAll(page);
            return result;
        }

        private static void sort(List<Row> list, String order) {
            OrderSpec spec = parseOrder(order, "createdat", "desc");
            list.sort((a, b) -> {
                int cmp;
                switch (spec.field) {
                    case "name":
                        cmp = a.Name.compareToIgnoreCase(b.Name);
                        break;
                    case "email":
                        cmp = a.Email.compareToIgnoreCase(b.Email);
                        break;
                    case "city":
                        cmp = a.City.compareToIgnoreCase(b.City);
                        break;
                    default:
                        cmp = Long.compare(a.CreatedAt.getTime(), b.CreatedAt.getTime());
                        break;
                }
                return "desc".equals(spec.dir) ? -cmp : cmp;
            });
        }

        private static OrderSpec parseOrder(String s, String defField, String defDir) {
            if (s == null) {
                return new OrderSpec(defField, defDir);
            }
            String txt = s.trim();
            if (txt.isEmpty()) {
                return new OrderSpec(defField, defDir);
            }
            String[] parts = txt.split("\\s+");
            String field = parts.length > 0 ? parts[0].toLowerCase() : defField;
            String dir = parts.length > 1 ? parts[1].toLowerCase() : defDir;
            if (!"asc".equals(dir) && !"desc".equals(dir)) {
                dir = defDir;
            }
            return new OrderSpec(field, dir);
        }

        private static java.util.Date truncateStart(java.util.Date date) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        private static java.util.Date truncateEnd(java.util.Date date) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            cal.set(java.util.Calendar.MILLISECOND, 999);
            return cal.getTime();
        }

        private static List<Data.TField> buildFilters() {
            List<Data.TField> fields = new ArrayList<>();

            Data.TField active = new Data.TField();
            active.DB = "Active";
            active.Field = "Active";
            active.Text = "Active";
            active.Value = "";
            active.As = Data.BOOL;
            active.Options = new ArrayList<>();
            active.Bool = false;
            active.Dates = new Data.TFieldDates();
            fields.add(active);

            Data.TField created = new Data.TField();
            created.DB = "CreatedAt";
            created.Field = "CreatedAt";
            created.Text = "Created";
            created.As = Data.DATES;
            created.Dates = new Data.TFieldDates();
            fields.add(created);

            Data.TField role = new Data.TField();
            role.DB = "Role";
            role.Field = "Role";
            role.Text = "Role";
            role.As = Data.SELECT;
            role.Options = List.of(
                    new Ui.AOption("", "All"),
                    new Ui.AOption("user", "User"),
                    new Ui.AOption("admin", "Admin"),
                    new Ui.AOption("manager", "Manager"),
                    new Ui.AOption("support", "Support"));
            role.Dates = new Data.TFieldDates();
            fields.add(role);

            return fields;
        }

        private static List<Data.TField> buildSort() {
            List<Data.TField> fields = new ArrayList<>();

            Data.TField name = new Data.TField();
            name.DB = "Name";
            name.Field = "Name";
            name.Text = "Name";
            fields.add(name);

            Data.TField email = new Data.TField();
            email.DB = "Email";
            email.Field = "Email";
            email.Text = "Email";
            fields.add(email);

            Data.TField city = new Data.TField();
            city.DB = "City";
            city.Field = "City";
            city.Text = "City";
            fields.add(city);

            Data.TField created = new Data.TField();
            created.DB = "CreatedAt";
            created.Field = "CreatedAt";
            created.Text = "Created";
            fields.add(created);

            return fields;
        }

        private static List<Data.TField> buildExcel() {
            List<Data.TField> fields = new ArrayList<>();
            Data.TField id = new Data.TField();
            id.DB = "ID";
            id.Field = "ID";
            id.Text = "#";
            fields.add(id);
            Data.TField name = new Data.TField();
            name.DB = "Name";
            name.Field = "Name";
            name.Text = "Name";
            fields.add(name);
            Data.TField email = new Data.TField();
            email.DB = "Email";
            email.Field = "Email";
            email.Text = "Email";
            fields.add(email);
            Data.TField city = new Data.TField();
            city.DB = "City";
            city.Field = "City";
            city.Text = "City";
            fields.add(city);
            Data.TField role = new Data.TField();
            role.DB = "Role";
            role.Field = "Role";
            role.Text = "Role";
            fields.add(role);
            Data.TField active = new Data.TField();
            active.DB = "Active";
            active.Field = "Active";
            active.Text = "Active";
            fields.add(active);
            Data.TField created = new Data.TField();
            created.DB = "CreatedAt";
            created.Field = "CreatedAt";
            created.Text = "Created";
            fields.add(created);
            return fields;
        }

        private static void seed() {
            if (SEEDED) {
                return;
            }
            String[] firstNames = { "John", "Jane", "Alex", "Emily", "Michael", "Sarah", "David", "Laura", "Chris",
                    "Anna", "Robert", "Julia", "Daniel", "Mia", "Peter", "Sophia" };
            String[] lastNames = { "Smith", "Johnson", "Brown", "Williams", "Jones", "Garcia", "Miller", "Davis",
                    "Martinez", "Lopez", "Taylor", "Anderson", "Thomas", "Harris", "Clark", "Lewis" };
            String[] cities = { "New York", "San Francisco", "London", "Berlin", "Paris", "Madrid", "Prague", "Tokyo",
                    "Sydney", "Toronto", "Dublin", "Vienna", "Oslo", "Copenhagen", "Warsaw", "Lisbon" };
            String[] roles = { "user", "admin", "manager", "support" };
            String[] domains = { "example.com", "mail.com", "corp.local", "dev.io" };

            java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
            for (int i = 0; i < 100; i++) {
                String fn = firstNames[rnd.nextInt(firstNames.length)];
                String ln = lastNames[rnd.nextInt(lastNames.length)];
                Row row = new Row();
                row.ID = i + 1;
                row.Name = fn + " " + ln;
                row.City = cities[rnd.nextInt(cities.length)];
                row.Role = roles[rnd.nextInt(roles.length)];
                row.Active = rnd.nextDouble() < 0.62;
                String dom = domains[rnd.nextInt(domains.length)];
                row.Email = fn.toLowerCase() + "." + ln.toLowerCase() + "@" + dom;
                long now = System.currentTimeMillis();
                long days = rnd.nextInt(0, 365);
                row.CreatedAt = new java.util.Date(now - days * 24L * 60L * 60L * 1000L);
                DB.add(row);
            }
            SEEDED = true;
        }

        private static final List<Row> DB = new ArrayList<>();
        private static boolean SEEDED = false;

        private static final class Row {
            int ID;
            String Name;
            String Email;
            String City;
            String Role;
            boolean Active;
            java.util.Date CreatedAt;

            Row copy() {
                Row r = new Row();
                r.ID = ID;
                r.Name = Name;
                r.Email = Email;
                r.City = City;
                r.Role = Role;
                r.Active = Active;
                r.CreatedAt = new java.util.Date(CreatedAt.getTime());
                return r;
            }
        }

        private record OrderSpec(String field, String dir) {
        }
    }
}
