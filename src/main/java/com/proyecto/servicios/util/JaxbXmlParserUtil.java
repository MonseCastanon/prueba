package com.proyecto.servicios.util;

import com.proyecto.servicios.enums.CatProductoRespuestaEnum;
import com.proyecto.servicios.exception.ExternalServiceException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;

@Component
public class JaxbXmlParserUtil {

    private static final Logger log = LoggerFactory.getLogger(JaxbXmlParserUtil.class);

    /**
     * Deserializa una cadena XML hacia la clase mapeada con anotaciones JAXB.
     */
    @SuppressWarnings("unchecked")
    public <T> T parseXmlString(String xmlContent, Class<T> clazz) {
        if (xmlContent == null || xmlContent.isBlank()) {
            throw new ExternalServiceException(
                    CatProductoRespuestaEnum.ERROR_PARSING_XML.getMensaje() + ": XML vacío",
                    CatProductoRespuestaEnum.ERROR_PARSING_XML.getCodigo()
            );
        }

        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xmlContent);
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("Error al deserializar XML con JAXB para la clase {}: {}", clazz.getSimpleName(), e.getMessage());
            throw new ExternalServiceException(
                    CatProductoRespuestaEnum.ERROR_PARSING_XML.getMensaje() + ": " + e.getMessage(),
                    CatProductoRespuestaEnum.ERROR_PARSING_XML.getCodigo()
            );
        }
    }
}
