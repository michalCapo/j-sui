package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.Ui;

public final class RadioPage {
    public static String render(Context ctx) {
        List<Ui.AOption> genders = List.of(
                new Ui.AOption("male", "Male"),
                new Ui.AOption("female", "Female"),
                new Ui.AOption("other", "Other"));

        RadioData selected = new RadioData();
        selected.Gender = "male";

        String singleRadios = Ui.div("flex flex-col gap-2").render(
                Ui.IRadio("Gender", selected).Value("male").Render("Male"),
                Ui.IRadio("Gender", selected).Value("female").Render("Female"),
                Ui.IRadio("Gender", selected).Value("other").Render("Other"));

        String radiosDefault = Ui.IRadioButtons("Group", null).Options(genders).Render("Gender");
        Group2Data group2 = new Group2Data();
        group2.Group2 = "female";
        String radiosWithSelected = Ui.IRadioButtons("Group2", group2).Options(genders).Render("Gender");

        String validation = Ui.div("flex flex-col gap-2").render(
                Ui.div("text-sm text-gray-700").render("Required group (no selection)"),
                Ui.IRadioButtons("ReqGroup", null).Options(genders).Required().Render("Gender (required)"),
                Ui.div("text-sm text-gray-700").render("Required standalone radios (no selection)"),
                Ui.div("flex flex-col gap-1").render(
                        Ui.IRadio("ReqSingle", null).Required().Value("a").Render("Option A"),
                        Ui.IRadio("ReqSingle", null).Required().Value("b").Render("Option B"),
                        Ui.IRadio("ReqSingle", null).Required().Value("c").Render("Option C")));

        String sizes = Ui.div("flex flex-col gap-2").render(
                Ui.IRadio("SizesA", null).Value("a").Render("Default"),
                Ui.IRadio("SizesB", null).Size(Ui.SM).ClassLabel("text-sm").Value("b").Render("Small (SM)"),
                Ui.IRadio("SizesC", null).Size(Ui.XS).ClassLabel("text-sm").Value("c").Render("Extra small (XS)"));

        String disabled = Ui.div("flex flex-col gap-2").render(
                Ui.IRadio("DisA", null).Disabled().Value("a").Render("Disabled A"),
                Ui.IRadio("DisB", null).Disabled().Value("b").Render("Disabled B"));

        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Radio"),
                Ui.div("text-gray-600")
                        .render("Single radio inputs and grouped radio buttons with a selected state."),
                card("Standalone radios (with selection)", singleRadios),
                card("Radio buttons group (no selection)", radiosDefault),
                card("Radio buttons group (with selection)", radiosWithSelected),
                card("Sizes", sizes),
                card("Validation", validation),
                card("Disabled", disabled));
    }

    private static String card(String title, String body) {
        return Ui.div("bg-white p-4 rounded-lg shadow flex flex-col gap-3").render(
                Ui.div("text-sm font-bold text-gray-700").render(title),
                body);
    }

    public static final class RadioData {
        public String Gender;
    }

    public static final class Group2Data {
        public String Group2;
    }
}
