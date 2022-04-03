package org.misty.expose.core;

import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MistyExposeTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void allowRegexPattern() {
        this.logger.info("name regex pattern : " + MistyExpose.NAME_REGEX_PATTERN);
        this.logger.info("version regex pattern : " + MistyExpose.VERSION_REGEX_PATTERN);
        new MistyExpose("ker.ker", "95.27");
        new MistyExpose("ker-ker", "95-27");
        new MistyExpose("kek_rer", "95_27");
        new MistyExpose("ker$ker", "95$27");

        String allowName = "Aery";
        String allowVersion = "9527";
        Class<?> thrownInstance = IllegalArgumentException.class;

        printException(() -> new MistyExpose(null, allowVersion)).isInstanceOf(thrownInstance); // name can't be null
        printException(() -> new MistyExpose("", allowVersion)).isInstanceOf(thrownInstance); // name can't be empty
        printException(() -> new MistyExpose(allowName, null)).isInstanceOf(thrownInstance); // version can't be null
        printException(() -> new MistyExpose(allowName, "")).isInstanceOf(thrownInstance); // version can't be empty

        printException(() -> new MistyExpose("Ae ry", allowVersion)).isInstanceOf(thrownInstance); // name can't include blank
        printException(() -> new MistyExpose("Ae*ry", allowVersion)).isInstanceOf(thrownInstance);  // name can't include *
        printException(() -> new MistyExpose(allowName, "95 27")).isInstanceOf(thrownInstance); // version can't include blank
        printException(() -> new MistyExpose(allowName, "95*27")).isInstanceOf(thrownInstance); // version can't include *

        printException(() -> new MistyExpose("9527", allowVersion)).isInstanceOf(thrownInstance); // name can't start without [a-zA-Z]
        printException(() -> new MistyExpose(".Aery", allowVersion)).isInstanceOf(thrownInstance); // name can't start without [a-zA-Z]
        printException(() -> new MistyExpose("Aery.", allowVersion)).isInstanceOf(thrownInstance); // name can't end without [a-zA-Z]
        printException(() -> new MistyExpose(allowName, ".9527")).isInstanceOf(thrownInstance); // version can't start without [a-zA-Z0-9]
        printException(() -> new MistyExpose(allowName, "9527.")).isInstanceOf(thrownInstance);// version can't start without [a-zA-Z0-9]
    }

    public AbstractThrowableAssert<?, ? extends Throwable> printException(ThrowableAssert.ThrowingCallable throwingCallable) {
        return Assertions.assertThatThrownBy(() -> {
            try {
                throwingCallable.call();
            } catch (Throwable t) {
                this.logger.error("", t);
                throw t;
            }
        });
    }

}
