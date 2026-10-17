package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para actualización controlada de datos de un usuario existente")
public class UsuarioActualizaRequest {

    @NotNull(message = "El ID del usuario a modificar es obligatorio")
    @Schema(description = "Identificador único del usuario a actualizar", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 100, message = "El correo electrónico no debe exceder 100 caracteres")
    @Schema(description = "Nuevo correo electrónico del usuario (opcional)", example = "nuevo_correo@empresa.com")
    private String correo;

    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#.+_-])[A-Za-z\\d@$!%*?&#.+_-]{8,64}$",
        message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial (@$!%*?&#.+_-)"
    )
    @Schema(description = "Nueva contraseña en texto plano para actualización y cifrado seguro (opcional)", example = "NuevoPassword2026!")
    private String password;

    @Pattern(
        regexp = "^(ROLE_ADMIN|ROLE_CLIENTE|ROLE_OPERADOR)$",
        message = "El rol debe ser ROLE_ADMIN, ROLE_CLIENTE o ROLE_OPERADOR"
    )
    @Schema(description = "Nuevo rol del usuario (opcional)", allowableValues = {"ROLE_ADMIN", "ROLE_CLIENTE", "ROLE_OPERADOR"}, example = "ROLE_OPERADOR")
    private String rol;

    @Schema(description = "Estatus de habilitación o inhabilitación del usuario (opcional)", example = "true")
    private Boolean activo;
}
