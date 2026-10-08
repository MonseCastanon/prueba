package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstadoCivil;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ClienteRegistroRequest {

    // --- Datos Personales ---
    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$", message = "El nombre solo debe contener letras sin caracteres especiales ni espacios dobles")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(example = "Juan Carlos")
    private String nombre;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$", message = "El segundo nombre solo debe contener letras")
    @Size(max = 50, message = "El segundo nombre no puede exceder 50 caracteres")
    @Schema(example = "Manuel")
    private String segundoNombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$", message = "El apellido paterno solo debe contener letras")
    @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
    @Schema(example = "Perez")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+( [a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$", message = "El apellido materno solo debe contener letras")
    @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
    @Schema(example = "Lopez")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Schema(example = "1995-08-15")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La CURP es obligatoria")
    @Pattern(regexp = "^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[A-Z0-9]{2}$", message = "Formato de CURP oficial inválido (18 caracteres alfanuméricos en mayúsculas)")
    @Schema(example = "PELJ950815HDFRPR01")
    private String curp;

    @NotBlank(message = "El RFC es obligatorio")
    @Pattern(regexp = "^[A-ZÑ&]{4}[0-9]{6}[A-Z0-9]{3}$", message = "Formato de RFC de persona física inválido (13 caracteres)")
    @Schema(example = "PELJ950815XXX")
    private String rfc;

    @NotBlank(message = "El sexo es obligatorio")
    @Pattern(regexp = "^[MFX]$", message = "El sexo debe ser 'M', 'F' o 'X'")
    @Schema(example = "M")
    private String sexo;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Pattern(regexp = "^[A-Z]{3}$", message = "La nacionalidad debe ser el código ISO 3166-1 alfa-3 (ej. MEX)")
    @Schema(example = "MEX")
    private String nacionalidad;

    @NotNull(message = "El estado civil es obligatorio (SOLTERO, CASADO, DIVORCIADO, VIUDO, UNION_LIBRE)")
    @Schema(example = "SOLTERO")
    private EstadoCivil estadoCivil;

    // --- Contacto ---
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Formato de correo electrónico inválido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    @Schema(example = "juan.perez@dominio.com")
    private String correo;

    @NotBlank(message = "El teléfono móvil es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono móvil debe contener exactamente 10 dígitos numéricos")
    @Schema(example = "5512345678")
    private String telefonoMovil;

    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono alternativo debe contener exactamente 10 dígitos numéricos")
    @Schema(example = "5587654321")
    private String telefonoAlternativo;

    // --- Laboral ---
    @NotBlank(message = "La ocupación es obligatoria")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 .,-]+$", message = "La ocupación contiene caracteres no permitidos")
    @Size(max = 100, message = "La ocupación no puede exceder 100 caracteres")
    @Schema(example = "Ingeniero de Software")
    private String ocupacion;

    @NotBlank(message = "La empresa es obligatoria")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 .,-]+$", message = "La empresa contiene caracteres no permitidos")
    @Size(max = 100, message = "La empresa no puede exceder 100 caracteres")
    @Schema(example = "Tech Solutions SA")
    private String empresa;

    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El ingreso mensual debe ser mayor a cero")
    @Schema(example = "45000.00")
    private BigDecimal ingresoMensual;

    // --- Domicilio ---
    @NotNull(message = "Los datos del domicilio son obligatorios")
    @Valid
    private DomicilioRequest domicilio;

    // --- Credenciales de Acceso Inicial ---
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,64}$",
        message = "La contraseña debe tener entre 8 y 64 caracteres, incluir al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    @Schema(example = "SeguroPass2026*")
    private String password;

    // --- Datos Biométricos Opcionales ---
    @Schema(description = "Fotografía selfie en Base64")
    private String fotoRostroBase64;

    @Schema(description = "Template dactilar en Base64")
    private String templateDactilarBase64;
}
