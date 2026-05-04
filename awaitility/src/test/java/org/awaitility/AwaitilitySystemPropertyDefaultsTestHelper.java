package org.awaitility;

import org.awaitility.constraint.WaitConstraint;
import org.awaitility.pollinterval.FixedPollInterval;
import org.awaitility.pollinterval.PollInterval;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AwaitilitySystemPropertyDefaultsTestHelper {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected exactly one scenario argument.");
        }

        switch (args[0]) {
            case "default-timeout":
                assertDurationEquals("DEFAULT_WAIT_CONSTRAINT", propertyDuration("awaitility.defaultTimeout"));
                assertDurationEquals("defaultWaitConstraint", propertyDuration("awaitility.defaultTimeout"));
                break;
            case "default-poll-interval":
                assertPollIntervalEquals("DEFAULT_POLL_INTERVAL", propertyDuration("awaitility.defaultPollInterval"));
                assertPollIntervalEquals("defaultPollInterval", propertyDuration("awaitility.defaultPollInterval"));
                break;
            case "default-poll-delay":
                assertPollDelayEquals("DEFAULT_POLL_DELAY", propertyDuration("awaitility.defaultPollDelay"));
                assertPollDelayEquals("defaultPollDelay", propertyDuration("awaitility.defaultPollDelay"));
                break;
            case "invalid-properties-fallback":
                assertDurationEquals("DEFAULT_WAIT_CONSTRAINT", Duration.ofSeconds(10));
                assertPollIntervalEquals("DEFAULT_POLL_INTERVAL", Duration.ofMillis(100));
                assertPollDelayEquals("DEFAULT_POLL_DELAY", null);
                break;
            case "reset-restores-property-derived-defaults":
                Awaitility.setDefaultTimeout(1, TimeUnit.MINUTES);
                Awaitility.setDefaultPollInterval(1, TimeUnit.SECONDS);
                Awaitility.setDefaultPollDelay(2, TimeUnit.SECONDS);
                Awaitility.reset();
                assertDurationEquals("defaultWaitConstraint", propertyDuration("awaitility.defaultTimeout"));
                assertPollIntervalEquals("defaultPollInterval", propertyDuration("awaitility.defaultPollInterval"));
                assertPollDelayEquals("defaultPollDelay", propertyDuration("awaitility.defaultPollDelay"));
                break;
            default:
                throw new IllegalArgumentException("Unknown scenario: " + args[0]);
        }
    }

    private static Duration propertyDuration(String propertyName) {
        String property = System.getProperty(propertyName);
        return property == null ? null : Duration.parse(property);
    }

    private static void assertDurationEquals(String fieldName, Duration expected) throws Exception {
        WaitConstraint waitConstraint = (WaitConstraint) getField(fieldName).get(null);
        Duration actual = waitConstraint.getMaxWaitTime();
        if (!actual.equals(expected)) {
            throw new AssertionError(fieldName + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertPollIntervalEquals(String fieldName, Duration expected) throws Exception {
        PollInterval pollInterval = (PollInterval) getField(fieldName).get(null);
        if (!(pollInterval instanceof FixedPollInterval)) {
            throw new AssertionError(fieldName + " expected FixedPollInterval but was " + pollInterval);
        }

        Duration actual = pollInterval.next(1, Duration.ZERO);
        if (!actual.equals(expected)) {
            throw new AssertionError(fieldName + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertPollDelayEquals(String fieldName, Duration expected) throws Exception {
        Duration actual = (Duration) getField(fieldName).get(null);
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(fieldName + " expected " + expected + " but was " + actual);
        }
    }

    private static Field getField(String fieldName) throws Exception {
        Class<?> awaitilityClass = Class.forName("org.awaitility.Awaitility", true, AwaitilitySystemPropertyDefaultsTestHelper.class.getClassLoader());
        Field field = awaitilityClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
