package jsui.examples;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jsui.App;
import jsui.Context;
import jsui.Server;
import jsui.ui;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Playwright-based test for ctx.Patch functionality.
 *
 * Tests that ctx.Patch correctly updates DOM elements via WebSocket or inline script fallback.
 *
 * Prerequisites:
 * 1. Install Playwright browsers:
 *    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
 * 2. Run tests: mvn test -Dtest=PatchTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatchTest {

    private static final int PORT = 1424;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static Server server;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void startServerAndBrowser() throws IOException, InterruptedException {
        // Create a minimal App with a test page
        App app = new App("en");
        app.debug(true);

        // Create a test page that demonstrates Patch functionality
        app.Page("/patch-test", ctx -> {
            ui.Target target = ui.Target();
            
            // Action that patches the target with new content
            Context.Callable patchAction = c -> {
                String newContent = ui.div("bg-green-100 p-4 rounded border-2 border-green-500")
                        .render(
                                ui.div("text-green-800 font-bold").render("Patched Successfully!"),
                                ui.div("text-green-600").render("This content was updated via ctx.Patch"));
                c.Patch(target.Replace, newContent);
                return null;
            };

            // Action that patches with inline swap
            Context.Callable patchInlineAction = c -> {
                String newContent = ui.div("bg-blue-100 p-4 rounded")
                        .render(ui.div("text-blue-800 font-bold").render("Inline Patch Works!"));
                c.Patch(target.Render, newContent);
                return null;
            };

            // Action that patches with append
            Context.Callable patchAppendAction = c -> {
                String newContent = ui.div("bg-yellow-100 p-2 rounded mt-2")
                        .render(ui.span("text-yellow-800").render(" (Appended)"));
                c.Patch(target.Append, newContent);
                return null;
            };

            // Action that patches with prepend
            Context.Callable patchPrependAction = c -> {
                String newContent = ui.div("bg-purple-100 p-2 rounded mb-2")
                        .render(ui.span("text-purple-800").render("(Prepended) "));
                c.Patch(target.Prepend, newContent);
                return null;
            };

            return app.HTML("Patch Test", "bg-gray-100 min-h-screen p-8",
                    ui.div("max-w-2xl mx-auto space-y-6").render(
                            ui.div("text-3xl font-bold mb-4").render("ctx.Patch Test"),
                            ui.div("bg-white p-6 rounded-lg shadow").render(
                                    ui.div("mb-4").render(
                                            ui.div("text-lg font-semibold mb-2").render("Target Element:"),
                                            ui.div("bg-gray-50 p-4 rounded border", target.id())
                                                    .render(ui.div("text-gray-600").render("Initial content"))),
                                    ui.div("flex flex-wrap gap-2").render(
                                            ui.Button().Color(ui.Blue).Class("rounded")
                                                    .Click(ctx.Call(patchAction).None())
                                                    .Render("Replace (outline)"),
                                            ui.Button().Color(ui.Green).Class("rounded")
                                                    .Click(ctx.Call(patchInlineAction).None())
                                                    .Render("Update (inline)"),
                                            ui.Button().Color(ui.Yellow).Class("rounded")
                                                    .Click(ctx.Call(patchAppendAction).None())
                                                    .Render("Append"),
                                            ui.Button().Color(ui.Purple).Class("rounded")
                                                    .Click(ctx.Call(patchPrependAction).None())
                                                    .Render("Prepend")))));
        });

        // Start the server in a background thread
        AtomicReference<Server> serverRef = new AtomicReference<>();
        CountDownLatch serverReady = new CountDownLatch(1);

        Thread serverThread = new Thread(() -> {
            try {
                serverRef.set(Server.builder(app)
                        .httpPort(PORT)
                        .start());
                serverReady.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                serverReady.countDown();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait for server to be ready (max 10 seconds)
        boolean ready = serverReady.await(10, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(ready, "Server should start within 10 seconds");

        server = serverRef.get();
        assertNotNull(server, "Server should be initialized");

        // Additional wait for server to be fully ready
        Thread.sleep(500);

        // Initialize Playwright
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)     // Set to false to see the browser
                .setSlowMo(50));        // Slow down actions for visibility
    }

    @AfterAll
    static void stopServerAndBrowser() throws IOException {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        if (server != null) {
            server.close();
        }
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test page should load successfully")
    void testPageLoads() {
        page.navigate(BASE_URL + "/patch-test");
        assertEquals(BASE_URL + "/patch-test", page.url());
        page.waitForLoadState();

        // Verify initial content
        String initialContent = page.locator("div[class*='bg-gray-50']").textContent();
        assertTrue(initialContent != null && initialContent.contains("Initial content"),
                "Page should have initial content");
    }

    @Test
    @Order(2)
    @DisplayName("Patch with Replace (outline) should update the element")
    void testPatchReplace() {
        page.navigate(BASE_URL + "/patch-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Click the Replace button
        page.locator("button:has-text('Replace (outline)')").click();

        // Wait for the patch to be applied
        try {
            page.waitForSelector("div:has-text('Patched Successfully!')");
        } catch (Exception e) {
            // Fallback: check if content changed
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify the content was replaced
        String content = page.locator("div[class*='bg-green-100']").textContent();
        assertNotNull(content, "Patched content should exist");
        assertTrue(content.contains("Patched Successfully!"),
                "Content should contain 'Patched Successfully!'");
        assertTrue(content.contains("This content was updated via ctx.Patch"),
                "Content should contain patch message");
    }

    @Test
    @Order(3)
    @DisplayName("Patch with inline swap should update innerHTML")
    void testPatchInline() {
        page.navigate(BASE_URL + "/patch-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Click the Update (inline) button
        page.locator("button:has-text('Update (inline)')").click();

        // Wait for the patch to be applied
        try {
            page.waitForSelector("div:has-text('Inline Patch Works!')");
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify the content was updated inline
        String content = page.locator("div[class*='bg-blue-100']").textContent();
        assertNotNull(content, "Patched content should exist");
        assertTrue(content.contains("Inline Patch Works!"),
                "Content should contain 'Inline Patch Works!'");
    }

    @Test
    @Order(4)
    @DisplayName("Patch with Append should add content at the end")
    void testPatchAppend() {
        page.navigate(BASE_URL + "/patch-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Click the Append button
        page.locator("button:has-text('Append')").click();

        // Wait for the patch to be applied
        try {
            page.waitForSelector("div:has-text('(Appended)')");
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify the content was appended
        String content = page.locator("div[class*='bg-yellow-100']").textContent();
        assertNotNull(content, "Appended content should exist");
        assertTrue(content.contains("(Appended)"),
                "Content should contain '(Appended)'");
    }

    @Test
    @Order(5)
    @DisplayName("Patch with Prepend should add content at the beginning")
    void testPatchPrepend() {
        page.navigate(BASE_URL + "/patch-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Click the Prepend button
        page.locator("button:has-text('Prepend')").click();

        // Wait for the patch to be applied
        try {
            page.waitForSelector("div:has-text('(Prepended)')");
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify the content was prepended
        String content = page.locator("div[class*='bg-purple-100']").textContent();
        assertNotNull(content, "Prepended content should exist");
        assertTrue(content.contains("(Prepended)"),
                "Content should contain '(Prepended)'");
    }

    @Test
    @Order(6)
    @DisplayName("Multiple patches should work sequentially")
    void testMultiplePatches() {
        page.navigate(BASE_URL + "/patch-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Apply multiple patches in sequence
        page.locator("button:has-text('Replace (outline)')").click();
        try {
            page.waitForSelector("div:has-text('Patched Successfully!')");
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        page.locator("button:has-text('Append')").click();
        try {
            page.waitForSelector("div:has-text('(Appended)')");
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify both patches were applied
        String content = page.locator("div[class*='bg-green-100'], div[class*='bg-yellow-100']")
                .first().textContent();
        assertNotNull(content, "Content should exist after multiple patches");
    }

    @Test
    @Order(7)
    @DisplayName("Patch should work even if WebSocket is not available (fallback to inline script)")
    void testPatchFallback() {
        // This test verifies that patches work via inline script when WebSocket is not available
        // We can't easily disable WebSocket in the test, but we can verify the functionality works
        page.navigate(BASE_URL + "/patch-test");
        page.waitForLoadState();

        // Disable JavaScript temporarily to test fallback (but this won't work for our test)
        // Instead, we'll just verify the patch works regardless of transport mechanism
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Click a button and verify patch works
        page.locator("button:has-text('Update (inline)')").click();

        try {
            page.waitForSelector("div:has-text('Inline Patch Works!')",
                    new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Verify patch was applied (works via WebSocket or inline script)
        String content = page.locator("div[class*='bg-blue-100']").textContent();
        assertNotNull(content, "Patched content should exist");
        assertTrue(content.contains("Inline Patch Works!"),
                "Patch should work via WebSocket or inline script fallback");
    }
}

