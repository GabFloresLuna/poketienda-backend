package cl.poketienda.ms_productos.security;

// Quién hace la petición según su token. Es el "principal" de Spring Security.
// id es nulo cuando el token es de otro microservicio (rol SERVICIO).
public record UsuarioToken(Long id, String correo, String rol) {
}
