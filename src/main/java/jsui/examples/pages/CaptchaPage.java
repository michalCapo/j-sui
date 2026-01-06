package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class CaptchaPage {
    public static String render(Context ctx) {
        return ui.div("max-w-full sm:max-w-6xl mx-auto flex flex-col gap-6 w-full").render(
                ui.div("text-3xl font-bold").render("CAPTCHA Component Examples"),
                ui.div("text-gray-600")
                        .render("Demonstrates the reusable CAPTCHA component with server-side validation helpers."),
                ui.div("bg-white p-4 rounded-lg shadow-md border").render(
                        ui.div("flex flex-wrap gap-4").render(
                                ui.div("").render(
                                        ui.div("text-lg font-bold mb-4").render("CAPTCHA v2 Component"),
                                        ui.Captcha2Component.create(CaptchaPage::validated).render(ctx)),
                                ui.div("flex-1").render(),
                                ui.div("").render(
                                        ui.div("text-lg font-bold mb-4").render("CAPTCHA v3 Component"),
                                        ui.Captcha3Component.create(CaptchaPage::validated).render(ctx)))));
    }

    private static String validated(Context ctx) {
        return ui.div("text-green-600").render("Captcha validated successfully!");
    }
}
