package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;
import jsui.examples.models.DateData;

public final class DatePage {
    public static String render(Context ctx) {
        DateData data = new DateData();
        data.Birth = new java.util.Date();

        String basics = Ui.div("flex flex-col gap-2").render(
                row("Date", Ui.IDate("Birth", data).Render("Birth date")),
                row("Time", Ui.ITime("Alarm", null).Render("Alarm")),
                row("DateTime", Ui.IDateTime("Meeting", null).Render("Meeting time")),
                row("Required date", Ui.IDate("Req", null).Required().Render("Required date")),
                row("Readonly time", Ui.ITime("RO", null).Readonly().Value("10:00").Render("Readonly time")),
                row("Disabled datetime", Ui.IDateTime("D", null).Disabled().Render("Disabled datetime")));

        String styling = Ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        Ui.IDate("C", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        Ui.ITime("CL", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                row("Input .ClassInput()",
                        Ui.IDateTime("CI", null).ClassInput("bg-blue-50").Render("Custom input background")),
                row("Size: ST", Ui.IDate("S", null).Size(Ui.ST).Render("Standard size")));

        String behavior = Ui.div("flex flex-col gap-2").render(
                row("Change handler (console.log)",
                        Ui.IDate("Change", null).Change("console.log('changed', this && this.value)")
                                .Render("On change, log")),
                row("Click handler (console.log)", Ui.IDateTime("Click", null)
                        .Click("console.log('clicked datetime')").Render("On click, log")));

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Date, Time, DateTime"),
                Ui.div("text-gray-600").render("Common attributes across temporal inputs."),
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

}
