package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.proyecto.servicios.entity.mongo.CatProductoDocument;
import com.proyecto.servicios.enums.CatProductoRespuestaEnum;
import com.proyecto.servicios.model.GenericResponse;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatProductoResponse extends GenericResponse {

    private String claveEstado;
    private Integer totalRegistros;
    private List<CatProductoDocument> productos;

    public CatProductoResponse() {
        super();
    }

    public static CatProductoResponse exito(List<CatProductoDocument> productos) {
        CatProductoResponse resp = new CatProductoResponse();
        resp.setCodigo(CatProductoRespuestaEnum.EXITO.getCodigo());
        resp.setMensaje(CatProductoRespuestaEnum.EXITO.getMensaje());
        resp.setClaveEstado(CatProductoRespuestaEnum.EXITO.getClave());
        resp.setProductos(productos);
        resp.setTotalRegistros(productos != null ? productos.size() : 0);
        return resp;
    }

    public static CatProductoResponse error(CatProductoRespuestaEnum errorEnum) {
        CatProductoResponse resp = new CatProductoResponse();
        resp.setCodigo(errorEnum.getCodigo());
        resp.setMensaje(errorEnum.getMensaje());
        resp.setClaveEstado(errorEnum.getClave());
        resp.setProductos(null);
        resp.setTotalRegistros(0);
        return resp;
    }

    public String getClaveEstado() {
        return claveEstado;
    }

    public void setClaveEstado(String claveEstado) {
        this.claveEstado = claveEstado;
    }

    public Integer getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Integer totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public List<CatProductoDocument> getProductos() {
        return productos;
    }

    public void setProductos(List<CatProductoDocument> productos) {
        this.productos = productos;
    }
}
