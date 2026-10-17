package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Criterios exclusivos de búsqueda dinámica de usuarios. Los únicos dos filtros permitidos son RFC y CURP.")
public class UsuarioBusquedaRequest {

    @Pattern(
        regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$",
        message = "El formato del RFC no es válido (debe tener entre 12 y 13 caracteres alfanuméricos, ej. PELJ950815XXX)"
    )
    @Schema(description = "RFC del cliente asociado al usuario (12 a 13 caracteres)", example = "PELJ950815XXX")
    private String rfc;

    @Pattern(
        regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9]\\d$",
        message = "El formato de la CURP no es válido (debe tener 18 caracteres en formato oficial, ej. PELJ950815HDFRPR01)"
    )
    @Schema(description = "CURP del cliente asociado al usuario (18 caracteres)", example = "PELJ950815HDFRPR01")
    private String curp;

    @AssertTrue(message = "Debe proporcionar al menos un criterio de búsqueda: RFC o CURP")
    @JsonIgnore
    public boolean isAlMenosUnFiltroPresente() {
        return (rfc != null && !rfc.isBlank()) || (curp != null && !curp.isBlank());
    }
}
