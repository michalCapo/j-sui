package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class TablePage {
    public static String render(Context ctx) {
        Ui.SimpleTable table = Ui.SimpleTable(4, "w-full table-auto");
        table.Class(0, "text-left font-bold p-2 border-b border-gray-200")
                .Class(1, "text-left p-2 border-b border-gray-200")
                .Class(2, "text-left p-2 border-b border-gray-200")
                .Class(3, "text-right p-2 border-b border-gray-200");
        table.Field("ID").Field("Name").Field("Email").Field("Actions");
        table.Field("1").Field("John Doe").Field("john@example.com").Field(
                Ui.Button().Class("px-3 py-1 rounded").Color(Ui.Blue).Render("View"));
        table.Field("2").Field("Jane Roe").Field("jane@example.com").Field(
                Ui.Button().Class("px-3 py-1 rounded").Color(Ui.Green).Render("Edit"));
        table.Field("Notice", "text-blue-700 font-semibold text-center").Attr("colspan=\"4\"");
        table.Field("3").Field("No Email User").Empty().Field(
                Ui.Button().Class("px-3 py-1 rounded").Color(Ui.Gray).Render("Disabled"));
        table.Field("Span 2", "text-center").Attr("colspan=\"2\"")
                .Field("Right side").Attr("colspan=\"2\"");
        table.Empty().Field("Span across 3 columns").Attr("colspan=\"3\"");
        String tableCard = card("Basic", table.Render());

        Ui.SimpleTable t2 = Ui.SimpleTable(4, "w-full table-auto");
        t2.Class(0, "p-2 border-b border-gray-200")
                .Class(1, "p-2 border-b border-gray-200")
                .Class(2, "p-2 border-b border-gray-200")
                .Class(3, "p-2 border-b border-gray-200");
        t2.Field("Full-width notice", "text-blue-700 font-semibold").Attr("colspan=\"4\"");
        t2.Field("Left span 2").Attr("colspan=\"2\"")
                .Field("Right span 2").Attr("colspan=\"2\"");
        t2.Field("Span 3").Attr("colspan=\"3\"").Field("End");
        String t2Card = card("Colspan", t2.Render());

        Ui.SimpleTable t3 = Ui.SimpleTable(3, "w-full table-auto");
        t3.Class(0, "text-left p-2 border-b border-gray-200")
                .Class(1, "text-right p-2 border-b border-gray-200")
                .Class(2, "text-right p-2 border-b border-gray-200");
        t3.Field("Item").Field("Qty").Field("Amount");
        t3.Field("Apples").Field("3").Field("$6.00");
        t3.Field("Oranges").Field("2").Field("$5.00");
        t3.Field("Total", "font-semibold").Attr("colspan=\"2\"")
                .Field("$11.00", "font-semibold");
        String t3Card = card("Column Classes & Totals", t3.Render());

        return Ui.div("flex flex-col gap-4").render(
                Ui.div("text-3xl font-bold").render("Table"),
                Ui.div("text-gray-600").render("SimpleTable with column classes, colspans, and totals."),
                tableCard,
                t2Card,
                t3Card);
    }

    private static String card(String title, String body) {
        return Ui.div("bg-white rounded shadow p-4 border border-gray-200 overflow-hidden").render(
                Ui.div("text-lg font-bold").render(title),
                body);
    }
}
