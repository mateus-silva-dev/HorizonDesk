package io.github.mateussilvadev.horizondesk.dto.response;

import java.util.UUID;

public record DepartmentResponseDTO(
        UUID uuid,
        String name,
        boolean active
) { }
