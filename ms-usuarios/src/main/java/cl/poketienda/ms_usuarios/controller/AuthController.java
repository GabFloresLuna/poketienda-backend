package cl.poketienda.ms_usuarios.controller;

import cl.poketienda.ms_usuarios.dto.LoginRequest;
import cl.poketienda.ms_usuarios.dto.RefreshRequest;
import cl.poketienda.ms_usuarios.dto.RegistroRequest;
import cl.poketienda.ms_usuarios.dto.TokenResponse;
import cl.poketienda.ms_usuarios.dto.UsuarioResponse;
import cl.poketienda.ms_usuarios.security.UsuarioToken;
import cl.poketienda.ms_usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio de sesión, registro y renovación de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión: devuelve access token y refresh token (público)")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una cuenta de cliente (público)")
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registro(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el access token con un refresh token (público)")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Datos del usuario dueño del token (con sesión)")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioToken actual) {
        return authService.me(actual.id());
    }
}
