package com.app.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Ventas")
public class Ventas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "ProductIds")
    private List<Long> productIds = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "Ticket", insertable = false, updatable = false)
    private Reportes reporte;

    @Column(name = "Cantidad")
    private float cantidad;

    @Column(name = "Total")
    private float total;

    @Column(name = "Fecha")
    private Date fecha;

    @Column(name = "Ticket")
    private String ticket;

    public Ventas() {
        this.ticket = String.format("%06d", (int) (Math.random() * 1000000));
    }

    public Ventas(List<Long> productIds, float cantidad, float total, Date fecha, Reportes reporte) {
        this.productIds = productIds;
        this.cantidad = cantidad;
        this.total = total;
        this.fecha = fecha;
        this.reporte = reporte;
        this.ticket = String.format("%06d", (int) (Math.random() * 1000000));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public Reportes getReporte() {
        return reporte;
    }

    public void setReporte(Reportes reporte) {
        this.reporte = reporte;
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}