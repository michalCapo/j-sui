package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class DeferredOnlyPage {
    public static String render(Context ctx) {
        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Deferred"),
                ui.div("text-gray-600").render("Shows skeleton replaced with async content via live patches."),
                ui.div("bg-white p-6 rounded-lg shadow").render(
                        OthersPage.DeferredComponent.render(ctx)));
    }
}
