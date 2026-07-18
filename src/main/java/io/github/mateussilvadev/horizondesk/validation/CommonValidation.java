package io.github.mateussilvadev.horizondesk.validation;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;

public class CommonValidation {

    public static <T> T required(T value, String field) {
        if (value == null || value.toString().isBlank()) {
            throw new BusinessException(Code.VALIDATION_ERROR, "validation.field_required", new Object[]{field});
        }
        return value;
    }

    public static String requiredText(String value, String field, int min) {
        String normalized = (value == null) ? "" : value.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank() || normalized.length() < min)
            throw new BusinessException(Code.VALIDATION_ERROR, "validation.field_min_length", new Object[]{field, min});
        return normalized;
    }

}
