package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.*;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.enums.EstatusFacial;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.ClienteSpecification;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.ClienteService;
import com.proyecto.servicios.util.GeneradorCuentaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeneradorCuentaUtil generadorCuentaUtil;

    @Value("${banco.onboarding.saldo-inicial-default:100.00}")
    private BigDecimal saldoInicialDefault;

    @Value("${banco.onboarding.prefijo-cuenta:9080}")
    private String prefijoCuenta;

    @Value("${banco.onboarding.edad-minima:18}")
    private int edadMinima;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
            CuentaRepository cuentaRepository,
            PasswordEncoder passwordEncoder,
            GeneradorCuentaUtil generadorCuentaUtil) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
        this.generadorCuentaUtil = generadorCuentaUtil;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public ClienteResponse registrarCliente(ClienteRegistroRequest request) {
        log.info("Iniciando proceso de onboarding para CURP: {}", request.getCurp());

        // 1. Validar Mayoría de Edad
        validarMayoriaEdad(request.getFechaNacimiento());

        // 2. Validar Unicidad de CURP, RFC y Correo
        if (clienteRepository.existsByCurp(request.getCurp())) {
            throw new DuplicateResourceException("Ya existe un cliente registrado con la CURP: " + request.getCurp());
        }
        if (clienteRepository.existsByRfc(request.getRfc())) {
            throw new DuplicateResourceException("Ya existe un cliente registrado con el RFC: " + request.getRfc());
        }
        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el correo: " + request.getCorreo());
        }

        // 3. Mapear Entidad Cliente
        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .segundoNombre(request.getSegundoNombre())
                .apellidoPaterno(request.getApellidoPaterno())
                .apellidoMaterno(request.getApellidoMaterno())
                .fechaNacimiento(request.getFechaNacimiento())
                .curp(request.getCurp().toUpperCase())
                .rfc(request.getRfc().toUpperCase())
                .sexo(request.getSexo().toUpperCase())
                .nacionalidad(request.getNacionalidad().toUpperCase())
                .estadoCivil(request.getEstadoCivil())
                .correo(request.getCorreo().toLowerCase())
                .telefonoMovil(request.getTelefonoMovil())
                .telefonoAlternativo(request.getTelefonoAlternativo())
                .ocupacion(request.getOcupacion())
                .empresa(request.getEmpresa())
                .ingresoMensual(request.getIngresoMensual())
                .activo(true)
                .build();

        // 4. Mapear Domicilio
        DomicilioRequest domReq = request.getDomicilio();
        Domicilio domicilio = Domicilio.builder()
                .calle(domReq.getCalle())
                .numeroExterior(domReq.getNumeroExterior())
                .numeroInterior(domReq.getNumeroInterior())
                .colonia(domReq.getColonia())
                .municipio(domReq.getMunicipio())
                .estado(domReq.getEstado())
                .codigoPostal(domReq.getCodigoPostal())
                .pais(domReq.getPais() != null ? domReq.getPais().toUpperCase() : "MEX")
                .build();
        cliente.setDomicilio(domicilio);

        // 5. Generar Cuenta Bancaria Automática
        String numeroCuenta = generarNumeroCuentaUnico();
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(numeroCuenta)
                .saldo(saldoInicialDefault)
                .estatus(EstatusCuenta.ACTIVA)
                .activa(true)
                .build();
        cuenta.agregarEventoHistorial("APERTURA", "Apertura de cuenta inicial por onboarding", "SYSTEM");
        cliente.addCuenta(cuenta);

        // 6. Generar Usuario de Acceso Automático con BCrypt
        Usuario usuario = Usuario.builder()
                .correo(cliente.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .rol("ROLE_CLIENTE")
                .build();
        cliente.setUsuario(usuario);

        // 7. Procesar y Almacenar Biometría y Reconocimiento Facial
        BiometriaCliente biometria = procesarBiometria(request);
        cliente.setBiometria(biometria);

        // 8. Persistencia en Cascada Atómica
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Onboarding completado exitosamente. ID Cliente: {}, Cuenta: {}", guardado.getId(), numeroCuenta);

        return mapearResponse(guardado);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public ClienteResponse actualizarCliente(Long id, ClienteActualizaRequest request) {
        Cliente cliente = buscarEntidadPorId(id);

        if (!cliente.getActivo()) {
            throw new BusinessRuleException("No se pueden realizar modificaciones a un cliente en estatus inactivo");
        }

        // Validar unicidad de correo si cambió
        if (!cliente.getCorreo().equalsIgnoreCase(request.getCorreo())
                && clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new DuplicateResourceException("El correo electrónico " + request.getCorreo() + " ya está en uso");
        }

        // Actualizar datos permitidos (CURP y RFC permanecen inmutables)
        cliente.setNombre(request.getNombre());
        cliente.setSegundoNombre(request.getSegundoNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setSexo(request.getSexo().toUpperCase());
        cliente.setNacionalidad(request.getNacionalidad().toUpperCase());
        cliente.setEstadoCivil(request.getEstadoCivil());
        cliente.setCorreo(request.getCorreo().toLowerCase());
        cliente.setTelefonoMovil(request.getTelefonoMovil());
        cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        cliente.setOcupacion(request.getOcupacion());
        cliente.setEmpresa(request.getEmpresa());
        cliente.setIngresoMensual(request.getIngresoMensual());

        // Actualizar Domicilio
        Domicilio dom = cliente.getDomicilio();
        if (dom == null) {
            dom = new Domicilio();
            cliente.setDomicilio(dom);
        }
        dom.setCalle(request.getDomicilio().getCalle());
        dom.setNumeroExterior(request.getDomicilio().getNumeroExterior());
        dom.setNumeroInterior(request.getDomicilio().getNumeroInterior());
        dom.setColonia(request.getDomicilio().getColonia());
        dom.setMunicipio(request.getDomicilio().getMunicipio());
        dom.setEstado(request.getDomicilio().getEstado());
        dom.setCodigoPostal(request.getDomicilio().getCodigoPostal());
        dom.setPais(request.getDomicilio().getPais() != null ? request.getDomicilio().getPais().toUpperCase() : "MEX");

        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setCorreo(cliente.getCorreo());
        }

        return mapearResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public ClienteResponse patchCliente(Long id, ClientePatchRequest request) {
        Cliente cliente = buscarEntidadPorId(id);

        if (!cliente.getActivo()) {
            throw new BusinessRuleException("No se pueden realizar modificaciones a un cliente en estatus inactivo");
        }

        if (request.getCurp() != null || request.getRfc() != null) {
            throw new BusinessRuleException("Por regla de negocio, la CURP y el RFC no pueden ser modificados una vez registrados");
        }

        if (request.getNombre() != null) {
            cliente.setNombre(request.getNombre());
        }
        if (request.getSegundoNombre() != null) {
            cliente.setSegundoNombre(request.getSegundoNombre());
        }
        if (request.getApellidoPaterno() != null) {
            cliente.setApellidoPaterno(request.getApellidoPaterno());
        }
        if (request.getApellidoMaterno() != null) {
            cliente.setApellidoMaterno(request.getApellidoMaterno());
        }
        if (request.getEstadoCivil() != null) {
            cliente.setEstadoCivil(request.getEstadoCivil());
        }
        if (request.getTelefonoMovil() != null) {
            cliente.setTelefonoMovil(request.getTelefonoMovil());
        }
        if (request.getTelefonoAlternativo() != null) {
            cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        }
        if (request.getOcupacion() != null) {
            cliente.setOcupacion(request.getOcupacion());
        }
        if (request.getEmpresa() != null) {
            cliente.setEmpresa(request.getEmpresa());
        }
        if (request.getIngresoMensual() != null) {
            cliente.setIngresoMensual(request.getIngresoMensual());
        }

        if (request.getCorreo() != null && !request.getCorreo().equalsIgnoreCase(cliente.getCorreo())) {
            if (clienteRepository.existsByCorreo(request.getCorreo())) {
                throw new DuplicateResourceException("El correo electrónico ya se encuentra registrado");
            }
            cliente.setCorreo(request.getCorreo().toLowerCase());
            if (cliente.getUsuario() != null) {
                cliente.getUsuario().setCorreo(cliente.getCorreo());
            }
        }

        if (request.getDomicilio() != null && cliente.getDomicilio() != null) {
            Domicilio dom = cliente.getDomicilio();
            DomicilioRequest dReq = request.getDomicilio();
            if (dReq.getCalle() != null) {
                dom.setCalle(dReq.getCalle());
            }
            if (dReq.getNumeroExterior() != null) {
                dom.setNumeroExterior(dReq.getNumeroExterior());
            }
            if (dReq.getNumeroInterior() != null) {
                dom.setNumeroInterior(dReq.getNumeroInterior());
            }
            if (dReq.getColonia() != null) {
                dom.setColonia(dReq.getColonia());
            }
            if (dReq.getMunicipio() != null) {
                dom.setMunicipio(dReq.getMunicipio());
            }
            if (dReq.getEstado() != null) {
                dom.setEstado(dReq.getEstado());
            }
            if (dReq.getCodigoPostal() != null) {
                dom.setCodigoPostal(dReq.getCodigoPostal());
            }
        }

        return mapearResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public GenericResponse bajaLogicaCliente(Long id) {
        Cliente cliente = buscarEntidadPorId(id);

        if (!cliente.getActivo()) {
            throw new BusinessRuleException("El cliente ya se encuentra en estatus inactivo");
        }

        cliente.setActivo(false);
        cliente.setFechaBaja(OffsetDateTime.now());

        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setActivo(false);
        }

        if (cliente.getCuentas() != null) {
            cliente.getCuentas().forEach(cta -> {
                cta.setEstatus(EstatusCuenta.CANCELADA);
                cta.setActiva(false);
                cta.setFechaBloqueo(OffsetDateTime.now());
                cta.setMotivoBloqueo("Baja lógica del cliente titular");
                cta.agregarEventoHistorial("CANCELACION", "Baja lógica automática por desactivación de cliente", "SYSTEM");
            });
        }

        clienteRepository.save(cliente);
        return new GenericResponse(0, "Cliente dado de baja lógica correctamente junto con usuario y cuentas asociadas");
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorId(Long id) {
        return mapearResponse(buscarEntidadPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCurp(String curp) {
        return clienteRepository.findByCurp(curp.trim().toUpperCase())
                .map(this::mapearResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró cliente con CURP: " + curp));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorRfc(String rfc) {
        return clienteRepository.findByRfc(rfc.trim().toUpperCase())
                .map(this::mapearResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró cliente con RFC: " + rfc));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorNumeroCuenta(String numeroCuenta) {
        return clienteRepository.findByNumeroCuenta(numeroCuenta.trim())
                .map(this::mapearResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró cliente con número de cuenta: " + numeroCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorFiltros(String nombre, String apePaterno, String apeMaterno, String curp, Boolean activo) {
        Specification<Cliente> spec = Specification
                .where(ClienteSpecification.conNombre(nombre))
                .and(ClienteSpecification.conApellidoPaterno(apePaterno))
                .and(ClienteSpecification.conApellidoMaterno(apeMaterno))
                .and(ClienteSpecification.conCurp(curp))
                .and(ClienteSpecification.esActivo(activo));

        return clienteRepository.findAll(spec).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorRangoFechas(OffsetDateTime inicio, OffsetDateTime fin) {
        return clienteRepository.findClientesByRangoFechas(inicio, fin).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    private void validarMayoriaEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new BusinessRuleException("La fecha de nacimiento es requerida");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La fecha de nacimiento no puede ser una fecha futura");
        }
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < edadMinima) {
            throw new BusinessRuleException("El solicitante debe ser mayor de edad (" + edadMinima + " años o más). Edad calculada: " + edad);
        }
    }

    private String generarNumeroCuentaUnico() {
        String num;
        do {
            num = generadorCuentaUtil.generarNumeroCuenta(prefijoCuenta);
        } while (cuentaRepository.existsByNumeroCuenta(num));
        return num;
    }

    private BiometriaCliente procesarBiometria(ClienteRegistroRequest request) {
        BiometriaCliente bio = BiometriaCliente.builder()
                .estatusFacial(EstatusFacial.APROBADO)
                .livenessScore(new BigDecimal("0.9850"))
                .build();

        if (request.getFotoRostroBase64() != null && !request.getFotoRostroBase64().isBlank()) {
            byte[] fotoBytes = Base64.getDecoder().decode(request.getFotoRostroBase64());
            bio.setFotoRostro(fotoBytes);
            bio.setSha256Foto(DigestUtils.sha256Hex(fotoBytes));
            bio.setVectorFacial("[0.1254,-0.0452,0.9542,...,0.0341]");
        }

        if (request.getTemplateDactilarBase64() != null && !request.getTemplateDactilarBase64().isBlank()) {
            bio.setTemplateDactilar(Base64.getDecoder().decode(request.getTemplateDactilarBase64()));
        }

        return bio;
    }

    private Cliente buscarEntidadPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
    }

    private ClienteResponse mapearResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .segundoNombre(c.getSegundoNombre())
                .apellidoPaterno(c.getApellidoPaterno())
                .apellidoMaterno(c.getApellidoMaterno())
                .fechaNacimiento(c.getFechaNacimiento())
                .curp(c.getCurp())
                .rfc(c.getRfc())
                .sexo(c.getSexo())
                .nacionalidad(c.getNacionalidad())
                .estadoCivil(c.getEstadoCivil() != null ? c.getEstadoCivil().name() : null)
                .correo(c.getCorreo())
                .telefonoMovil(c.getTelefonoMovil())
                .telefonoAlternativo(c.getTelefonoAlternativo())
                .ocupacion(c.getOcupacion())
                .empresa(c.getEmpresa())
                .ingresoMensual(c.getIngresoMensual())
                .activo(c.getActivo())
                .fechaCreacion(c.getFechaCreacion())
                .cuentas(c.getCuentas().stream().map(cta -> CuentaResponse.builder()
                        .id(cta.getId())
                        .clienteId(c.getId())
                        .nombreTitular(c.getNombre() + " " + c.getApellidoPaterno())
                        .numeroCuenta(cta.getNumeroCuenta())
                        .saldo(cta.getSaldo())
                        .estatus(cta.getEstatus().name())
                        .activa(cta.getActiva())
                        .fechaCreacion(cta.getFechaCreacion())
                        .fechaBloqueo(cta.getFechaBloqueo())
                        .motivoBloqueo(cta.getMotivoBloqueo())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
