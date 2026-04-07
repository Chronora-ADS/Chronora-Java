package br.com.senai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
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

    @Value("${server.port:8085}")
    private String serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadBase64Image(String base64Image, String folder, String userJwtToken) {
        if (!isSupabaseConfigured()) {
            return "http://localhost:" + serverPort + "/assets/fundo.jpg";
        }

        String[] parts = base64Image.split(",");
        String base64Data = (parts.length > 1) ? parts[1] : parts[0];
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        String extension = guessExtension(base64Image);
        String fileName = folder + "/" + UUID.randomUUID() + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set("apikey", anonKey);

        String tokenToUse = StringUtils.hasText(userJwtToken) ? userJwtToken : serviceRole;
        headers.set("Authorization", "Bearer " + tokenToUse);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageBytes, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
        }

        throw new RuntimeException("Falha no upload da imagem: " + response.getStatusCode());
    }

    private String guessExtension(String base64Image) {
        if (base64Image.startsWith("data:image/")) {
            int start = "data:image/".length();
            int end = base64Image.indexOf(";", start);
            if (end != -1) {
                String type = base64Image.substring(start, end);
                return "." + type;
            }
        }
        return ".png";
    }

    private boolean isSupabaseConfigured() {
        return StringUtils.hasText(supabaseUrl)
                && StringUtils.hasText(anonKey)
                && StringUtils.hasText(serviceRole);
    }
}
