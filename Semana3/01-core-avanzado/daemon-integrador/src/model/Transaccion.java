package model;

public record Transaccion(
        String idTransaccion,
        String cuentaOrigen,
        double monto,
        String estado
) {}