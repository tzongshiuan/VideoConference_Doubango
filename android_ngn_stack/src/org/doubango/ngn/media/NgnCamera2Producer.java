/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 *
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.doubango.ngn.media;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.Arrays;

import static android.os.Looper.getMainLooper;

public class NgnCamera2Producer {
    //	private static final String TAG = NgnCamera2Producer.class.getCanonicalName();
    private static final String TAG = NgnProxyVideoProducer.class.getCanonicalName();

    private CameraDevice mCameraDevice;
    private boolean useFrontFacingCamera;

    private CameraManager mCameraManager;
    private Handler childHandler;
    private Handler mainHandler;
    private String mCameraID;  // id 0: back, id 1: front
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;

    private Context mContext;
    private ImageReader.OnImageAvailableListener listener;

    private boolean isCameraOpen = false;
    private boolean isUpdate = false;

    // Default values
//    private int lastUpdateWidth = 176;
//    private int lastUpdateHeight = 144;

    private int width = 176;
    private int height = 144;

    private Camera2ProducerCallback callback;

//    private static final int UPDATE_INTERVAL = 2000;
//    private boolean isThreadRunning;
//    private Thread mUpdateThread;
//    private Runnable mRunnableUpdate = new Runnable() {
//        @Override
//        public void run() {
//            while (isThreadRunning) {
//                if (lastUpdateFps != fps || lastUpdateWidth != width || lastUpdateHeight != height) {
//                    isUpdate = true;
//                    lastUpdateFps = fps;
//                    lastUpdateWidth = width;
//                    lastUpdateHeight = height;
//                    Log.d(TAG, "Detect camera setting have changed.");
//                }
//
//
//                Log.d(TAG, "isUpdate = " + isUpdate + ", isCameraOpen = " + isCameraOpen);
//                if (isCameraOpen && isUpdate) {
//                    Log.d(TAG,"Release camera in update thread");
//                    releaseCamera();
//                } else if (!isInitCamera) {
//                    // if find that camera is not open, reopen it
//                    reopenCamera2();
//                }
//
//                try {
//                    // check for every two seconds
//                    Thread.sleep(UPDATE_INTERVAL);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };

//    private void reopenCamera2() {
//        Log.d(TAG, "Reopen camera2");
//        if (mContext != null && listener != null) {
//            if (null != callback) {
//                callback.onImageResolutionChanged(lastUpdateWidth, lastUpdateHeight);
//            }
//            initCamera2(mContext, lastUpdateFps, lastUpdateWidth, lastUpdateHeight, listener);
//        }
//    }

//    void startUpdateThread() {
//        Log.d(TAG, "startUpdateThread()");
//        lastUpdateFps = fps;
//        lastUpdateWidth = width;
//        lastUpdateHeight = height;
//
//        isThreadRunning = true;
//        mUpdateThread = new Thread(mRunnableUpdate);
//        mUpdateThread.start();
//    }

//    private void stopUpdateThread() {
//        Log.d(TAG, "stopUpdateThread()");
//        isThreadRunning = false;
//        if (mUpdateThread != null) {
//            mUpdateThread.interrupt();
//            mUpdateThread = null;
//        }
//    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {  // open camera
            Log.d(TAG, "onOpened()");

            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {  // close camera
            Log.d(TAG, "onDisconnected()");

            releaseCamera();

            isCameraOpen = false;
            //cameraCount = CAMERA_OPEN_RETRY_COUNT;
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "onClosed()");

            isCameraOpen = false;
            //cameraCount = CAMERA_OPEN_RETRY_COUNT;

//            if (isUpdate && mContext != null && listener != null) {
//                isUpdate = false;
//                initCamera2(mContext, lastUpdateFps, lastUpdateWidth, lastUpdateHeight, listener);
//            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "Open camera failed");
            isCameraOpen = false;
        }
    };

    void setResolutoinCallback(Camera2ProducerCallback callback) {
        this.callback = callback;
    }

    void initCamera2(Context context, int width, int height, ImageReader.OnImageAvailableListener listener) {
        Log.d(TAG, "initCamera2(), isCameraOpen = " + isCameraOpen);

        if (isCameraOpen) {
            return;
        }
        isCameraOpen = true;

        if (null == mCameraDevice) {
            this.mContext = context;
            this.listener = listener;

            // divide a fixed number to bring down the system loading
            this.width = width;
            this.height = height;

            HandlerThread handlerThread = new HandlerThread("Camera2");
            handlerThread.start();
            childHandler = new Handler(handlerThread.getLooper());
            mainHandler = new Handler(getMainLooper());
            mCameraID = "" + CameraCharacteristics.LENS_FACING_BACK;

            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Failed to get Camera permission!!");
                    return;
                }
                mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
	}

    void startPreview() {
        try {
            if (null == mCameraDevice) {
                return;
            }

            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mImageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 1);
            mImageReader.setOnImageAvailableListener(listener, mainHandler);

            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            //previewRequestBuilder.addTarget(holder.getSurface());
            previewRequestBuilder.addTarget(mImageReader.getSurface());

            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice)
                        return;

                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // Auto Focus
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                        // Open Flash
                        //previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                        // Show preview
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null/*mCaptureCallback*/, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "onConfigureFailed()");
                    //Toast.makeText(getContext(), "配置失败", Toast.LENGTH_SHORT).show();
                    isCameraOpen = false;
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    void releaseCamera(){
        if (null != mCameraDevice){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    void destroyCamera2() {
//        isThreadRunning = false;
//        isUpdate = false;
//
//        stopUpdateThread();

        releaseCamera();
    }

    synchronized void updateCamera(int fps, int width, int height) {
        this.width = width;
        this.height = height;
    }

    interface Camera2ProducerCallback {
        void onImageResolutionChanged(int width, int height);
    }
}
