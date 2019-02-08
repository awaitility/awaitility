package org.awaitility.core;

public class ConditionAwaiterDefaultFactory extends ConditionAwaiterFactory {
    public ConditionAwaiterDefaultFactory() {
    }

    public ConditionAwaiter newConditionAwaiter(ConditionEvaluator conditionEvaluator,
                                                ConditionSettings conditionSettings,
                                                TimeoutMessageSupplier timeoutMessageSupplier) {
        return new ConditionAwaiterImpl(conditionEvaluator, conditionSettings, timeoutMessageSupplier);
    }
}
