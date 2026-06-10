package com.proyecto.hce_backend.controller;

import com.proyecto.hce_backend.model.Cita;
import com.proyecto.hce_backend.repository.AtencionMedicaRepository;
import com.proyecto.hce_backend.repository.CitaRepository;
import com.proyecto.hce_backend.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "https://hce-system.vercel.app")
public class DashboardController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private AtencionMedicaRepository atencionMedicaRepository;

    @GetMapping("/medico/{id}")
    public Map<String, Object> dashboardMedico(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        LocalDate hoy = LocalDate.now();

        List<Cita> citasDelDia = citaRepository.findByFecha(hoy);

        response.put("totalPacientes", pacienteRepository.count());
        response.put("citasHoy", citasDelDia.size());
        response.put("totalCitasMedico", citaRepository.findByMedicoId(id).size());
        response.put("totalAtenciones", atencionMedicaRepository.count());
        response.put("porcentajeInasistencia", 0);

        response.put("citasDelDia", citasDelDia);

        return response;
    }
}
