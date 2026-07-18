package io.github.mateussilvadev.horizondesk.support;

import org.assertj.core.api.ThrowableAssert;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public interface DomainAssertions {

    default <T> void assertThatException(ThrowableAssert.ThrowingCallable action, T exception, String expectedMessage) {
        assertThatThrownBy(action).isInstanceOf((Class<?>) exception).hasMessage(expectedMessage);
    }

    default <T> void assertUpdateWorkflow(Consumer<T> updateMethod, Supplier<T> getterMethod, T newValue, T expectedValue) {
        assertIdempotent(() -> updateMethod.accept(newValue), getterMethod);
        assertEquals(expectedValue, getterMethod.get());
    }

    default void assertNoChange(Runnable action, Supplier<?>... getters) {
        Object[] before = captureState(getters);
        action.run();
        Object[] after = captureState(getters);
        assertThat(after)
                .as("State should not change")
                .containsExactly(before);
    }

    default void assertIdempotent(Runnable action, Supplier<?>... getters) {
        action.run();
        Object[] afterFirst = captureState(getters);
        action.run();
        Object[] afterSecond = captureState(getters);
        assertThat(afterSecond)
                .as("Second execution should not change state")
                .containsExactly(afterFirst);
    }

    default void assertEntityState(Object entity, Map<String, Object> expectedField) {
        expectedField.forEach((fieldName, expectedValue) -> {
            assertThat(entity)
                    .as("Entity should have field " + fieldName)
                    .extracting(fieldName)
                    .isEqualTo(expectedValue);
        });
    }

    private Object[] captureState(Supplier<?>... getters) {
        return Arrays.stream(getters)
                .map(Supplier::get)
                .toArray();
    }
}
