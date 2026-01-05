package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class SpaPage {
    public static String render(Context ctx) {
        ui.Target target = ui.Target();

        return ui.div("max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Single Page Application (SPA)"),
                ui.div("text-gray-600").render(
                        "This page demonstrates j-sui's SPA capabilities. When SPA mode is enabled, all internal links are intercepted and handled via background loading."),
                ui.div("grid grid-cols-1 md:grid-cols-2 gap-6").render(
                        ui.div("bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700")
                                .render(
                                        ui.div("text-xl font-semibold mb-2").render("Seamless Transitions"),
                                        ui.div("text-gray-500 mb-4").render(
                                                "Navigate between pages without a full browser reload. The scroll position and application state can be preserved better than with traditional multi-page apps."),
                                        ui.a("text-blue-600 hover:underline", ui.Attr.of().href("/"))
                                                .render("Back to Showcase (Smoothly)")),
                        ui.div("bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700")
                                .render(
                                        ui.div("text-xl font-semibold mb-2").render("Background Loading"),
                                        ui.div("text-gray-500 mb-4").render(
                                                "Resources are fetched in the background. A smart loader appears only if the transition takes longer than 50ms."),
                                        ui.Button()
                                                .Color(ui.Blue)
                                                .Click(ctx.Call((Context c) -> {
                                                    try {
                                                        Thread.sleep(1000);
                                                    } catch (InterruptedException e) {
                                                        Thread.currentThread().interrupt();
                                                    }
                                                    return ui.div("text-green-600 font-medium")
                                                            .render("Content loaded after simulation!");
                                                }).Replace(target.id()))
                                                .Render("Trigger Delayed Content"),
                                        ui.div("mt-4", target.id()).render())),
                ui.div("bg-blue-50 dark:bg-blue-900/20 p-6 rounded-xl border border-blue-100 dark:border-blue-800")
                        .render(
                                ui.div("font-semibold text-blue-800 dark:text-blue-300 mb-2").render("How it works"),
                                ui.div("text-blue-700 dark:text-blue-400 text-sm space-y-2").render(
                                        ui.div("").render(
                                                "1. `app.SmoothNavigation(true)` enables global link interception."),
                                        ui.div("")
                                                .render("2. Clicking an internal link triggers a background `fetch`."),
                                        ui.div("").render("3. The server returns the partial or full HTML."),
                                        ui.div("").render("4. The client updates the DOM and browser history."))),
                ui.div("mt-10 pt-6 border-t border-gray-100 dark:border-gray-800 text-center").render(
                        ui.div("text-gray-400 text-xs").render(
                                String.format("Page rendered at %s", java.time.LocalTime.now()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))))));
    }
}
