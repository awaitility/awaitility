
package org.awaitility.core;

public class AssertionErrorThrowingOnTimeoutCallback implements Consumer<OnTimeoutContext> {
    
    private final DefaultTimeoutMessageBuilder messageBuilder = new DefaultTimeoutMessageBuilder();
    
    @Override
    public void accept(OnTimeoutContext ctx) {
        String message = messageBuilder.getTimeoutErrorMessage(ctx);
        throw new AssertionError(message, ctx.getCause().orElse(null));
    }
    
}
