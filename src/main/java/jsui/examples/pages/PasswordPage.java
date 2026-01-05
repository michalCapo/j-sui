package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class PasswordPage {
    public static String render(Context ctx) {
        String basics = ui.div("flex flex-col gap-2").render(
                row("Default", ui.IPassword("P1", null).Render("Password")),
                row("With placeholder", ui.IPassword("P2", null).Placeholder("••••••••").Render("Password")),
                row("Required", ui.IPassword("P3", null).Required().Render("Password (required)")),
                row("Readonly", ui.IPassword("P4", null).Readonly().Value("secret").Render("Readonly password")),
                row("Disabled", ui.IPassword("P5", null).Disabled().Render("Password (disabled)")),
                row("Preset value", ui.IPassword("P6", null).Value("topsecret").Render("Preset value")),
                row("Type switched to text (visible)",
                        ui.IPassword("P7", null).Type("text").Value("visible value").Render("As text")));

        String styling = ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        ui.IPassword("C1", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        ui.IPassword("C2", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                row("Input .ClassInput()",
                        ui.IPassword("C3", null).ClassInput("bg-blue-50").Render("Custom input background")),
                row("Size: XS", ui.IPassword("S1", null).Size(ui.XS).Render("XS")),
                row("Size: XL", ui.IPassword("S2", null).Size(ui.XL).Render("XL")));

        String behavior = ui.div("flex flex-col gap-2").render(
                row("Autocomplete (new-password)",
                        ui.IPassword("A1", null).Autocomplete("new-password").Render("New password")),
                row("Pattern (min 8 chars)",
                        ui.IPassword("A2", null).Pattern(".{8,}").Placeholder("at least 8 characters")
                                .Render("Min length pattern")),
                row("Change handler (console.log)",
                        ui.IPassword("A3", null).Change("console.log('changed pw', this && this.value)")
                                .Render("On change, log")),
                row("Click handler (console.log)",
                        ui.IPassword("A4", null).Click("console.log('clicked pw')").Render("On click, log")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Password"),
                ui.div("text-gray-600").render("Common features and states for password inputs."),
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
