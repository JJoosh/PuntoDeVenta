    package com.app.models;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;
    import javax.persistence.*;

    import java.math.BigDecimal;

    @Entity
    @Table(name = "Ventas")
    public class Ventas {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String ticket;

        @Column(columnDefinition = "TIMESTAMP")
        private LocalDateTime fecha;

        private BigDecimal total;
        @ManyToOne
        @JoinColumn(name = "cliente_id")
        private Clientes cliente;

        @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<DetallesVenta> detalles = new ArrayList<>();

        

        public Ventas(Long id, String ticket, LocalDateTime fecha, BigDecimal total) {
            this.id = id;
            this.ticket = ticket;
            this.fecha = fecha;
            this.total = total;
        }

        public Ventas() {
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

        public LocalDateTime getFecha() {
            return fecha;
        }

        public void setFecha(LocalDateTime fecha) {
            this.fecha = fecha;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
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
        public void setCliente(Clientes cliente) {
            this.cliente = cliente;
        }
        
    }