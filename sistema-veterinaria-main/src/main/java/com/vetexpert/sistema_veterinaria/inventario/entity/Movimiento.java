package com.vetexpert.sistema_veterinaria.inventario.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que registra los movimientos automáticos y manuales de stock en el inventario.
 */
@Entity
@Table(name = "movimientos_inventario")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "producto_tipo", nullable = false, length = 20)
    private String productoTipo; // VACUNA o PRODUCTO

    @Column(name = "producto_nombre", nullable = false, length = 150)
    private String productoNombre;

    @Column(nullable = false)
    private Integer cantidad; // Cantidad operada (positiva para ingresos, negativa para salidas)

    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private String tipoMovimiento; // INGRESO, SALIDA, VENTA, APLICACION_VACUNA, CONSUMO_CLINICO, HOSPITALIZACION, AJUSTE

    @Column(length = 250)
    private String motivo;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    // ========== Constructores ==========

    public Movimiento() {
    }

    public Movimiento(String usuario, Long productoId, String productoTipo, String productoNombre, Integer cantidad, String tipoMovimiento, String motivo) {
        this.usuario = usuario;
        this.productoId = productoId;
        this.productoTipo = productoTipo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
    }

    // ========== Getters y Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoTipo() {
        return productoTipo;
    }

    public void setProductoTipo(String productoTipo) {
        this.productoTipo = productoTipo;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
