package io.mrarm.switchlinuxlauncher.log;

public interface LogOutput {

    void log(@Logger.LogLevel int level, String tag, String message);

}
