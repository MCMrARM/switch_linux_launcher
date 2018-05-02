cmake_minimum_required(VERSION 3.4.1)

set (CMAKE_CXX_STANDARD 11)

add_library(switchlauncher SHARED ShofEL2.cpp JLogProxy.h JLogProxy.cpp HexString.h HexString.cpp)