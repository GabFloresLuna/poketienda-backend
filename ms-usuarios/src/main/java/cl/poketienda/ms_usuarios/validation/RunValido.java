package cl.poketienda.ms_usuarios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Anotación propia: comprueba el formato y el dígito verificador del RUN.
@Documented
@Constraint(validatedBy = RunValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunValido {

    String message() default "El RUN no es válido (revisa el dígito verificador)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
