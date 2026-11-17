package cl.poketienda.ms_usuarios.validation;

public final class ValidadorRun {

    private ValidadorRun() {
    }

    /**
     * Devuelve el RUN en formato "12345678-5" (sin puntos, con guion, K en mayúscula),
     * o null si no es válido (formato incorrecto o dígito verificador que no calza).
     */
    public static String normalizar(String run) {
        if (run == null) {
            return null;
        }
        String limpio = run.replace(".", "").replace(" ", "").toUpperCase();
        if (!limpio.matches("\\d{7,8}-?[0-9K]")) {
            return null;
        }
        limpio = limpio.replace("-", "");
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        String digito = limpio.substring(limpio.length() - 1);
        if (!calcularDigito(cuerpo).equals(digito)) {
            return null;
        }
        return cuerpo + "-" + digito;
    }

    // Módulo 11: se multiplica de derecha a izquierda por 2,3,4,5,6,7,2,3...
    private static String calcularDigito(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return "0";
        }
        if (resto == 10) {
            return "K";
        }
        return String.valueOf(resto);
    }
}
