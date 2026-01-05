package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.ui;
import jsui.examples.models.FormData;

public final class FormPage {
    public static String render(Context ctx) {
        FormData data = new FormData();
        data.Gender = "male";
        data.GenderNext = "male";
        data.Number = 2;
        return render(ctx, data, null);
    }

    private static String submit(Context ctx) {
        FormData form = new FormData();
        ctx.Body(form);
        ctx.Success(
                "Form submitted successfully" + (form.Title == null || form.Title.isEmpty() ? "" : ": " + form.Title));
        return render(ctx, form, null);
    }

    private static String render(Context ctx, FormData data, Exception error) {
        ui.Target target = ui.Target();
        ui.FormInstance form = ui.FormNew(ctx.Submit(FormPage::submit).Replace(target.id()));

        String result = "Form submit result will be displayed here.";
        if (error == null && data != null) {
            result = String.format(
                    "Form data: Title=%s, Some=%s, Gender=%s, GenderNext=%s, Number=%s, Country=%s, Agree=%s",
                    data.Title, data.Some, data.Gender, data.GenderNext, data.Number, data.Country, data.Agree);
        }

        List<ui.AOption> genders = List.of(
                new ui.AOption("male", "Male"),
                new ui.AOption("female", "Female"),
                new ui.AOption("other", "Other"));
        List<ui.AOption> numbers = List.of(
                new ui.AOption("1", ui.div("h-10 py-2 px-4 rounded-md border border-gray-300").render("1")),
                new ui.AOption("2", ui.div("h-10 py-2 px-4 rounded-md border border-gray-300").render("2")),
                new ui.AOption("3", ui.div("h-10 py-2 px-4 rounded-md border border-gray-300").render("3")));
        List<ui.AOption> countries = List.of(
                new ui.AOption("1", "USA"),
                new ui.AOption("2", "Slovakia"),
                new ui.AOption("3", "Germany"),
                new ui.AOption("4", "Japan"));

        return ui.div("max-w-5xl mx-auto flex flex-col gap-4", target.id()).render(
                ui.div("text-2xl font-bold").render("Form association"),
                ui.div("text-gray-600").render(
                        "Form input fields and submit button is defined outside html form element. This is useful when you want to reuse to form in multiple places."),
                ui.div("border rounded-lg p-4 bg-white dark:bg-gray-900 shadow-lg border rounded-lg flex flex-col gap-4")
                        .render(
                                ui.div("flex flex-col").render(
                                        ui.div("text-lg font-semibold").render("Form creation example"),
                                        ui.div("text-gray-600 text-sm mb-4")
                                                .render("Form example with input fields and submit button.")),
                                ui.div("flex flex-col").render(result),
                                form.Render(),
                                form.Text("Title", data).Required().Render("Title"),
                                form.Radio("GenderNext", data).Value("male").Render("Male"),
                                form.Radio("GenderNext", data).Value("female").Render("Female"),
                                form.Checkbox("Agree", data).Render("I agree"),
                                form.RadioButtons("Gender", data).Options(genders).Render("Gender"),
                                form.Select("Country", data).Options(countries).Render("Country"),
                                form.Hidden("Some", 123),
                                form.RadioDiv("Number", data).Options(numbers).Render("Number"),
                                form.Button().Color(ui.Blue).Submit().Render("Submit")));
    }

}
