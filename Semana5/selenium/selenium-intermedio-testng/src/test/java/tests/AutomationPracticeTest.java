package tests;

import base.BaseTest;
import java.nio.file.Path;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.AutomationPracticePage;

public class AutomationPracticeTest extends BaseTest {

    @Test(priority = 1)
    public void shouldCompleteFormAndControls() {
        AutomationPracticePage practicePage = new AutomationPracticePage(driver);
        Path file = Path.of("src", "test", "resources", "files", "sample-upload.txt");

        practicePage.fillPersonalData();
        Assert.assertTrue(practicePage.areMainControlsSelected());
        practicePage.uploadFile(file);
    }

    @Test(priority = 2)
    public void shouldInteractWithProductTable() {
        AutomationPracticePage practicePage = new AutomationPracticePage(driver);

        Assert.assertTrue(practicePage.getProductCount() > 0);
        practicePage.selectFirstProduct();
    }

    @Test(priority = 3)
    public void shouldHandleAlerts() {
        AutomationPracticePage practicePage = new AutomationPracticePage(driver);

        Assert.assertTrue(practicePage.acceptSimpleAlert().toLowerCase().contains("alert"));
        Assert.assertTrue(practicePage.dismissConfirmationAlert()
                .toLowerCase().contains("cancel"));
        Assert.assertTrue(practicePage.answerPromptAlert("Selenium").contains("Selenium"));
    }

    @Test(priority = 4)
    public void shouldPerformAdvancedActions() {
        AutomationPracticePage practicePage = new AutomationPracticePage(driver);

        Assert.assertTrue(practicePage.showHoverMenu());
        Assert.assertEquals(practicePage.copyTextWithDoubleClick("Curso Selenium"),
                "Curso Selenium");
        Assert.assertTrue(practicePage.dragAndDropElement().toLowerCase().contains("dropped"));
        Assert.assertFalse(practicePage.openAndCloseNewTab().isBlank());

        if (Boolean.parseBoolean(System.getProperty("forceFailure", "false"))) {
            Assert.fail("Falla intencional para demostrar la captura automática");
        }
    }
}
