package cl.poketienda.ms_ventas.client;

import cl.poketienda.ms_ventas.dto.ErrorResponse;
import cl.poketienda.ms_ventas.dto.ProductoExternoResponse;
import cl.poketienda.ms_ventas.dto.StockRequest;
import cl.poketienda.ms_ventas.exception.RecursoNoEncontradoException;
import cl.poketienda.ms_ventas.exception.ReglaNegocioException;
import cl.poketienda.ms_ventas.exception.ServicioNoDisponibleException;
import cl.poketienda.ms_ventas.security.JwtService;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// Único lugar que conoce cómo hablar con ms-productos
@Component
public class ProductosCliente {

    private static final String MENSAJE_NO_DISPONIBLE = "Servicio de productos no disponible";

    private final RestClient restClient;
    private final JwtService jwtService;

    public ProductosCliente(@Value("${ms-productos.url}") String urlBase, JwtService jwtService) {
        this.jwtService = jwtService;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .build();
    }

    public ProductoExternoResponse obtenerProducto(Long id) {
        try {
            ProductoExternoResponse producto = restClient.get()
                    .uri("/productos/{id}", id)
                    .retrieve()
                    .body(ProductoExternoResponse.class);
            if (producto == null) {
                throw new RecursoNoEncontradoException("No existe el producto con id " + id);
            }
            return producto;
        } catch (RestClientResponseException ex) {
            throw traducir(ex);
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException(MENSAJE_NO_DISPONIBLE);
        }
    }

    public void descontarStock(List<StockRequest.Item> items) {
        enviarStock("/productos/stock/descontar", items);
    }

    public void reponerStock(List<StockRequest.Item> items) {
        enviarStock("/productos/stock/reponer", items);
    }

    private void enviarStock(String ruta, List<StockRequest.Item> items) {
        try {
            restClient.post()
                    .uri(ruta)
                    // Los endpoints de stock son internos: ms-productos exige el token de servicio.
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generarTokenServicio())
                    .body(new StockRequest(items))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw traducir(ex);
        } catch (ResourceAccessException ex) {
            throw new ServicioNoDisponibleException(MENSAJE_NO_DISPONIBLE);
        }
    }

    // Convierte la respuesta de error de ms-productos en una excepción propia de ms-ventas
    private RuntimeException traducir(RestClientResponseException ex) {
        int estado = ex.getStatusCode().value();
        String mensaje = mensajeDe(ex);

        if (estado == 404) {
            return new RecursoNoEncontradoException(mensaje);
        }
        if (estado == 409) {
            return new ReglaNegocioException(mensaje);
        }
        if (estado >= 500) {
            return new ServicioNoDisponibleException(MENSAJE_NO_DISPONIBLE);
        }
        // Otros 4xx (por ejemplo 400): ms-ventas envió algo mal; es un error interno
        return new IllegalStateException("Respuesta inesperada de ms-productos: " + estado);
    }

    private String mensajeDe(RestClientResponseException ex) {
        try {
            ErrorResponse error = ex.getResponseBodyAs(ErrorResponse.class);
            if (error != null && error.mensaje() != null) {
                return error.mensaje();
            }
        } catch (RuntimeException ignorada) {
            // si el cuerpo no se puede leer, se usa el mensaje por defecto
        }
        return "Error al consultar ms-productos";
    }
}
