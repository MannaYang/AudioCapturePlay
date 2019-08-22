# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.2)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#指定生成路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# 编译出一个动态库 native-lib，源文件只有 src/main/cpp/native-lib.cpp
add_library( # Sets the name of the library.
        lame-lib
        #so文件名称
        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        lame-lib.cpp
        libmp3lame/bitstream.c
        libmp3lame/encoder.c
        libmp3lame/fft.c
        libmp3lame/gain_analysis.c
        libmp3lame/id3tag.c
        libmp3lame/lame.c
        libmp3lame/mpglib_interface.c
        libmp3lame/newmdct.c
        libmp3lame/presets.c
        libmp3lame/psymodel.c
        libmp3lame/quantize.c
        libmp3lame/quantize_pvt.c
        libmp3lame/reservoir.c
        libmp3lame/set_get.c
        libmp3lame/tables.c
        libmp3lame/takehiro.c
        libmp3lame/util.c
        libmp3lame/vbrquantize.c
        libmp3lame/VbrTag.c
        libmp3lame/version.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# 找到预编译库 log_lib 并link到我们的动态库 native-lib中
find_library( # Sets the name of the path variable.
        log-lib
        android
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
        lame-lib
)