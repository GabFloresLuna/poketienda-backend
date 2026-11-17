package cl.poketienda.ms_usuarios.exception;

// Se responde 401: credenciales incorrectas o refresh token inválido.
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
