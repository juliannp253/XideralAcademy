package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.SantanderHomePage;

public class PyMesMenuTest extends BaseTest {
    public record MenuTestData(By subOption, String expectedUrlFragment) {}

    @DataProvider(name = "pymesData")
    public Object[][] getPyMesData() {
        return new Object[][] {
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_SANTANDER_PYME, "pyme") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_DIVISAS_COBERTURAS, "coberturas-y-cambios") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_CUENTAS_PYME, "cuentas") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_NEGOCIO_INTERNACIONA, "negocio-internacional") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_PAQUETES_PYMES, "paquetes-pyme") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_CREDITOS, "pyme/creditos.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_SEGUROS_PYMES, "pyme/seguros.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_ALIANZAS, "pyme/alianzas.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_NEGOCIO_TRANSACCIONAL, "pyme/negocio-transaccional.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_ECOSISTEMA_NO_FIN, "pyme/ecosistemas-pyme.html") },
                { new EmpresasMenuTest.MenuTestData(SantanderHomePage.PyMes.LINK_INVERSIONES, "pyme/inversiones.html") }
        };
    }

    @Test(dataProvider = "pymesData", priority = 1)
    public void testNavegacionMenuPyMes(EmpresasMenuTest.MenuTestData testData) {
        SantanderHomePage homePage = new SantanderHomePage(driver);

        homePage.selectMenuOption(SantanderHomePage.MENU_PYMES, testData.subOption());

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(testData.expectedUrlFragment()),
                "Redirección fallida en Empresas. URL actual: " + currentUrl);

        driver.get("https://www.santander.com.mx/");
    }
}
