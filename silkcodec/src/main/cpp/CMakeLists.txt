cmake_minimum_required(VERSION 3.18.1)

# 项目名称
project(silk)

# 设置 C++ 标准
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Os -flto")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Os -flto")

# 添加大小优化选项 对优化大小很重要
# -Os: 优化代码大小的编译选项  -O0 -O1-3
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    # 大小优化
    set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -Os")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Os")

    # 链接优化
    set(CMAKE_EXE_LINKER_FLAGS_RELEASE "${CMAKE_EXE_LINKER_FLAGS_RELEASE} -Wl,--gc-sections")
endif()

message(${CMAKE_CXX_FLAGS_RELEASE} )
message(${CMAKE_BUILD_TYPE})



# 添加源文件目录 cpp文件，值为存储在变量中
# key value = 目录 变量名称
aux_source_directory(src SRC_LIST)
aux_source_directory(. NATIVE_LIB)
message(${SRC_LIST}) # src/index.cpp
message(${NATIVE_LIB}) # ./native-lib.cpp
# 添加所有的SILK源文件
file(GLOB SILK_SRC
        libSilkCodec/silk/src/SILK_SDK_SRC_ARM_v1.0.9/src/*.c
)
file(GLOB SILK_CODEC libSilkCodec/src/*.c) # decoder.c 和 encoder.c 文件
message(${SILK_CODEC})



# 添加所有的头文件目录 h文件
include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/src
#        ${CMAKE_CURRENT_SOURCE_DIR}/include
        libSilkCodec/silk/src/SILK_SDK_SRC_ARM_v1.0.9/interface/
        libSilkCodec/silk/src/SILK_SDK_SRC_ARM_v1.0.9/src/
        libSilkCodec/src/include
)

# 添加库文件目录
link_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}
        ${CMAKE_CURRENT_BINARY_DIR}
)
# CMAKE_CURRENT_SOURCE_DIR: 代码内置的库文件目录，当前项目暂时没有
# CMAKE_CURRENT_BINARY_DIR：当前项目交叉编译生成的库文件目录
message(${CMAKE_CURRENT_SOURCE_DIR})
message(${CMAKE_CURRENT_BINARY_DIR})



# 创建silk-codec共享库 添加所有的C和C++源文件
add_library(silk-codec SHARED
        ${SRC_LIST}
        ${NATIVE_LIB}
        ${SILK_SRC}
        ${SILK_CODEC}
#        libSilkCodec/src/encoder.c
#        libSilkCodec/src/decoder.c
)

# 查找 Android 日志库
find_library(log-lib log)


# 链接所有的库
target_link_libraries(silk-codec
        ${log-lib}
        android
)

# 移除调试符号
# 在Release模式下移除调试信息 ，优化大小  一定要放在末尾
if(CMAKE_BUILD_TYPE STREQUAL "Release")

    # 方法1：使用strip命令
#    add_custom_command(TARGET silk-codec POST_BUILD
#            COMMAND ${CMAKE_STRIP} --strip-all $<TARGET_FILE:silk-codec>
#    )

    # 方法2：使用编译标志
#    target_compile_options(silk-codec PRIVATE
#            -fvisibility=hidden
#            -fdata-sections
#            -ffunction-sections
#    )

    # 方法3：使用链接标志
#    set_target_properties(silk-codec PROPERTIES
#            LINK_FLAGS "-Wl,--strip-all -Wl,--gc-sections"
#    )

endif()



