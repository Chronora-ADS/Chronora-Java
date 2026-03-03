package br.com.senai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Faz upload de uma imagem codificada em Base64 e retorna a URL pública.
     * @param base64Image A string Base64 da imagem (pode conter prefixo data:image/...)
     * @param folder Subpasta dentro do bucket (ex: "users", "services")
     */
    public String uploadBase64Image(String base64Image, String folder, String userJwtToken) {
        // 1. Extrair os dados Base64 (remover prefixo data:image/... se existir)
        String[] partes = base64Image.split(",");
        String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
        byte[] imageBytes = Base64.getDecoder().decode(dadosBase64);

        // 2. Gerar nome único para o arquivo (UUID + extensão)
        String extension = guessExtension(base64Image); // você pode implementar ou fixar .png
        String fileName = folder + "/" + UUID.randomUUID().toString() + extension;

        // 3. Fazer upload via API REST do Supabase Storage
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); // Ajuste conforme o tipo real
        headers.set("apikey", anonKey);

        String tokenToUse;
        if (userJwtToken != null && !userJwtToken.isEmpty()) {
            // Se temos um token do usuário logado, usamos ele
            tokenToUse = userJwtToken;
        } else {
            // Operação interna (sem usuário logado), usamos a service role key
            tokenToUse = serviceRole;
        }

        headers.set("Authorization", "Bearer " + tokenToUse);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageBytes, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            // 4. Retornar URL pública
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
        } else {
            throw new RuntimeException("Falha no upload da imagem: " + response.getStatusCode());
        }
    }

    /**
     * Adivinha a extensão do arquivo a partir do prefixo Base64 (ex: "data:image/png;base64,")
     * Se não conseguir, retorna ".png" como padrão.
     */
    private String guessExtension(String base64Image) {
        if (base64Image.startsWith("data:image/")) {
            int start = "data:image/".length();
            int end = base64Image.indexOf(";", start);
            if (end != -1) {
                String type = base64Image.substring(start, end);
                return "." + type; // "png", "jpeg", etc.
            }
        }
        return ".png"; // padrão
    }
}