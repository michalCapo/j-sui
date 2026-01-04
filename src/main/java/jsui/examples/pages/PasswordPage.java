package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class PasswordPage {
    public static String render(Context ctx) {
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
