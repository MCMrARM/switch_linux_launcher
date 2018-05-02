#include "JLogProxy.h"
#include <cstdio>
#include <algorithm>

JLogProxyEnv::JLogProxyEnv(JNIEnv* env) : env(env) {
    logClass = env->FindClass("io/mrarm/switchlinuxlauncher/log/LogProxy");
    logMethod = env->GetMethodID(logClass, "log", "(ILjava/lang/String;)V");
}

void JLogProxy::vlog(LogLevel level, const char* text, va_list args) {
    char buffer[4096];
    vsnprintf(buffer, sizeof(buffer), text, args);
    log(level, buffer);
}

void JLogProxy::log(LogLevel level, const char* text) {
    jstring str = env.env->NewStringUTF(text);
    env.env->CallVoidMethod(object, env.logMethod, (jint) level, str);
    env.env->DeleteLocalRef(str);
}