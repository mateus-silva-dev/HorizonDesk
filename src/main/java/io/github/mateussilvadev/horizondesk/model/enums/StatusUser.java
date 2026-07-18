package io.github.mateussilvadev.horizondesk.model.enums;

import io.github.mateussilvadev.horizondesk.exception.BusinessException;
import io.github.mateussilvadev.horizondesk.exception.Code;

import java.util.EnumSet;
import java.util.Map;

public enum StatusUser {
    ACTIVE("Active") {
        @Override
        public boolean canLogIn() { return true; }
    },

    DISABLED("Disabled") {
        @Override
        public boolean canLogIn() { return false; }
    },

    PENDING_EXCLUSION("Pending Exclusion") {
        @Override
        public boolean canLogIn() { return false; }
    },

    EXCLUDED("Excluded") {
        @Override
        public boolean canLogIn() { return false; }
    };

    private final String value;

    StatusUser(String value) {
        this.value = value;
    }

    public abstract boolean canLogIn();

    private static final Map<StatusUser, EnumSet<StatusUser>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = Map.of(
                ACTIVE, EnumSet.of(DISABLED, PENDING_EXCLUSION),
                DISABLED, EnumSet.of(ACTIVE, PENDING_EXCLUSION),
                PENDING_EXCLUSION, EnumSet.of(ACTIVE, EXCLUDED),
                EXCLUDED, EnumSet.noneOf(StatusUser.class)
        );
    }

    public void transitionTo(StatusUser nextStatus) {
        if (nextStatus == null)
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.status_required");

        EnumSet<StatusUser> allowedTargets = VALID_TRANSITIONS.get(this);

        if (allowedTargets == null || !allowedTargets.contains(nextStatus)) {
            throw new BusinessException(Code.BUSINESS_RULE, "user.error.invalid_status_transition");
        }
    }
}
