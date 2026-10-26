# Selenium Grid + TestNG

A Maven framework with Selenium Grid 4.50.0 and TestNG 7.10.2. Separate Chrome and Firefox nodes register with a Hub. TestNG runs the same three login tests on both browsers in parallel: six browser test executions in total.

## Windows quick start
Start Docker Desktop with Linux containers. Extract the project, open Command Prompt in the folder containing compose.yaml, and run:
```
run-grid.bat
```
No host Java, Maven, Chrome, Firefox, or ChromeDriver is needed for a full Docker execution. Docker downloads the images and builds the Java runner. Initial downloads need internet. Provide sufficient Docker memory for two browsers plus Hub/runner (6 GB or more is practical).

The batch file preserves the test exit code and removes containers afterwards. Reports remain in reports/testng/index.html. Failure screenshots are in reports/screenshots. The practice page includes the corrected hidden-button CSS from the earlier Docker project.

## Architecture and configuration
- selenium-hub: official Grid Hub, published on localhost:4444. Event bus ports 4442/4443 are internal to the Compose network.
- chrome: Chrome node with max 1 session and 2 GB shared memory.
- firefox: Firefox node with max 1 session and 2 GB shared memory.
- web: nginx serves the bundled login page; published at http://localhost:8080 for manual inspection.
- test-runner: Maven + Java 21; RemoteWebDriver connects to http://selenium-hub:4444. Browser nodes navigate to http://web.

All Selenium images use the same pinned tag 4.50.0-20261010. Compose waits for Hub and website health. The Java BeforeSuite additionally polls /status until both Chrome and Firefox nodes are UP and registered; a started container alone does not guarantee node registration. It fails after 120 seconds rather than silently skipping tests.

## Observe the Grid while running
```
docker compose up -d selenium-hub chrome firefox web
docker compose run --rm test-runner
```
On the first use build the runner first with `docker compose build test-runner`.
Grid dashboard: http://localhost:4444/ui
Chrome noVNC: http://localhost:7900
Firefox noVNC: http://localhost:7901
Default noVNC password: secret. The browser is visible by default. Tests may complete quickly. Stop services afterwards with `docker compose down`.

## Parallel execution
In testng.xml, parallel="tests" and thread-count="2" run Chrome Tests and Firefox Tests concurrently. Within each browser's test block, methods run sequentially. @Parameters("browser") supplies browser capabilities; the Hub routes sessions to the matching node. DriverFactory uses ThreadLocal so parallel tests do not share a WebDriver. Every test method creates and closes its own session. Startup logs include browser, thread, and session ID. No scenario depends on another.

## Java source layout
base/BaseTest.java: Grid readiness and browser setup/cleanup.
driver/DriverFactory.java: per-thread RemoteWebDriver, Chrome/Firefox capabilities.
support/GridWait.java: registered-node readiness polling.
pages/LoginPage.java: locators and waits.
tests/LoginTest.java: valid login, invalid login, logout.
tests/ConfigurationTest.java: four browser-free configuration/fixture tests, run separately.
support/PracticeServer.java: test-only HTTP helper used by ConfigurationTest; Docker serves the actual page through nginx.

## Settings
config.properties values can be overridden with Java -D properties or uppercase environment variables (dots become underscores). Precedence: -D, environment, properties.
- grid.url / GRID_URL: host default http://localhost:4444; Docker http://selenium-hub:4444.
- app.base.url / APP_BASE_URL: http://web. Browser nodes resolve this Compose service name, even when the Java runner is on the Windows host.
- grid.required.browsers / GRID_REQUIRED_BROWSERS: chrome,firefox.
- headless / HEADLESS: false.
- wait.seconds / WAIT_SECONDS: 15.
- page.load.seconds / PAGE_LOAD_SECONDS: 30.

For one-browser execution, remove the other test block from testng.xml and adjust grid.required.browsers accordingly. Do not point the remote browser to localhost:8080: localhost would mean the browser's own container.

## Optional Eclipse or host Maven runner
Start services with `docker compose up -d selenium-hub chrome firefox web` first. Import as Existing Maven Project in Eclipse and run testng.xml as TestNG Suite. Or use JDK 21 / Maven:
```
mvn test
mvn -Dtest=ConfigurationTest test
```
The second command needs no Grid, browser, or Docker and validates config/fixture handling. Maven targets Java 17+, compatible with JDK 21. The suite itself uses TestNG, not Cucumber or JUnit. Default listeners generate HTML reports; reports are excluded from Git.

## Troubleshooting
Stop the previous SeleniumTestNGDocker stack before starting this project: ports 4444/7900 may conflict. For DNS/image-pull errors check Docker Hub connectivity; changing tests will not fix network errors. If Grid readiness fails, inspect `docker compose logs selenium-hub chrome firefox`. For test failures inspect reports/testng and screenshots. Do not disable assertions to make results pass. The practice page is a client-side demo with public synthetic credentials, not real authentication security.
