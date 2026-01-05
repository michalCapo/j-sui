package jsui.examples.pages;

import jsui.Context;
import jsui.ui;
import jsui.examples.models.DateData;

public final class DatePage {
    public static String render(Context ctx) {
        DateData data = new DateData();
        data.Birth = new java.util.Date();

        String basics = ui.div("flex flex-col gap-2").render(
                row("Date", ui.IDate("Birth", data).Render("Birth date")),
                row("Time", ui.ITime("Alarm", null).Render("Alarm")),
                row("DateTime", ui.IDateTime("Meeting", null).Render("Meeting time")),
                row("Required date", ui.IDate("Req", null).Required().Render("Required date")),
                row("Readonly time", ui.ITime("RO", null).Readonly().Value("10:00").Render("Readonly time")),
                row("Disabled datetime", ui.IDateTime("D", null).Disabled().Render("Disabled datetime")));

        String styling = ui.div("flex flex-col gap-2").render(
                row("Wrapper .Class()",
                        ui.IDate("C", null).Class("p-2 rounded bg-yellow-50").Render("Styled wrapper")),
                row("Label .ClassLabel()",
                        ui.ITime("CL", null).ClassLabel("text-purple-700 font-bold").Render("Custom label")),
                row("Input .ClassInput()",
                        ui.IDateTime("CI", null).ClassInput("bg-blue-50").Render("Custom input background")),
                row("Size: ST", ui.IDate("S", null).Size(ui.ST).Render("Standard size")));

        String behavior = ui.div("flex flex-col gap-2").render(
                row("Change handler (console.log)",
                        ui.IDate("Change", null).Change("console.log('changed', this && this.value)")
                                .Render("On change, log")),
                row("Click handler (console.log)", ui.IDateTime("Click", null)
                        .Click("console.log('clicked datetime')").Render("On click, log")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Date, Time, DateTime"),
                ui.div("text-gray-600").render("Common attributes across temporal inputs."),
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

}
