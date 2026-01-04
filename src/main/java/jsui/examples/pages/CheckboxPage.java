package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class CheckboxPage {
    public static String render(Context ctx) {
        CheckboxData data = new CheckboxData();
        data.Agree = true;

        String basics = Ui.div("flex flex-col gap-2").render(
                example("Default", Ui.ICheckbox("Agree", data).Render("I agree")),
                example("Required", Ui.ICheckbox("Terms", null).Required().Render("Accept terms")),
                example("Unchecked", Ui.ICheckbox("X", null).Render("Unchecked")),
                example("Disabled", Ui.ICheckbox("D", null).Disabled().Render("Disabled")));

        String sizes = Ui.div("flex flex-col gap-2").render(
                example("Small (SM)", Ui.ICheckbox("S", null).Size(Ui.SM).Render("Small")),
                example("Extra small (XS)", Ui.ICheckbox("XS", null).Size(Ui.XS).Render("Extra small")));

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Checkbox"),
                Ui.div("text-gray-600").render("Checkbox states, sizes, and required validation."),
                card("Basics", basics),
                card("Sizes", sizes));
    }

    private static String card(String title, String body) {
        return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                Ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String example(String label, String control) {
        return Ui.div("flex items-start justify-between gap-4 w-full").render(
                Ui.div("text-sm text-gray-600").render(label),
                control);
    }

    public static final class CheckboxData {
        public boolean Agree;
    }
}
