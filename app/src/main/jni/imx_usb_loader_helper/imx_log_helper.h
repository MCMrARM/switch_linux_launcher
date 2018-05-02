#ifndef __IMX_LOG_HELPER__
#define __IMX_LOG_HELPER__

#ifdef __cplusplus
#include "../JLogProxy.h"
extern "C" {

extern void imx_logger_set(JLogProxy* proxy);

#endif

extern void imx_logger_log(const char* msg, ...);

#ifdef __cplusplus
};
#endif

#endif /* __IMX_LOG_HELPER__ */