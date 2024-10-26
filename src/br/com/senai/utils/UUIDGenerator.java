package br.com.senai.utils;
import java.util.UUID;

public class UUIDGenerator {
    public static String generateUUID() {
        return String.valueOf(UUID.randomUUID());
    }
}
