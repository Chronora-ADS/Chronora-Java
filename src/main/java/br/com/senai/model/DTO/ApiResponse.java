package br.com.senai.model.DTO;

public class ApiResponse<T> {
    private T content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(T content, Integer page, Integer size, Long totalElements, Integer totalPages, String message) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.message = message;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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
