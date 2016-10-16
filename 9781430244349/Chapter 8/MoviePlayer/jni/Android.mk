LOCAL_PATH := $(call my-dir)

#
# AVILib
#
include $(CLEAR_VARS)

# Module name is avilib
LOCAL_MODULE := avilib

# Temporary variable to hold list of source files
MY_SRC_FILES := avilib.c platform_posix.c

# Prefix them with the sub-directory name
LOCAL_SRC_FILES := $(addprefix avilib/, $(MY_SRC_FILES))

# Export the includes directory for dependent modules
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/avilib

# Build it as static library 
include $(BUILD_STATIC_LIBRARY)

#
# Movie Player
#
include $(CLEAR_VARS)

# Module name
LOCAL_MODULE := MoviePlayer

# Source files
LOCAL_SRC_FILES := com_apress_movieplayer_AviPlayer.c

# Static libraries
LOCAL_STATIC_LIBRARIES := avilib

# System libraries
LOCAL_LDLIBS := -llog -ljnigraphics

# Build as shared library
include $(BUILD_SHARED_LIBRARY)

