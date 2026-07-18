package io.github.mateussilvadev.horizondesk.dto.response;

import io.github.mateussilvadev.horizondesk.model.enums.StatusUser;

import java.util.UUID;

public final class UserResponseDTOs {

    private UserResponseDTOs() { }

    public record Response(
            UUID uuid,
            String name,
            String email,
            String role,
            String department,
            String status) { }

    public record TechnicianOption(UUID uuid, String name, StatusUser status) { }
}
