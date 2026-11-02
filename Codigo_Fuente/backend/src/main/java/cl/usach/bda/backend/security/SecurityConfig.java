package cl.usach.bda.backend.security;

import cl.usach.bda.backend.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tarea 2: Reglas de acceso por rol. Los usuarios inician y finalizan sus viajes y ven su historial;
    // los operadores administran la flota y las estaciones.
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtService jwtService,
            UsuarioRepository usuarioRepository) throws Exception {
        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/clave-publica", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/viajes",
                                "/api/viajes/*/finalizar").hasRole("Usuario")
                        .requestMatchers(HttpMethod.GET, "/api/viajes/resumen-mensual",
                                "/api/viajes/mis-viajes").hasRole("Usuario")
                        .requestMatchers(HttpMethod.GET, "/api/viajes/resumen-mensual",
                                "/api/viajes/mis-viajes", "/api/billeteras/mi-billetera").hasRole("Usuario")
                        .requestMatchers(HttpMethod.GET, "/api/estaciones/**", "/api/tarifas/**",
                                "/api/tipos-vehiculos/**").authenticated()
                        .requestMatchers("/api/estaciones/**", "/api/tarifas/**", "/api/tipos-vehiculos/**",
                                "/api/roles/**", "/api/rebalanceos/**").hasRole("Operador")
                        .requestMatchers(
                                "/api/usuarios/**",
                                "/api/vehiculos/**",
                                "/api/billeteras/**",
                                "/api/movimientos-billeteras/**",
                                "/api/viajes/**",
                                "/api/ordenes-rebalanceo/**",
                                "/api/detalles-rebalanceo/**",
                                "/api/auditorias-vehiculos/**"
                        ).hasRole("Operador")
                        .requestMatchers("/api/reportes/**").hasRole("Operador")
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
