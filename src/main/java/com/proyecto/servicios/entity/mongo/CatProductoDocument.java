package com.proyecto.servicios.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "cat_productos")
public class CatProductoDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private Integer idProducto;

    private String producto;
    private Integer idServicio;
    private String servicio;
    private Integer idCatTipoServicio;
    private Integer tipoFront;
    private String tipoReferencia;
    private String precio;
    private Boolean hasDigitoVerificador;
    private Boolean showAyuda;
    private String legend;
    private Boolean activo;
    private LocalDateTime fechaActualizacion;

    public CatProductoDocument() {
    }

    public CatProductoDocument(String id, Integer idProducto, String producto, Integer idServicio, String servicio,
                               Integer idCatTipoServicio, Integer tipoFront, String tipoReferencia, String precio,
                               Boolean hasDigitoVerificador, Boolean showAyuda, String legend, Boolean activo, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.idProducto = idProducto;
        this.producto = producto;
        this.idServicio = idServicio;
        this.servicio = servicio;
        this.idCatTipoServicio = idCatTipoServicio;
        this.tipoFront = tipoFront;
        this.tipoReferencia = tipoReferencia;
        this.precio = precio;
        this.hasDigitoVerificador = hasDigitoVerificador;
        this.showAyuda = showAyuda;
        this.legend = legend;
        this.activo = activo;
        this.fechaActualizacion = fechaActualizacion;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Integer idProducto;
        private String producto;
        private Integer idServicio;
        private String servicio;
        private Integer idCatTipoServicio;
        private Integer tipoFront;
        private String tipoReferencia;
        private String precio;
        private Boolean hasDigitoVerificador;
        private Boolean showAyuda;
        private String legend;
        private Boolean activo;
        private LocalDateTime fechaActualizacion;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder idProducto(Integer idProducto) {
            this.idProducto = idProducto;
            return this;
        }

        public Builder producto(String producto) {
            this.producto = producto;
            return this;
        }

        public Builder idServicio(Integer idServicio) {
            this.idServicio = idServicio;
            return this;
        }

        public Builder servicio(String servicio) {
            this.servicio = servicio;
            return this;
        }

        public Builder idCatTipoServicio(Integer idCatTipoServicio) {
            this.idCatTipoServicio = idCatTipoServicio;
            return this;
        }

        public Builder tipoFront(Integer tipoFront) {
            this.tipoFront = tipoFront;
            return this;
        }

        public Builder tipoReferencia(String tipoReferencia) {
            this.tipoReferencia = tipoReferencia;
            return this;
        }

        public Builder precio(String precio) {
            this.precio = precio;
            return this;
        }

        public Builder hasDigitoVerificador(Boolean hasDigitoVerificador) {
            this.hasDigitoVerificador = hasDigitoVerificador;
            return this;
        }

        public Builder showAyuda(Boolean showAyuda) {
            this.showAyuda = showAyuda;
            return this;
        }

        public Builder legend(String legend) {
            this.legend = legend;
            return this;
        }

        public Builder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Builder fechaActualizacion(LocalDateTime fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
            return this;
        }

        public CatProductoDocument build() {
            return new CatProductoDocument(id, idProducto, producto, idServicio, servicio,
                    idCatTipoServicio, tipoFront, tipoReferencia, precio, hasDigitoVerificador, showAyuda, legend, activo, fechaActualizacion);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
