#include <cstring>
#include <cstdio>
#include "imx_log_helper.h"

static thread_local JLogProxy* current_log;

extern "C"
void imx_logger_set(JLogProxy* proxy) {
    current_log = proxy;
}

extern "C"
void imx_logger_log(const char* msg, ...) {
    if (current_log == nullptr)
        return;
    va_list args;
    va_start(args, msg);

    char buffer[4096];
    vsnprintf(buffer, sizeof(buffer), msg, args);
    size_t buflen = strlen(buffer);
    if (buflen > 0 && buffer[buflen - 1] == '\n')
        buffer[buflen - 1] = '\0';
    current_log->log(LogLevel::INFO, buffer);
    va_end(args);
}
