package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.Ui;

public final class SelectPage {
    public static String render(Context ctx) {
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

    public static final class SelectData {
        public String Country;
    }
}
