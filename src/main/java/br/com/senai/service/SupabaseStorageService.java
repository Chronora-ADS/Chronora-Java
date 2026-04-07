package br.com.senai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    @Value("${supabase.storage-bucket}")
    private String bucketName;

    @Value("${supabase.service-role}")
    private String serviceRole;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadBase64Image(String base64Image, String folder, String userJwtToken) {
        return uploadBase64Image(base64Image, folder, userJwtToken, null);
    }

    public String uploadBase64Image(String base64Image, String folder, String userJwtToken, String fileTypeHint) {
        String[] partes = base64Image.split(",");
        String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
        byte[] imageBytes = Base64.getDecoder().decode(dadosBase64);

        String extension = guessExtension(base64Image, fileTypeHint);
        String fileName = folder + "/" + UUID.randomUUID() + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resolveMediaType(extension));
        headers.set("apikey", anonKey);
        headers.set("Authorization", "Bearer " + resolveToken(userJwtToken));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageBytes, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
        }

        throw new RuntimeException("Falha no upload da imagem: " + response.getStatusCode());
    }

    private String resolveToken(String userJwtToken) {
        if (userJwtToken != null && !userJwtToken.isEmpty()) {
            return userJwtToken;
        }
        return serviceRole;
    }

    private String guessExtension(String base64Image, String fileTypeHint) {
        if (fileTypeHint != null && !fileTypeHint.isBlank()) {
            String normalized = normalizeExtension(fileTypeHint);
            if (!normalized.isBlank()) {
                return "." + normalized;
            }
        }

        if (base64Image.startsWith("data:image/")) {
            int start = "data:image/".length();
            int end = base64Image.indexOf(";", start);
            if (end != -1) {
                return "." + normalizeExtension(base64Image.substring(start, end));
            }
        }

        return ".png";
    }

    private String normalizeExtension(String extension) {
        String normalized = extension.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        if ("jpeg".equals(normalized)) {
            return "jpg";
        }
        return normalized;
    }

    private MediaType resolveMediaType(String extension) {
        return switch (normalizeExtension(extension)) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.IMAGE_PNG;
        };
    }
}
