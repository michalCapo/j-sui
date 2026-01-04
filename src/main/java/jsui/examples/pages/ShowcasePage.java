package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.Ui;
import jsui.examples.models.DemoForm;

public final class ShowcasePage {
    public static String render(Context ctx) {
        DemoForm form = new DemoForm();
        return render(ctx, form, null);
    }

    private static final Ui.Target demoTarget = Ui.Target();

    public static String actionSubmit(Context ctx) {
        DemoForm form = new DemoForm();
        ctx.Body(form);
        ctx.Success("Form submitted successfully");
        return render(ctx, form, null);
    }

    public static String render(Context ctx, DemoForm form, String error) {
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

        return Ui.div("max-w-full sm:max-w-6xl mx-auto flex flex-col gap-8 w-full").render(
                Ui.div("text-3xl font-bold").render("Component Showcase"),
                Ui.div("text-gray-600").render("A collection of reusable UI components."),
                renderAlerts(),
                renderBadges(),
                renderCards(),
                renderProgress(),
                renderStepProgress(),
                renderTooltips(),
                renderTabs(),
                renderAccordion(),
                renderDropdowns(),
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

    private static String renderAlerts() {
        return Ui.div("flex flex-col gap-4").render(
                Ui.div("text-2xl font-bold").render("Alerts"),
                Ui.div("grid grid-cols-1 md:grid-cols-2 gap-4").render(
                        Ui.div("flex flex-col gap-2").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase mb-1").render("With Titles"),
                                Ui.Alert().Variant(Ui.Alert.Info).Title("Heads up!")
                                        .Message("This is an info alert with important information.")
                                        .Dismissible(true).Render(),
                                Ui.Alert().Variant(Ui.Alert.Success).Title("Great success!")
                                        .Message("Your changes have been saved successfully.").Dismissible(true)
                                        .Render()),
                        Ui.div("flex flex-col gap-2").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase mb-1").render("Outline Variants"),
                                Ui.Alert().Variant("warning-outline").Title("Warning")
                                        .Message("Please review your input before proceeding.").Dismissible(true)
                                        .Render(),
                                Ui.Alert().Variant("error-outline").Title("Error occurred")
                                        .Message("Something went wrong while saving your data.").Dismissible(true)
                                        .Render())));
    }

    private static String renderBadges() {
        String icon = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"12\" height=\"12\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"3\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z\"/><path d=\"m9 12 2 2 4-4\"/></svg>";
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Badges"),
                Ui.div("flex flex-col gap-6").render(
                        Ui.div("flex flex-wrap items-center gap-4").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase w-full mb-1")
                                        .render("Variants & Icons"),
                                Ui.Badge().Color("green-soft").Text("Verified").Icon(icon).Render(),
                                Ui.Badge().Color("blue").Text("New").Size("lg").Render(),
                                Ui.Badge().Color("red").Text("Urgent").Square().Render(),
                                Ui.Badge().Color("yellow-soft").Text("Warning").Size("sm").Render()),
                        Ui.div("flex flex-wrap items-center gap-4").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase w-full mb-1")
                                        .render("Dots & Sizes"),
                                Ui.Badge().Color("green").Dot().Size("sm").Render(),
                                Ui.Badge().Color("blue").Dot().Size("md").Render(),
                                Ui.Badge().Color("red").Dot().Size("lg").Render(),
                                Ui.Badge().Color("purple-soft").Text("Large Badge").Size("lg").Render())));
    }

    private static String renderCards() {
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Cards"),
                Ui.div("grid grid-cols-1 md:grid-cols-3 gap-6").render(
                        Ui.Card().Header("<h3 class='font-bold'>Standard Card</h3>")
                                .Body("<p class='text-gray-600 dark:text-gray-400'>This is a standard shadowed card with default padding.</p>")
                                .Footer("<div class='text-xs text-gray-500'>Card Footer</div>")
                                .Render(),
                        Ui.Card().Image(
                                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop",
                                "Landscape")
                                .Header("<h3 class='font-bold'>Card with Image</h3>")
                                .Body("<p class='text-gray-600 dark:text-gray-400'>Cards can now display images at the top.</p>")
                                .Hover(true)
                                .Render(),
                        Ui.Card().Variant(Ui.Card.Glass)
                                .Header("<h3 class='font-bold'>Glass Variant</h3>")
                                .Body("<p class='text-gray-600 dark:text-gray-400'>This card uses a glassmorphism effect with backdrop blur.</p>")
                                .Hover(true)
                                .Render()));
    }

    private static String renderProgress() {
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Progress Bars"),
                Ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                        Ui.div("flex flex-col gap-4").render(
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Gradient Style (75%)"),
                                        Ui.ProgressBar().Value(75).Gradient("#3b82f6", "#8b5cf6").Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Outside Label"),
                                        Ui.ProgressBar().Value(45).Label("System Update").LabelPosition("outside")
                                                .Color("bg-indigo-600").Render())),
                        Ui.div("flex flex-col gap-4").render(
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Animated Stripes"),
                                        Ui.ProgressBar().Value(65).Color("bg-green-500").Striped(true)
                                                .Animated(true).Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Indeterminate"),
                                        Ui.ProgressBar().Indeterminate(true).Color("bg-blue-600").Render()))));
    }

    private static String renderStepProgress() {
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Step Progress"),
                Ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                        Ui.div("flex flex-col gap-4").render(
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Step 1 of 4"),
                                        Ui.StepProgress(1, 4).Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Step 2 of 4"),
                                        Ui.StepProgress(2, 4).Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Step 3 of 4"),
                                        Ui.StepProgress(3, 4).Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Step 4 of 4 (Complete)"),
                                        Ui.StepProgress(4, 4).Color("bg-green-500").Render())),
                        Ui.div("flex flex-col gap-4").render(
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Small Size - Step 1 of 5"),
                                        Ui.StepProgress(1, 5).Size("sm").Color("bg-purple-500").Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Large Size - Step 2 of 5"),
                                        Ui.StepProgress(2, 5).Size("lg").Color("bg-yellow-500").Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Extra Large - Step 3 of 5"),
                                        Ui.StepProgress(3, 5).Size("xl").Color("bg-red-500").Render()),
                                Ui.div("").render(
                                        Ui.div("mb-1 text-sm font-medium").render("Custom Step Progress"),
                                        Ui.StepProgress(7, 10).Color("bg-indigo-500").Size("md").Render()))));
    }

    private static String renderTooltips() {
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Tooltips"),
                Ui.div("flex flex-wrap gap-4").render(
                        Ui.Tooltip().Content("Delayed tooltip").Delay(500).Render(
                                Ui.Button().Color(Ui.Blue).Class("rounded-lg").Render("500ms Delay")),
                        Ui.Tooltip().Content("Bottom position").Position("bottom").Render(
                                Ui.Button().Color(Ui.Green).Class("rounded-lg").Render("Bottom")),
                        Ui.Tooltip().Content("Success variant").Variant("green").Render(
                                Ui.Button().Color(Ui.GreenOutline).Class("rounded-lg").Render("Success")),
                        Ui.Tooltip().Content("Danger variant").Variant("red").Render(
                                Ui.Button().Color(Ui.RedOutline).Class("rounded-lg").Render("Danger")),
                        Ui.Tooltip().Content("Light variant").Variant("light").Render(
                                Ui.Button().Color(Ui.GrayOutline).Class("rounded-lg").Render("Light"))));
    }

    private static String renderTabs() {
        String iconHome = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z\"/><polyline points=\"9 22 9 12 15 12 15 22\"/></svg>";
        String iconUser = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2\"/><circle cx=\"12\" cy=\"7\" r=\"4\"/></svg>";
        String iconSettings = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg>";
        String contentClass = "p-6 bg-white dark:bg-gray-900 rounded-lg shadow-sm border border-gray-100 dark:border-gray-800";
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Tabs"),
                Ui.div("grid grid-cols-1 gap-8").render(
                        Ui.div("").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                        .render("Boxed Style with Icons"),
                                Ui.Tabs()
                                        .Tab("Home", Ui.div(contentClass).render(
                                                Ui.div("text-lg font-bold mb-2").render("🏠 Dashboard Home"),
                                                Ui.div("text-gray-600 dark:text-gray-400").render(
                                                        "Welcome to your central dashboard. This panel demonstrates how tabs can wrap complex HTML content with a clean white background.")),
                                                iconHome)
                                        .Tab("Profile", Ui.div(contentClass).render(
                                                Ui.div("text-lg font-bold mb-2").render("👤 User Profile"),
                                                Ui.div("text-gray-600 dark:text-gray-400").render(
                                                        "Manage your personal information, display name, and avatar settings here.")),
                                                iconUser)
                                        .Tab("Settings", Ui.div(contentClass).render(
                                                Ui.div("text-lg font-bold mb-2").render("⚙️ System Settings"),
                                                Ui.div("text-gray-600 dark:text-gray-400").render(
                                                        "Fine-tune application behavior, notification preferences, and privacy controls.")),
                                                iconSettings)
                                        .Active(0)
                                        .Style(Ui.Tabs.Boxed)
                                        .Render()),
                        Ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                                Ui.div("").render(
                                        Ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                                .render("Underline Style"),
                                        Ui.Tabs()
                                                .Tab("General", Ui.div(contentClass).render(
                                                        Ui.div("font-bold").render("General Info"),
                                                        Ui.div("mt-2")
                                                                .render("Basic configuration for your workspace.")))
                                                .Tab("Security", Ui.div(contentClass).render(
                                                        Ui.div("font-bold").render("Privacy & Security"),
                                                        Ui.div("mt-2").render(
                                                                "Manage passwords, two-factor authentication and active sessions.")))
                                                .Active(0)
                                                .Style(Ui.Tabs.Underline)
                                                .Render()),
                                Ui.div("").render(
                                        Ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                                .render("Pills Style"),
                                        Ui.Tabs()
                                                .Tab("Daily", Ui.div(contentClass).render(
                                                        Ui.div("font-bold text-blue-600")
                                                                .render("Today's Progress"),
                                                        Ui.div("mt-1").render(
                                                                "Detailed activities for the last 24 hours.")))
                                                .Tab("Weekly", Ui.div(contentClass).render(
                                                        Ui.div("font-bold text-green-600").render("Weekly Trends"),
                                                        Ui.div("mt-1").render(
                                                                "Summary of performance over the past 7 days.")))
                                                .Tab("Monthly", Ui.div(contentClass).render(
                                                        Ui.div("font-bold text-purple-600")
                                                                .render("Monthly Report"),
                                                        Ui.div("mt-1").render(
                                                                "Strategic overview of goals achieved this month.")))
                                                .Active(1)
                                                .Style(Ui.Tabs.Pills)
                                                .Render()))));
    }

    private static String renderAccordion() {
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Accordion"),
                Ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                        Ui.div("").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                        .render("Bordered with Default Open"),
                                Ui.Accordion().Variant(Ui.Accordion.Bordered)
                                        .Item("What is g-sui?",
                                                "g-sui is a Go-based server-rendered UI framework that provides a component-based approach to building web applications.",
                                                true)
                                        .Item("How do I get started?",
                                                "Simply import the ui package and start composing components.")
                                        .Item("Is it responsive?",
                                                "All components are built with responsive design in mind, using Tailwind's responsive modifiers.")
                                        .Render()),
                        Ui.div("").render(
                                Ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                        .render("Separated Variant (Multiple)"),
                                Ui.Accordion().Variant(Ui.Accordion.Separated).Multiple(true)
                                        .Item("Separated Section 1",
                                                "In the separated variant, each item is its own card.")
                                        .Item("Separated Section 2",
                                                "Multiple sections can be open at once when Multiple(true) is used.")
                                        .Render())));
    }

    private static String renderDropdowns() {
        String iconEdit = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7\"/><path d=\"M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z\"/></svg>";
        String iconDelete = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><polyline points=\"3 6 5 6 21 6\"/><path d=\"M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2\"/><line x1=\"10\" y1=\"11\" x2=\"10\" y2=\"17\"/><line x1=\"14\" y1=\"11\" x2=\"14\" y2=\"17\"/></svg>";
        return Ui.div("").render(
                Ui.div("text-2xl font-bold mb-4").render("Dropdown Menus"),
                Ui.div("flex flex-wrap gap-4").render(
                        Ui.Dropdown()
                                .Trigger(Ui.Button().Color(Ui.Blue).Class("rounded-lg").Render("Actions ▼"))
                                .Header("General")
                                .Item("Edit Profile", "alert('Edit')", iconEdit)
                                .Item("Account Settings", "alert('Settings')")
                                .Divider()
                                .Header("Danger Zone")
                                .Danger("Delete Account", "alert('Delete')", iconDelete)
                                .Position("bottom-left")
                                .Render(),
                        Ui.Dropdown()
                                .Trigger(Ui.Button().Color(Ui.GrayOutline).Class("rounded-lg").Render("Options ▼"))
                                .Item("Share", "alert('Share')")
                                .Item("Download", "alert('Download')")
                                .Position("bottom-right")
                                .Render()));
    }
}
