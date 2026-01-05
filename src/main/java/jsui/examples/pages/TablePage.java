package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class TablePage {
    public static String render(Context ctx) {
        ui.SimpleTable table = ui.SimpleTable(4, "w-full table-auto");
        table.Class(0, "text-left font-bold p-2 border-b border-gray-200")
                .Class(1, "text-left p-2 border-b border-gray-200")
                .Class(2, "text-left p-2 border-b border-gray-200")
                .Class(3, "text-right p-2 border-b border-gray-200");
        table.Field("ID").Field("Name").Field("Email").Field("Actions");
        table.Field("1").Field("John Doe").Field("john@example.com").Field(
                ui.Button().Class("px-3 py-1 rounded").Color(ui.Blue).Render("View"));
        table.Field("2").Field("Jane Roe").Field("jane@example.com").Field(
                ui.Button().Class("px-3 py-1 rounded").Color(ui.Green).Render("Edit"));
        table.Field("Notice", "text-blue-700 font-semibold text-center").Attr("colspan=\"4\"");
        table.Field("3").Field("No Email User").Empty().Field(
                ui.Button().Class("px-3 py-1 rounded").Color(ui.Gray).Render("Disabled"));
        table.Field("Span 2", "text-center").Attr("colspan=\"2\"")
                .Field("Right side").Attr("colspan=\"2\"");
        table.Empty().Field("Span across 3 columns").Attr("colspan=\"3\"");
        String tableCard = card("Basic", table.Render());

        ui.SimpleTable t2 = ui.SimpleTable(4, "w-full table-auto");
        t2.Class(0, "p-2 border-b border-gray-200")
                .Class(1, "p-2 border-b border-gray-200")
                .Class(2, "p-2 border-b border-gray-200")
                .Class(3, "p-2 border-b border-gray-200");
        t2.Field("Full-width notice", "text-blue-700 font-semibold").Attr("colspan=\"4\"");
        t2.Field("Left span 2").Attr("colspan=\"2\"")
                .Field("Right span 2").Attr("colspan=\"2\"");
        t2.Field("Span 3").Attr("colspan=\"3\"").Field("End");
        String t2Card = card("Colspan", t2.Render());

        ui.SimpleTable t3 = ui.SimpleTable(3, "w-full table-auto");
        t3.Class(0, "text-left p-2 border-b border-gray-200")
                .Class(1, "text-right p-2 border-b border-gray-200")
                .Class(2, "text-right p-2 border-b border-gray-200");
        t3.Field("Item").Field("Qty").Field("Amount");
        t3.Field("Apples").Field("3").Field("$6.00");
        t3.Field("Oranges").Field("2").Field("$5.00");
        t3.Field("Total", "font-semibold").Attr("colspan=\"2\"")
                .Field("$11.00", "font-semibold");
        String t3Card = card("Column Classes & Totals", t3.Render());

        return ui.div("flex flex-col gap-4").render(
                ui.div("text-3xl font-bold").render("Table"),
                ui.div("text-gray-600").render("SimpleTable with column classes, colspans, and totals."),
                tableCard,
                t2Card,
                t3Card);
    }

    private static String card(String title, String body) {
        return ui.div("bg-white rounded shadow p-4 border border-gray-200 overflow-hidden").render(
                ui.div("text-lg font-bold").render(title),
                body);
    }
}
