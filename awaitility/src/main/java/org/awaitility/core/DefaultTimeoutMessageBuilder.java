
package org.awaitility.core;

import org.awaitility.constraint.WaitConstraint;

public class DefaultTimeoutMessageBuilder {

    public String getTimeoutErrorMessage(OnTimeoutContext ctx) {
        final String maxWaitTimeLowerCase = ctx.getMaxWaitTime().getTimeUnitAsString();
        final long maxTimeout = ctx.getMaxWaitTime().getValue();
        String timeoutMessage = ctx.getTimeoutMessage();
        return ctx.getAlias()
            .map(alias -> String.format("Condition with alias '%s' didn't complete within %s %s because %s.",
                alias, maxTimeout, maxWaitTimeLowerCase, decapitalize(timeoutMessage)))
            .orElse(String.format("%s within %s %s.", timeoutMessage, maxTimeout, maxWaitTimeLowerCase));
    }
    
    private static String decapitalize(String str) {
        if (str == null) {
            return "";
        }
        String firstLetter = str.substring(0, 1).toLowerCase();
        String restLetters = str.substring(1);
        return firstLetter + restLetters;
    }
    
}
