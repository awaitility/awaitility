package org.awaitility.core;

public abstract class ConditionAwaiterFactory {
    public static final String PROP_AWAITILITY_CONDITION_AWAITER_FACTORY = "awaitility-condition-awaiter-factory";

    public static ConditionAwaiterFactory getInstance() {
        String factoryName = System.getProperty(PROP_AWAITILITY_CONDITION_AWAITER_FACTORY);
        if (factoryName != null) {
            try {
                Class targetFactory = Class.forName(factoryName);
                return (ConditionAwaiterFactory) targetFactory.getConstructor().newInstance();
            } catch (Exception e) {
                throw new ReflectiveOperationRuntimeException("Could not create factory for class " + factoryName, e);
            }
        }
        return new ConditionAwaiterDefaultFactory();
    }

    public abstract ConditionAwaiter newConditionAwaiter(ConditionEvaluator conditionEvaluator,
                                                         ConditionSettings conditionSettings,
                                                         TimeoutMessageSupplier timeoutMessageSupplier);
}

class ReflectiveOperationRuntimeException extends RuntimeException {
    public ReflectiveOperationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}