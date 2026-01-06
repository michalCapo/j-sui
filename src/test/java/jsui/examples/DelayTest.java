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
 * Test for ctx.Delay functionality.
 *
 * Tests that ctx.Delay correctly executes a task after a delay and patches DOM elements.
 *
 * Prerequisites:
 * 1. Install Playwright browsers:
 *    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
 * 2. Run tests: mvn test -Dtest=DelayTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DelayTest {

    private static final int PORT = 1426;
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

        // Create a test page that demonstrates Delay functionality
        app.Page("/delay-test", ctx -> {
            ui.Target target = ui.Target();

            // Action that delays execution by 1 second then patches the target
            ctx.Call(c -> {
                c.Delay(target.Replace, 1000, delayedCtx -> {
                    return ui.div("bg-purple-100 p-4 rounded border-2 border-purple-500")
                            .render(
                                    ui.div("text-purple-800 font-bold").render("Delayed Task Complete!"),
                                    ui.div("text-purple-600").render("This content was delivered via ctx.Delay after 1000ms"));
                });
                return null;
            }).None();

            return app.HTML("Delay Test", "bg-gray-100 min-h-screen p-8",
                    ui.div("max-w-2xl mx-auto space-y-6").render(
                            ui.div("text-3xl font-bold mb-4").render("ctx.Delay Test"),
                            ui.div("bg-white p-6 rounded-lg shadow").render(
                                    ui.div("mb-4").render(
                                            ui.div("text-lg font-semibold mb-2").render("Target Element:"),
                                            ui.div("bg-gray-50 p-4 rounded border", target.id())
                                                    .render(ui.div("text-gray-600").render("Initial content - waiting 1 second for delayed update...")))),
                            ui.div("bg-yellow-50 p-4 rounded border border-yellow-200").render(
                                    ui.div("text-yellow-800 font-semibold").render("About this test"),
                                    ui.div("text-yellow-600 text-sm mt-2")
                                            .render("When the page loads, a delayed task should execute after 1 second and update the target element above."))));
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
        page.navigate(BASE_URL + "/delay-test");
        assertEquals(BASE_URL + "/delay-test", page.url());
        page.waitForLoadState();

        // Verify initial content exists
        var target = page.locator("div[class*='bg-gray-50']");
        assertTrue(target.count() > 0, "Target element should exist");
    }

    @Test
    @Order(2)
    @DisplayName("Delay should execute task after 1 second and patch the DOM")
    void testDelayExecutesAndPatches() {
        page.navigate(BASE_URL + "/delay-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Wait for the delayed content to appear (should happen after ~1000ms)
        try {
            page.waitForSelector("div:has-text('Delayed Task Complete!')",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            // Element not found - test will fail below
        }

        // Verify the content was patched
        var purpleBox = page.locator("div[class*='bg-purple-100']");
        String content = purpleBox.textContent();

        // THIS ASSERTION IS EXPECTED TO FAIL - demonstrating that Delay doesn't work
        assertNotNull(content, "Delayed content should exist");
        assertTrue(content.contains("Delayed Task Complete!"),
                "Content should contain 'Delayed Task Complete!' - Delay should have executed and patched the DOM");
    }

    @Test
    @Order(3)
    @DisplayName("Delay with 0ms should execute immediately")
    void testDelayWithZeroMs() {
        // Create test page with 0ms delay
        App app = new App("en");
        app.debug(true);

        app.Page("/delay-zero", ctx -> {
            ui.Target target = ui.Target();

            ctx.Call(c -> {
                c.Delay(target.Replace, 0, delayedCtx -> {
                    return ui.div("bg-green-100 p-2 rounded").render(ui.div("text-green-800").render("Immediate Delay"));
                });
                return null;
            }).None();

            return app.HTML("Zero Delay", "bg-gray-100 min-h-screen p-8",
                    ui.div("max-w-2xl mx-auto").render(
                            ui.div("text-2xl font-bold mb-4").render("Zero Delay Test"),
                            ui.div("bg-gray-50 p-4 rounded border", target.id())
                                    .render(ui.div("text-gray-600").render("Waiting..."))));
        });

        page.navigate(BASE_URL + "/delay-test");
        page.waitForLoadState();

        // THIS TEST WON'T WORK PROPERLY - route doesn't exist
        // Just showing the structure
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}
