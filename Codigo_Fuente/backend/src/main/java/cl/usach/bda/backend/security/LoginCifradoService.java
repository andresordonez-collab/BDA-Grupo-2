package cl.usach.bda.backend.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginCifradoService {

    private static final long VENTANA_MS = 120_000;
    private static final int LARGO_MAXIMO_NONCE = 64;

    private final KeyPair claves;
    private final ConcurrentHashMap<String, Long> nonceUsados = new ConcurrentHashMap<>();

    public LoginCifradoService() {
        try {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(2048);
            this.claves = generador.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar el par de claves RSA.", e);
        }
    }

    // Tarea 2: Clave pública RSA (formato X.509 en Base64) con la que el navegador cifra la contraseña.
    public String clavePublicaBase64() {
        return Base64.getEncoder().encodeToString(claves.getPublic().getEncoded());
    }

    // Tarea 2: Descifra la contraseña recibida con formato "nonce|marca de tiempo|contraseña".
    // Devuelve vacío si no se puede descifrar, está vencida o ya fue usada (evita reenviar un valor capturado).
    public Optional<String> descifrarPassword(String cifrado) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.DECRYPT_MODE, claves.getPrivate(),
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
            String texto = new String(cipher.doFinal(Base64.getDecoder().decode(cifrado)), StandardCharsets.UTF_8);

            String[] partes = texto.split("\\|", 3);
            if (partes.length != 3) {
                return Optional.empty();
            }

            String nonce = partes[0];
            long marca = Long.parseLong(partes[1]);
            long ahora = System.currentTimeMillis();
            if (nonce.isBlank() || nonce.length() > LARGO_MAXIMO_NONCE || Math.abs(ahora - marca) > VENTANA_MS) {
                return Optional.empty();
            }
            if (!registrarNonce(nonce, ahora)) {
                return Optional.empty();
            }
            return Optional.of(partes[2]);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private boolean registrarNonce(String nonce, long ahora) {
        nonceUsados.values().removeIf(instante -> ahora - instante > VENTANA_MS);
        return nonceUsados.putIfAbsent(nonce, ahora) == null;
    }
}
