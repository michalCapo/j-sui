package jsui.examples.pages;

import java.util.List;
import jsui.Context;
import jsui.Ui;
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
        Ui.Target target = Ui.Target();
        Ui.FormInstance form = Ui.FormNew(ctx.Submit(FormPage::submit).Replace(Ui.targetAttr(target)));

        String result = "Form submit result will be displayed here.";
        if (error == null && data != null) {
            result = String.format(
                    "Form data: Title=%s, Some=%s, Gender=%s, GenderNext=%s, Number=%s, Country=%s, Agree=%s",
                    data.Title, data.Some, data.Gender, data.GenderNext, data.Number, data.Country, data.Agree);
        }

        List<Ui.AOption> genders = List.of(
                new Ui.AOption("male", "Male"),
                new Ui.AOption("female", "Female"),
                new Ui.AOption("other", "Other"));
        List<Ui.AOption> numbers = List.of(
                new Ui.AOption("1", Ui.div("h-10 py-2 px-4 rounded-md border border-gray-300").render("1")),
                new Ui.AOption("2", Ui.div("h-10 py-2 px-4 rounded-md border border-gray-300").render("2")),
                new Ui.AOption("3", Ui.div("h-10 py-2 px-4 rounded-md border border-gray-300").render("3")));
        List<Ui.AOption> countries = List.of(
                new Ui.AOption("1", "USA"),
                new Ui.AOption("2", "Slovakia"),
                new Ui.AOption("3", "Germany"),
                new Ui.AOption("4", "Japan"));

        return Ui.div("max-w-5xl mx-auto flex flex-col gap-4", Ui.targetAttr(target)).render(
                Ui.div("text-2xl font-bold").render("Form association"),
                Ui.div("text-gray-600").render(
                        "Form input fields and submit button is defined outside html form element. This is useful when you want to reuse the form in multiple places."),
                Ui.div("border rounded-lg p-4 bg-white dark:bg-gray-900 shadow-lg border rounded-lg flex flex-col gap-4")
                        .render(
                                Ui.div("flex flex-col").render(
                                        Ui.div("text-lg font-semibold").render("Form creation example"),
                                        Ui.div("text-gray-600 text-sm mb-4")
                                                .render("Form example with input fields and submit button.")),
                                Ui.div("flex flex-col").render(result),
                                form.Render(),
                                form.Text("Title", data).Required().Render("Title"),
                                form.Radio("GenderNext", data).Value("male").Render("Male"),
                                form.Radio("GenderNext", data).Value("female").Render("Female"),
                                form.Checkbox("Agree", data).Render("I agree"),
                                form.RadioButtons("Gender", data).Options(genders).Render("Gender"),
                                form.Select("Country", data).Options(countries).Render("Country"),
                                form.Hidden("Some", 123),
                                form.RadioDiv("Number", data).Options(numbers).Render("Number"),
                                form.Button().Color(Ui.Blue).Submit().Render("Submit")));
    }

}
