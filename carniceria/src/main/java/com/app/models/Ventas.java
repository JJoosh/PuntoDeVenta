package com.app.models;

import java.sql.Date;

import javax.persistence.*;

@Entity
@Table(name = "Ventas")
public class Ventas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Producto")
    private Productos producto;

    @Column(name = "Cantidad")
    private float Cantidad;

    @Column(name = "Total")
    private float Total;

    @Column(name = "Fecha")
    private Date fecha;

    public Ventas() {
    }

    public Ventas(Productos producto, float cantidad, float total, Date fecha) {
        this.producto = producto;
        this.Cantidad = cantidad;
        this.Total = total;
        this.fecha = fecha;
    } 

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

 

    public float getcantidad(){
        return Cantidad;
    }
    
    public void setCantidad(float Cantidad){
        this.Cantidad=Cantidad;
    }

    public float getTotal(){
        return Total;
    }

    public void setTotal(float Total){
        this.Total=Total;
    }

    public Date getFecha(){
        return fecha;
    }

    public void setFecha(Date fecha){
        this.fecha=fecha;
    }
    
}