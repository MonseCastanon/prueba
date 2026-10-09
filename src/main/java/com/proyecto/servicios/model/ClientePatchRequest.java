package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstadoCivil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientePatchRequest {

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(min = 2, max = 50)
    private String nombre;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(max = 50)
    private String segundoNombre;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(min = 2, max = 50)
    private String apellidoPaterno;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(min = 2, max = 50)
    private String apellidoMaterno;

    private EstadoCivil estadoCivil;

    @Email(message = "Formato de correo no válido")
    @Size(max = 100)
    private String correo;

    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener 10 dígitos")
    private String telefonoMovil;

    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono alternativo debe contener 10 dígitos")
    private String telefonoAlternativo;

    @Size(max = 100)
    private String ocupacion;

    @Size(max = 100)
    private String empresa;

    @DecimalMin(value = "0.01", message = "El ingreso debe ser mayor a cero")
    private BigDecimal ingresoMensual;

    @Valid
    private DomicilioRequest domicilio;

    // Regla de Negocio: CURP y RFC son inmutables
    @Schema(hidden = true)
    private String curp;

    @Schema(hidden = true)
    private String rfc;
}
