package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para registrar un nuevo usuario en el sistema")
public class UsuarioRegistroRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 100, message = "El correo electrónico no debe exceder 100 caracteres")
    @Schema(description = "Correo electrónico institucional o personal del usuario", example = "usuario@empresa.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#.+_-])[A-Za-z\\d@$!%*?&#.+_-]{8,64}$",
        message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial (@$!%*?&#.+_-)"
    )
    @Schema(description = "Contraseña en texto plano para cifrado seguro en servidor", example = "Seguro2026!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "^(ROLE_ADMIN|ROLE_CLIENTE|ROLE_OPERADOR)$",
        message = "El rol debe ser ROLE_ADMIN, ROLE_CLIENTE o ROLE_OPERADOR"
    )
    @Schema(description = "Rol de autorización en el sistema", allowableValues = {"ROLE_ADMIN", "ROLE_CLIENTE", "ROLE_OPERADOR"}, example = "ROLE_CLIENTE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rol;

    @NotNull(message = "El ID del cliente asociado es obligatorio")
    @Schema(description = "Identificador del cliente físico al cual se vincula la cuenta de acceso", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clienteId;

    @Pattern(regexp = "^\\d{4,6}$", message = "El PIN transaccional debe contener entre 4 y 6 dígitos numéricos")
    @Schema(description = "PIN transaccional opcional de 4 a 6 dígitos", example = "4829")
    private String pin;
}
