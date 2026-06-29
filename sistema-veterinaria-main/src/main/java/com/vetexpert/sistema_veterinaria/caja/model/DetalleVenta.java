package com.vetexpert.sistema_veterinaria.caja.model;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa un detalle de concepto en una Venta.
 */
@Entity
@Table(name = "detalle_ventas")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Column(nullable = false, length = 150)
    private String concepto; // Nombre visible de lo que se cobra (ej: "Vacuna Rabia", "Amoxicilina")

    @Column(name = "concepto_id")
    private Long conceptoId; // ID de referencia en su tabla respectiva (ej: id de VacunaAplicada)

    @Column(name = "concepto_tipo", length = 30)
    private String conceptoTipo; // VACUNA, DESPARASITACION, PRODUCTO, SERVICIO, CONSULTA

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario = 0.0;

    @Column(nullable = false)
    private Double subtotal = 0.0;

    // ========== Constructores ==========

    public DetalleVenta() {
    }

    public DetalleVenta(String concepto, Long conceptoId, String conceptoTipo, Integer cantidad, Double precioUnitario) {
        this.concepto = concepto;
        this.conceptoId = conceptoId;
        this.conceptoTipo = conceptoTipo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Long getConceptoId() {
        return conceptoId;
    }

    public void setConceptoId(Long conceptoId) {
        this.conceptoId = conceptoId;
    }

    public String getConceptoTipo() {
        return conceptoTipo;
    }

    public void setConceptoTipo(String conceptoTipo) {
        this.conceptoTipo = conceptoTipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
