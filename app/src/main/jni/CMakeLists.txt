cmake_minimum_required(VERSION 3.22.1)

project(MMRL)

set(CMAKE_C_FLAGS_RELEASE "-O3 -Wall -Werror")
set(CMAKE_CXX_FLAGS_RELEASE ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_C_FLAGS_RELWITHDEBINFO ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO ${CMAKE_C_FLAGS_RELWITHDEBINFO})

include_directories(include)

find_library(log-lib log)

add_library(file-manager SHARED
        file-manager.cpp
        include/logging.hpp
)
target_link_libraries(file-manager ${log-lib})

add_library(kernelsu SHARED
        kernelsu/ksu.cpp
        kernelsu/jni.cpp
        include/kernelsu/ksu.hpp
        include/logging.hpp
)
target_link_libraries(kernelsu ${log-lib})