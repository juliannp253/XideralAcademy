package com.academymty.testing.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Pattern;


public class ValidadorCurp {

    private static final Pattern PATRON_ESTRUCTURA = Pattern.compile(
            "^[A-Z][AEIOUX][A-Z]{2}[0-9]{6}[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9][0-9]$"
    );

    private static final Set<String> ENTIDADES_FEDERATIVAS = Set.of(
            "AS", "BC", "BS", "CC", "CL", "CM", "CS", "CH", "DF", "DG",
            "GT", "GR", "HG", "JC", "MC", "MN", "MS", "NT", "NL", "OC",
            "PL", "QT", "QR", "SP", "SL", "SR", "TC", "TS", "TL", "VZ",
            "YN", "ZS", "NE"
    );

    public boolean esValida(String curp) {
        if (curp == null) {
            return false;
        }

        String limpia = curp.trim().toUpperCase();
        if (limpia.length() != 18) {
            return false;
        }

        if (!PATRON_ESTRUCTURA.matcher(limpia).matches()) {
            return false;
        }

        String entidad = limpia.substring(11, 13);
        if (!ENTIDADES_FEDERATIVAS.contains(entidad)) {
            return false;
        }

        return esFechaValida(limpia);
    }

    private boolean esFechaValida(String curp) {
        try {
            int anioCorto = Integer.parseInt(curp.substring(4, 6));
            int mes = Integer.parseInt(curp.substring(6, 8));
            int dia = Integer.parseInt(curp.substring(8, 10));

            char homoclaveSiglo = curp.charAt(16);
            int anioCompleto = Character.isDigit(homoclaveSiglo) ? (1900 + anioCorto) : (2000 + anioCorto);

            LocalDate.of(anioCompleto, mes, dia);
            return true;
        } catch (DateTimeException | NumberFormatException e) {
            return false;
        }
    }

    public String obtenerGenero(String curp) {
        if (!esValida(curp)) {
            throw new IllegalArgumentException("La CURP no es válida: " + curp);
        }
        return curp.charAt(10) == 'H' ? "HOMBRE" : "MUJER";
    }
}
