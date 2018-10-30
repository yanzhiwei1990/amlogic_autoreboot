LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
# Build all java files in the java subdirectory
LOCAL_SRC_FILES := $(call all-subdir-java-files)

# Name of the APK to build
LOCAL_PACKAGE_NAME := AutoReboot
LOCAL_CERTIFICATE := platform
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4

ifndef PRODUCT_SHIPPING_API_LEVEL
LOCAL_PRIVATE_PLATFORM_APIS := true
else
LOCAL_SDK_VERSION := current
endif

# Tell it to build an APK
include $(BUILD_PACKAGE) 
