package com.proyecto.servicios.model.gestopago.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "producto")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductItemXmlDto {

    @XmlAttribute(name = "idProducto")
    private Integer idProducto;

    @XmlAttribute(name = "producto")
    private String producto;

    @XmlAttribute(name = "idServicio")
    private Integer idServicio;

    @XmlAttribute(name = "servicio")
    private String servicio;

    @XmlAttribute(name = "idCatTipoServicio")
    private Integer idCatTipoServicio;

    @XmlAttribute(name = "tipoFront")
    private Integer tipoFront;

    @XmlAttribute(name = "tipoReferencia")
    private String tipoReferencia;

    @XmlAttribute(name = "precio")
    private String precio;

    @XmlAttribute(name = "hasDigitoVerificador")
    private Boolean hasDigitoVerificador;

    @XmlAttribute(name = "showAyuda")
    private Boolean showAyuda;

    @XmlElement(name = "legend")
    private String legend;

    public ProductItemXmlDto() {
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public Integer getIdCatTipoServicio() {
        return idCatTipoServicio;
    }

    public void setIdCatTipoServicio(Integer idCatTipoServicio) {
        this.idCatTipoServicio = idCatTipoServicio;
    }

    public Integer getTipoFront() {
        return tipoFront;
    }

    public void setTipoFront(Integer tipoFront) {
        this.tipoFront = tipoFront;
    }

    public String getTipoReferencia() {
        return tipoReferencia;
    }

    public void setTipoReferencia(String tipoReferencia) {
        this.tipoReferencia = tipoReferencia;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public Boolean getHasDigitoVerificador() {
        return hasDigitoVerificador;
    }

    public void setHasDigitoVerificador(Boolean hasDigitoVerificador) {
        this.hasDigitoVerificador = hasDigitoVerificador;
    }

    public Boolean getShowAyuda() {
        return showAyuda;
    }

    public void setShowAyuda(Boolean showAyuda) {
        this.showAyuda = showAyuda;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }
}
