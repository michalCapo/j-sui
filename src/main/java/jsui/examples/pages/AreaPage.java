package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class AreaPage {
    public static String render(Context ctx) {
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

    public static final class AreaData {
        public String Bio;
    }
}
