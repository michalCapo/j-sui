package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class IconsPage {
    public static String render(Context ctx) {
        ui.TagBuilder row = ui.div("flex items-center gap-3 bg-white border rounded p-4");
        String basic = row.render(ui.Icon("w-6 h-6 bg-gray-400 rounded"), ui.div("flex-1").render("Basic icon"));
        String start = row.render(ui.IconStart("w-6 h-6 bg-gray-400 rounded", "Start aligned icon"));
        String left = row.render(ui.IconLeft("w-6 h-6 bg-blue-600 rounded", "Centered with icon left"));
        String right = row.render(ui.IconRight("w-6 h-6 bg-green-600 rounded", "Centered with icon right"));
        String end = row.render(ui.IconEnd("w-6 h-6 bg-purple-600 rounded", "End-aligned icon"));
        String card = ui.div("").render(
                ui.div("flex flex-col gap-3").render(basic, start, left, right, end));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Icons"),
                ui.div("text-gray-600").render("Icon positioning helpers and layouts."),
                card);
    }
}
