package io.mrarm.switchlinuxlauncher.log;

public final class LogProxy {

    private final Logger logger;
    private final String tag;

    public LogProxy(Logger logger, String tag) {
        this.logger = logger;
        this.tag = tag;
    }

    public void log(@Logger.LogLevel int level, String message) {
        logger.log(level, tag, message);
    }

    public void v(String message) {
        log(Logger.VERBOSE, message);
    }

    public void d(String message) {
        log(Logger.DEBUG, message);
    }

    public void i(String message) {
        log(Logger.INFO, message);
    }

    public void w(String message) {
        log(Logger.WARN, message);
    }

    public void e(String message) {
        log(Logger.ERROR, message);
    }

    public void e(String message, Throwable t) {
        logger.e(tag, message, t);
    }

}
