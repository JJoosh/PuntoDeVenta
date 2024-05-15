package com.app.models;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Devoluciones")
public class Devoluciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Devolucion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Venta")
    private Ventas venta;

    @Column(name = "Cantidad_devuelta")
    private Double cantidadDevuelta;

    @Column(name = "Motivo")
    private String motivo;

    @Column(name = "Fecha_devolucion")
    private Timestamp fechaDevolucion;

    public Devoluciones() {
    }

    public Devoluciones(Long id,Ventas venta, Double cantidadDevuelta, String motivo, Timestamp fechaDevolucion) {
        this.id = id;
        this.venta = venta;
        this.cantidadDevuelta = cantidadDevuelta;
        this.motivo = motivo;
        this.fechaDevolucion = fechaDevolucion;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ventas getVenta() {
        return venta;
    }

    public void setVenta(Ventas venta) {
        this.venta = venta;
    }

    public Double getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public void setCantidadDevuelta(Double cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Timestamp getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Timestamp fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
}