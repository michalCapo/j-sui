package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.ui;

public final class RadioPage {
    public static String render(Context ctx) {
        List<ui.AOption> genders = List.of(
                new ui.AOption("male", "Male"),
                new ui.AOption("female", "Female"),
                new ui.AOption("other", "Other"));

        RadioData selected = new RadioData();
        selected.Gender = "male";

        String singleRadios = ui.div("flex flex-col gap-2").render(
                ui.IRadio("Gender", selected).Value("male").Render("Male"),
                ui.IRadio("Gender", selected).Value("female").Render("Female"),
                ui.IRadio("Gender", selected).Value("other").Render("Other"));

        String radiosDefault = ui.IRadioButtons("Group", null).Options(genders).Render("Gender");
        Group2Data group2 = new Group2Data();
        group2.Group2 = "female";
        String radiosWithSelected = ui.IRadioButtons("Group2", group2).Options(genders).Render("Gender");

        String validation = ui.div("flex flex-col gap-2").render(
                ui.div("text-sm text-gray-700").render("Required group (no selection)"),
                ui.IRadioButtons("ReqGroup", null).Options(genders).Required().Render("Gender (required)"),
                ui.div("text-sm text-gray-700").render("Required standalone radios (no selection)"),
                ui.div("flex flex-col gap-1").render(
                        ui.IRadio("ReqSingle", null).Required().Value("a").Render("Option A"),
                        ui.IRadio("ReqSingle", null).Required().Value("b").Render("Option B"),
                        ui.IRadio("ReqSingle", null).Required().Value("c").Render("Option C")));

        String sizes = ui.div("flex flex-col gap-2").render(
                ui.IRadio("SizesA", null).Value("a").Render("Default"),
                ui.IRadio("SizesB", null).Size(ui.SM).ClassLabel("text-sm").Value("b").Render("Small (SM)"),
                ui.IRadio("SizesC", null).Size(ui.XS).ClassLabel("text-sm").Value("c").Render("Extra small (XS)"));

        String disabled = ui.div("flex flex-col gap-2").render(
                ui.IRadio("DisA", null).Disabled().Value("a").Render("Disabled A"),
                ui.IRadio("DisB", null).Disabled().Value("b").Render("Disabled B"));

        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                ui.div("text-3xl font-bold").render("Radio"),
                ui.div("text-gray-600")
                        .render("Single radio inputs and grouped radio buttons with a selected state."),
                card("Standalone radios (with selection)", singleRadios),
                card("Radio buttons group (no selection)", radiosDefault),
                card("Radio buttons group (with selection)", radiosWithSelected),
                card("Sizes", sizes),
                card("Validation", validation),
                card("Disabled", disabled));
    }

    private static String card(String title, String body) {
        return ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    public static final class RadioData {
        public String Gender;
    }

    public static final class Group2Data {
        public String Group2;
    }
}
