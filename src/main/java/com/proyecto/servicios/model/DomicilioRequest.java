package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomicilioRequest {

    @NotBlank(message = "La calle es obligatoria")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 .,#-]+$", message = "La calle contiene caracteres inválidos")
    @Size(max = 100, message = "La calle no puede exceder 100 caracteres")
    @Schema(example = "Av. Insurgentes Sur")
    private String calle;

    @NotBlank(message = "El número exterior es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9 -]+$", message = "El número exterior contiene caracteres inválidos")
    @Size(max = 20, message = "El número exterior no puede exceder 20 caracteres")
    @Schema(example = "1602")
    private String numeroExterior;

    @Pattern(regexp = "^[a-zA-Z0-9 -]*$", message = "El número interior contiene caracteres inválidos")
    @Size(max = 20, message = "El número interior no puede exceder 20 caracteres")
    @Schema(example = "Piso 4 Depto B")
    private String numeroInterior;

    @NotBlank(message = "La colonia es obligatoria")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 .,-]+$", message = "La colonia contiene caracteres inválidos")
    @Size(max = 100, message = "La colonia no puede exceder 100 caracteres")
    @Schema(example = "Crédito Constructor")
    private String colonia;

    @NotBlank(message = "El municipio o alcaldía es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 .,-]+$", message = "El municipio contiene caracteres inválidos")
    @Size(max = 100, message = "El municipio no puede exceder 100 caracteres")
    @Schema(example = "Benito Juárez")
    private String municipio;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El estado solo debe contener letras")
    @Size(max = 50, message = "El estado no puede exceder 50 caracteres")
    @Schema(example = "Ciudad de México")
    private String estado;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "^[0-9]{5}$", message = "El código postal debe contener exactamente 5 dígitos numéricos")
    @Schema(example = "03940")
    private String codigoPostal;

    @Pattern(regexp = "^[A-Z]{3}$", message = "El país debe ser el código ISO 3166-1 alfa-3 (ej. MEX)")
    @Schema(example = "MEX")
    @Builder.Default
    private String pais = "MEX";
}
