package com.vetexpert.sistema_veterinaria.agenda.service.impl;

import com.vetexpert.sistema_veterinaria.agenda.dto.CitaDTO;
import com.vetexpert.sistema_veterinaria.agenda.dto.ResumenMascotaDTO;
import com.vetexpert.sistema_veterinaria.agenda.entity.Cita;
import com.vetexpert.sistema_veterinaria.agenda.entity.EstadoCita;
import com.vetexpert.sistema_veterinaria.agenda.repository.CitaRepository;
import com.vetexpert.sistema_veterinaria.agenda.service.CitaService;
import com.vetexpert.sistema_veterinaria.mascotas.entity.Mascota;
import com.vetexpert.sistema_veterinaria.mascotas.repository.MascotaRepository;
import com.vetexpert.sistema_veterinaria.propietarios.entity.Propietario;
import com.vetexpert.sistema_veterinaria.propietarios.repository.PropietarioRepository;
import com.vetexpert.sistema_veterinaria.historias.entity.HistoriaClinica;
import com.vetexpert.sistema_veterinaria.historias.repository.HistoriaClinicaRepository;
import com.vetexpert.sistema_veterinaria.vacunas.entity.VacunaAplicada;
import com.vetexpert.sistema_veterinaria.vacunas.repository.VacunaAplicadaRepository;
import com.vetexpert.sistema_veterinaria.desparasitaciones.entity.Desparasitacion;
import com.vetexpert.sistema_veterinaria.desparasitaciones.repository.DesparasitacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final VacunaAplicadaRepository vacunaAplicadaRepository;
    private final DesparasitacionRepository desparasitacionRepository;

    public CitaServiceImpl(CitaRepository citaRepository,
                           MascotaRepository mascotaRepository,
                           PropietarioRepository propietarioRepository,
                           HistoriaClinicaRepository historiaClinicaRepository,
                           VacunaAplicadaRepository vacunaAplicadaRepository,
                           DesparasitacionRepository desparasitacionRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.propietarioRepository = propietarioRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
        this.desparasitacionRepository = desparasitacionRepository;
    }

    @Override
    public Cita registrarCita(CitaDTO dto) {
        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));
        
        Propietario propietario = mascota.getPropietario();

        // 1. Validar que la fecha no sea pasada
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede programar una cita para una fecha pasada.");
        }

        // 2. Resolver duración estándar según el tipo de cita
        int duracion = resolverDuracionEstandar(dto.getTipoCita(), dto.getDuracionMinutos());
        dto.setDuracionMinutos(duracion);

        // 2. Validar colisiones horarias para el veterinario
        validarColision(dto.getFecha(), dto.getHora(), duracion, dto.getVeterinario(), null);

        Cita cita = new Cita();
        cita.setCodigoCita(generarSiguienteCodigo());
        cita.setMascota(mascota);
        cita.setPropietario(propietario);
        cita.setVeterinario(dto.getVeterinario());
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setDuracionMinutos(duracion);
        cita.setTipoCita(dto.getTipoCita());
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoCita.PROGRAMADA);
        cita.setPrioridad(dto.getPrioridad());
        cita.setObservaciones(dto.getObservaciones());
        cita.setRecordatorioProgramado(dto.isRecordatorioProgramado());

        return citaRepository.save(cita);
    }

    @Override
    public Cita actualizarCita(Long id, CitaDTO dto) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + dto.getMascotaId()));
        
        Propietario propietario = mascota.getPropietario();

        // Validar que la fecha no sea pasada
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede programar una cita para una fecha pasada.");
        }

        int duracion = resolverDuracionEstandar(dto.getTipoCita(), dto.getDuracionMinutos());
        dto.setDuracionMinutos(duracion);

        // Validar colisiones excluyendo la cita actual
        validarColision(dto.getFecha(), dto.getHora(), duracion, dto.getVeterinario(), id);

        cita.setMascota(mascota);
        cita.setPropietario(propietario);
        cita.setVeterinario(dto.getVeterinario());
        cita.setFecha(dto.getFecha());
        cita.setHora(dto.getHora());
        cita.setDuracionMinutos(duracion);
        cita.setTipoCita(dto.getTipoCita());
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        if (dto.getEstado() != null) {
            cita.setEstado(dto.getEstado());
        }
        cita.setPrioridad(dto.getPrioridad());
        cita.setObservaciones(dto.getObservaciones());
        cita.setRecordatorioProgramado(dto.isRecordatorioProgramado());

        return citaRepository.save(cita);
    }

    @Override
    public void eliminarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cita> obtenerPorId(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }

    @Override
    public Cita cambiarEstado(Long id, String estado) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));
        cita.setEstado(EstadoCita.valueOf(estado));
        return citaRepository.save(cita);
    }

    @Override
    public Cita reprogramarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        if (nuevaFecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede programar una cita para una fecha pasada.");
        }

        validarColision(nuevaFecha, nuevaHora, cita.getDuracionMinutos(), cita.getVeterinario(), id);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REPROGRAMADA);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenMascotaDTO obtenerResumenMascota(Long mascotaId) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada con ID: " + mascotaId));

        Propietario propietario = mascota.getPropietario();
        ResumenMascotaDTO res = new ResumenMascotaDTO();

        // Datos del Propietario
        res.setPropietarioNombre(propietario.getNombre() + " " + propietario.getApellido());
        res.setPropietarioDni(propietario.getDni());
        res.setPropietarioTelefono(propietario.getTelefono());
        res.setPropietarioCorreo(propietario.getCorreo());

        // Lógica clínica
        List<HistoriaClinica> historias = historiaClinicaRepository.buscarPorMascota(mascotaId);
        if (!historias.isEmpty()) {
            res.setTotalVisitas(historias.size());
            // Ordenar por fecha de consulta desc
            historias.stream()
                    .max(Comparator.comparing(HistoriaClinica::getFechaConsulta))
                    .ifPresent(h -> res.setUltimaConsulta(h.getFechaConsulta()));
        }

        List<VacunaAplicada> vacunas = vacunaAplicadaRepository.buscarPorMascotaId(mascotaId);
        if (!vacunas.isEmpty()) {
            vacunas.stream()
                    .max(Comparator.comparing(VacunaAplicada::getFechaAplicacion))
                    .ifPresent(v -> {
                        res.setUltimaVacuna(v.getFechaAplicacion());
                        res.setProximaVacuna(v.getFechaProximaDosis());
                    });
        }

        List<Desparasitacion> desps = desparasitacionRepository.buscarPorMascotaId(mascotaId);
        if (!desps.isEmpty()) {
            desps.stream()
                    .max(Comparator.comparing(Desparasitacion::getFechaAplicacion))
                    .ifPresent(d -> res.setUltimaDesparasitacion(d.getFechaAplicacion()));
        }

        res.setEstadoGeneral("Activa (Estable)"); // Por defecto
        res.setMascotaEspecie(mascota.getEspecie().name());
        return res;
    }

    private int resolverDuracionEstandar(com.vetexpert.sistema_veterinaria.agenda.entity.TipoCita tipo, int duracionPropuesta) {
        if (tipo == null) return 30;
        return switch (tipo) {
            case CONSULTA_GENERAL -> 30;
            case VACUNACION -> 15;
            case DESPARASITACION -> 15;
            case CONTROL -> 20;
            case PELUQUERIA -> 60;
            case HIGIENE_DENTAL -> 30;
            default -> duracionPropuesta > 0 ? duracionPropuesta : 30;
        };
    }

    @Override
    public void validarColision(LocalDate fecha, LocalTime hora, int duracion, String veterinario, Long excludeId) {
        LocalTime start = hora;
        LocalTime end = hora.plusMinutes(duracion);

        List<Cita> citas = citaRepository.findByFechaAndVeterinario(fecha, veterinario);
        for (Cita c : citas) {
            if (excludeId != null && c.getId().equals(excludeId)) {
                continue;
            }
            LocalTime existingStart = c.getHora();
            LocalTime existingEnd = c.getHora().plusMinutes(c.getDuracionMinutos());

            // Overlap check
            if (start.isBefore(existingEnd) && end.isAfter(existingStart)) {
                throw new IllegalArgumentException("El veterinario " + veterinario + 
                        " ya tiene una cita de " + c.getTipoCita() + 
                        " programada de " + existingStart + " a " + existingEnd + ".");
            }
        }
    }

    private synchronized String generarSiguienteCodigo() {
        String maxCodigo = citaRepository.findMaxCodigoCita();
        if (maxCodigo == null || !maxCodigo.startsWith("CITA-")) {
            return "CITA-000001";
        }
        try {
            int numero = Integer.parseInt(maxCodigo.substring(5));
            return String.format("CITA-%06d", numero + 1);
        } catch (NumberFormatException e) {
            return "CITA-000001";
        }
    }
}
