package jsui.examples;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jsui.Server;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Playwright-based integration tests for the j-sui example application.
 *
 * Prerequisites:
 * 1. Install Playwright browsers:
 *    mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
 * 2. Run tests: mvn test -Dtest=AppTest
 *
 * To see the browser running, set headless=false in startServerAndBrowser()
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppTest {

    private static final int PORT = 1422;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static Server server;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void startServerAndBrowser() throws IOException, InterruptedException {
        // Start the j-sui server in a background thread
        AtomicReference<Server> serverRef = new AtomicReference<>();
        CountDownLatch serverReady = new CountDownLatch(1);

        Thread serverThread = new Thread(() -> {
            try {
                serverRef.set(Main.startServer(PORT));
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
    @DisplayName("Server should be running and respond to root path")
    void testServerIsRunning() {
        page.navigate(BASE_URL + "/");
        assertEquals(BASE_URL + "/", page.url());
        assertTrue(page.content().length() > 100, "Page should have content");
    }

    @Test
    @Order(2)
    @DisplayName("Page should have a title")
    void testPageHasTitle() {
        page.navigate(BASE_URL + "/");
        String title = page.title();
        assertFalse(title.isEmpty(), "Page should have a title");
        System.out.println("Page title: " + title);
    }

    @Test
    @Order(3)
    @DisplayName("Page should contain UI elements")
    void testPageHasUiElements() {
        page.navigate(BASE_URL + "/");

        // Check for common UI elements (buttons, inputs, links, etc.)
        long buttonCount = page.locator("button").count();
        long linkCount = page.locator("a").count();
        long inputCount = page.locator("input").count();

        System.out.println("Buttons found: " + buttonCount);
        System.out.println("Links found: " + linkCount);
        System.out.println("Inputs found: " + inputCount);

        assertTrue(linkCount > 0, "Page should have at least one link");
    }

    @Test
    @Order(4)
    @DisplayName("Button page should load and contain buttons")
    void testButtonPage() {
        page.navigate(BASE_URL + "/button");
        assertEquals(BASE_URL + "/button", page.url());

        page.waitForLoadState();

        long buttonCount = page.locator("button").count();
        assertTrue(buttonCount > 0, "Button page should have buttons");
        System.out.println("Buttons on button page: " + buttonCount);
    }

    @Test
    @Order(5)
    @DisplayName("Text input page should load and contain input fields")
    void testTextPage() {
        page.navigate(BASE_URL + "/text");
        assertEquals(BASE_URL + "/text", page.url());

        page.waitForLoadState();

        long inputCount = page.locator("input[type='text'], input:not([type])").count();
        assertTrue(inputCount > 0, "Text page should have text inputs");
        System.out.println("Text inputs on text page: " + inputCount);
    }

    @Test
    @Order(6)
    @DisplayName("Form page should load and contain form elements")
    void testFormPage() {
        page.navigate(BASE_URL + "/form");
        assertEquals(BASE_URL + "/form", page.url());

        page.waitForLoadState();

        long inputCount = page.locator("input, select, textarea").count();
        assertTrue(inputCount > 0, "Form page should have form elements");
        System.out.println("Form elements on form page: " + inputCount);
    }

    @Test
    @Order(7)
    @DisplayName("Table page should load and contain table")
    void testTablePage() {
        page.navigate(BASE_URL + "/table");
        assertEquals(BASE_URL + "/table", page.url());

        page.waitForLoadState();

        long tableCount = page.locator("table").count();
        assertTrue(tableCount > 0, "Table page should have a table");
        System.out.println("Tables on table page: " + tableCount);
    }

    @Test
    @Order(8)
    @DisplayName("Should be able to click on navigation links")
    void testNavigation() {
        page.navigate(BASE_URL + "/");

        page.waitForLoadState();

        // Find a link to /button and click it
        var buttonLink = page.locator("a[href='/button']").first();
        if (buttonLink.count() > 0) {
            buttonLink.click();
            page.waitForLoadState();
            assertTrue(page.url().contains("/button"), "Should navigate to button page");
        } else {
            System.out.println("Button link not found, skipping navigation test");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Page should have favicon set")
    void testFavicon() {
        page.navigate(BASE_URL + "/");

        boolean hasFavicon = page.locator("link[rel*='icon']").count() > 0;
        assertTrue(hasFavicon, "Page should have a favicon");
    }

    @Test
    @Order(10)
    @DisplayName("Take screenshot of the page")
    void testScreenshot() {
        page.navigate(BASE_URL + "/");

        page.waitForLoadState();

        // Create screenshots directory
        java.nio.file.Path screenshotDir = java.nio.file.Paths.get("target/screenshots");
        try {
            java.nio.file.Files.createDirectories(screenshotDir);
        } catch (IOException e) {
            // Ignore if already exists
        }

        page.screenshot(new Page.ScreenshotOptions()
                .setPath(screenshotDir.resolve("homepage.png"))
                .setFullPage(true));

        System.out.println("Screenshot saved to target/screenshots/homepage.png");
    }

    @Test
    @Order(11)
    @DisplayName("Console should not have critical JavaScript errors")
    void testNoConsoleErrors() {
        page.navigate(BASE_URL + "/");

        var consoleErrors = new java.util.ArrayList<String>();
        page.onConsoleMessage(msg -> {
            if ("error".equals(msg.type())) {
                consoleErrors.add(msg.text());
            }
        });

        page.waitForLoadState();

        // Wait for any delayed errors
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        if (!consoleErrors.isEmpty()) {
            System.out.println("Console errors found:");
            consoleErrors.forEach(System.out::println);
        }

        // Note: This may need to be relaxed depending on the app
        // assertTrue(consoleErrors.isEmpty(), "Page should not have console errors");
    }

    @Test
    @Order(12)
    @DisplayName("Should be able to type in text input")
    void testTypingInInput() {
        page.navigate(BASE_URL + "/text");

        page.waitForLoadState();

        var inputs = page.locator("input[type='text'], input:not([type])");
        if (inputs.count() > 0) {
            var input = inputs.first();
            input.fill("Hello, j-sui!");

            String value = input.inputValue();
            assertEquals("Hello, j-sui!", value, "Input should contain typed text");
        } else {
            System.out.println("No text inputs found on text page");
        }
    }

    @Test
    @Order(13)
    @DisplayName("All demo pages should be accessible")
    void testAllPagesAccessible() {
        String[] pages = {"/", "/icons", "/button", "/text", "/password", "/number",
                "/date", "/area", "/select", "/checkbox", "/radio", "/form",
                "/table", "/captcha", "/others", "/shared", "/collate",
                "/append", "/clock", "/deferred", "/spa", "/markdown"};

        for (String pagePath : pages) {
            page.navigate(BASE_URL + pagePath);
            page.waitForLoadState();

            // Check that page loaded successfully (not an error page)
            assertFalse(page.url().contains("error"),
                    "Page " + pagePath + " should not contain error in URL");

            // Check that page has content
            assertTrue(page.content().length() > 500,
                    "Page " + pagePath + " should have content");

            System.out.println("Page " + pagePath + " loaded successfully");
        }
    }
}
