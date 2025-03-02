package com.fbytes.llmka.logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

public class Logger {

    private final org.slf4j.Logger logger;

    private Logger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(message, args);
        }
    }

    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }

    public void error(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(message, args);
        }
    }

    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, args);
        }
    }

    public void trace(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(message, args);
        }
    }

    public void info(String message, Throwable t, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(message, t, args);
        }
    }

    public void debug(String message, Throwable t, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, t, args);
        }
    }

    public void error(String message, Throwable t, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(message, t, args);
        }
    }

    public void warn(String message, Throwable t, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, t, args);
        }
    }

    public void trace(String message, Throwable t, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(message, t, args);
        }
    }

    public void logException(Exception e) {
        logger.error(String.format("%s\n%s", e.getMessage(), ExceptionUtils.getStackTrace(e)));
    }


    public void logException(String msg, Exception e) {
        logger.error(String.format("%s\n%s", msg + "\n" + e.getMessage(), ExceptionUtils.getStackTrace(e)), 2);
    }
}
