package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class AreaPage {
    public static String render(Context ctx) {
        AreaData data = new AreaData();
        data.Bio = "Short text";

        String basics = ui.div("flex flex-col gap-2").render(
                row("Default", ui.IArea("Bio", data).Rows(3).Render("Bio")),
                row("Placeholder", ui.IArea("P", null).Placeholder("Tell us something").Rows(3).Render("Your bio")),
                row("Required", ui.IArea("R", null).Required().Rows(3).Render("Required")),
                row("Readonly", ui.IArea("RO", null).Readonly().Value("Read-only text").Rows(3).Render("Readonly")),
                row("Disabled", ui.IArea("D", null).Disabled().Rows(3).Render("Disabled")),
                row("With preset value",
                        ui.IArea("V", null).Value("Initial text value").Rows(3).Render("With value")));

        String styling = ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        ui.IArea("C", null).Class("p-2 rounded bg-yellow-50").Rows(3).Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        ui.IArea("CL", null).ClassLabel("text-purple-700 font-bold").Rows(3)
                                .Render("Custom label")),
                row("Input .ClassInput()",
                        ui.IArea("CI", null).ClassInput("bg-blue-50").Rows(3).Render("Custom input background")),
                row("Size: XL", ui.IArea("S", null).Size(ui.XL).Rows(3).Render("XL size")));

        String behavior = ui.div("flex flex-col gap-2").render(
                row("Change handler (console.log)",
                        ui.IArea("Change", null).Change("console.log('changed', this && this.value)").Rows(3)
                                .Render("On change, log")),
                row("Click handler (console.log)", ui.IArea("Click", null).Click("console.log('clicked textarea')")
                        .Rows(3).Render("On click, log")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Textarea"),
                ui.div("text-gray-600").render("Common features supported by textarea."),
                card("Basics & states", basics),
                card("Styling", styling),
                card("Behavior & attributes", behavior));
    }

    private static String card(String title, String body) {
        return ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3 border").render(
                ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String row(String label, String control) {
        return ui.div("flex items-center justify-between gap-4").render(
                ui.div("text-sm text-gray-600").render(label),
                control);
    }

    public static final class AreaData {
        public String Bio;
    }
}
