package cl.poketienda.ms_usuarios.security;

import cl.poketienda.ms_usuarios.model.Rol;

// Los datos del usuario que viajan dentro del token. Es el "principal" de Spring Security.
public record UsuarioToken(Long id, String correo, Rol rol) {
}
