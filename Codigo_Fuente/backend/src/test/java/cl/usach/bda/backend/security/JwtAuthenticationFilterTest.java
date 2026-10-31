package cl.usach.bda.backend.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "clave-de-prueba-de-al-menos-32-bytes-para-hs256";
    private final JwtService jwtService = new JwtService(SECRET, 60000);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void verificar(String token, boolean autenticado) throws Exception {
        var request = new MockHttpServletRequest();
        if (token != null) {
            request.setCookies(new Cookie("access_token", token));
        }
        new JwtAuthenticationFilter(jwtService).doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (autenticado) {
                assertNotNull(authentication);
                assertEquals(7L, authentication.getPrincipal());
                assertTrue(authentication.isAuthenticated());
                assertTrue(authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_Operador")));
            } else {
                assertNull(authentication);
            }
        });
    }

    @Test
    void autenticaCookieValida() throws Exception {
        verificar(jwtService.generarToken(7L, "Operador"), true);
    }

    @Test
    void cookieAusenteNoAutentica() throws Exception {
        verificar(null, false);
    }

    @Test
    void rechazaTokenMalformado() throws Exception {
        verificar("token-invalido", false);
    }

    @Test
    void rechazaTokenExpirado() throws Exception {
        verificar(new JwtService(SECRET, -60000).generarToken(7L, "Operador"), false);
    }

    @Test
    void rechazaFirmaDeOtraClave() throws Exception {
        verificar(new JwtService(SECRET + "otra-clave", 60000).generarToken(7L, "Operador"), false);
    }
}
