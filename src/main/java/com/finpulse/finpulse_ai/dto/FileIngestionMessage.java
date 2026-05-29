package com.finpulse.finpulse_ai.dto;

public record FileIngestionMessage(
        String filePath,
        String originalFilename,
        String chunkType
) {}
