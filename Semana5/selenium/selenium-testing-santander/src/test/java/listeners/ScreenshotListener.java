package listeners;

import base.BaseTest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

public class ScreenshotListener implements ITestListener {
    private static final Path SCREENSHOT_FOLDER = Path.of("test-output", "screenshots");

    @Override
    public void onTestFailure(ITestResult result) {
        if (!(result.getInstance() instanceof BaseTest test)) {
            return;
        }
        WebDriver driver = test.getDriver();
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }

        try {
            Files.createDirectories(SCREENSHOT_FOLDER);
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String fileName = result.getMethod().getMethodName() + "-" + timestamp + ".png";
            Path destination = SCREENSHOT_FOLDER.resolve(fileName);
            File source = camera.getScreenshotAs(OutputType.FILE);
            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            Reporter.log("Captura: " + destination.toAbsolutePath(), true);
            Reporter.log("<a href='screenshots/" + fileName
                    + "' target='_blank'>Ver captura del error</a>");
        } catch (IOException error) {
            Reporter.log("No fue posible guardar la captura: " + error.getMessage(), true);
        }
    }
}
