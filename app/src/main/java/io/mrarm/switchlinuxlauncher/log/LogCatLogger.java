package io.mrarm.switchlinuxlauncher.log;

import android.util.Log;

public final class LogCatLogger implements LogOutput {

    public static final LogCatLogger instance = new LogCatLogger();

    private LogCatLogger() {
    }

    private static int mapLogLevel(@Logger.LogLevel int level) {
        switch (level) {
            case Logger.VERBOSE:
                return Log.VERBOSE;
            case Logger.DEBUG:
                return Log.DEBUG;
            case Logger.INFO:
                return Log.INFO;
            case Logger.WARN:
                return Log.WARN;
            case Logger.ERROR:
                return Log.ERROR;
            default:
                return Log.ERROR;
        }
    }

    @Override
    public void log(int level, String tag, String message) {
        Log.println(mapLogLevel(level), tag, message);
    }

}
