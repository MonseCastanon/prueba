package com.proyecto.servicios.service;

import com.proyecto.servicios.model.UsuarioActualizaRequest;
import com.proyecto.servicios.model.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.UsuarioEliminaRequest;
import com.proyecto.servicios.model.UsuarioRegistroRequest;
import com.proyecto.servicios.model.UsuarioResponse;

import java.util.List;

/**
 * Contrato de servicio para la administración y ciclo de vida de usuarios del sistema.
 */
public interface UsuarioService {

    /**
     * Registra un nuevo usuario en el sistema vinculado a un cliente existente.
     * Cifra la contraseña de acceso y previene registros duplicados.
     *
     * @param request datos para el registro del usuario
     * @return representación pública del usuario registrado
     */
    UsuarioResponse registrarUsuario(UsuarioRegistroRequest request);

    /**
     * Consulta usuarios mediante filtros multicriterio dinámicos combinables.
     *
     * @param filtro criterios opcionales de búsqueda
     * @return lista de usuarios que coinciden con los criterios provistos
     */
    List<UsuarioResponse> consultarUsuarios(UsuarioBusquedaRequest filtro);

    /**
     * Actualiza atributos controlados de un usuario existente.
     *
     * @param request datos a modificar del usuario
     * @return usuario actualizado
     */
    UsuarioResponse actualizarUsuario(UsuarioActualizaRequest request);

    /**
     * Aplica la baja lógica inhabilitando el usuario en el sistema y registrando la justificación.
     *
     * @param request datos para la baja con justificación obligatoria
     */
    void eliminarUsuario(UsuarioEliminaRequest request);
}
