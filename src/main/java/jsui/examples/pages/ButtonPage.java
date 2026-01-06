package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class ButtonPage {
    public static String render(Context ctx) {
        String[][] sizeDefs = {
                { ui.XS, "Extra small" },
                { ui.SM, "Small" },
                { ui.MD, "Medium (default)" },
                { ui.ST, "Standard" },
                { ui.LG, "Large" },
                { ui.XL, "Extra large" },
        };

        String[][] solid = {
                { ui.Blue, "Blue" },
                { ui.Green, "Green" },
                { ui.Red, "Red" },
                { ui.Purple, "Purple" },
                { ui.Yellow, "Yellow" },
                { ui.Gray, "Gray" },
                { ui.White, "White" },
        };

        String[][] outline = {
                { ui.BlueOutline, "Blue (outline)" },
                { ui.GreenOutline, "Green (outline)" },
                { ui.RedOutline, "Red (outline)" },
                { ui.PurpleOutline, "Purple (outline)" },
                { ui.YellowOutline, "Yellow (outline)" },
                { ui.GrayOutline, "Gray (outline)" },
                { ui.WhiteOutline, "White (outline)" },
        };

        StringBuilder colorsGrid = new StringBuilder();
        for (String[] entry : solid) {
            colorsGrid.append(
                    ui.Button()
                            .Color(entry[0])
                            .Class("rounded w-full")
                            .Render(entry[1]));
        }
        for (String[] entry : outline) {
            colorsGrid.append(
                    ui.Button()
                            .Color(entry[0])
                            .Class("rounded w-full")
                            .Render(entry[1]));
        }
        String colors = ui.div("grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2")
                .render(colorsGrid.toString());

        StringBuilder sizes = new StringBuilder();
        for (String[] entry : sizeDefs) {
            sizes.append(example(
                    entry[1],
                    ui.Button()
                            .Size(entry[0])
                            .Class("rounded")
                            .Color(ui.Blue)
                            .Render("Click me")));
        }
        String sizesGrid = ui.div("flex flex-col gap-2").render(sizes.toString());

        String basics = ui.div("flex flex-col gap-2").render(
                example("Button", ui.Button().Class("rounded").Color(ui.Blue).Render("Click me")),
                example("Button — disabled",
                        ui.Button().Disabled().Class("rounded").Color(ui.Blue).Render("Unavailable")),
                example("Button as link",
                        ui.a(ui.Classes(ui.BTN, ui.MD, "rounded", ui.Blue),
                                ui.Attr.of().href("https://example.com")).render("Visit example.com")),
                example("Submit button (visual)",
                        ui.Button().Submit().Class("rounded").Color(ui.Green).Render("Submit")),
                example("Reset button (visual)",
                        ui.Button().Reset().Class("rounded").Color(ui.Gray).Render("Reset")));

        String colorsCard = card("Colors (solid and outline)", colors);
        String sizesCard = card("Sizes", sizesGrid);
        String basicsCard = card("Basics", basics);

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Button"),
                ui.div("text-gray-600")
                        .render("Common button states and variations. Clicks here are for visual demo only."),
                basicsCard,
                colorsCard,
                sizesCard);
    }

    private static String card(String title, String body) {
        return ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3 border").render(
                ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String example(String label, String control) {
        return ui.div("flex items-center justify-between gap-4 w-full").render(
                ui.div("text-sm text-gray-600").render(label),
                control);
    }
}
