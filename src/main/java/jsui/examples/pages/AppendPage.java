package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class AppendPage {
    public static String render(Context ctx) {
        Ui.Target target = Ui.Target();
        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Append / Prepend"),
                Ui.div("text-gray-600").render("Demonstrates appending and prepending items to a container."),
                Ui.div("bg-white p-6 rounded-lg shadow flex items-center gap-3").render(
                        Ui.Button().Color(Ui.Green).Class("rounded")
                                .Click(ctx.Call(AppendPage::doPrepend).Prepend(Ui.targetAttr(target)))
                                .Render("Prepend"),
                        Ui.Button().Color(Ui.Purple).Class("rounded")
                                .Click(ctx.Call(AppendPage::doAppend).Append(Ui.targetAttr(target)))
                                .Render("Append")),
                Ui.div("bg-white p-6 rounded-lg shadow min-h-20 space-y-2", Ui.targetAttr(target)).render());
    }

    public static String doAppend(Context ctx) {
        return Ui.div("px-3 py-2 rounded bg-blue-50 border").render("Appended at " + java.time.LocalTime.now());
    }

    public static String doPrepend(Context ctx) {
        return Ui.div("px-3 py-2 rounded bg-green-50 border").render("Prepended at " + java.time.LocalTime.now());
    }
}
