package com.fatihdemir.diyetappbackend.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * Classpath'teki PKCS#8 PEM private key dosyasının yolu.
     */
    private String privateKeyPath;

    /**
     * Classpath'teki X.509 PEM public key dosyasının yolu.
     */
    private String publicKeyPath;

    /**
     * Access token geçerlilik süresi (ms). Varsayılan: 15 dakika.
     */
    private long accessTokenExpiration = 900_000L;

    /**
     * Refresh token geçerlilik süresi (ms). Varsayılan: 7 gün.
     */
    private long refreshTokenExpiration = 604_800_000L;

}
