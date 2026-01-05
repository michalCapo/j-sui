package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class ClockPage {
    private static ui.Target target = ui.Target();

    public static String clock() {
        String currentTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        return ui.div("flex items-baseline gap-3", target.id()).render(
                ui.div("text-4xl font-mono tracking-widest").render(currentTime),
                ui.div("text-gray-500").render("Live server time"));
    }

    public static String render(Context ctx) {
        ctx.Repeat(target.Render, 1000, c -> {
            return clock();
        });

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Clock"),
                ui.div("text-gray-600").render("Updates every second via live patches."),
                ui.div("bg-white p-6 rounded-lg shadow").render(clock()));
    }
}
