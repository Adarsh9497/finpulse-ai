package com.finpulse.finpulse_ai.dto;

public record IngestRequest(
        String text,
        String sourceFile,
        int pageNumber,
        String chunkType   // "TEXT" or "TABLE"
) {}