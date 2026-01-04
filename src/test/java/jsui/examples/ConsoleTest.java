package jsui.examples;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jsui.Server;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that captures and analyzes browser console output.
 *
 * Prerequisites:
 * 1. Install system dependencies for Playwright browsers
 * 2. Install Playwright browsers: mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
 * 3. Run tests: mvn test -Dtest=ConsoleTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConsoleTest {

    private static final int PORT = 1423;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static Server server;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    record ConsoleMessage(String type, String text, String location) {
        @Override
        public String toString() {
            return "[" + type.toUpperCase() + "] " + text +
                   (location != null && !location.isEmpty() ? " at " + location : "");
        }
    }

    private final List<ConsoleMessage> consoleMessages = new ArrayList<>();

    @BeforeAll
    static void startServerAndBrowser() throws IOException, InterruptedException {
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

        boolean ready = serverReady.await(10, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(ready, "Server should start within 10 seconds");

        server = serverRef.get();
        assertNotNull(server, "Server should be initialized");
        Thread.sleep(500);

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));
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
        consoleMessages.clear();
        context = browser.newContext();
        page = context.newPage();

        // Capture all console messages
        page.onConsoleMessage(msg -> {
            String location = msg.location();
            consoleMessages.add(new ConsoleMessage(msg.type(), msg.text(), location));
        });
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Capture and display all console messages from homepage")
    void testCaptureConsoleMessages() {
        page.navigate(BASE_URL + "/");
        page.waitForLoadState();

        // Wait for any delayed console output
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore
        }

        System.out.println("\n=== Console Messages from Homepage ===");
        if (consoleMessages.isEmpty()) {
            System.out.println("No console messages captured.");
        } else {
            for (ConsoleMessage msg : consoleMessages) {
                System.out.println("  " + msg);
            }
        }
        System.out.println("Total messages: " + consoleMessages.size());
        System.out.println();
    }

    @Test
    @Order(2)
    @DisplayName("Check for JavaScript errors on homepage")
    void testNoErrorsOnHomepage() {
        page.navigate(BASE_URL + "/");
        page.waitForLoadState();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore
        }

        List<ConsoleMessage> errors = consoleMessages.stream()
                .filter(m -> m.type().equals("error"))
                .toList();

        if (!errors.isEmpty()) {
            System.err.println("\n=== JavaScript Errors Found ===");
            for (ConsoleMessage error : errors) {
                System.err.println("  " + error);
            }
            System.err.println();
        }

        assertTrue(errors.isEmpty(),
                "Page should not have JavaScript errors. Found: " + errors.size());
    }

    @Test
    @Order(3)
    @DisplayName("Check for warnings on homepage")
    void testWarningsOnHomepage() {
        page.navigate(BASE_URL + "/");
        page.waitForLoadState();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore
        }

        List<ConsoleMessage> warnings = consoleMessages.stream()
                .filter(m -> m.type().equals("warning"))
                .toList();

        System.out.println("\n=== Warnings on Homepage ===");
        if (warnings.isEmpty()) {
            System.out.println("No warnings found.");
        } else {
            for (ConsoleMessage warning : warnings) {
                System.out.println("  " + warning);
            }
        }
        System.out.println("Total warnings: " + warnings.size());
        System.out.println();

        // Note: This assertion can be enabled/disabled based on requirements
        // assertTrue(warnings.isEmpty(), "Page should not have warnings");
    }

    @Test
    @Order(4)
    @DisplayName("Check console errors on all demo pages")
    void testNoErrorsOnAllPages() {
        String[] pages = {"/", "/icons", "/button", "/text", "/password",
                "/number", "/date", "/area", "/select", "/checkbox", "/radio",
                "/form", "/table", "/captcha", "/others", "/shared", "/collate",
                "/append", "/clock", "/deferred", "/spa", "/markdown"};

        List<String> pagesWithErrors = new ArrayList<>();

        for (String pagePath : pages) {
            consoleMessages.clear();
            page.navigate(BASE_URL + pagePath);
            page.waitForLoadState();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }

            List<ConsoleMessage> errors = consoleMessages.stream()
                    .filter(m -> m.type().equals("error"))
                    .toList();

            if (!errors.isEmpty()) {
                pagesWithErrors.add(pagePath);
                System.err.println("\nErrors on " + pagePath + ":");
                for (ConsoleMessage error : errors) {
                    System.err.println("  " + error);
                }
            } else {
                System.out.println(pagePath + " - OK");
            }
        }

        if (!pagesWithErrors.isEmpty()) {
            fail("\nPages with JavaScript errors: " + pagesWithErrors);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Log network errors from console")
    void testNetworkErrors() {
        page.navigate(BASE_URL + "/");
        page.waitForLoadState();

        // Also capture network failures
        List<String> networkErrors = new ArrayList<>();
        page.onRequestFailed(request -> {
            networkErrors.add(request.url() + " - " + request.failure());
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        System.out.println("\n=== Network Errors ===");
        if (networkErrors.isEmpty()) {
            System.out.println("No network errors.");
        } else {
            for (String error : networkErrors) {
                System.out.println("  " + error);
            }
        }
        System.out.println();
    }

    @Test
    @Order(6)
    @DisplayName("Check console after user interaction")
    void testConsoleAfterInteraction() {
        page.navigate(BASE_URL + "/button");
        page.waitForLoadState();

        consoleMessages.clear();

        // Click a button and check for errors
        var buttons = page.locator("button");
        if (buttons.count() > 0) {
            buttons.first().click();
            page.waitForTimeout(500);
        }

        List<ConsoleMessage> errors = consoleMessages.stream()
                .filter(m -> m.type().equals("error"))
                .toList();

        System.out.println("\n=== Console Messages After Button Click ===");
        for (ConsoleMessage msg : consoleMessages) {
            System.out.println("  " + msg);
        }
        System.out.println();

        assertTrue(errors.isEmpty(),
                "No errors should occur after button click. Found: " + errors.size());
    }

    @Test
    @Order(7)
    @DisplayName("Display log messages")
    void testLogMessages() {
        page.navigate(BASE_URL + "/");
        page.waitForLoadState();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }

        List<ConsoleMessage> logs = consoleMessages.stream()
                .filter(m -> m.type().equals("log"))
                .toList();

        System.out.println("\n=== Console Log Messages ===");
        if (logs.isEmpty()) {
            System.out.println("No log messages.");
        } else {
            for (ConsoleMessage log : logs) {
                System.out.println("  " + log.text());
            }
        }
        System.out.println("Total logs: " + logs.size());
        System.out.println();
    }

    @Test
    @Order(8)
    @DisplayName("Summary of console activity across all pages")
    void testConsoleSummary() {
        String[] pages = {"/", "/button", "/form", "/table", "/spa"};

        System.out.println("\n=== Console Activity Summary ===");

        for (String pagePath : pages) {
            consoleMessages.clear();
            page.navigate(BASE_URL + pagePath);
            page.waitForLoadState();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }

            long errors = consoleMessages.stream().filter(m -> m.type().equals("error")).count();
            long warnings = consoleMessages.stream().filter(m -> m.type().equals("warning")).count();
            long logs = consoleMessages.stream().filter(m -> m.type().equals("log")).count();

            System.out.printf("  %-10s: %d errors, %d warnings, %d logs%n",
                    pagePath, errors, warnings, logs);
        }
        System.out.println();
    }
}
