package com.app.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Ventas")
public class Ventas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ticket;
    
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    private float total;
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallesVenta> detalles = new ArrayList<>();

    public Ventas(Long id, String ticket,Date fecha, float total){
        this.id = id;
        this.ticket = ticket;
        this.fecha = fecha;
        this.total=total;
    }
    public Ventas(){

    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
    

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public List<DetallesVenta> getDetalles() {
        return detalles;
    }

    public void addDetalle(DetallesVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void removeDetalle(DetallesVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }
}