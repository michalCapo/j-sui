package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;
import jsui.examples.models.TextData;

public final class TextPage {
    public static String render(Context ctx) {
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

}
