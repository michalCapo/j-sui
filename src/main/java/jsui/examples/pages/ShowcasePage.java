package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.ui;
import jsui.examples.models.DemoForm;

public final class ShowcasePage {
    public static String render(Context ctx) {
        DemoForm form = new DemoForm();
        return render(ctx, form, null);
    }

    private static final ui.Target demoTarget = ui.Target();

    public static String actionSubmit(Context ctx) {
        DemoForm form = new DemoForm();
        ctx.Body(form);
        ctx.Success("Form submitted successfully");
        return render(ctx, form, null);
    }

    public static String render(Context ctx, DemoForm form, String error) {
        List<ui.AOption> countries = List.of(
                new ui.AOption("", "Select..."),
                new ui.AOption("USA", "USA"),
                new ui.AOption("Slovakia", "Slovakia"),
                new ui.AOption("Germany", "Germany"),
                new ui.AOption("Japan", "Japan"));
        List<ui.AOption> genders = List.of(
                new ui.AOption("male", "Male"),
                new ui.AOption("female", "Female"),
                new ui.AOption("other", "Other"));

        String errHtml = "";
        if (error != null) {
            errHtml = ui.div("text-red-600 p-4 rounded text-center border-4 border-red-600 bg-white").render(error);
        }

        return ui.div("max-w-full sm:max-w-6xl mx-auto flex flex-col gap-8 w-full").render(
                ui.div("text-3xl font-bold").render("Component Showcase"),
                ui.div("text-gray-600").render("A collection of reusable UI components."),
                renderAlerts(),
                renderBadges(),
                renderCards(),
                renderProgress(),
                renderStepProgress(),
                renderTooltips(),
                renderTabs(),
                renderAccordion(),
                renderDropdowns(),
                ui.div("grid gap-4 sm:gap-6 items-start w-full", demoTarget.id()).render(
                        ui.form("flex flex-col gap-4 bg-white p-6 rounded-lg shadow w-full",
                                demoTarget.id(),
                                ctx.Submit(ShowcasePage::actionSubmit).Replace(demoTarget.id())).render(
                                        ui.div("text-xl font-bold").render("Component Showcase Form"),
                                        errHtml,
                                        ui.IText("Name", form).Required(true).Render("Name"),
                                        ui.IEmail("Email", form).Required(true).Render("Email"),
                                        ui.IPhone("Phone", form).Render("Phone"),
                                        ui.IPassword("Password", null).Required(true).Render("Password"),
                                        ui.INumber("Age", form).Numbers(0.0, 120.0, 1.0).Render("Age"),
                                        ui.INumber("Price", form).Format("%.2f").Render("Price (USD)"),
                                        ui.IArea("Bio", form).Rows(4).Render("Short Bio"),
                                        ui.div("block sm:hidden").render(
                                                ui.div("text-sm font-bold").render("Gender"),
                                                ui.IRadio("Gender", form).Value("male").Render("Male"),
                                                ui.IRadio("Gender", form).Value("female").Render("Female"),
                                                ui.IRadio("Gender", form).Value("other").Render("Other")),
                                        ui.div("hidden sm:block overflow-x-auto").render(
                                                ui.IRadioButtons("Gender", form).Options(genders).Render("Gender")),
                                        ui.ISelect("Country", form).Options(countries).Placeholder("Select...")
                                                .Render("Country"),
                                        ui.ICheckbox("Agree", form).Required().Render("I agree to the terms"),
                                        ui.IDate("BirthDate", form).Render("Birth Date"),
                                        ui.ITime("AlarmTime", form).Render("Alarm Time"),
                                        ui.IDateTime("Meeting", form).Render("Meeting (Local)"),
                                        ui.div("flex gap-2 mt-2").render(
                                                ui.Button().Submit().Color(ui.Blue).Class("rounded")
                                                        .Render("Submit"),
                                                ui.Button().Reset().Color(ui.Gray).Class("rounded")
                                                        .Render("Reset")))));
    }

    private static String renderAlerts() {
        return ui.div("flex flex-col gap-4").render(
                ui.div("text-2xl font-bold").render("Alerts"),
                ui.div("grid grid-cols-1 md:grid-cols-2 gap-4").render(
                        ui.div("flex flex-col gap-2").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase mb-1").render("With Titles"),
                                ui.Alert().Variant(ui.Alert.Info).Title("Heads up!")
                                        .Message("This is an info alert with important information.")
                                        .Dismissible(true).Render(),
                                ui.Alert().Variant(ui.Alert.Success).Title("Great success!")
                                        .Message("Your changes have been saved successfully.").Dismissible(true)
                                        .Render()),
                        ui.div("flex flex-col gap-2").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase mb-1").render("Outline Variants"),
                                ui.Alert().Variant("warning-outline").Title("Warning")
                                        .Message("Please review your input before proceeding.").Dismissible(true)
                                        .Render(),
                                ui.Alert().Variant("error-outline").Title("Error occurred")
                                        .Message("Something went wrong while saving your data.").Dismissible(true)
                                        .Render())));
    }

    private static String renderBadges() {
        String icon = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"12\" height=\"12\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"3\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z\"/><path d=\"m9 12 2 2 4-4\"/></svg>";
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Badges"),
                ui.div("flex flex-col gap-6").render(
                        ui.div("flex flex-wrap items-center gap-4").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase w-full mb-1")
                                        .render("Variants & Icons"),
                                ui.Badge().Color("green-soft").Text("Verified").Icon(icon).Render(),
                                ui.Badge().Color("blue").Text("New").Size("lg").Render(),
                                ui.Badge().Color("red").Text("Urgent").Square().Render(),
                                ui.Badge().Color("yellow-soft").Text("Warning").Size("sm").Render()),
                        ui.div("flex flex-wrap items-center gap-4").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase w-full mb-1")
                                        .render("Dots & Sizes"),
                                ui.Badge().Color("green").Dot().Size("sm").Render(),
                                ui.Badge().Color("blue").Dot().Size("md").Render(),
                                ui.Badge().Color("red").Dot().Size("lg").Render(),
                                ui.Badge().Color("purple-soft").Text("Large Badge").Size("lg").Render())));
    }

    private static String renderCards() {
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Cards"),
                ui.div("grid grid-cols-1 md:grid-cols-3 gap-6").render(
                        ui.Card().Header("<h3 class='font-bold'>Standard Card</h3>")
                                .Body("<p class='text-gray-600 dark:text-gray-400'>This is a standard shadowed card with default padding.</p>")
                                .Footer("<div class='text-xs text-gray-500'>Card Footer</div>")
                                .Render(),
                        ui.Card().Image(
                                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop",
                                "Landscape")
                                .Header("<h3 class='font-bold'>Card with Image</h3>")
                                .Body("<p class='text-gray-600 dark:text-gray-400'>Cards can now display images at the top.</p>")
                                .Hover(true)
                                .Render(),
                        ui.Card().Variant(ui.Card.Glass)
                                .Header("<h3 class='font-bold'>Glass Variant</h3>")
                                .Body("<p class='text-gray-600 dark:text-gray-400'>This card uses a glassmorphism effect with backdrop blur.</p>")
                                .Hover(true)
                                .Render()));
    }

    private static String renderProgress() {
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Progress Bars"),
                ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                        ui.div("flex flex-col gap-4").render(
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Gradient Style (75%)"),
                                        ui.ProgressBar().Value(75).Gradient("#3b82f6", "#8b5cf6").Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Outside Label"),
                                        ui.ProgressBar().Value(45).Label("System Update").LabelPosition("outside")
                                                .Color("bg-indigo-600").Render())),
                        ui.div("flex flex-col gap-4").render(
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Animated Stripes"),
                                        ui.ProgressBar().Value(65).Color("bg-green-500").Striped(true)
                                                .Animated(true).Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Indeterminate"),
                                        ui.ProgressBar().Indeterminate(true).Color("bg-blue-600").Render()))));
    }

    private static String renderStepProgress() {
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Step Progress"),
                ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                        ui.div("flex flex-col gap-4").render(
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Step 1 of 4"),
                                        ui.StepProgress(1, 4).Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Step 2 of 4"),
                                        ui.StepProgress(2, 4).Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Step 3 of 4"),
                                        ui.StepProgress(3, 4).Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Step 4 of 4 (Complete)"),
                                        ui.StepProgress(4, 4).Color("bg-green-500").Render())),
                        ui.div("flex flex-col gap-4").render(
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Small Size - Step 1 of 5"),
                                        ui.StepProgress(1, 5).Size("sm").Color("bg-purple-500").Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Large Size - Step 2 of 5"),
                                        ui.StepProgress(2, 5).Size("lg").Color("bg-yellow-500").Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Extra Large - Step 3 of 5"),
                                        ui.StepProgress(3, 5).Size("xl").Color("bg-red-500").Render()),
                                ui.div("").render(
                                        ui.div("mb-1 text-sm font-medium").render("Custom Step Progress"),
                                        ui.StepProgress(7, 10).Color("bg-indigo-500").Size("md").Render()))));
    }

    private static String renderTooltips() {
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Tooltips"),
                ui.div("flex flex-wrap gap-4").render(
                        ui.Tooltip().Content("Delayed tooltip").Delay(500).Render(
                                ui.Button().Color(ui.Blue).Class("rounded-lg").Render("500ms Delay")),
                        ui.Tooltip().Content("Bottom position").Position("bottom").Render(
                                ui.Button().Color(ui.Green).Class("rounded-lg").Render("Bottom")),
                        ui.Tooltip().Content("Success variant").Variant("green").Render(
                                ui.Button().Color(ui.GreenOutline).Class("rounded-lg").Render("Success")),
                        ui.Tooltip().Content("Danger variant").Variant("red").Render(
                                ui.Button().Color(ui.RedOutline).Class("rounded-lg").Render("Danger")),
                        ui.Tooltip().Content("Light variant").Variant("light").Render(
                                ui.Button().Color(ui.GrayOutline).Class("rounded-lg").Render("Light"))));
    }

    private static String renderTabs() {
        String iconHome = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z\"/><polyline points=\"9 22 9 12 15 12 15 22\"/></svg>";
        String iconUser = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2\"/><circle cx=\"12\" cy=\"7\" r=\"4\"/></svg>";
        String iconSettings = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z\"/><circle cx=\"12\" cy=\"12\" r=\"3\"/></svg>";
        String contentClass = "p-6 bg-white dark:bg-gray-900 rounded-lg shadow-sm border border-gray-100 dark:border-gray-800";
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Tabs"),
                ui.div("grid grid-cols-1 gap-8").render(
                        ui.div("").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                        .render("Boxed Style with Icons"),
                                ui.Tabs()
                                        .Tab("Home", ui.div(contentClass).render(
                                                ui.div("text-lg font-bold mb-2").render("🏠 Dashboard Home"),
                                                ui.div("text-gray-600 dark:text-gray-400").render(
                                                        "Welcome to your central dashboard. This panel demonstrates how tabs can wrap complex HTML content with a clean white background.")),
                                                iconHome)
                                        .Tab("Profile", ui.div(contentClass).render(
                                                ui.div("text-lg font-bold mb-2").render("👤 User Profile"),
                                                ui.div("text-gray-600 dark:text-gray-400").render(
                                                        "Manage your personal information, display name, and avatar settings here.")),
                                                iconUser)
                                        .Tab("Settings", ui.div(contentClass).render(
                                                ui.div("text-lg font-bold mb-2").render("⚙️ System Settings"),
                                                ui.div("text-gray-600 dark:text-gray-400").render(
                                                        "Fine-tune application behavior, notification preferences, and privacy controls.")),
                                                iconSettings)
                                        .Active(0)
                                        .Style(ui.Tabs.Boxed)
                                        .Render()),
                        ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                                ui.div("").render(
                                        ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                                .render("Underline Style"),
                                        ui.Tabs()
                                                .Tab("General", ui.div(contentClass).render(
                                                        ui.div("font-bold").render("General Info"),
                                                        ui.div("mt-2")
                                                                .render("Basic configuration for your workspace.")))
                                                .Tab("Security", ui.div(contentClass).render(
                                                        ui.div("font-bold").render("Privacy & Security"),
                                                        ui.div("mt-2").render(
                                                                "Manage passwords, two-factor authentication and active sessions.")))
                                                .Active(0)
                                                .Style(ui.Tabs.Underline)
                                                .Render()),
                                ui.div("").render(
                                        ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                                .render("Pills Style"),
                                        ui.Tabs()
                                                .Tab("Daily", ui.div(contentClass).render(
                                                        ui.div("font-bold text-blue-600")
                                                                .render("Today's Progress"),
                                                        ui.div("mt-1").render(
                                                                "Detailed activities for last 24 hours.")))
                                                .Tab("Weekly", ui.div(contentClass).render(
                                                        ui.div("font-bold text-green-600").render("Weekly Trends"),
                                                        ui.div("mt-1").render(
                                                                "Summary of performance over past 7 days.")))
                                                .Tab("Monthly", ui.div(contentClass).render(
                                                        ui.div("font-bold text-purple-600")
                                                                .render("Monthly Report"),
                                                        ui.div("mt-1").render(
                                                                "Strategic overview of goals achieved this month.")))
                                                .Active(1)
                                                .Style(ui.Tabs.Pills)
                                                .Render()))));
    }

    private static String renderAccordion() {
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Accordion"),
                ui.div("grid grid-cols-1 md:grid-cols-2 gap-8").render(
                        ui.div("").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                        .render("Bordered with Default Open"),
                                ui.Accordion().Variant(ui.Accordion.Bordered)
                                        .Item("What is g-sui?",
                                                "g-sui is a Go-based server-rendered UI framework that provides a component-based approach to building web applications.",
                                                true)
                                        .Item("How do I get started?",
                                                "Simply import the ui package and start composing components.")
                                        .Item("Is it responsive?",
                                                "All components are built with responsive design in mind, using Tailwind's responsive modifiers.")
                                        .Render()),
                        ui.div("").render(
                                ui.div("text-sm font-bold text-gray-500 uppercase mb-3")
                                        .render("Separated Variant (Multiple)"),
                                ui.Accordion().Variant(ui.Accordion.Separated).Multiple(true)
                                        .Item("Separated Section 1",
                                                "In separated variant, each item is its own card.")
                                        .Item("Separated Section 2",
                                                "Multiple sections can be open at once when Multiple(true) is used.")
                                        .Render())));
    }

    private static String renderDropdowns() {
        String iconEdit = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7\"/><path d=\"M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z\"/></svg>";
        String iconDelete = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"14\" height=\"14\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><polyline points=\"3 6 5 6 21 6\"/><path d=\"M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2\"/><line x1=\"10\" y1=\"11\" x2=\"10\" y2=\"17\"/><line x1=\"14\" y1=\"11\" x2=\"14\" y2=\"17\"/></svg>";
        return ui.div("").render(
                ui.div("text-2xl font-bold mb-4").render("Dropdown Menus"),
                ui.div("flex flex-wrap gap-4").render(
                        ui.Dropdown()
                                .Trigger(ui.Button().Color(ui.Blue).Class("rounded-lg").Render("Actions ▼"))
                                .Header("General")
                                .Item("Edit Profile", "alert('Edit')", iconEdit)
                                .Item("Account Settings", "alert('Settings')")
                                .Divider()
                                .Header("Danger Zone")
                                .Danger("Delete Account", "alert('Delete')", iconDelete)
                                .Position("bottom-left")
                                .Render(),
                        ui.Dropdown()
                                .Trigger(ui.Button().Color(ui.GrayOutline).Class("rounded-lg").Render("Options ▼"))
                                .Item("Share", "alert('Share')")
                                .Item("Download", "alert('Download')")
                                .Position("bottom-right")
                                .Render()));
    }
}
