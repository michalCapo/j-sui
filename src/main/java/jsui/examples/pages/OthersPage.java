package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;
import jsui.examples.models.LoginForm;
import jsui.examples.models.CounterModel;

public final class OthersPage {
    public static String render(Context ctx) {
        return Ui.div("max-w-full sm:max-w-5xl mx-auto flex flex-col gap-10").render(
                Ui.div("text-3xl font-bold").render("Other Components"),
                Ui.div("text-gray-600").render("A miscellany of interactive and structural components."),
                simpleCard("Deferred Content", DeferredComponent.render(ctx)),
                simpleCard("Interactive Login", LoginComponent.render(ctx)),
                simpleCard("Interactive Counter", CounterComponent.render(ctx, 10)),
                simpleCard("Action Callbacks", HelloComponent.render(ctx)));
    }

    private static String simpleCard(String title, String body) {
        return Ui.div("flex flex-col gap-3").render(
                Ui.div("text-xl font-bold border-b pb-2").render(title),
                body);
    }

    public static final class HelloComponent {
        private static final String BUTTON_CLASS = "rounded whitespace-nowrap";

        public static String render(Context ctx) {
            return Ui.div("flex flex-col sm:flex-row gap-2").render(
                    Ui.Button().Color(Ui.Blue).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayHello).None())
                            .Render("Say Hello"),
                    Ui.Button().Color(Ui.Gray).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayDelay).None())
                            .Render("Delayed Response (2s)"),
                    Ui.Button().Color(Ui.Yellow).Class(BUTTON_CLASS)
                            .Click(ctx.Call(HelloComponent::sayError).None())
                            .Render("Return Error"),
                    Ui.Button().Color(Ui.Red).Class(BUTTON_CLASS)
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
            Ui.Target target = Ui.Target();
            return Ui.div("flex items-center gap-4 bg-gray-50 p-6 rounded-lg border w-fit",
                    Ui.targetAttr(target)).render(
                            Ui.Button().Color(Ui.RedOutline).Class("rounded-full w-10 h-10")
                                    .Click(ctx.Call(CounterComponent::decrement, model).Replace(Ui.targetAttr(target)))
                                    .Render("-"),
                            Ui.div("text-2xl font-mono font-bold w-12 text-center")
                                    .render(Integer.toString(model.Count)),
                            Ui.Button().Color(Ui.BlueOutline).Class("rounded-full w-10 h-10")
                                    .Click(ctx.Call(CounterComponent::increment, model).Replace(Ui.targetAttr(target)))
                                    .Render("+"));
        }

    }

    public static final class LoginComponent {
        private static final Ui.Target target = Ui.Target();

        public static String render(Context ctx) {
            return render(ctx, new LoginForm(), null);
        }

        private static String submit(Context ctx) {
            LoginForm form = new LoginForm();
            ctx.Body(form);
            if (!"user".equals(form.Name) || !"password".equals(form.Password)) {
                return render(ctx, form, "Invalid credentials");
            }
            return Ui.div("text-green-600 max-w-md p-8 text-center font-bold rounded-lg bg-white shadow-xl")
                    .render("Success");
        }

        private static String render(Context ctx, LoginForm form, String error) {
            String errHtml = "";
            if (error != null) {
                errHtml = Ui.div("text-red-600 p-4 rounded text-center border-4 border-red-600 bg-white")
                        .render(error);
            }
            return Ui.form("border flex flex-col gap-4 max-w-md bg-white p-8 rounded-lg shadow-xl",
                    Ui.targetAttr(target), ctx.Submit(LoginComponent::submit).Replace(Ui.targetAttr(target)))
                    .render(
                            errHtml,
                            Ui.IText("Name", form).Required().Render("Name"),
                            Ui.IPassword("Password", form).Required().Render("Password"),
                            Ui.Button().Submit().Color(Ui.Blue).Class("rounded").Render("Login"));
        }

    }

    public static final class DeferredComponent {
        public static String render(Context ctx) {
            Ui.Target target = Ui.Target();
            // One-shot delayed patch using the new helper; auto-cancellable on navigation
            ctx.Delay(target.Replace, 1000, c -> Ui.div("bg-white rounded-lg shadow p-4").render(
                    Ui.div("text-green-700 font-semibold").render("Deferred content ready"),
                    Ui.div("text-gray-600")
                            .render("This content was rendered asynchronously and delivered via live patch.")));
            return Ui.div("space-y-4", Ui.targetAttr(target)).render(
                    target.Skeleton(Ui.SkeletonType.component));
        }
    }
}
