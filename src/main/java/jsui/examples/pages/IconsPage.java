package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class IconsPage {
    public static String render(Context ctx) {
        Ui.TagBuilder row = Ui.div("flex items-center gap-3 bg-white border rounded p-4");
        String basic = row.render(Ui.Icon("w-6 h-6 bg-gray-400 rounded"), Ui.div("flex-1").render("Basic icon"));
        String start = row.render(Ui.IconStart("w-6 h-6 bg-gray-400 rounded", "Start aligned icon"));
        String left = row.render(Ui.IconLeft("w-6 h-6 bg-blue-600 rounded", "Centered with icon left"));
        String right = row.render(Ui.IconRight("w-6 h-6 bg-green-600 rounded", "Centered with icon right"));
        String end = row.render(Ui.IconEnd("w-6 h-6 bg-purple-600 rounded", "End-aligned icon"));
        String card = Ui.div("").render(
                Ui.div("flex flex-col gap-3").render(basic, start, left, right, end));

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Icons"),
                Ui.div("text-gray-600").render("Icon positioning helpers and layouts."),
                card);
    }
}
