package io.github.mateussilvadev.horizondesk.dto.request;

import io.github.mateussilvadev.horizondesk.model.enums.Role;
import jakarta.validation.constraints.*;

import java.util.UUID;

public final class UserRequestDTOs {

    private UserRequestDTOs() { }

    public record Create(
            @NotNull(message = "Name is required.")
            @NotBlank(message = "Name is required.")
            @Size(min = 3, max = 50, message = "The name must contain at least 3 characters.")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9\\s]*$", message = "Invalid characters in name.")
            String name,

            @NotNull(message = "Email is required.")
            @NotBlank(message = "Email is required.")
            @Email(message = "Invalid email format.")
            String email,

            @NotNull(message = "Password is required.")
            @NotBlank(message = "Password is required.")
            @Size(min = 8, max = 20, message = "The password must contain at least 8 characters.")
            String password,

            @NotNull(message = "Department is required.")
            UUID departmentUuid) { }

    public record Update(
            @Size(min = 3, max = 50, message = "The name must contain at least 3 characters.")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9\\s]*$", message = "Invalid characters in name.")
            String name,

            @Email(message = "Invalid email format.")
            String email) { }

    public record UpdatePassword(
            @NotNull(message = "Current password is required.")
            @NotBlank(message = "Current password is required.")
            @Size(min = 8, max = 20, message = "Password must be 8 characters long.")
            String currentPassword,

            @NotNull(message = "New password is required.")
            @NotBlank(message = "New password is required.")
            @Size(min = 8, max = 20, message = "Password must be 8 characters long.")
            String newPassword,

            @NotNull(message = "Confirmation of the new password is required.")
            @NotBlank(message = "Confirmation of the new password is required.")
            @Size(min = 8, max = 20, message = "Password must be 8 characters long.")
            String confirmPassword) { }

    public record ChangeRole(Role role) { }

    public record ChangeDepartment(UUID departmentUuid) { }

}
