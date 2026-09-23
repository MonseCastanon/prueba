package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoCatProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.enums.CatProductoRespuestaEnum;
import com.proyecto.servicios.model.gestopago.CatProductoResponse;
import com.proyecto.servicios.model.gestopago.xml.CatProductXmlDto;
import com.proyecto.servicios.repositorys.mongo.CatProductoMongoRepository;
import com.proyecto.servicios.service.CatProductoService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.util.JaxbXmlParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CatProductoServiceImpl implements CatProductoService {

    private static final Logger log = LoggerFactory.getLogger(CatProductoServiceImpl.class);

    private final CatProductoMongoRepository mongoRepository;
    private final GestoPagoCatProductClient catProductClient;
    private final GestoPagoTokenService tokenService;
    private final JaxbXmlParserUtil jaxbUtil;

    @Value("${gestopago.auth.id-distribuidor:83}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo:GPS83-TPV-17}")
    private String codigoDispositivo;

    @Value("${gestopago.api.api-key:}")
    private String apiKey;

    public CatProductoServiceImpl(CatProductoMongoRepository mongoRepository,
                                  GestoPagoCatProductClient catProductClient,
                                  GestoPagoTokenService tokenService,
                                  JaxbXmlParserUtil jaxbUtil) {
        this.mongoRepository = mongoRepository;
        this.catProductClient = catProductClient;
        this.tokenService = tokenService;
        this.jaxbUtil = jaxbUtil;
    }

    @Override
    public CatProductoResponse obtenerCatalogo() {
        log.info("[CONSULTA] Consultando catálogo de productos en MongoDB");
        try {
            List<CatProductoDocument> lista = mongoRepository.findByActivoTrue();

            // Si está vacío, intentar sincronizar inicialmente desde GestoPago
            if (lista == null || lista.isEmpty()) {
                log.info("[CONSULTA] MongoDB no contiene productos aún. Ejecutando sincronización inicial con PuntoRed...");
                sincronizarCatalogoDesdeGp();
                lista = mongoRepository.findByActivoTrue();
            }

            if (lista == null || lista.isEmpty()) {
                log.warn("[CONSULTA] No se encontraron productos en el catálogo tras la sincronización");
                return CatProductoResponse.error(CatProductoRespuestaEnum.CATALOGO_NO_ENCONTRADO);
            }

            log.info("[CONSULTA] Consulta exitosa. {} productos recuperados de MongoDB", lista.size());
            return CatProductoResponse.exito(lista);

        } catch (Exception e) {
            log.error("[CONSULTA ERROR] Error al consultar MongoDB: {}", e.getMessage(), e);
            return CatProductoResponse.error(CatProductoRespuestaEnum.ERROR_BASE_DATOS_MONGO);
        }
    }

    @Override
    public void sincronizarCatalogoDesdeGp() {
        log.info("[SINCRONIZACIÓN] Iniciando descarga y sincronización de catProduct GP para distribuidor={}, dispositivo={}", idDistribuidor, codigoDispositivo);

        // 1. Obtener Token de GP desde GestoPagoTokenService
        Optional<GestoPagoToken> tokenOpt = tokenService.obtenerTokenActivo(idDistribuidor, codigoDispositivo);
        if (tokenOpt.isEmpty() || tokenOpt.get().getToken() == null) {
            log.error("[SINCRONIZACIÓN ERROR] No hay token activo disponible para el distribuidor {}", idDistribuidor);
            return;
        }

        String rawToken = tokenOpt.get().getToken();
        String authHeader = rawToken.startsWith("Bearer ") ? rawToken : "Bearer " + rawToken;

        try {
            // 2. Invocar API GP (código 200 -> XML)
            log.info("[SINCRONIZACIÓN] Llamando a getProductListXml...");
            String xmlResponse = catProductClient.getProductListXml(authHeader, (apiKey != null && !apiKey.isBlank()) ? apiKey : null);
            log.info("[SINCRONIZACIÓN] XML recibido con longitud {} caracteres", xmlResponse != null ? xmlResponse.length() : 0);

            // 3. Parsear XML a DTOs con JAXB
            CatProductXmlDto xmlDto = jaxbUtil.parseXmlString(xmlResponse, CatProductXmlDto.class);

            if (xmlDto == null || xmlDto.getProductos() == null || xmlDto.getProductos().isEmpty()) {
                log.warn("[SINCRONIZACIÓN] El XML recibido de GP no contiene productos");
                return;
            }

            // 4. Mapear y guardar en MongoDB
            LocalDateTime now = LocalDateTime.now();
            List<CatProductoDocument> documentos = xmlDto.getProductos().stream()
                    .map(item -> CatProductoDocument.builder()
                            .id(String.valueOf(item.getIdProducto()))
                            .idProducto(item.getIdProducto())
                            .producto(item.getProducto())
                            .idServicio(item.getIdServicio())
                            .servicio(item.getServicio())
                            .idCatTipoServicio(item.getIdCatTipoServicio())
                            .tipoFront(item.getTipoFront())
                            .tipoReferencia(item.getTipoReferencia())
                            .precio(item.getPrecio())
                            .hasDigitoVerificador(item.getHasDigitoVerificador())
                            .showAyuda(item.getShowAyuda())
                            .legend(item.getLegend())
                            .activo(true)
                            .fechaActualizacion(now)
                            .build())
                    .toList();

            mongoRepository.saveAll(documentos);
            log.info("[SINCRONIZACIÓN ÉXITO] Se sincronizaron y guardaron {} productos en MongoDB correctamente", documentos.size());

        } catch (Exception e) {
            log.error("[SINCRONIZACIÓN ERROR] Error en el proceso de sincronización con GP: {}", e.getMessage(), e);
        }
    }
}
