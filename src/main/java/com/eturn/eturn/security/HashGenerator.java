package com.eturn.eturn.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {

    public static String generateSHA256Hash(long longValue) {
        try {
            // Преобразуем long в массив байтов
            byte[] bytes = BigInteger.valueOf(longValue).toByteArray();

            // Получаем объект MessageDigest для алгоритма SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Вычисляем хеш
            byte[] hash = digest.digest(bytes);

            // Преобразуем хеш в строку в шестнадцатеричном формате
            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Не удалось получить алгоритм хеширования SHA-256", e);
        }
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}