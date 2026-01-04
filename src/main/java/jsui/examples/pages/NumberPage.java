package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class NumberPage {
    public static String render(Context ctx) {
        NumberData data = new NumberData();
        data.Age = 30;
        data.Price = 19.9;

        String basics = Ui.div("flex flex-col gap-2").render(
                row("Integer with range/step", Ui.INumber("Age", data).Numbers(0.0, 120.0, 1.0).Render("Age")),
                row("Float formatted (%.2f)", Ui.INumber("Price", data).Format("%.2f").Render("Price")),
                row("Required", Ui.INumber("Req", null).Required().Render("Required")),
                row("Readonly", Ui.INumber("RO", null).Readonly().Value("42").Render("Readonly")),
                row("Disabled", Ui.INumber("D", null).Disabled().Render("Disabled")),
                row("Placeholder", Ui.INumber("PH", null).Placeholder("0..100").Render("Number")));

        String styling = Ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        Ui.INumber("C", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        Ui.INumber("CL", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                row("Input .ClassInput()",
                        Ui.INumber("CI", null).ClassInput("bg-blue-50").Render("Custom input background")),
                row("Size: LG", Ui.INumber("S", null).Size(Ui.LG).Render("Large size")));

        String behavior = Ui.div("flex flex-col gap-2").render(
                row("Change handler (console.log)",
                        Ui.INumber("Change", null).Change("console.log('changed', this && this.value)")
                                .Render("On change, log")),
                row("Click handler (console.log)",
                        Ui.INumber("Click", null).Click("console.log('clicked number')").Render("On click, log")));

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Number input"),
                Ui.div("text-gray-600").render("Ranges, formatting, and common attributes."),
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
                Ui.div("w-64").render(control));
    }

    public static final class NumberData {
        public double Age;
        public double Price;
    }
}
