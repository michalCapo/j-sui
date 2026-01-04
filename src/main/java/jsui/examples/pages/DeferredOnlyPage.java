package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class DeferredOnlyPage {
    public static String render(Context ctx) {
        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Deferred"),
                Ui.div("text-gray-600").render("Shows skeleton replaced with async content via live patches."),
                Ui.div("bg-white p-6 rounded-lg shadow").render(
                        OthersPage.DeferredComponent.render(ctx)));
    }
}
