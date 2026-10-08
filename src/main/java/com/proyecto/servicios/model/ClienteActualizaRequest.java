package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstadoCivil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteActualizaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(min = 2, max = 50)
    private String nombre;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(max = 50)
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(min = 2, max = 50)
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$")
    @Size(min = 2, max = 50)
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past
    private LocalDate fechaNacimiento;

    @NotBlank
    @Pattern(regexp = "^[MFX]$")
    private String sexo;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    private String nacionalidad;

    @NotNull
    private EstadoCivil estadoCivil;

    @NotBlank
    @Email
    @Size(max = 100)
    private String correo;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    private String telefonoMovil;

    @Pattern(regexp = "^[0-9]{10}$")
    private String telefonoAlternativo;

    @NotBlank
    @Size(max = 100)
    private String ocupacion;

    @NotBlank
    @Size(max = 100)
    private String empresa;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal ingresoMensual;

    @NotNull
    @Valid
    private DomicilioRequest domicilio;
}
