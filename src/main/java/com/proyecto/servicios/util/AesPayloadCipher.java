package com.proyecto.servicios.util;

import com.proyecto.servicios.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AesPayloadCipher {

    private final SecretKey secretKey;
    private final IvParameterSpec ivSpec;

    public AesPayloadCipher(@Value("${gestopago.crypto.aes-secret-key:}") String base64Key,
                            @Value("${gestopago.crypto.aes-iv:}") String base64Iv) {
        if (base64Key != null && !base64Key.isBlank() && base64Iv != null && !base64Iv.isBlank()) {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            byte[] ivBytes = Base64.getDecoder().decode(base64Iv.trim());
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            this.ivSpec = new IvParameterSpec(ivBytes);
        } else {
            // Claves por defecto para pruebas si no están inyectadas
            byte[] defaultKey = "12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8);
            byte[] defaultIv = "1234567890123456".getBytes(StandardCharsets.UTF_8);
            this.secretKey = new SecretKeySpec(defaultKey, "AES");
            this.ivSpec = new IvParameterSpec(defaultIv);
        }
    }

    public String cifrarJson(String jsonPlano) {
        if (jsonPlano == null) {
            throw new IllegalArgumentException("El contenido JSON no puede ser nulo");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(jsonPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BusinessRuleException("Error criptográfico al cifrar el payload de la transacción: " + e.getMessage());
        }
    }

    public String descifrarJson(String base64Cifrado) {
        if (base64Cifrado == null) {
            throw new IllegalArgumentException("El texto cifrado no puede ser nulo");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(base64Cifrado.trim()));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessRuleException("Error criptográfico al descifrar el payload: " + e.getMessage());
        }
    }
}

