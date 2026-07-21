package io.github.mateussilvadev.horizondesk.dto.response;

import java.util.UUID;

public final class DepartmentResponseDTOs {

    private DepartmentResponseDTOs() { }

    public record DepartmentResponse(UUID uuid, String name, boolean active) { }

    public record DepartmentOptions(UUID uuid, String name) { }
}
