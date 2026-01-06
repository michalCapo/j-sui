package jsui.examples.pages;

import jsui.Context;
import jsui.ui;
import jsui.examples.models.LoginForm;
import jsui.examples.models.CounterModel;

public final class OthersPage {
    public static String render(Context ctx) {
        return ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-10").render(
                ui.div("text-3xl font-bold").render("Other Components"),
                ui.div("text-gray-600").render("A miscellany of interactive and structural components."),
                simpleCard("Deferred Content", DeferredComponent.render(ctx)),
                simpleCard("Interactive Login", LoginComponent.render(ctx)),
                simpleCard("Interactive Counter", CounterComponent.render(ctx, 10)),
                simpleCard("Action Callbacks", HelloComponent.render(ctx)));
    }

    private static String simpleCard(String title, String body) {
        return ui.div("flex flex-col gap-3").render(
                ui.div("text-xl font-bold pb-2").render(title),
                body);
    }

    public static final class HelloComponent {
        private static final String BUTTON_CLASS = "rounded whitespace-nowrap";

        public static String render(Context ctx) {
            return ui.div("flex flex-col sm:flex-row gap-2").render(
                    ui.Button().Color(ui.Blue).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayHello).None())
                            .Render("Say Hello"),
                    ui.Button().Color(ui.Gray).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayDelay).None())
                            .Render("Delayed Response (2s)"),
                    ui.Button().Color(ui.Yellow).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayError).None())
                            .Render("Return Error"),
                    ui.Button().Color(ui.Red).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayCrash).None())
                            .Render("Crash Server"));
        }

        private static String sayHello(Context ctx) {
            ctx.Success("Hello from server!");
            return null;
        }

        private static String sayDelay(Context ctx) throws Exception {
            ctx.Info("Processing request...");
            Thread.sleep(2000);
            ctx.Success("Delayed response finished.");
            return null;
        }

        private static String sayError(Context ctx) {
            ctx.Error("Something went wrong on the server.");
            return null;
        }

        private static String sayCrash(Context ctx) {
            throw new RuntimeException("Server crash simulation");
        }
    }

    public static final class CounterComponent {
        public static String render(Context ctx, int start) {
            CounterModel model = new CounterModel();
            model.Count = start;
            return renderCounter(ctx, model);
        }

        private static String decrement(Context ctx) {
            CounterModel model = new CounterModel();
            ctx.Body(model);
            if (model.Count > 0) {
                model.Count--;
            } else {
                ctx.Error("Counter cannot be negative");
            }
            return renderCounter(ctx, model);
        }

        private static String increment(Context ctx) {
            CounterModel model = new CounterModel();
            ctx.Body(model);
            model.Count++;
            return renderCounter(ctx, model);
        }

        private static String renderCounter(Context ctx, CounterModel model) {
            ui.Target target = ui.Target();
            return ui.div("flex items-center gap-4 bg-gray-50 p-6 rounded-lg border w-fit",
                    target.id()).render(
                            ui.Button().Color(ui.RedOutline).Class("rounded-full w-10 h-10")
                                    .Click(ctx.Call(CounterComponent::decrement, model).Replace(target.id()))
                                    .Render("-"),
                            ui.div("text-2xl font-mono font-bold w-12 text-center")
                                    .render(Integer.toString(model.Count)),
                            ui.Button().Color(ui.BlueOutline).Class("rounded-full w-10 h-10")
                                    .Click(ctx.Call(CounterComponent::increment, model).Replace(target.id()))
                                    .Render("+"));
        }

    }

    public static final class LoginComponent {
        private static final ui.Target target = ui.Target();

        public static String render(Context ctx) {
            return render(ctx, new LoginForm(), null);
        }

        private static String submit(Context ctx) {
            LoginForm form = new LoginForm();
            ctx.Body(form);
            if (!"user".equals(form.Name) || !"password".equals(form.Password)) {
                return render(ctx, form, "Invalid credentials");
            }
            return ui.div("text-green-600 max-w-md p-8 text-center font-bold rounded-lg bg-white shadow-xl")
                    .render("Success");
        }

        private static String render(Context ctx, LoginForm form, String error) {
            String errHtml = "";
            if (error != null) {
                errHtml = ui.div("text-red-600 p-4 rounded text-center border-4 border-red-600 bg-white")
                        .render(error);
            }
            return ui.form("border flex flex-col gap-4 max-w-md bg-white p-8 rounded-lg shadow-xl",
                    target.id(), ctx.Submit(LoginComponent::submit).Replace(target.id()))
                    .render(
                            errHtml,
                            ui.IText("Name", form).Required().Render("Name"),
                            ui.IPassword("Password", form).Required().Render("Password"),
                            ui.Button().Submit().Color(ui.Blue).Class("rounded").Render("Login"));
        }

    }

    public static final class DeferredComponent {
        public static String render(Context ctx) {
            ui.Target target = ui.Target();

            ctx.Delay(target.Replace, 1000, c -> ui.div("bg-white rounded-lg shadow p-4").render(
                    ui.div("text-green-700 font-semibold").render("Deferred content ready"),
                    ui.div("text-gray-600")
                            .render("This content was rendered asynchronously and delivered via live patch.")));

            return ui.div("space-y-4").render(
                    target.Skeleton(ui.SkeletonType.component));
        }
    }
}
