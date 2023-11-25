cmake_minimum_required(VERSION 3.15)
set(PROJECT_NAME "thermal_printer")
project(${PROJECT_NAME} LANGUAGES CXX)

# This value is used when generating builds using this plugin, so it must
# not be changed
set(PLUGIN_NAME "thermal_printer_plugin")

add_definitions(-D_CRT_SECURE_NO_WARNINGS)

add_library(${PLUGIN_NAME} SHARED
  "thermal_printer_plugin.cpp"
  "include/printer.cpp"
  "include/printer.h"
  "include/utils.hpp"
)
apply_standard_settings(${PLUGIN_NAME})
set_target_properties(${PLUGIN_NAME} PROPERTIES
  CXX_VISIBILITY_PRESET hidden)
target_compile_definitions(${PLUGIN_NAME} PRIVATE FLUTTER_PLUGIN_IMPL)
target_include_directories(${PLUGIN_NAME} INTERFACE
  "${CMAKE_CURRENT_SOURCE_DIR}/include")
target_link_libraries(${PLUGIN_NAME} PRIVATE flutter flutter_wrapper_plugin)

# List of absolute paths to libraries that should be bundled with the plugin
set(thermal_printer_bundled_libraries
  ""
  PARENT_SCOPE
)
