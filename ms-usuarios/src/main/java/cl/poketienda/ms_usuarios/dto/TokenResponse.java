package cl.poketienda.ms_usuarios.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        long expiraEnSegundos,
        UsuarioResponse usuario
) {

    public static TokenResponse de(String accessToken, String refreshToken,
                                   long expiraEnSegundos, UsuarioResponse usuario) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiraEnSegundos, usuario);
    }
}
