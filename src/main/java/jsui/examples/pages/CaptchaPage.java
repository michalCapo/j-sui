package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class CaptchaPage {
    public static String render(Context ctx) {
        return Ui.div("max-w-full sm:max-w-6xl mx-auto flex flex-col gap-6 w-full").render(
                Ui.div("text-3xl font-bold").render("CAPTCHA Component Examples"),
                Ui.div("text-gray-600")
                        .render("Demonstrates the reusable CAPTCHA component with server-side validation helpers."),
                Ui.div("bg-white p-4 rounded-lg shadow-md").render(
                        Ui.div("flex flex-wrap gap-4").render(
                                Ui.div("").render(
                                        Ui.div("text-lg font-bold mb-4").render("CAPTCHA v2 Component"),
                                        Ui.Captcha2Component.create(CaptchaPage::validated).render(ctx)),
                                Ui.div("flex-1").render(),
                                Ui.div("").render(
                                        Ui.div("text-lg font-bold mb-4").render("CAPTCHA v3 Component"),
                                        Ui.Captcha3Component.create(CaptchaPage::validated).render(ctx)))));
    }

    private static String validated(Context ctx) {
        return Ui.div("text-green-600").render("Captcha validated successfully!");
    }
}
