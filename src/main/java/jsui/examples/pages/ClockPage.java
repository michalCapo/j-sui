package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class ClockPage {
    private static Ui.Target target = Ui.Target();

    public static String clock() {
        String currentTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        return Ui.div("flex items-baseline gap-3", Ui.targetAttr(target)).render(
                Ui.div("text-4xl font-mono tracking-widest").render(currentTime),
                Ui.div("text-gray-500").render("Live server time"));
    }

    public static String render(Context ctx) {
        ctx.Repeat(target.Render, 1000, c -> {
            return clock();
        });

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Clock"),
                Ui.div("text-gray-600").render("Updates every second via live patches."),
                Ui.div("bg-white p-6 rounded-lg shadow").render(clock()));
    }
}
