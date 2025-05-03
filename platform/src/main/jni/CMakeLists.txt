cmake_minimum_required(VERSION 3.22.1)

project(MMRL)

set(CMAKE_C_FLAGS_RELEASE "-O3 -Wall -Werror")
set(CMAKE_CXX_FLAGS_RELEASE ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_C_FLAGS_RELWITHDEBINFO ${CMAKE_C_FLAGS_RELEASE})
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO ${CMAKE_C_FLAGS_RELWITHDEBINFO})

add_link_options("-Wl,--build-id=none")

include_directories(include)

find_library(log-lib log)

add_library(mmrl-file-manager SHARED
        file-manager.cpp
        include/logging.hpp
)
target_link_libraries(mmrl-file-manager ${log-lib})

add_library(mmrl-kernelsu SHARED
        kernelsu/ksu.cpp
        kernelsu/jni.cpp
        include/kernelsu/ksu.hpp
        include/logging.hpp
)
target_link_libraries(mmrl-kernelsu ${log-lib})