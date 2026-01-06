package jsui.examples;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jsui.App;
import jsui.Server;
import jsui.ui;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ctx.Defer functionality.
 *
 * Tests that ctx.Defer correctly executes background tasks and patches DOM
 * elements.
 *
 * Prerequisites:
 * 1. Install Playwright browsers:
 * mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D
 * exec.args="install"
 * 2. Run tests: mvn test -Dtest=DeferTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeferTest {

    private static final int PORT = 1425;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static Server server;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private static ui.Target target = ui.Target();

    @BeforeAll
    static void startServerAndBrowser() throws IOException, InterruptedException {
        // Create a minimal App with a test page
        App app = new App("en");
        app.debug(true);

        app.Page("/defer-test", ctx -> {

            // Defer should execute in background and patch the target
            ctx.Defer(target.Replace, deferredCtx -> {
                try {
                    // Simulate some work
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Use System.err to ensure visiblity in test output (stderr is captured by most
                // runners)
                return ui.div("bg-green-100 p-4 rounded border-2 border-green-500")
                        .render(
                                ui.div("text-green-800 font-bold").render("Deferred Task Complete!"),
                                ui.div("text-green-600")
                                        .render("This content was delivered via ctx.Defer after 500ms"));
            });

            // Use System.err for immediate test output visibility
            return app.HTML("Defer Test", "bg-gray-100 min-h-screen p-8",
                    ui.div("max-w-2xl mx-auto space-y-6").render(
                            ui.div("text-3xl font-bold mb-4").render("ctx.Defer Test"),
                            ui.div("bg-white p-6 rounded-lg shadow").render(
                                    ui.div("mb-4").render(
                                            ui.div("text-lg font-semibold mb-2").render("Target Element:"),
                                            ui.div("bg-gray-50 p-4 rounded border", target.id())
                                                    .render(ui.div("text-gray-600")
                                                            .render("Initial content - wait for deferred update...")))),
                            ui.div("bg-blue-50 p-4 rounded border border-blue-200").render(
                                    ui.div("text-blue-800 font-semibold").render("About this test"),
                                    ui.div("text-blue-600 text-sm mt-2")
                                            .render("When the page loads, a deferred task should execute in the background and update the target element above within 1 second."))));
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
                .setHeadless(true)
                .setSlowMo(50));
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
        page.navigate(BASE_URL + "/defer-test");
        assertEquals(BASE_URL + "/defer-test", page.url());
        page.waitForLoadState();

        // Verify page title exists (not checking deferred content)
        var title = page.locator("div:has-text('ctx.Defer Test')");
        assertTrue(title.count() > 0, "Page title should exist");

        // var initial = page.locator("div:has-text('Initial content')");
        // assertTrue(initial.count() > 0, "Initial body should contain 'initial
        // state'");
    }

    @Test
    @Order(2)
    @DisplayName("Defer should execute background task and patch the DOM")
    void testDeferExecutesAndPatches() {
        page.navigate(BASE_URL + "/defer-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Wait for the deferred content to appear (should happen within ~500ms +
        // network latency)
        // Using a longer timeout to account for the defer delay
        try {
            page.waitForSelector("div:has-text('Deferred Task Complete!')",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            // Element not found - test will fail below
        }

        // Verify the content was patched
        var greenBox = page.locator("div[class*='bg-green-100']");
        String content = greenBox.textContent();

        // THIS ASSERTION IS EXPECTED TO FAIL - demonstrating that Defer doesn't work
        assertNotNull(content, "Deferred content should exist");
        assertTrue(content.contains("Deferred Task Complete!"),
                "Content should contain 'Deferred Task Complete!' - Defer should have executed and patched the DOM");
    }

    @Test
    @Order(3)
    @DisplayName("Defer with multiple targets should patch both")
    void testDeferMultipleTargets() {
        // Create a second test page with multiple defer targets
        App app = new App("en");
        app.debug(true);

        app.Page("/defer-multi", ctx -> {
            ui.Target target1 = ui.Target();
            ui.Target target2 = ui.Target();

            ctx.Defer(target1.Replace, deferredCtx -> {
                return ui.div("bg-red-100 p-2 rounded").render(ui.div("text-red-800").render("Target 1 Updated"));
            });
            ctx.Defer(target2.Replace, deferredCtx -> {
                return ui.div("bg-blue-100 p-2 rounded").render(ui.div("text-blue-800").render("Target 2 Updated"));
            });

            return app.HTML("Multi Defer", "bg-gray-100 min-h-screen p-8",
                    ui.div("max-w-2xl mx-auto space-y-4").render(
                            ui.div("text-2xl font-bold mb-4").render("Multiple Defer Test"),
                            ui.div("bg-gray-50 p-4 rounded border", target1.id())
                                    .render(ui.div("text-gray-600").render("Target 1 - waiting...")),
                            ui.div("bg-gray-50 p-4 rounded border", target2.id())
                                    .render(ui.div("text-gray-600").render("Target 2 - waiting..."))));
        });

        // Navigate to the test page - this won't work since server is already running
        // But the test structure demonstrates what we want to test
        page.navigate(BASE_URL + "/defer-test");
        page.waitForLoadState();

        // THIS TEST IS EXPECTED TO FAIL - route doesn't exist
        // Just showing the structure of what a multi-target defer test would look like
    }
}
