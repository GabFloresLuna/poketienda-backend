package cl.poketienda.ms_usuarios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunValidoValidator implements ConstraintValidator<RunValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        // Si viene vacío no se opina aquí: de eso se encarga @NotBlank.
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return ValidadorRun.normalizar(valor) != null;
    }
}
