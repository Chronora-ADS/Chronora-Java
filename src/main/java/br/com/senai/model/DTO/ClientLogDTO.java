package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotBlank;

public class ClientLogDTO {
    @NotBlank
    private String level;
    @NotBlank
    private String source;
    @NotBlank
    private String message;
    private String stackTrace;
    private String platform;
    private Boolean isReleaseMode;
    private String timestamp;
    private java.util.Map<String, Object> context;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Boolean getIsReleaseMode() {
        return isReleaseMode;
    }

    public void setIsReleaseMode(Boolean isReleaseMode) {
        this.isReleaseMode = isReleaseMode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public java.util.Map<String, Object> getContext() {
        return context;
    }

    public void setContext(java.util.Map<String, Object> context) {
        this.context = context;
    }
}
