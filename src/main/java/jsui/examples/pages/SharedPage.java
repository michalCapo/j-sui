package jsui.examples.pages;

import jsui.Context;
import jsui.ui;

public final class SharedPage {
    public static String render(Context ctx) {
        TemplateForm form1 = new TemplateForm("Hello", "What a nice day");
        form1.onSubmit = c -> {
            c.Error("Data not stored");
            return form1.render(c);
        };

        TemplateForm form2 = new TemplateForm("Next Title", "Next Description");
        form2.onSubmit = c -> {
            c.Success("Data stored but do not shared");
            return form2.render(c);
        };

        return ui.div("max-w-5xl mx-auto flex flex-col gap-4").render(
                ui.div("text-2xl font-bold").render("Shared"),
                ui.div("text-gray-600")
                        .render("Tries to mimmic real application: reused form in multiplate places"),
                ui.div("border rounded-lg p-4 bg-white dark:bg-gray-900 shadow-lg border rounded-lg").render(
                        ui.div("text-lg font-semibold").render("Form 1"),
                        ui.div("text-gray-600 text-sm mb-4").render("This form is reused."),
                        form1.render(ctx)),
                ui.div("border rounded-lg p-4 bg-white dark:bg-gray-900 shadow-lg border rounded-lg").render(
                        ui.div("text-lg font-semibold").render("Form 2"),
                        ui.div("text-gray-600 text-sm mb-4").render("This form is reused."),
                        form2.render(ctx)));
    }

    public static final class TemplateForm {
        private final ui.Target target;
        public String Title;
        public String Description;
        public Context.Callable onSubmit;

        public TemplateForm(String title, String description) {
            this.target = ui.Target();
            this.Title = title;
            this.Description = description;
        }

        public String onCancel(Context ctx) {
            Title = "";
            Description = "";
            return render(ctx);
        }

        public String render(Context ctx) {
            return ui.form("flex flex-col gap-4", target.id(),
                    ctx.Submit(this.onSubmit).Replace(target.id())).render(
                            ui.div("").render(
                                    ui.div("text-gray-600 text-sm").render("Title"),
                                    ui.IText("Title", this).Class("w-full").Placeholder("Title").Render("")),
                            ui.div("").render(
                                    ui.div("text-gray-600 text-sm").render("Description"),
                                    ui.IArea("Description", this).Class("w-full").Placeholder("Description")
                                            .Render("")),
                            ui.div("flex flex-row gap-4 justify-end").render(
                                    ui.Button()
                                            .Class("rounded-lg hover:text-red-700 hover:underline text-gray-400")
                                            .Click(ctx.Call(this::onCancel).Replace(target.id()))
                                            .Render("Reset"),
                                    ui.Button()
                                            .Submit()
                                            .Class("rounded-lg")
                                            .Color(ui.Blue)
                                            .Render("Submit")));
        }
    }
}
