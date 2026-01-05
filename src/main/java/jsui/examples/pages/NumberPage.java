package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class NumberPage {
    public static String render(Context ctx) {
        NumberData data = new NumberData();
        data.Age = 30;
        data.Price = 19.9;

        String basics = ui.div("flex flex-col gap-2").render(
                row("Integer with range/step", ui.INumber("Age", data).Numbers(0.0, 120.0, 1.0).Render("Age")),
                row("Float formatted (%.2f)", ui.INumber("Price", data).Format("%.2f").Render("Price")),
                row("Required", ui.INumber("Req", null).Required().Render("Required")),
                row("Readonly", ui.INumber("RO", null).Readonly().Value("42").Render("Readonly")),
                row("Disabled", ui.INumber("D", null).Disabled().Render("Disabled")),
                row("Placeholder", ui.INumber("PH", null).Placeholder("0..100").Render("Number")));

        String styling = ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        ui.INumber("C", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        ui.INumber("CL", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                row("Input .ClassInput()",
                        ui.INumber("CI", null).ClassInput("bg-blue-50").Render("Custom input background")),
                row("Size: LG", ui.INumber("S", null).Size(ui.LG).Render("Large size")));

        String behavior = ui.div("flex flex-col gap-2").render(
                row("Change handler (console.log)",
                        ui.INumber("Change", null).Change("console.log('changed', this && this.value)")
                                .Render("On change, log")),
                row("Click handler (console.log)",
                        ui.INumber("Click", null).Click("console.log('clicked number')").Render("On click, log")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Number input"),
                ui.div("text-gray-600").render("Ranges, formatting, and common attributes."),
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
                ui.div("w-64").render(control));
    }

    public static final class NumberData {
        public double Age;
        public double Price;
    }
}
