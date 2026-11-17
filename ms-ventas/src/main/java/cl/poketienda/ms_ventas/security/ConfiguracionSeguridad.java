package cl.poketienda.ms_ventas.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http, FiltroJwt filtroJwt) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Compra pública: con o sin cuenta.
                        .requestMatchers(HttpMethod.POST, "/api/v1/boletas").permitAll()
                        // El orden importa: "mias" debe ir antes de "{id}", si no "mias" se leería como un id.
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas/mias").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas", "/api/v1/boletas/{id}")
                                .hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/boletas/{id}/estado").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas/usuario/{usuarioId}").hasRole("ADMIN")
                        .requestMatchers("/api/v1/reportes/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((peticion, respuesta, ex) ->
                                escribirError(respuesta, 401, "Debes iniciar sesión para acceder a este recurso"))
                        .accessDeniedHandler((peticion, respuesta, ex) ->
                                escribirError(respuesta, 403, "No tienes permiso para realizar esta acción")))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cors);
        return fuente;
    }

    // Los errores 401 y 403 nacen en el filtro, antes de llegar al manejador global,
    // así que se escriben aquí con el mismo formato JSON de ErrorResponse.
    private void escribirError(HttpServletResponse respuesta, int codigo, String mensaje) throws IOException {
        respuesta.setStatus(codigo);
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write(
                "{\"codigo\":" + codigo + ",\"mensaje\":\"" + mensaje + "\",\"campos\":null}");
    }
}
