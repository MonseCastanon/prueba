package com.proyecto.servicios.service;

import com.proyecto.servicios.util.AesPayloadCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AesPayloadCipherTest {

    private AesPayloadCipher cipher;

    @BeforeEach
    void setUp() {
        // Llave de 32 bytes (AES-256) e IV de 16 bytes en Base64
        String keyBase64 = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        String ivBase64 = Base64.getEncoder().encodeToString("1234567890123456".getBytes());
        cipher = new AesPayloadCipher(keyBase64, ivBase64);
    }

    @Test
    @DisplayName("Cifrado y descifrado de JSON recupera exactamente el contenido plano")
    void testCifradoYDescifradoExitoso() {
        String jsonOriginal = "{\"idProducto\":101,\"idServicio\":5,\"upc\":\"U000000100001\",\"montoPago\":150.00}";

        String cifradoBase64 = cipher.cifrarJson(jsonOriginal);
        assertNotNull(cifradoBase64);
        assertNotEquals(jsonOriginal, cifradoBase64);

        String descifrado = cipher.descifrarJson(cifradoBase64);
        assertEquals(jsonOriginal, descifrado);
    }

    @Test
    @DisplayName("Cifrado falla cuando el texto es nulo")
    void testCifrarNulo() {
        assertThrows(IllegalArgumentException.class, () -> cipher.cifrarJson(null));
    }

    @Test
    @DisplayName("Descifrado falla cuando el texto es nulo")
    void testDescifrarNulo() {
        assertThrows(IllegalArgumentException.class, () -> cipher.descifrarJson(null));
    }
}

