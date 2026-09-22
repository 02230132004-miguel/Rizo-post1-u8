package com.example.auditoria.config;

import com.example.auditoria.usecase.CerrarHallazgoUseCase;
import com.example.auditoria.usecase.ConsultarHallazgoUseCase;
import com.example.auditoria.usecase.IniciarRemediacionUseCase;
import com.example.auditoria.usecase.ObtenerDashboardAuditoriaUseCase;
import com.example.auditoria.usecase.ReabrirHallazgoUseCase;
import com.example.auditoria.usecase.RegistrarHallazgoUseCase;
import com.example.auditoria.usecase.impl.CerrarHallazgoService;
import com.example.auditoria.usecase.impl.ConsultarHallazgoService;
import com.example.auditoria.usecase.impl.IniciarRemediacionService;
import com.example.auditoria.usecase.impl.ObtenerDashboardAuditoriaService;
import com.example.auditoria.usecase.impl.ReabrirHallazgoService;
import com.example.auditoria.usecase.impl.RegistrarHallazgoService;
import com.example.auditoria.usecase.port.HallazgoRepositoryPort;
import com.example.auditoria.usecase.port.HistorialAuditoriaPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Spring para la inyección de dependencias de los casos de uso.
 * Mantiene los servicios de aplicación completamente desacoplados de las anotaciones de Spring.
 */
@Configuration
public class AuditoriaConfiguration {

    @Bean
    public RegistrarHallazgoUseCase registrarHallazgoUseCase(HallazgoRepositoryPort hallazgoRepositoryPort) {
        return new RegistrarHallazgoService(hallazgoRepositoryPort);
    }

    @Bean
    public IniciarRemediacionUseCase iniciarRemediacionUseCase(
            HallazgoRepositoryPort hallazgoRepositoryPort,
            HistorialAuditoriaPort historialAuditoriaPort
    ) {
        return new IniciarRemediacionService(hallazgoRepositoryPort, historialAuditoriaPort);
    }

    @Bean
    public CerrarHallazgoUseCase cerrarHallazgoUseCase(
            HallazgoRepositoryPort hallazgoRepositoryPort,
            HistorialAuditoriaPort historialAuditoriaPort
    ) {
        return new CerrarHallazgoService(hallazgoRepositoryPort, historialAuditoriaPort);
    }

    @Bean
    public ReabrirHallazgoUseCase reabrirHallazgoUseCase(
            HallazgoRepositoryPort hallazgoRepositoryPort,
            HistorialAuditoriaPort historialAuditoriaPort
    ) {
        return new ReabrirHallazgoService(hallazgoRepositoryPort, historialAuditoriaPort);
    }

    @Bean
    public ConsultarHallazgoUseCase consultarHallazgoUseCase(
            HallazgoRepositoryPort hallazgoRepositoryPort,
            HistorialAuditoriaPort historialAuditoriaPort
    ) {
        return new ConsultarHallazgoService(hallazgoRepositoryPort, historialAuditoriaPort);
    }

    @Bean
    public ObtenerDashboardAuditoriaUseCase obtenerDashboardAuditoriaUseCase(HallazgoRepositoryPort hallazgoRepositoryPort) {
        return new ObtenerDashboardAuditoriaService(hallazgoRepositoryPort);
    }
}
