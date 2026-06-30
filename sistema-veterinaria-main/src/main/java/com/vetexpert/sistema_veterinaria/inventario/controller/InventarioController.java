package com.vetexpert.sistema_veterinaria.inventario.controller;

import com.vetexpert.sistema_veterinaria.inventario.entity.Producto;
import com.vetexpert.sistema_veterinaria.inventario.entity.Proveedor;
import com.vetexpert.sistema_veterinaria.inventario.service.ProductoService;
import com.vetexpert.sistema_veterinaria.inventario.service.ProveedorService;
import com.vetexpert.sistema_veterinaria.inventario.service.MovimientoService;
import com.vetexpert.sistema_veterinaria.propietarios.service.PropietarioService;
import com.vetexpert.sistema_veterinaria.caja.service.VentaService;
import com.vetexpert.sistema_veterinaria.caja.entity.Venta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private final ProductoService productoService;
    private final ProveedorService proveedorService;
    private final MovimientoService movimientoService;
    private final PropietarioService propietarioService;
    private final VentaService ventaService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public InventarioController(ProductoService productoService,
                                ProveedorService proveedorService,
                                MovimientoService movimientoService,
                                PropietarioService propietarioService,
                                VentaService ventaService) {
        this.productoService = productoService;
        this.proveedorService = proveedorService;
        this.movimientoService = movimientoService;
        this.propietarioService = propietarioService;
        this.ventaService = ventaService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "dashboard");
        
        List<Producto> stockCritico = productoService.listarStockCritico();
        List<Producto> porVencer = productoService.listarProductosPorVencer(90);
        List<Producto> vencidos = productoService.listarProductosVencidos();
        List<Producto> todos = productoService.listarProductos();

        long totalMedicamentos = todos.stream().filter(p -> "MEDICAMENTO".equals(p.getCategoria())).count();
        long totalAlimentos = todos.stream().filter(p -> "ALIMENTO".equals(p.getCategoria())).count();

        model.addAttribute("totalProductos", todos.size());
        model.addAttribute("stockCriticoCount", stockCritico.size());
        model.addAttribute("porVencerCount", porVencer.size());
        model.addAttribute("vencidosCount", vencidos.size());
        model.addAttribute("totalMedicamentos", totalMedicamentos);
        model.addAttribute("totalAlimentos", totalAlimentos);
        
        model.addAttribute("criticos", stockCritico);
        model.addAttribute("vencimientos", porVencer);
        model.addAttribute("recientes", movimientoService.listarMovimientos());
        
        return "inventario/dashboard";
    }

    @GetMapping("/medicamentos")
    public String listarMedicamentos(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "medicamentos");
        List<Producto> lista;
        if (query != null && !query.trim().isEmpty()) {
            lista = productoService.buscarProductos(query).stream()
                    .filter(p -> "MEDICAMENTO".equals(p.getCategoria()))
                    .toList();
            model.addAttribute("query", query);
        } else {
            lista = productoService.listarProductosPorCategoria("MEDICAMENTO");
        }
        model.addAttribute("lista", lista);
        model.addAttribute("categoriaNombre", "Medicamentos");
        model.addAttribute("propietarios", propietarioService.listarTodos());
        return "inventario/productos_cards";
    }

    @GetMapping("/alimentos")
    public String listarAlimentos(Model model, 
                                  @RequestParam(value = "especie", required = false) String especie,
                                  @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "alimentos");
        
        List<Producto> lista;
        if (query != null && !query.trim().isEmpty()) {
            lista = productoService.buscarProductos(query).stream()
                    .filter(p -> "ALIMENTO".equals(p.getCategoria()))
                    .toList();
            model.addAttribute("query", query);
        } else {
            lista = productoService.listarProductosPorCategoria("ALIMENTO");
        }

        // Separar/filtrar por especie en la descripción o nombre si se especifica
        if (especie != null && !especie.trim().isEmpty() && !"TODOS".equalsIgnoreCase(especie)) {
            lista = lista.stream()
                    .filter(p -> (p.getNombre() != null && p.getNombre().toUpperCase().contains(especie.toUpperCase())) 
                              || (p.getDescripcion() != null && p.getDescripcion().toUpperCase().contains(especie.toUpperCase())))
                    .toList();
            model.addAttribute("especieSeleccionada", especie);
        }
        
        model.addAttribute("lista", lista);
        model.addAttribute("categoriaNombre", "Alimentos");
        model.addAttribute("propietarios", propietarioService.listarTodos());
        return "inventario/productos_cards";
    }

    @GetMapping("/accesorios")
    public String listarAccesorios(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "accesorios");
        List<Producto> lista;
        if (query != null && !query.trim().isEmpty()) {
            lista = productoService.buscarProductos(query).stream()
                    .filter(p -> "ACCESORIO".equals(p.getCategoria()))
                    .toList();
            model.addAttribute("query", query);
        } else {
            lista = productoService.listarProductosPorCategoria("ACCESORIO");
        }
        model.addAttribute("lista", lista);
        model.addAttribute("categoriaNombre", "Accesorios");
        model.addAttribute("propietarios", propietarioService.listarTodos());
        return "inventario/productos_cards";
    }

    @GetMapping("/insumos")
    public String listarInsumos(Model model, @RequestParam(value = "query", required = false) String query) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "insumos");
        List<Producto> lista;
        if (query != null && !query.trim().isEmpty()) {
            lista = productoService.buscarProductos(query).stream()
                    .filter(p -> "INSUMO_MEDICO".equals(p.getCategoria()))
                    .toList();
            model.addAttribute("query", query);
        } else {
            lista = productoService.listarProductosPorCategoria("INSUMO_MEDICO");
        }
        model.addAttribute("lista", lista);
        model.addAttribute("categoriaNombre", "Insumos Médicos");
        model.addAttribute("propietarios", propietarioService.listarTodos());
        return "inventario/productos_cards";
    }

    @GetMapping("/movimientos")
    public String listarMovimientos(Model model) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "movimientos");
        model.addAttribute("lista", movimientoService.listarMovimientos());
        return "inventario/movimientos";
    }

    @GetMapping("/vencimientos")
    public String listarVencimientos(Model model) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "vencimientos");
        
        model.addAttribute("porVencer", productoService.listarProductosPorVencer(90));
        model.addAttribute("vencidos", productoService.listarProductosVencidos());
        
        return "inventario/vencimientos";
    }

    @GetMapping("/reportes")
    public String reportesInventario(Model model) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("activeSubPage", "reportes");
        
        List<Producto> todos = productoService.listarProductos();
        long totalMedicamentos = todos.stream().filter(p -> "MEDICAMENTO".equals(p.getCategoria())).count();
        long totalAlimentos = todos.stream().filter(p -> "ALIMENTO".equals(p.getCategoria())).count();
        long totalAccesorios = todos.stream().filter(p -> "ACCESORIO".equals(p.getCategoria())).count();
        long totalInsumos = todos.stream().filter(p -> "INSUMO_MEDICO".equals(p.getCategoria())).count();

        model.addAttribute("todos", todos);
        model.addAttribute("medsCount", totalMedicamentos);
        model.addAttribute("alimsCount", totalAlimentos);
        model.addAttribute("accsCount", totalAccesorios);
        model.addAttribute("insCount", totalInsumos);
        model.addAttribute("criticos", productoService.listarStockCritico());
        model.addAttribute("vencidos", productoService.listarProductosVencidos());
        
        return "inventario/reportes";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("activePage", "inventario");
        model.addAttribute("producto", new Producto());
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "inventario/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto producto,
                          BindingResult bindingResult,
                          @RequestParam(value = "imagenFile", required = false) MultipartFile file,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "inventario");
            model.addAttribute("proveedores", proveedorService.listarProveedores());
            return "inventario/formulario";
        }

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                producto.setFotoUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        productoService.registrarProducto(producto);
        return "redirect:/inventario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Producto producto = productoService.obtenerProductoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        model.addAttribute("activePage", "inventario");
        model.addAttribute("producto", producto);
        model.addAttribute("proveedores", proveedorService.listarProveedores());
        return "inventario/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("producto") Producto producto,
                             BindingResult bindingResult,
                             @RequestParam(value = "imagenFile", required = false) MultipartFile file,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "inventario");
            model.addAttribute("proveedores", proveedorService.listarProveedores());
            return "inventario/formulario";
        }

        Producto existente = productoService.obtenerProductoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                producto.setFotoUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            producto.setFotoUrl(existente.getFotoUrl());
        }

        productoService.actualizarProducto(id, producto);
        return "redirect:/inventario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id) {
        productoService.eliminarProducto(id);
        return "redirect:/inventario";
    }

    @PostMapping("/reabastecer/{id}")
    public String reabastecer(@PathVariable("id") Long id,
                              @RequestParam("cantidad") Integer cantidad,
                              @RequestParam(value = "proveedorId", required = false) Long proveedorId,
                              @RequestParam(value = "precioCompra", required = false) Double precioCompra) {
        productoService.agregarStock(id, cantidad, proveedorId, precioCompra, "admin");
        return "redirect:/inventario";
    }

    @PostMapping("/vender-directo")
    public String venderDirecto(@RequestParam("productId") Long productId,
                                @RequestParam("propietarioId") Long propietarioId,
                                @RequestParam("cantidad") Integer cantidad) {
        Producto prod = productoService.obtenerProductoPorId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        double total = prod.getPrecioVenta() * cantidad;

        // Crear venta pendiente en caja
        ventaService.crearVentaPendiente(
            propietarioId,
            null, // Mascota opcional
            "Venta Directa: " + prod.getNombre() + " x" + cantidad,
            prod.getId(),
            "PRODUCTO",
            total
        );

        // Descontar stock directamente si se hace la venta
        // Nota: en VentaServiceImpl, el descuento ocurre cuando la venta se cobra (registraCobro).
        // Sin embargo, si es una Venta Directa que el cajero debe cobrar, se genera como pendiente.
        // El stock se descontará cuando se procese el cobro en Caja.
        return "redirect:/caja";
    }

    @GetMapping("/ficha/{id}")
    public String verFicha(@PathVariable("id") Long id, Model model) {
        Producto producto = productoService.obtenerProductoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        model.addAttribute("activePage", "inventario");
        model.addAttribute("p", producto);
        model.addAttribute("movimientos", movimientoService.listarMovimientosDeProducto(id, "PRODUCTO"));
        return "inventario/ficha";
    }
}
