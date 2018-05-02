cmake_minimum_required(VERSION 3.4.1)

set (CMAKE_CXX_STANDARD 11)

set(IMX_USB_LOADER_SOURCES imx_usb_loader/imx_loader_config.c imx_usb_loader/imx_sdp.c)

add_library(switchlauncher SHARED ShofEL2.cpp JLogProxy.h JLogProxy.cpp HexString.h HexString.cpp
    ImxUsbLoader.cpp imx_usb_loader_helper/imx_log_helper.h
    imx_usb_loader_helper/imx_log_helper.cpp ${IMX_USB_LOADER_SOURCES})
target_include_directories(switchlauncher PUBLIC imx_usb_loader imx_usb_loader_helper)
target_compile_definitions(switchlauncher PUBLIC IMX_CUSTOM_LOG SYSCONFDIR="")