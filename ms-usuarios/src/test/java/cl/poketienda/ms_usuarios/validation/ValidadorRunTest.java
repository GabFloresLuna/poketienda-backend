package cl.poketienda.ms_usuarios.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidadorRunTest {

    @Test
    void normalizaUnRunValidoAlFormatoConGuion() {
        assertThat(ValidadorRun.normalizar("11.111.111-1")).isEqualTo("11111111-1");
        assertThat(ValidadorRun.normalizar("123456785")).isEqualTo("12345678-5");
    }

    @Test
    void rechazaUnRunConDigitoVerificadorIncorrecto() {
        assertThat(ValidadorRun.normalizar("12345678-9")).isNull();
    }

    @Test
    void rechazaTextoQueNoEsUnRun() {
        assertThat(ValidadorRun.normalizar("abc")).isNull();
        assertThat(ValidadorRun.normalizar(null)).isNull();
    }
}
