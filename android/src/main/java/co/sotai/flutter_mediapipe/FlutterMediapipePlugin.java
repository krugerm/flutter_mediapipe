package co.sotai.flutter_mediapipe;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.platform.PlatformViewRegistry;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

import android.os.Build;
import android.util.Log;

/**
 * FlutterMediapipePlugin
 */
public class FlutterMediapipePlugin implements FlutterPlugin, ActivityAware {

    public static final String VIEW = "flutter_mediapipe/view";
    private static final String TAG = "FlutterMediapipePlugin";

    private PlatformViewRegistry registry;
    private BinaryMessenger messenger;
    private PlatformViewFactory factory;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
//        Log.d(TAG, "onAttachedToEngine called");
//        Log.d(TAG, "CPU_ABI: " + Build.CPU_ABI);
        registry = binding.getPlatformViewRegistry();
        messenger = binding.getBinaryMessenger();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
//        Log.d(TAG, "onDetachedFromEngine called");
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
//        Log.d(TAG, "onAttachedToActivity called - started");
        try {
            Activity activity = binding.getActivity();
//            Log.d(TAG, "onAttachedToActivity called - activity retrieved: " + activity.toString());
            factory = new NativeViewFactory(messenger, activity);
//            Log.d(TAG, "onAttachedToActivity called - factory created");
            registry.registerViewFactory(VIEW, factory);
//            Log.d(TAG, "onAttachedToActivity called - registered");
            binding.addRequestPermissionsResultListener(new PermissionsListener(factory));
//            Log.d(TAG, "onAttachedToActivity called - bound");
        }
        catch (Exception ex)
        {
            Log.d(TAG, "onAttachedToActivity exception:" + ex.getMessage(), ex);
        }
        catch (Throwable ex)
        {
            Log.d(TAG, "onAttachedToActivity throwable:" + ex.getMessage(), ex);
        }
//        Log.d(TAG, "onAttachedToActivity called - completed");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
//        Log.d(TAG, "onDetachedFromActivityForConfigChanges called");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
//        Log.d(TAG, "onReattachedToActivityForConfigChanges called");
    }

    @Override
    public void onDetachedFromActivity() {
        //methodChannel.setMethodCallHandler(null);
//        Log.d(TAG, "onDetachedFromActivity called");
    }

    private static class PermissionsListener implements RequestPermissionsResultListener {

        private final NativeViewFactory factory;

        public PermissionsListener(PlatformViewFactory factory){
            this.factory = (NativeViewFactory)factory;
        }

        @Override
        public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode != 0){
                return false;
            } else {
                for (int result : grantResults) {
                    if(result == PackageManager.PERMISSION_GRANTED){
                        NativeView nativeView = factory.getNativeView();
                        nativeView.onResume();
                    }
                }
                return true;
            }
        }
    }

}

