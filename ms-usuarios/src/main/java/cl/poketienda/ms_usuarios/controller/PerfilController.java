package cl.poketienda.ms_usuarios.controller;

import cl.poketienda.ms_usuarios.dto.PerfilRequest;
import cl.poketienda.ms_usuarios.dto.UsuarioResponse;
import cl.poketienda.ms_usuarios.security.UsuarioToken;
import cl.poketienda.ms_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil", description = "Datos del usuario con sesión iniciada")
public class PerfilController {

    private final UsuarioService usuarioService;

    // El id sale del token, no de la URL: nadie puede ver ni editar el perfil de otro.
    @GetMapping
    @Operation(summary = "Ver mis datos (con sesión)")
    public UsuarioResponse ver(@AuthenticationPrincipal UsuarioToken actual) {
        return usuarioService.obtener(actual.id());
    }

    @PutMapping
    @Operation(summary = "Editar mis datos (con sesión)")
    public UsuarioResponse editar(@AuthenticationPrincipal UsuarioToken actual,
                                  @Valid @RequestBody PerfilRequest request) {
        return usuarioService.actualizarPerfil(actual.id(), request);
    }
}
