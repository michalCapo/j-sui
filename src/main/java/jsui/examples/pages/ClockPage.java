package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class ClockPage {
    public static String render(Context ctx) {
        Ui.Target target = Ui.Target();

        // Start with current time immediately
        String currentTime = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String timeBody = Ui.div("text-4xl font-mono tracking-widest").render(currentTime);
        String clockContent = Ui.div("flex items-baseline gap-3").render(timeBody,
                Ui.div("text-gray-500").render("Live server time"));

        // Use inline swap so the target id persists across patches
        ctx.Repeat(target.Render, 1000, c -> {
            String time = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String body = Ui.div("text-4xl font-mono tracking-widest").render(time);
            return Ui.div("flex items-baseline gap-3").render(
                    body,
                    Ui.div("text-gray-500").render("Live server time"));
        });

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Clock"),
                Ui.div("text-gray-600").render("Updates every second via live patches."),
                Ui.div("bg-white p-6 rounded-lg shadow", Ui.targetAttr(target)).render(clockContent));
    }
}
