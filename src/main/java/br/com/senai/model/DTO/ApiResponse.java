package br.com.senai.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private T content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private String message;

    public static <T> ApiResponse<T> ofContent(T content) {
        return new ApiResponse<>(content, null, null, null, null, null);
    }

    public static <T> ApiResponse<T> ofPage(
            T content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        return new ApiResponse<>(content, page, size, totalElements, totalPages, null);
    }
}
