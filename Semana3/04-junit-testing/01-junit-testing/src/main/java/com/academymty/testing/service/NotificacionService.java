package com.academymty.testing.service;

// Servicio de notificaciones y correos electrónicos externos.
public interface NotificacionService {
    boolean enviarCorreoBienvenida(String email, String mensaje);
    void enviarAlertaAdministrador(String asunto, String detalle);
}
