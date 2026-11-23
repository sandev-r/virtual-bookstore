package com.sandev.virtualbookstoreapp.security;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {
    private KeyUtils() {}
    public static PrivateKey loadPrivateKey(final String path)throws Exception {
        final String key = readKeyFromResources(path);
        final String realKey = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+","");
        final byte[] decodedKey = Base64.getDecoder().decode(realKey);
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
    public static PublicKey loadPublicKey(final String path)throws Exception {
        final String key = readKeyFromResources(path);
        final String realKey = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+","");
        final byte[] decodedKey = Base64.getDecoder().decode(realKey);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String readKeyFromResources(final String path) throws Exception {
        try (InputStream inputStream = KeyUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new FileNotFoundException(path);
            }
            return new String(inputStream.readAllBytes());
        }
    }
}
