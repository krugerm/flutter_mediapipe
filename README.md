# flutter_mediapipe

This plugin demonstrates how to use mediapipe projects in flutter.  The process is that we create a mediapipe project, configure it (including calculators, input, output, etc), compile the .so jniLibs to be included in the flutter project, and then create the flutter code which calls the plugin's native .java code which in turn wraps the jniLibs.

[Flutter plugin](https://codelabs.developers.google.com/codelabs/write-flutter-plugin/#0) with 
[mediapipe_posedetection](https://developers.google.com/mediapipe/solutions/vision/pose_landmarker).

## Devices
Currently, it runs on below devices with "OK".
- [OK] [Android](##Android)
- [NG] [iOS](##iOS)

## Android

There are Mediapipe Manual Build for Android flutter plugin.

There are [mobile_calculators](https://github.com/google/mediapipe/search?q=mobile_calculators) list to run on Mobile.

Choose one. This plugin choose _**pose_detection**_.
- face_detection
- face_mesh 
- object_detection
- _**pose_tracking**_
- hand_tracking

```
git clone https://github.com/google/mediapipe.git
cd mediapipe
```

### Setup
```
apt update
apt install vim
apt install zip
bash ./setup_android_sdk_and_ndk.sh
```

### Prepare
Download the .tflite models required to compile the native binaries.  Links can be found in /docs/solutions/models.md
*   Pose detection model:
    [TFLite model](https://storage.googleapis.com/mediapipe-assets/pose_detection.tflite)
*   Pose landmark model:
    [TFLite model (lite)](https://storage.googleapis.com/mediapipe-assets/pose_landmark_lite.tflite),
    [TFLite model (full)](https://storage.googleapis.com/mediapipe-assets/pose_landmark_full.tflite),
    [TFLite model (heavy)](https://storage.googleapis.com/mediapipe-assets/pose_landmark_heavy.tflite)
*   [Model card](https://mediapipe.page.link/blazepose-mc)
*   For Holistic detection, also download the Hand recrop model:
    [TFLite model](https://storage.googleapis.com/mediapipe-assets/hand_recrop.tflite)

These files need to be put in mediapipe/modules/pose_detection/

    
Find the calculators we want to use in ```mediapipe/graphs/pose_tracking/BUILD```, and them add them to the list to be included in the BUILD file:
```
mkdir mediapipe/examples/android/src/java/com/google/mediapipe/apps/flutter_mediapipe
vi mediapipe/examples/android/src/java/com/google/mediapipe/apps/flutter_mediapipe/BUILD 
```

- "BUILD" file content.
```
load("//mediapipe/java/com/google/mediapipe:mediapipe_aar.bzl", "mediapipe_aar")

mediapipe_aar(
    name = "flutter_mediapipe",
    calculators = ["//mediapipe/graphs/pose_tracking:pose_tracking_gpu_deps"]
)
```

### pre-download any required files
```
mkdir cache/

curl -o cache/pose_landmarker_heavy.task https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/float16/1/pose_landmarker_heavy.task

curl -o cache/pose_landmarker_full.task https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/float16/1/pose_landmarker_full.task

curl -o cache/pose_landmarker_lite.task https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/float16/1/pose_landmarker_lite.task
```

### Build
- jniLibs & binary graphs (to be included in the flutter project's assets)
```
bazel build -c opt --host_crosstool_top=@bazel_tools//tools/cpp:toolchain --fat_apk_cpu=arm64-v8a,armeabi-v7a --strip=ALWAYS //mediapipe/examples/android/src/java/com/google/mediapipe/apps/flutter_mediapipe:BUILD --linkopt="-s"
bazel build -c opt --host_crosstool_top=@bazel_tools//tools/cpp:toolchain --fat_apk_cpu=arm64-v8a,armeabi-v7a //mediapipe/examples/android/src/java/com/google/mediapipe/apps/flutter_mediapipe:flutter_mediapipe --linkopt="-s"
bazel build -c opt --host_crosstool_top=@bazel_tools//tools/cpp:toolchain --fat_apk_cpu=arm64-v8a,armeabi-v7a --strip=ALWAYS //mediapipe/examples/android/src/java/com/google/mediapipe/apps/posetrackinggpu:BUILD
bazel build -c opt --host_crosstool_top=@bazel_tools//tools/cpp:toolchain --fat_apk_cpu=arm64-v8a,armeabi-v7a //mediapipe/examples/android/src/java/com/google/mediapipe/apps/posetrackinggpu:posetrackinggpu

rm -rf flutter_mediapipe
mkdir flutter_mediapipe/
mkdir flutter_mediapipe/android
mkdir flutter_mediapipe/android/libs
mkdir flutter_mediapipe/android/src
mkdir flutter_mediapipe/android/src/main
mkdir flutter_mediapipe/android/src/main/assets
mkdir flutter_mediapipe/android/src/main/jniLibs
mkdir flutter_mediapipe/protos

cp mediapipe/framework/formats/landmark.proto flutter_mediapipe/protos/
cp bazel-bin/mediapipe/examples/android/src/java/com/google/mediapipe/apps/flutter_mediapipe/libflutter_mediapipe_android_lib.jar flutter_mediapipe/android/libs
cp cache/*.task flutter_mediapipe/android/src/main/assets/.
cp mediapipe/modules/pose_detection/*.pbtxt flutter_mediapipe/android/src/main/assets/.
cp mediapipe/modules/pose_landmark/*.pbtxt flutter_mediapipe/android/src/main/assets/.
cp bazel-out/darwin_arm64-opt/bin/mediapipe/graphs/pose_tracking/pose_tracking_gpu.binarypb flutter_mediapipe/android/src/main/assets

rm -rf work
mkdir work
cp bazel-bin/mediapipe/examples/android/src/java/com/google/mediapipe/apps/flutter_mediapipe/flutter_mediapipe.aar work/aar.zip
cd work/
unzip aar.zip
cd ..
cp -r work/jni/* flutter_mediapipe/android/src/main/jniLibs/
```

See [regenerate.md](../protos/regenerate.md)


Then copy "flutter_mediapipe" directory to flutter plugin projects.

### [APK](https://flutter.dev/docs/deployment/android#build-an-apk)
```
flutter build apk --split-per-abi
```

### References
- [sample plugin](https://github.com/zhouzaihang/flutter_hand_tracking_plugin) is useful.
- [sample android app without flutter](https://github.com/jiuqiant/mediapipe_multi_hands_tracking_aar_example) 
- [Flutter don't support local AAR](https://github.com/decodedhealth/flutter_zoom_plugin/issues/53)
- [jniLibs size down](https://github.com/google/mediapipe/issues/77)


## iOS
Not implemented. Help me to develop.


## Customize mediapipe

### Graph
When editing graphs or subgraphs, [Build](###Build) again. 
- graphs: _binary graph_
- subgraphs: _jniLibs_

```
vi mediapipe/graphs/face_mesh/subgraphs/face_renderer_gpu.pbtxt
```

It is a sample to comment out of lines from 92 to 94 for hiding rectangles and landmarks.
```
# Draws annotations and overlays them on top of the input images.
node {
  calculator: "AnnotationOverlayCalculator"
  input_stream: "IMAGE_GPU:input_image"
#  input_stream: "detections_render_data"
#  input_stream: "VECTOR:0:multi_face_landmarks_render_data"
#  input_stream: "rects_render_data"
  output_stream: "IMAGE_GPU:output_image"
}
```

### Calculator
When editing c++ calculator source, [Build](###Build) again. 
- c++ source: _jniLibs_

```
vi mediapipe/graphs/face_mesh/calculators/face_landmarks_to_render_data_calculator.cc
```

It is a sample to comment out the lines(31 and 45) for hiding Left eyebrow connections.
```
constexpr int kNumFaceLandmarkConnections = 116; // (124 - (16/2))  c.f. l.93
// Pairs of landmark indices to be rendered with connections.
constexpr int kFaceLandmarkConnections[] = {
  :
    133,
    // Left eyebrow.
//    46, 53, 53, 52, 52, 65, 65, 55, 70, 63, 63, 105, 105, 66, 66, 107,
    // Right eye.
  :
  for (int i = 0; i < kNumFaceLandmarkConnections; ++i) {
    landmark_connections_.push_back(kFaceLandmarkConnections[i * 2]);
    landmark_connections_.push_back(kFaceLandmarkConnections[i * 2 + 1]);
  }
```
