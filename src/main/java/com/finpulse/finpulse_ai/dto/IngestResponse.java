package com.finpulse.finpulse_ai.dto;

public record IngestResponse(
        int chunksStored,
        String sourceFile
) {}