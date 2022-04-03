package org.misty.expose._tool;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertException {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertException.class);

    public static AbstractThrowableAssert<?, ? extends Throwable> print(ThrowableAssert.ThrowingCallable throwingCallable) {
        return Assertions.assertThatThrownBy(() -> {
            try {
                throwingCallable.call();
            } catch (Throwable t) {
                LOGGER.error("", t);
                throw t;
            }
        });
    }

}
