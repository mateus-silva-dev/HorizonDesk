package io.github.mateussilvadev.horizondesk.model.enums;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Map;

@Getter
public enum Role {
    ROLE_ADMIN("ADMIN"),
    ROLE_CUSTOMER("CUSTOMER"),
    ROLE_TECHNICIAN("TECHNICIAN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    private static final Map<Role, EnumSet<Role>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = Map.of(
                ROLE_CUSTOMER, EnumSet.of(ROLE_TECHNICIAN, ROLE_ADMIN),
                ROLE_TECHNICIAN, EnumSet.of(ROLE_CUSTOMER, ROLE_ADMIN),
                ROLE_ADMIN, EnumSet.of(ROLE_CUSTOMER, ROLE_TECHNICIAN)
        );
    }

    public void transitionTo(Role nextRole) {
        if (nextRole == null)
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.role_required");

        EnumSet<Role> allowedTargets = VALID_TRANSITIONS.get(this);

        if (allowedTargets == null || !allowedTargets.contains(nextRole)) {
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.invalid_role_transition");
        }
    }
}
