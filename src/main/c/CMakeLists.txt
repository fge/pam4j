cmake_minimum_required(VERSION 2.6)

add_library(pam4j SHARED PamHandle.c log.c)

find_package(JNI)

include_directories($ENV{JDK_HOME}/include)

target_link_libraries(pam4j pam pam_misc)

add_definitions(-g -O2 -Wall -Wstrict-prototypes -Wmissing-prototypes -Werror)

set(CMAKE_VERBOSE_MAKEFILE true)
set(CMAKE_COLOR_MAKEFILE false)
set(CMAKE_SKIP_PREPROCESSED_SOURCE_RULES true)
set(CMAKE_SKIP_ASSEMBLY_SOURCE_RULES true)
