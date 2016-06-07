package org.age.hz.core.services.worker.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.function.Consumer;

@Named
public class ExceptionHandler implements Consumer<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void accept(Throwable throwable) {
        log.error("Exception", throwable);
    }
}
