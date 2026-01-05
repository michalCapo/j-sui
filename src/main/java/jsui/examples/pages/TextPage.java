package jsui.examples.pages;

import jsui.Context;
import jsui.ui;
import jsui.examples.models.TextData;

public final class TextPage {
    public static String render(Context ctx) {
        TextData data = new TextData();
        data.Name = "John Doe";

        String basics = ui.div("flex flex-col gap-2").render(
                row("Default", ui.IText("Name", data).Render("Name")),
                row("With placeholder", ui.IText("X", null).Placeholder("Type your name").Render("Your name")),
                row("Required field", ui.IText("Y", null).Required().Render("Required field")),
                row("Readonly", ui.IText("Y2", null).Readonly().Value("Read-only value").Render("Readonly field")),
                row("Disabled", ui.IText("Z", null).Disabled().Placeholder("Cannot type").Render("Disabled")),
                row("With preset value", ui.IText("Preset", null).Value("Preset text").Render("Preset")));

        String styling = ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        ui.IText("C1", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        ui.IText("C2", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                row("Input .ClassInput()",
                        ui.IText("C3", null).ClassInput("bg-blue-50").Render("Custom input background")),
                row("Size: XS", ui.IText("S1", null).Size(ui.XS).Render("XS")),
                row("Size: MD (default)", ui.IText("S2", null).Size(ui.MD).Render("MD")),
                row("Size: XL", ui.IText("S3", null).Size(ui.XL).Render("XL")));

        String behavior = ui.div("flex flex-col gap-2").render(
                row("Autocomplete", ui.IText("Auto", null).Autocomplete("name").Render("Name (autocomplete)")),
                row("Pattern (email-like)",
                        ui.IText("Pattern", null).Type("email").Pattern("[^@]+@[^@]+\\.[^@]+")
                                .Placeholder("user@example.com").Render("Email")),
                row("Type switch (password)", ui.IText("PassLike", null).Type("password").Render("Password-like")),
                row("Change handler (console.log)",
                        ui.IText("Change", null).Change("console.log('changed', this && this.value)")
                                .Render("On change, log value")),
                row("Click handler (console.log)",
                        ui.IText("Click", null).Click("console.log('clicked input')").Render("On click, log")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Text input"),
                ui.div("text-gray-600").render("Common features supported by text-like inputs."),
                card("Basics & states", basics),
                card("Styling", styling),
                card("Behavior & attributes", behavior));
    }

    private static String card(String title, String body) {
        return ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String row(String label, String control) {
        return ui.div("flex items-center justify-between gap-4").render(
                ui.div("text-sm text-gray-600").render(label),
                control);
    }

}
