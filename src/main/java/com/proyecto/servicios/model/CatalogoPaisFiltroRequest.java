package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Filtro para consultar estados por código ISO de país")
public class CatalogoPaisFiltroRequest {

    @NotBlank(message = "El código ISO del país es obligatorio")
    @Size(min = 2, max = 3, message = "El código ISO del país debe tener entre 2 y 3 caracteres")
    @Schema(description = "Código ISO Alfa-3 del país", example = "MEX", requiredMode = Schema.RequiredMode.REQUIRED)
    private String codigoPaisIso;
}
