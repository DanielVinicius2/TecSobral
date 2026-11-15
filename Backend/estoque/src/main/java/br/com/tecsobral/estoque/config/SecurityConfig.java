package br.com.tecsobral.estoque.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.tecsobral.estoque.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())                       // NOVO: libera o front-end (ver corsConfigurationSource)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()          // NOVO: pré-requisição do navegador (CORS)
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() // NOVO: a tela de login é pública
                        .anyRequest().authenticated())
                // NOVO: responde 401 sem o cabeçalho WWW-Authenticate, para o navegador não abrir a janelinha de login
                .httpBasic(b -> b.authenticationEntryPoint(
                        (request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return login -> usuarios.findByLoginIgnoreCase(login)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .map(u -> User.withUsername(u.getLogin()).password(u.getSenhaHash()).authorities("ROLE_" + u.getPerfil().name()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário ou senha inválidos."));
    }

    /** NOVO: origens do front-end permitidas. Para mudar, defina app.cors.origins no application.properties. */
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origins:http://localhost:5173,http://localhost:3000}") String[] origens) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.asList(origens));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setExposedHeaders(List.of("Content-Disposition"));  // para o front ler o nome do arquivo do CSV
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}