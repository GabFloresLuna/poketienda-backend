package cl.poketienda.ms_ventas.security;

// Quién hace la petición según su token. Es el "principal" de Spring Security.
public record UsuarioToken(Long id, String correo, String rol) {
}
