package com.fatihdemir.diyetappbackend.config;

import com.fatihdemir.diyetappbackend.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class RsaKeyConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public KeyPair rsaKeyPair() throws Exception {
        String privatePath = jwtProperties.getPrivateKeyPath();
        String publicPath  = jwtProperties.getPublicKeyPath();

        if (privatePath != null && !privatePath.isBlank()) {
            return loadFromPemFiles(privatePath, publicPath);
        }
        return generateAndLogKeyPair();
    }

    private KeyPair loadFromPemFiles(String privatePath, String publicPath) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(decodePem(readClasspath(privatePath)))
        );
        PublicKey publicKey = keyFactory.generatePublic(
                new X509EncodedKeySpec(decodePem(readClasspath(publicPath)))
        );

        log.info("RSA anahtarları yüklendi: {}, {}", privatePath, publicPath);
        return new KeyPair(publicKey, privateKey);
    }

    private String readClasspath(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        );
    }

    private byte[] decodePem(String pem) {
        String base64 = pem
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    private KeyPair generateAndLogKeyPair() throws Exception {
        log.warn("RSA PEM dosyası bulunamadı. Yeni anahtar çifti üretiliyor (yalnızca geliştirme ortamı).");

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
}