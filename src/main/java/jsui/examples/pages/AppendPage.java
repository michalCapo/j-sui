package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class AppendPage {
    public static String render(Context ctx) {
        ui.Target target = ui.Target();

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Append / Prepend"),
                ui.div("text-gray-600").render("Demonstrates appending and prepending items to a container."),
                ui.div("bg-white p-6 rounded-lg shadow flex items-center gap-3").render(
                        ui.Button().Color(ui.Green).Class("rounded")
                                .Click(ctx.Call(AppendPage::doPrepend).Prepend(target.id()))
                                .Render("Prepend"),
                        ui.Button().Color(ui.Purple).Class("rounded")
                                .Click(ctx.Call(AppendPage::doAppend).Append(target.id()))
                                .Render("Append")),
                ui.div("bg-white p-6 rounded-lg shadow min-h-20 space-y-2", target.id()).render());
    }

    public static String doAppend(Context ctx) {
        return ui.div("px-3 py-2 rounded bg-blue-50 border").render("Appended at " + java.time.LocalTime.now());
    }

    public static String doPrepend(Context ctx) {
        return ui.div("px-3 py-2 rounded bg-green-50 border").render("Prepended at " + java.time.LocalTime.now());
    }
}
