package io.github.mateussilvadev.horizondesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class DepartmentRequestDTOs {

    private DepartmentRequestDTOs() { }

    public record DepartmentCreate(
            @NotNull(message = "Name is required.")
            @NotBlank(message = "Name is required.")
            @Size(min = 2, max = 50, message = "The name must contain at least 2 characters.")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9\\s]*$", message = "Invalid characters in name.")
            String name
    ) { }

    public record DepartmentUpdate(
            @NotNull(message = "Name is required.")
            @NotBlank(message = "Name is required.")
            @Size(min = 2, max = 50, message = "The name must contain at least 2 characters.")
            @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9\\s]*$", message = "Invalid characters in name.")
            String name
    ) { }

}
