package io.github.mateussilvadev.horizondesk.dto.response;

import java.util.UUID;

public final class DepartmentResponseDTOs {

    private DepartmentResponseDTOs() { }

    public record Response(UUID uuid, String name, boolean active) { }

    public record Options(UUID uuid, String name) { }
}
