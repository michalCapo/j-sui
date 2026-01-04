package jsui.examples.pages;

import jsui.Context;
import jsui.Ui;

public final class MarkdownPage {
    public static String render(Context ctx) {
        String md = String.join("\n",
                "# Markdown Demo",
                "",
                "This page shows the Ui.Markdown wrapper.",
                "",
                "## Features",
                "- Headings (#, ##, ###)",
                "- Paragraphs and line breaks",
                "- Inline code like `console.log('hi')`",
                "- Bold **strong** and *emphasis*",
                "",
                "### Code Block",
                "```",
                "function greet(name) {",
                "  return 'Hello, ' + name;",
                "}",
                "```",
                "",
                "That's it!");

        String body = Ui.Markdown("prose max-w-none", md);
        return Ui.div("max-w-full sm:max-w-3xl mx-auto flex flex-col gap-6").render(
                Ui.div("text-3xl font-bold").render("Markdown"),
                Ui.div("bg-white p-6 rounded-lg shadow").render(body));
    }
}
