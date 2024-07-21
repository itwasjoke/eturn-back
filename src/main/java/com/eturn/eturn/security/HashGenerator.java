package com.eturn.eturn.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashGenerator {

    public static String generateSHA256Hash(String stringValue) {
        try {
            // Преобразуем строку в массив байтов
            byte[] bytes = stringValue.getBytes("UTF-8"); // Используем UTF-8 для кодирования строки

            // Получаем объект MessageDigest для алгоритма SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Вычисляем хеш
            byte[] hash = digest.digest(bytes);

            // Преобразуем хеш в строку в шестнадцатеричном формате
            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Не удалось получить алгоритм хеширования SHA-256", e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при кодировании строки в байты", e);
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