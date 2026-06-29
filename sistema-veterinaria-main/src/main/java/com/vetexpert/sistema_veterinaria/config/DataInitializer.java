package com.vetexpert.sistema_veterinaria.config;

import com.vetexpert.sistema_veterinaria.auth.model.Usuario;
import com.vetexpert.sistema_veterinaria.auth.repository.UsuarioRepository;
import com.vetexpert.sistema_veterinaria.vacunacion.model.Vacuna;
import com.vetexpert.sistema_veterinaria.vacunacion.repository.VacunaRepository;
import com.vetexpert.sistema_veterinaria.inventario.model.Proveedor;
import com.vetexpert.sistema_veterinaria.inventario.repository.ProveedorRepository;
import com.vetexpert.sistema_veterinaria.inventario.model.Producto;
import com.vetexpert.sistema_veterinaria.inventario.repository.ProductoRepository;
import com.vetexpert.sistema_veterinaria.servicios.model.Servicio;
import com.vetexpert.sistema_veterinaria.servicios.repository.ServicioRepository;
import com.vetexpert.sistema_veterinaria.promociones.model.Promocion;
import com.vetexpert.sistema_veterinaria.promociones.repository.PromocionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final VacunaRepository vacunaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final PromocionRepository promocionRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           VacunaRepository vacunaRepository,
                           ProveedorRepository proveedorRepository,
                           ProductoRepository productoRepository,
                           ServicioRepository servicioRepository,
                           PromocionRepository promocionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vacunaRepository = vacunaRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.promocionRepository = promocionRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Crear usuarios por defecto si no existen
        inicializarUsuarios();

        // 2. Inicializar catálogo de vacunas
        inicializarVacunas();

        // 3. Inicializar proveedores por defecto
        Proveedor proveedorDefault = inicializarProveedores();

        // 4. Inicializar catálogo de productos (Medicamentos y Alimentos)
        inicializarProductos(proveedorDefault);

        // 5. Inicializar catálogo de servicios
        inicializarServicios();

        // 6. Inicializar catálogo de promociones
        inicializarPromociones();
    }

    private void inicializarUsuarios() {
        // Usuario admin legacy (acceso por /auth/login)
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario("admin", "admin123", "ADMIN");
            admin.setNombre("Admin");
            admin.setApellido("Sistema");
            admin.setCorreo("admin_legacy@vetexperts.com");
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario administrador legacy creado: admin / admin123");
        }

        // Usuario veterinario demo
        if (!usuarioRepository.existsByUsername("veterinario")) {
            Usuario vet = new Usuario("veterinario", "vet123", "VETERINARIO");
            vet.setNombre("Dr. Veterinario");
            vet.setApellido("Demo");
            vet.setCorreo("veterinario@vetexperts.com");
            usuarioRepository.save(vet);
            System.out.println(">>> Usuario veterinario creado: veterinario / vet123");
        }

        // ✅ Administrador General por defecto (acceso por /admin/login con email)
        if (!usuarioRepository.existsByUsername("admin@vetexperts.com")) {
            String hashedPass = org.mindrot.jbcrypt.BCrypt.hashpw("Admin123*", org.mindrot.jbcrypt.BCrypt.gensalt());
            Usuario adminGeneral = new Usuario("admin@vetexperts.com", hashedPass, "ADMIN");
            adminGeneral.setNombre("Administrador");
            adminGeneral.setApellido("General");
            adminGeneral.setCorreo("admin@vetexperts.com");
            adminGeneral.setEnabled(true);
            usuarioRepository.save(adminGeneral);
            System.out.println(">>> Administrador General creado: admin@vetexperts.com / Admin123*");
        }

        // ✅ Secretaria demo
        if (!usuarioRepository.existsByUsername("secretaria@vetexperts.com")) {
            String hashedPass = org.mindrot.jbcrypt.BCrypt.hashpw("Sec123*", org.mindrot.jbcrypt.BCrypt.gensalt());
            Usuario secretaria = new Usuario("secretaria@vetexperts.com", hashedPass, "SECRETARIA");
            secretaria.setNombre("Secretaria");
            secretaria.setApellido("Demo");
            secretaria.setCorreo("secretaria@vetexperts.com");
            secretaria.setEnabled(true);
            usuarioRepository.save(secretaria);
            System.out.println(">>> Secretaria demo creada: secretaria@vetexperts.com / Sec123*");
        }
    }


    private void inicializarVacunas() {
        if (vacunaRepository.count() == 0) {
            vacunaRepository.save(new Vacuna("VAC-000001", "Rabia", "Zoetis", 45.0, "Vacuna obligatoria contra la rabia", 30, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000002", "Séxtuple", "MSD Animal Health", 65.0, "Previene parvovirosis, distemper, hepatitis, laringitis, parainfluenza y leptospirosis", 25, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000003", "Óctuple", "Virbac", 80.0, "Protección completa séxtuple + coronavirus + leptospira bacterina adicional", 20, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000004", "Triple Felina", "Boehringer Ingelheim", 55.0, "Protege contra rinotraqueitis, calicivirus y panleucopenia felina", 15, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000005", "Leucemia Felina", "Zoetis", 70.0, "Previene la leucemia viral felina", 10, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000006", "Parvovirus", "MSD Animal Health", 40.0, "Vacuna Puppy contra el parvovirus canino", 20, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000007", "Moquillo", "Agrovet Market", 35.0, "Vacuna preventiva contra el distemper canino", 15, 5, 12));
            vacunaRepository.save(new Vacuna("VAC-000008", "Otra", "Virbac", 50.0, "Otras vacunas específicas", 10, 5, 12));
            System.out.println(">>> Catálogo maestro de vacunas pre-poblado exitosamente.");
        }
    }

    private Proveedor inicializarProveedores() {
        if (proveedorRepository.count() == 0) {
            Proveedor prov = new Proveedor("20123456789", "Distribuidora Veterinaria FarmaVet S.A.", "Juan Pérez", "987654321", "contacto@farmavet.com.pe", "Av. Industrial 450, Lima", "Vacunas, Medicamentos y Reactivos");
            Proveedor prov2 = new Proveedor("20987654321", "Pet Food Corp del Perú", "Ana Gómez", "912345678", "ventas@petfood.com.pe", "Av. Evitamiento 1200, Ate", "Alimentos premium, snacks y accesorios");
            proveedorRepository.save(prov2);
            Proveedor saved = proveedorRepository.save(prov);
            System.out.println(">>> Proveedores por defecto pre-poblados.");
            return saved;
        }
        return proveedorRepository.findAll().get(0);
    }

    private void inicializarProductos(Proveedor prov) {
        if (productoRepository.count() == 0) {
            // Medicamentos
            productoRepository.save(new Producto("PROD-000001", "Amoxicilina 250mg", "MEDICAMENTO", 12.5, 20.0, 50, 10, "LOT-AMX-98", LocalDate.now().plusMonths(12), prov));
            productoRepository.save(new Producto("PROD-000002", "Enrofloxacina 50mg", "MEDICAMENTO", 15.0, 25.0, 40, 5, "LOT-ENR-22", LocalDate.now().plusMonths(15), prov));
            productoRepository.save(new Producto("PROD-000003", "Prednisolona 10mg", "MEDICAMENTO", 8.0, 15.0, 30, 5, "LOT-PRED-11", LocalDate.now().plusMonths(8), prov));
            productoRepository.save(new Producto("PROD-000004", "Meloxicam Gotas", "MEDICAMENTO", 18.0, 30.0, 25, 5, "LOT-MEL-04", LocalDate.now().plusMonths(6), prov));

            // Alimentos
            productoRepository.save(new Producto("PROD-000005", "Pro Plan Adult Perro 15kg", "ALIMENTO", 180.0, 240.0, 15, 2, "LOT-PP-AD", LocalDate.now().plusMonths(18), prov));
            productoRepository.save(new Producto("PROD-000006", "Cat Chow Adulto Gato 8kg", "ALIMENTO", 85.0, 120.0, 20, 3, "LOT-CC-AD", LocalDate.now().plusMonths(24), prov));
            productoRepository.save(new Producto("PROD-000007", "Royal Canin Mini Cachorro 3kg", "ALIMENTO", 90.0, 130.0, 12, 2, "LOT-RC-MINI", LocalDate.now().plusMonths(12), prov));
            productoRepository.save(new Producto("PROD-000008", "Cunipic Premium Conejo 1kg", "ALIMENTO", 22.0, 35.0, 10, 2, "LOT-CP-CN", LocalDate.now().plusMonths(10), prov));

            System.out.println(">>> Catálogo inicial de productos (medicamentos y alimentos) pre-poblado.");
        }
    }

    private void inicializarServicios() {
        if (servicioRepository.count() == 0) {
            servicioRepository.save(new Servicio("SERV-000001", "Consulta General", 40.0, "Consulta clínica de rutina para evaluación general"));
            servicioRepository.save(new Servicio("SERV-000002", "Baño", 30.0, "Baño sanitario y estético para la mascota"));
            servicioRepository.save(new Servicio("SERV-000003", "Peluquería", 50.0, "Corte de pelo estético y arreglo general"));
            servicioRepository.save(new Servicio("SERV-000004", "Desparasitación Interna", 25.0, "Administración de antiparasitario oral"));
            servicioRepository.save(new Servicio("SERV-000005", "Desparasitación Externa", 30.0, "Aplicación de antiparasitario pipeta/pastilla"));
            servicioRepository.save(new Servicio("SERV-000006", "Hospitalización por día", 80.0, "Cuidado médico e internamiento las 24 horas"));
            System.out.println(">>> Catálogo maestro de servicios pre-poblado.");
        }
    }

    private void inicializarPromociones() {
        if (promocionRepository.count() == 0) {
            promocionRepository.save(new Promocion(
                "Campaña de Vacunación Anual", 
                "Protege a tu mascota contra las principales enfermedades infecciosas. Obtén 20% de descuento en la vacuna Óctuple y Séxtuple.", 
                "20% OFF", 
                "/img/promo_vaccination.jpg", 
                LocalDate.now().minusDays(5), 
                LocalDate.now().plusMonths(2)
            ));
            promocionRepository.save(new Promocion(
                "Spa & Peluquería Canina", 
                "Regálale un día de mimos a tu engreído. Baño premium + corte estético de pelo + corte de uñas con un precio especial de introducción.", 
                "S/ 39.90", 
                "/img/promo_grooming.jpg", 
                LocalDate.now().minusDays(2), 
                LocalDate.now().plusMonths(1)
            ));
            promocionRepository.save(new Promocion(
                "Chequeo Geriátrico Preventivo", 
                "Para mascotas mayores de 7 años. Análisis de sangre completo, descarte de problemas renales y consulta médica con 30% de descuento.", 
                "30% OFF", 
                "/img/promo_checkup.jpg", 
                LocalDate.now().minusDays(1), 
                LocalDate.now().plusWeeks(3)
            ));
            System.out.println(">>> Catálogo inicial de promociones pre-poblado exitosamente.");
        }
    }
}
