package com.proyecto.servicios.model.gestopago.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "RESPONSE")
@XmlAccessorType(XmlAccessType.FIELD)
public class CatProductXmlDto {

    @XmlElement(name = "codigoRespuesta")
    private String codigoRespuesta;

    @XmlElement(name = "mensaje")
    private String mensaje;

    @XmlElementWrapper(name = "PRODUCTOS")
    @XmlElement(name = "producto")
    private List<ProductItemXmlDto> productos;

    public CatProductXmlDto() {
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public void setCodigoRespuesta(String codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<ProductItemXmlDto> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductItemXmlDto> productos) {
        this.productos = productos;
    }
}
