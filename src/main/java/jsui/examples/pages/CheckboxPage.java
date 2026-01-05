package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class CheckboxPage {
    public static String render(Context ctx) {
        CheckboxData data = new CheckboxData();
        data.Agree = true;

        String basics = ui.div("flex flex-col gap-2").render(
                example("Default", ui.ICheckbox("Agree", data).Render("I agree")),
                example("Required", ui.ICheckbox("Terms", null).Required().Render("Accept terms")),
                example("Unchecked", ui.ICheckbox("X", null).Render("Unchecked")),
                example("Disabled", ui.ICheckbox("D", null).Disabled().Render("Disabled")));

        String sizes = ui.div("flex flex-col gap-2").render(
                example("Small (SM)", ui.ICheckbox("S", null).Size(ui.SM).Render("Small")),
                example("Extra small (XS)", ui.ICheckbox("XS", null).Size(ui.XS).Render("Extra small")));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Checkbox"),
                ui.div("text-gray-600").render("Checkbox states, sizes, and required validation."),
                card("Basics", basics),
                card("Sizes", sizes));
    }

    private static String card(String title, String body) {
        return ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String example(String label, String control) {
        return ui.div("flex items-start justify-between gap-4 w-full").render(
                ui.div("text-sm text-gray-600").render(label),
                control);
    }

    public static final class CheckboxData {
        public boolean Agree;
    }
}
