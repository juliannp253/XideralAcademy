package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.SantanderHomePage;

public class PersonasMenuTest extends BaseTest {

    public record MenuTestData(By subOption, String expectedUrlFragment) {}

    // CRÉDITO Y FINANCIAMIENTO
    @DataProvider(name = "creditoFinanciamientoData")
    public Object[][] getCreditoData() {
        return new Object[][] {
                { new MenuTestData(SantanderHomePage.Personas.LINK_TARJETAS_CREDITO, "tarjetas-de-credito") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_CREDITO_PERSONAL, "creditos-personales") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_CREDITO_HIPOTECARIO, "creditos-hipotecarios") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_SIM_HIPO, "simulador-hipotecario") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_CREDITO_AUTO, "credito-automotriz") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_BURO_CREDITO, "buro-de-credito") }
        };
    }

    @Test(dataProvider = "creditoFinanciamientoData", priority = 1)
    public void testCreditoYFinanciamiento(MenuTestData testData) {
        validateNavigation(testData);
    }

    // CANALES DIGITALES
    @DataProvider(name = "canalesDigitalesData")
    public Object[][] getCanalesData() {
        return new Object[][] {
                { new MenuTestData(SantanderHomePage.Personas.LINK_SANT_DIG, "santander-digital") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_APP_SANT, "app-santander") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_SANT_WEB, "santander-web") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_LIM_TRANS, "limite-por-transaccion") }
        };
    }

    @Test(dataProvider = "canalesDigitalesData", priority = 2)
    public void testCanalesDigitales(MenuTestData testData) {
        validateNavigation(testData);
    }

    // TIPOS DE CUENTA
    @DataProvider(name = "tiposCuentaData")
    public Object[][] getTiposCuentaData() {
        return new Object[][] {
                { new MenuTestData(SantanderHomePage.Personas.LINK_CUENTAS, "cuentas") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_BASICA, "basica") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_NOMIA, "basica-nomina") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_CHEQUES, "cheque-saldo-promedio") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_PORT_NOM, "portabilidad-de-nomina") },
        };
    }

    @Test(dataProvider = "tiposCuentaData", priority = 3)
    public void testTiposCuentas(MenuTestData testData) {
        validateNavigation(testData);
    }

    // AHORRO E INVERSION
    @DataProvider(name = "ahorroData")
    public Object[][] getAhorroData() {
        return new Object[][] {
                { new MenuTestData(SantanderHomePage.Personas.LINK_FONDOS_INVER, "fondos-de-inversion") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_INV_PLAZO, "inversiones-a-plazo") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_NOTAS_ESTRUCTURADAS, "notas-estructuradas") },
        };
    }

    @Test(dataProvider = "ahorroData", priority = 4)
    public void testAhorroInversion(MenuTestData testData) {
        validateNavigation(testData);
    }

    // SEGUROS
    @DataProvider(name = "segurosData")
    public Object[][] getSegurosData() {
        return new Object[][] {
                { new MenuTestData(SantanderHomePage.Personas.LINK_AUTO, "auto") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_VIDA, "vida") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_HOGAR, "hogar") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_AHORRO, "ahorro") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_GSTOS_MED, "gastos-medicos") },
                { new MenuTestData(SantanderHomePage.Personas.LINK_PERTENENCIAS, "pertenencias") }
        };
    }

    @Test(dataProvider = "segurosData", priority = 5)
    public void testSeguros(MenuTestData testData) {
        validateNavigation(testData);
    }

    private void validateNavigation(MenuTestData testData) {
        SantanderHomePage homePage = new SantanderHomePage(driver);
        homePage.selectMenuOption(SantanderHomePage.MENU_PERSONAS, testData.subOption());

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(testData.expectedUrlFragment()),
                "Redirección fallida. URL actual: " + currentUrl);

        driver.get("https://www.santander.com.mx/");
    }
}