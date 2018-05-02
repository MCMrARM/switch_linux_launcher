#pragma once

#include <jni.h>

#define LogFuncDef(name, logLevel) \
    void name(const char* text, ...) { \
        va_list args; \
        va_start(args, text); \
        vlog(logLevel, text, args); \
        va_end(args); \
    }

enum class LogLevel {
    VERBOSE = 0,
    DEBUG,
    INFO,
    WARN,
    ERROR
};

class JLogProxy;

class JLogProxyEnv {

private:
    friend class JLogProxy;

    JNIEnv* env;
    jclass logClass;
    jmethodID logMethod;

public:
    JLogProxyEnv(JNIEnv* env);

};

class JLogProxy {

private:
    JLogProxyEnv& env;
    jobject object;

public:
    JLogProxy(JLogProxyEnv& env, jobject object) : env(env), object(object) {
    }

    void log(LogLevel level, const char* text);

    void vlog(LogLevel level, const char* text, va_list args);

    LogFuncDef(v, LogLevel::VERBOSE)
    LogFuncDef(d, LogLevel::DEBUG)
    LogFuncDef(i, LogLevel::INFO)
    LogFuncDef(w, LogLevel::WARN)
    LogFuncDef(e, LogLevel::ERROR)

};

#undef LogFuncDef