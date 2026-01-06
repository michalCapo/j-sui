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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ctx.Repeat functionality.
 *
 * Tests that ctx.Repeat correctly executes recurring tasks and patches DOM elements.
 *
 * Prerequisites:
 * 1. Install Playwright browsers:
 *    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
 * 2. Run tests: mvn test -Dtest=RepeatTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepeatTest {

    private static final int PORT = 1427;
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

        // Counter for repeat iterations
        AtomicInteger repeatCounter = new AtomicInteger(0);

        // Create a test page that demonstrates Repeat functionality
        app.Page("/repeat-test", ctx -> {
            ui.Target target = ui.Target();

            // Action that repeats every 500ms and updates the counter
            ctx.Call(c -> {
                c.Repeat(target.Replace, 500, repeatCtx -> {
                    int count = repeatCounter.incrementAndGet();
                    return ui.div("bg-orange-100 p-4 rounded border-2 border-orange-500")
                            .render(
                                    ui.div("text-orange-800 font-bold").render("Repeat Count: " + count),
                                    ui.div("text-orange-600").render("This content updates every 500ms via ctx.Repeat"));
                });
                return null;
            }).None();

            return app.HTML("Repeat Test", "bg-gray-100 min-h-screen p-8",
                    ui.div("max-w-2xl mx-auto space-y-6").render(
                            ui.div("text-3xl font-bold mb-4").render("ctx.Repeat Test"),
                            ui.div("bg-white p-6 rounded-lg shadow").render(
                                    ui.div("mb-4").render(
                                            ui.div("text-lg font-semibold mb-2").render("Target Element:"),
                                            ui.div("bg-gray-50 p-4 rounded border", target.id())
                                                    .render(ui.div("text-gray-600").render("Initial content - waiting for repeat updates...")))),
                            ui.div("bg-red-50 p-4 rounded border border-red-200").render(
                                    ui.div("text-red-800 font-semibold").render("About this test"),
                                    ui.div("text-red-600 text-sm mt-2")
                                            .render("When the page loads, a repeating task should execute every 500ms and update the counter above."))));
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
        page.navigate(BASE_URL + "/repeat-test");
        assertEquals(BASE_URL + "/repeat-test", page.url());
        page.waitForLoadState();

        // Verify initial content exists
        var target = page.locator("div[class*='bg-gray-50']");
        assertTrue(target.count() > 0, "Target element should exist");
    }

    @Test
    @Order(2)
    @DisplayName("Repeat should execute recurring task and patch the DOM")
    void testRepeatExecutesAndPatches() {
        page.navigate(BASE_URL + "/repeat-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Wait for the repeated content to appear (should happen within ~500ms)
        try {
            page.waitForSelector("div:has-text('Repeat Count:')",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            // Element not found - test will fail below
        }

        // Verify the content was patched
        var orangeBox = page.locator("div[class*='bg-orange-100']");
        String content = orangeBox.textContent();

        // THIS ASSERTION IS EXPECTED TO FAIL - demonstrating that Repeat doesn't work
        assertNotNull(content, "Repeated content should exist");
        assertTrue(content.contains("Repeat Count:"),
                "Content should contain 'Repeat Count:' - Repeat should have executed and patched the DOM");
    }

    @Test
    @Order(3)
    @DisplayName("Repeat should increment counter on each iteration")
    void testRepeatIncrementsCounter() {
        page.navigate(BASE_URL + "/repeat-test");
        page.waitForLoadState();

        // Wait for WebSocket connection
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Wait for first update
        try {
            page.waitForSelector("div:has-text('Repeat Count:')", new Page.WaitForSelectorOptions().setTimeout(3000));
        } catch (Exception e) {
            // Element not found
        }

        // Wait for another iteration (500ms interval)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Get the updated count
        String updatedContent = page.locator("div[class*='bg-orange-100']").textContent();
        Integer updatedCount = null;
        if (updatedContent != null && updatedContent.contains("Repeat Count:")) {
            String[] parts = updatedContent.split("Repeat Count:");
            if (parts.length > 1) {
                try {
                    updatedCount = Integer.parseInt(parts[1].trim().split("\\s+")[0]);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        // THIS ASSERTION IS EXPECTED TO FAIL - demonstrating that Repeat doesn't increment
        assertNotNull(updatedCount, "Updated count should exist");
        assertTrue(updatedCount > 1,
                "Count should be greater than 1 after multiple repeat iterations - got: " + updatedCount);
    }
}
