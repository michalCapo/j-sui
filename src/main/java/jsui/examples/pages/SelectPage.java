package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.ui;

public final class SelectPage {
    public static String render(Context ctx) {
        List<ui.AOption> opts = List.of(
                new ui.AOption("", "Select..."),
                new ui.AOption("one", "One"),
                new ui.AOption("two", "Two"),
                new ui.AOption("three", "Three"));
        List<ui.AOption> optsNoPlaceholder = List.of(
                new ui.AOption("one", "One"),
                new ui.AOption("two", "Two"),
                new ui.AOption("three", "Three"));

        SelectData data = new SelectData();
        data.Country = "";

        String basics = ui.div("flex flex-col gap-2").render(
                example("Default", ui.ISelect("Country", data).Options(opts).Render("Country")),
                example("Placeholder",
                        ui.ISelect("Country", data).Options(opts).Placeholder("Pick one").Render("Choose")));

        String validation = ui.div("flex flex-col gap-2").render(
                example("Error state",
                        ui.ISelect("Err", null).Options(opts).Placeholder("Please select").Error()
                                .Render("Invalid")),
                example("Required + empty",
                        ui.ISelect("Z", null).Options(opts).Empty().Required().Render("Required")),
                example("Disabled", ui.ISelect("Y", null).Options(opts).Disabled().Render("Disabled")));

        String variants = ui.div("flex flex-col gap-2").render(
                example("No placeholder + <empty>", ui.ISelect("Country", data).Options(optsNoPlaceholder)
                        .EmptyText("<empty>").Render("Choose")));

        String sizes = ui.div("flex flex-col gap-2").render(
                example("Small (SM)",
                        ui.ISelect("Country", data).Options(opts).Size(ui.SM).ClassLabel("text-sm")
                                .Render("Country")),
                example("Extra small (XS)", ui.ISelect("Country", data).Options(opts).Size(ui.XS)
                        .ClassLabel("text-sm").Render("Country")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Select"),
                ui.div("text-gray-600").render("Select input variations, validation, and sizing."),
                card("Basics", basics),
                card("Validation", validation),
                card("Variants", variants),
                card("Sizes", sizes));
    }

    private static String card(String title, String body) {
        return ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3 border").render(
                ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String example(String label, String control) {
        return ui.div("flex items-center justify-between gap-4 w-full").render(
                ui.div("text-sm text-gray-600").render(label),
                ui.div("flex items-center gap-3").render(ui.div("w-64").render(control)));
    }

    public static final class SelectData {
        public String Country;
    }
}
