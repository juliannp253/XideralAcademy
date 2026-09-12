package pages;

import base.BasePage;
import java.nio.file.Path;
import java.util.Set;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class AutomationPracticePage extends BasePage {
    private static final By NAME = By.id("name");
    private static final By EMAIL = By.id("email");
    private static final By PHONE = By.id("phone");
    private static final By ADDRESS = By.id("textarea");
    private static final By MALE = By.id("male");
    private static final By SUNDAY = By.id("sunday");
    private static final By WEDNESDAY = By.id("wednesday");
    private static final By COUNTRY = By.id("country");
    private static final By COLORS = By.id("colors");
    private static final By ANIMALS = By.id("animals");
    private static final By START_DATE = By.id("start-date");
    private static final By END_DATE = By.id("end-date");
    private static final By FILE_INPUT = By.id("singleFileInput");
    private static final By UPLOAD_BUTTON =
            By.cssSelector("#singleFileForm button[type='submit']");
    private static final By PRODUCT_TABLE = By.id("productTable");
    private static final By FIRST_PRODUCT =
            By.cssSelector("#productTable tbody tr:first-child input[type='checkbox']");
    private static final By ALERT_BUTTON = By.id("alertBtn");
    private static final By CONFIRM_BUTTON = By.id("confirmBtn");
    private static final By PROMPT_BUTTON = By.id("promptBtn");
    private static final By ALERT_RESULT = By.id("demo");
    private static final By HOVER_BUTTON = By.cssSelector("button.dropbtn");
    private static final By LAPTOPS_OPTION = By.xpath(
            "//div[contains(@class,'dropdown-content')]//a[normalize-space()='Laptops']");
    private static final By FIELD_ONE = By.id("field1");
    private static final By FIELD_TWO = By.id("field2");
    private static final By COPY_BUTTON =
            By.xpath("//button[normalize-space()='Copy Text']");
    private static final By DRAGGABLE = By.id("draggable");
    private static final By DROPPABLE = By.id("droppable");
    private static final By NEW_TAB_BUTTON =
            By.xpath("//button[normalize-space()='New Tab']");

    public AutomationPracticePage(WebDriver driver) {
        super(driver);
    }

    public AutomationPracticePage fillPersonalData() {
        write(NAME, "Alumno Selenium");
        write(EMAIL, "alumno@example.com");
        write(PHONE, "5551234567");
        write(ADDRESS, "Ciudad de México");
        click(MALE);
        click(SUNDAY);
        click(WEDNESDAY);
        selectByText(COUNTRY, "Australia");

        Select colorList = new Select(find(COLORS));
        colorList.selectByVisibleText("Blue");
        colorList.selectByVisibleText("Green");
        selectByText(ANIMALS, "Lion");
        write(START_DATE, "09/01/2026");
        write(END_DATE, "09/07/2026");
        return this;
    }

    public boolean areMainControlsSelected() {
        return isSelected(MALE) && isSelected(SUNDAY) && isSelected(WEDNESDAY);
    }

    public void uploadFile(Path file) {
        find(FILE_INPUT).sendKeys(file.toAbsolutePath().toString());
        click(UPLOAD_BUTTON);
    }

    public int getProductCount() {
        return find(PRODUCT_TABLE).findElements(By.cssSelector("tbody tr")).size();
    }

    public void selectFirstProduct() {
        click(FIRST_PRODUCT);
    }

    public String acceptSimpleAlert() {
        click(ALERT_BUTTON);
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String message = alert.getText();
        alert.accept();
        return message;
    }

    public String dismissConfirmationAlert() {
        click(CONFIRM_BUTTON);
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
        return text(ALERT_RESULT);
    }

    public String answerPromptAlert(String answer) {
        click(PROMPT_BUTTON);
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.sendKeys(answer);
        alert.accept();
        return text(ALERT_RESULT);
    }

    public boolean showHoverMenu() {
        actions.moveToElement(find(HOVER_BUTTON)).perform();
        return find(LAPTOPS_OPTION).isDisplayed();
    }

    public String copyTextWithDoubleClick(String value) {
        write(FIELD_ONE, value);
        actions.doubleClick(find(COPY_BUTTON)).perform();
        return find(FIELD_TWO).getAttribute("value");
    }

    public String dragAndDropElement() {
        WebElement source = find(DRAGGABLE);
        WebElement target = find(DROPPABLE);
        actions.dragAndDrop(source, target).perform();
        return target.getText();
    }

    public String openAndCloseNewTab() {
        String originalWindow = driver.getWindowHandle();
        Set<String> previousWindows = driver.getWindowHandles();
        click(NEW_TAB_BUTTON);
        wait.until(d -> d.getWindowHandles().size() > previousWindows.size());

        String newWindow = driver.getWindowHandles().stream()
                .filter(handle -> !previousWindows.contains(handle))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newWindow);
        String title = driver.getTitle();
        driver.close();
        driver.switchTo().window(originalWindow);
        return title;
    }
}
