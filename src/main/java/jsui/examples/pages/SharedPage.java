package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class SharedPage {
    public static String render(Context ctx) {
        TemplateForm form1 = new TemplateForm("Hello", "What a nice day");
        TemplateForm form2 = new TemplateForm("Next Title", "Next Description");

        form1.onSubmit = c -> {
            c.Error("Data not stored");
            return form1.render(c);
        };

        form2.onSubmit = c -> {
            c.Success("Data stored but do not shared");
            return form2.render(c);
        };

        return Ui.div("max-w-5xl mx-auto flex flex-col gap-4").render(
                Ui.div("text-2xl font-bold").render("Shared"),
                Ui.div("text-gray-600")
                        .render("Tries to mimmic real application: reused form in multiplate places"),
                Ui.div("border rounded-lg p-4 bg-white dark:bg-gray-900 shadow-lg border rounded-lg").render(
                        Ui.div("text-lg font-semibold").render("Form 1"),
                        Ui.div("text-gray-600 text-sm mb-4").render("This form is reused."),
                        form1.render(ctx)),
                Ui.div("border rounded-lg p-4 bg-white dark:bg-gray-900 shadow-lg border rounded-lg").render(
                        Ui.div("text-lg font-semibold").render("Form 2"),
                        Ui.div("text-gray-600 text-sm mb-4").render("This form is reused."),
                        form2.render(ctx)));
    }

    public static final class TemplateForm {
        private final Ui.Target target;
        public String Title;
        public String Description;
        public Context.Callable onSubmit;

        public TemplateForm(String title, String description) {
            this.target = Ui.Target();
            this.Title = title;
            this.Description = description;
        }

        public String onCancel(Context ctx) {
            Title = "";
            Description = "";
            return render(ctx);
        }

        public String render(Context ctx) {
            return Ui.form("flex flex-col gap-4", Ui.targetAttr(target),
                    ctx.Submit(onSubmit != null ? onSubmit : c -> "").Replace(Ui.targetAttr(target))).render(
                            Ui.div("").render(
                                    Ui.div("text-gray-600 text-sm").render("Title"),
                                    Ui.IText("Title", this).Class("w-full").Placeholder("Title").Render("")),
                            Ui.div("").render(
                                    Ui.div("text-gray-600 text-sm").render("Description"),
                                    Ui.IArea("Description", this).Class("w-full").Placeholder("Description")
                                            .Render("")),
                            Ui.div("flex flex-row gap-4 justify-end").render(
                                    Ui.Button()
                                            .Class("rounded-lg hover:text-red-700 hover:underline text-gray-400")
                                            .Click(ctx.Call(this::onCancel).Replace(Ui.targetAttr(target)))
                                            .Render("Reset"),
                                    Ui.Button()
                                            .Submit()
                                            .Class("rounded-lg")
                                            .Color(Ui.Blue)
                                            .Render("Submit")));
        }
    }
}
