package br.dev.matheushenrique.floccus.dto;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp,
        String path
) {}