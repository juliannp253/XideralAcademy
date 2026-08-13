package com.logistic.fast_track.core.model;

public class Direccion {
    private String calle;
    private String ciudad;
    private String codigoPostal;

    public Direccion(String calle, String ciudad, String codigoPostal) {
        this.calle = calle;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
    }

    public String getCalle() { return calle; }
    public String getCiudad() { return ciudad; }
    public String getCodigoPostal() { return codigoPostal; }
}
