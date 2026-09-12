package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SantanderHomePage extends BasePage {

    public static final By MENU_PERSONAS = By.id("firstLevel-mainItem-0-menu-button");
    public static final By MENU_EMPRESAS = By.id("firstLevel-mainItem-1-menu-button");
    public static final By MENU_PYMES = By.id("firstLevel-mainItem-2-menu-button");
    public static final By MENU_BANCA_PRIVADA = By.linkText("Banca Privada");
    public static final By MENU_ACERCA_BANCO = By.id("firstLevel-mainItem-4-menu-button");

    public static class Personas {
        public static final By LINK_TARJETAS_CREDITO = By.linkText("Tarjetas de crédito");
        public static final By LINK_CREDITO_PERSONAL = By.linkText("Crédito personal");
        public static final By LINK_CREDITO_HIPOTECARIO = By.linkText("Crédito hipotecario");
        public static final By LINK_SIM_HIPO = By.linkText("Simulador de hipoteca");
        public static final By LINK_CREDITO_AUTO = By.linkText("Crédito automotríz");
        public static final By LINK_BURO_CREDITO = By.linkText("Buró de crédito");
        // Canales digitales
        public static final By LINK_SANT_DIG = By.linkText("Santander digital");
        public static final By LINK_APP_SANT = By.linkText("App Santander");
        public static final By LINK_SANT_WEB = By.linkText("Santander Web");
        public static final By LINK_LIM_TRANS = By.linkText("Límite por transacción");
        // Tipo de cuenta
        public static final By LINK_CUENTAS = By.linkText("Cuentas");
        public static final By LINK_BASICA = By.linkText("Básica");
        public static final By LINK_NOMIA = By.linkText("Nómina");
        public static final By LINK_CHEQUES = By.linkText("Cheques");
        public static final By LINK_PORT_NOM = By.linkText("Portabilidad de nómina");
        // Ahorro e inversión
        public static final By LINK_FONDOS_INVER = By.linkText("Fondos de inversión");
        public static final By LINK_INV_PLAZO = By.linkText("Inversiones a plazo");
        public static final By LINK_NOTAS_ESTRUCTURADAS = By.linkText("Notas estructuradas");
        // Seguros
        public static final By LINK_AUTO = By.linkText("Auto");
        public static final By LINK_VIDA = By.linkText("Vida");
        public static final By LINK_HOGAR = By.linkText("Hogar");
        public static final By LINK_AHORRO = By.linkText("Ahorro");
        public static final By LINK_GSTOS_MED = By.linkText("Gastos médicos");
        public static final By LINK_PERTENENCIAS = By.linkText("Pertenencias");
    }

    public static class Empresas {
        public static final By LINK_EMPRESAS_GOBIERNO = By.linkText("Empresas y gobierno");
        public static final By LINK_MULTINACIONALES = By.linkText("Multinacionales");
    }

    public static class PyMes {
        public static final By LINK_SANTANDER_PYME = By.linkText("Santander Pyme");
        public static final By LINK_DIVISAS_COBERTURAS = By.linkText("Divisas y coberturas");
        public static final By LINK_CUENTAS_PYME = By.linkText("Cuentas");
        public static final By LINK_NEGOCIO_INTERNACIONA = By.linkText("Negocio internacional");
        public static final By LINK_PAQUETES_PYMES = By.linkText("Paquetes Pymes");
        public static final By LINK_CREDITOS = By.linkText("Créditos");
        public static final By LINK_SEGUROS_PYMES = By.linkText("Seguros");
        public static final By LINK_ALIANZAS = By.linkText("Alianzas");
        public static final By LINK_NEGOCIO_TRANSACCIONAL = By.linkText("Negocio transaccional");
        public static final By LINK_ECOSISTEMA_NO_FIN = By.linkText("Ecosistema no financiero");
        public static final By LINK_INVERSIONES = By.linkText("Inversiones");
    }
    public static class AcercaBanco {
        public static final By LINK_FUNDACION_SANTANDER = By.linkText("Fundación Santander");
        public static final By LINK_BLOG = By.linkText("Blog");
        public static final By LINK_SOSTENIBILIDAD = By.linkText("Sostenibilidad");
        public static final By LINK_EDUCACION_FINANCIERA = By.linkText("Educación Financiera");
        public static final By LINK_INVERSIONISTAS = By.linkText("Inversionistas");
        public static final By LINK_SALA_COMUNICACION = By.linkText("Sala de comunicación");
        public static final By LINK_BOLSA_TRABAJO = By.linkText("Bolsa de trabajo");
    }

    public SantanderHomePage(WebDriver driver) {
        super(driver);
    }

    public void selectMenuOption(By categoryLocator, By subOptionLocator) {
        click(categoryLocator);
        click(subOptionLocator);
    }

    public void clickBancaPrivada() {
        click(MENU_BANCA_PRIVADA);
    }
}