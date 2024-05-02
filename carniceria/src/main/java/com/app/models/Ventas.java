package com.app.models;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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

    @ManyToOne
    @JoinColumn(name = "Ticket", insertable = false, updatable = false)
    private Reportes reporte;

    @Column(name = "Total")
    private float total;

    @Column(name = "Fecha")
    private Date fecha;

    @Column(name = "Ticket")
    private String ticket;

    public Ventas() {
        this.ticket = String.format("%06d", (int) (Math.random() * 1000000));
    }

    public Ventas(float total, Date fecha, Reportes reporte) {
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


    public Reportes getReporte() {
        return reporte;
    }

    public void setReporte(Reportes reporte) {
        this.reporte = reporte;
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