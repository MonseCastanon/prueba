package com.proyecto.servicios.model.gestopago.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "producto")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductItemXmlDto {

    @XmlElement(name = "idProducto")
    private Integer idProducto;

    @XmlElement(name = "producto")
    private String producto;

    @XmlElement(name = "idServicio")
    private Integer idServicio;

    @XmlElement(name = "servicio")
    private String servicio;

    @XmlElement(name = "idCatTipoServicio")
    private Integer idCatTipoServicio;

    @XmlElement(name = "tipoFront")
    private Integer tipoFront;

    @XmlElement(name = "tipoReferencia")
    private String tipoReferencia;

    @XmlElement(name = "precio")
    private String precio;

    @XmlElement(name = "hasDigitoVerificador")
    private Boolean hasDigitoVerificador;

    @XmlElement(name = "showAyuda")
    private Boolean showAyuda;

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
}
