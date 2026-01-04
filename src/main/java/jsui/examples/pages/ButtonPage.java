package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class ButtonPage {
    public static String render(Context ctx) {
        String[][] sizeDefs = {
                { Ui.XS, "Extra small" },
                { Ui.SM, "Small" },
                { Ui.MD, "Medium (default)" },
                { Ui.ST, "Standard" },
                { Ui.LG, "Large" },
                { Ui.XL, "Extra large" },
        };

        String[][] solid = {
                { Ui.Blue, "Blue" },
                { Ui.Green, "Green" },
                { Ui.Red, "Red" },
                { Ui.Purple, "Purple" },
                { Ui.Yellow, "Yellow" },
                { Ui.Gray, "Gray" },
                { Ui.White, "White" },
        };

        String[][] outline = {
                { Ui.BlueOutline, "Blue (outline)" },
                { Ui.GreenOutline, "Green (outline)" },
                { Ui.RedOutline, "Red (outline)" },
                { Ui.PurpleOutline, "Purple (outline)" },
                { Ui.YellowOutline, "Yellow (outline)" },
                { Ui.GrayOutline, "Gray (outline)" },
                { Ui.WhiteOutline, "White (outline)" },
        };

        StringBuilder colorsGrid = new StringBuilder();
        for (String[] entry : solid) {
            colorsGrid.append(
                    Ui.Button()
                            .Color(entry[0])
                            .Class("rounded w-full")
                            .Render(entry[1]));
        }
        for (String[] entry : outline) {
            colorsGrid.append(
                    Ui.Button()
                            .Color(entry[0])
                            .Class("rounded w-full")
                            .Render(entry[1]));
        }
        String colors = Ui.div("grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2")
                .render(colorsGrid.toString());

        StringBuilder sizes = new StringBuilder();
        for (String[] entry : sizeDefs) {
            sizes.append(example(
                    entry[1],
                    Ui.Button()
                            .Size(entry[0])
                            .Class("rounded")
                            .Color(Ui.Blue)
                            .Render("Click me")));
        }
        String sizesGrid = Ui.div("flex flex-col gap-2").render(sizes.toString());

        String basics = Ui.div("flex flex-col gap-2").render(
                example("Button", Ui.Button().Class("rounded").Color(Ui.Blue).Render("Click me")),
                example("Button — disabled",
                        Ui.Button().Disabled().Class("rounded").Color(Ui.Blue).Render("Unavailable")),
                example("Button as link",
                        Ui.a(Ui.Classes(Ui.BTN, Ui.MD, "rounded", Ui.Blue),
                                Ui.Attr.of().href("https://example.com")).render("Visit example.com")),
                example("Submit button (visual)",
                        Ui.Button().Submit().Class("rounded").Color(Ui.Green).Render("Submit")),
                example("Reset button (visual)",
                        Ui.Button().Reset().Class("rounded").Color(Ui.Gray).Render("Reset")));

        String colorsCard = card("Colors (solid and outline)", colors);
        String sizesCard = card("Sizes", sizesGrid);
        String basicsCard = card("Basics", basics);

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Button"),
                Ui.div("text-gray-600")
                        .render("Common button states and variations. Clicks here are for visual demo only."),
                basicsCard,
                colorsCard,
                sizesCard);
    }

    private static String card(String title, String body) {
        return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                Ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    private static String example(String label, String control) {
        return Ui.div("flex items-center justify-between gap-4 w-full").render(
                Ui.div("text-sm text-gray-600").render(label),
                control);
    }
}
