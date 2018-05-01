package io.mrarm.switchlinuxlauncher.log;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class Logger {

    @Retention(SOURCE)
    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR})
    public @interface LogLevel {}
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;

    @NonNull
    private List<LogOutput> outputs;

    public Logger() {
        this.outputs = new ArrayList<>();
        this.outputs.add(LogCatLogger.instance);
    }

    public Logger(List<LogOutput> outputs) {
        this.outputs = outputs;
    }

    public void addOutput(LogOutput output) {
        this.outputs.add(output);
    }

    public void removeOutput(LogOutput output) {
        this.outputs.remove(output);
    }


    public void log(@LogLevel int level, String tag, String message) {
        for (LogOutput o : outputs)
            o.log(level, tag, message);
    }

    public void v(String tag, String message) {
        log(VERBOSE, tag, message);
    }

    public void d(String tag, String message) {
        log(DEBUG, tag, message);
    }

    public void i(String tag, String message) {
        log(INFO, tag, message);
    }

    public void w(String tag, String message) {
        log(WARN, tag, message);
    }

    public void e(String tag, String message) {
        log(ERROR, tag, message);
    }

}
