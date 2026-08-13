package com.logistic.fast_track.core.model;

public class Cliente {
    private int id;
    private String nombre;
    private String email;

    public Cliente(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}
