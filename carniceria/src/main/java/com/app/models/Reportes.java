package com.app.models;

import java.sql.Date;

import javax.persistence.*;

@Entity
@Table(name = "Reportes")
public class Reportes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Column(name = "Fecha_Reporte")
    private Date fecha;
    

    public Reportes() {
    }

    public Reportes(long id, Date fecha) {
       this.id=id;
       this.fecha=fecha;
    } 

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFecha(){
        return fecha;
    }

}