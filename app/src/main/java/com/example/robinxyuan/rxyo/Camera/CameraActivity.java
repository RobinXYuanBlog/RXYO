package com.example.robinxyuan.rxyo.Camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robinxyuan.rxyo.App.App;
import com.example.robinxyuan.rxyo.CustomView.LineButton.ExposureButton;
import com.example.robinxyuan.rxyo.CustomView.LineButton.SensitivityButton;
import com.example.robinxyuan.rxyo.Enum.FlashType;
import com.example.robinxyuan.rxyo.Enum.StateType;
import com.example.robinxyuan.rxyo.ImageProcessing.ImageProcessingActivity;
import com.example.robinxyuan.rxyo.R;
import com.example.robinxyuan.rxyo.Utils.CameraUtils;
import com.example.robinxyuan.rxyo.Utils.CommonUtils;
import com.jakewharton.rxbinding2.view.RxView;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;

import butterknife.*;


/**
 * Camera2 API. Android Lollipop 及以后版本的 Android 使用 Camera2 API.
 * <p>
 * 从https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/
 * com/example/android/camera2basic/Camera2BasicFragment.java拷贝而来.
 * <p>
 * 进行了一些修改, 以文档注释的形式写出.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.texture_camera_preview)
    TextureView mTextureView;

    @BindView(R.id.iv_camera_button)
    ImageView mIvCameraButton;

    @BindView(R.id.exp_button)
    TextView expButton;

    @BindView(R.id.flash_button)
    SwitchFlashButton flashButton;

    @BindView(R.id.camera_button)
    Button changeCameraButton;

    @BindView(R.id.format_button)
    Button formatButton;

    @BindView(R.id.iso_line_button)
    SensitivityButton isoLineButton;

    @BindView(R.id.iso_button)
    TextView isoButton;

    @BindView(R.id.iso_plus_button)
    TextView isoPlusButton;

    @BindView(R.id.iso_minus_button)
    TextView isoMinusButton;

    @BindView(R.id.iso_text)
    TextView isoText;

    @BindView(R.id.exp_line_button)
    ExposureButton expLineButton;

    @BindView(R.id.exp_plus_button)
    TextView expPlusButton;

    @BindView(R.id.exp_minus_button)
    TextView expMinusButton;

    @BindView(R.id.exp_text)
    TextView expText;

    Unbinder mUnbinder;

    HashMap<String, Long> exposureTimeList = new HashMap<>();

    /**
     * finish()是否已调用过
     */
    volatile boolean mFinishCalled;

    /**
     * 最大允许的拍照尺寸（像素数）
     */
    long mMaxPicturePixels;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    static final String TAG = "Camera2BasicFragment";

    /**
     * Camera state: Showing camera preview.
     */
    static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     *  Set stateType
     */
    private StateType state = StateType.STATE_PREVIEW;

    /**
     *  Initialize iso value to 400
     */
    private int isoValue = 400;

    /**
     *  ISO values list
     */

    private int[] isoValues = {
            100, 200, 400, 800, 1600,
            2400, 3200, 4800, 6400
    };

    /**
     *  Variable for storing the iso range of camera
     */
    private Range<Integer> isoRange = null;

    /**
     *  max Exposure Time Value
     */
    private long maxExpValue;

    /**
     *  min Exposure Time Value
     */
    private Long minExpValue;

    /**
     *  Variable for changing exposure time
     */
    private long expTime;

    /**
     *  Variable for storing exposure time range of camera
     */
    private Range<Long> expTimeRange = null;

    /**
     *  Array for storing exposure time levels
     */
    private long[] expTimeValues = new long[1200];

    /**
     *  Pre-define the ids of CAMERA
     */
    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";

    private String isoContent = null;
    private String expTimeContent = null;

    private Handler handler = null;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    String mCameraId = CAMERA_BACK;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    CameraDevice mCameraDevice;

    /**
     * The {@link Size} of camera preview.
     */
    Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Toast.makeText(App.sApp, "相机开启失败，再试一次吧", Toast.LENGTH_LONG).show();
            mFinishCalled = true;
            finish();
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    File mFile;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the sApp from exiting before closing the camera.
     */
    Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    /**
                     * 判断可以立即拍摄的autoFocusState增加到4种.
                     */
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState ||
                            CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        /**
                         * 判断可以立即拍摄的autoExposureState增加到4种.
                         */
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED ||
                                aeState == CaptureResult.CONTROL_AE_STATE_LOCKED ||
                                aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResId());

        handler = new Handler();

        mUnbinder = ButterKnife.bind(this);
        preInitData();
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                  int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        double minRatio = ((double) w) / ((double) h) * 0.95;
        double maxRatio = ((double) w) / ((double) h) * 1.05;
        for (Size option : choices) {
            double ratio = ((double) option.getWidth()) / ((double) option.getHeight());
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    /**
                     * 现在允许宽高比相对于16:9有正负5%的误差.
                     */
                    ratio >= minRatio && ratio <= maxRatio) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings({"ConstantConditions", "SuspiciousNameCombination"})
    void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);

                expTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);

                minExpValue = expTimeRange.getLower();
                maxExpValue = expTimeRange.getUpper();

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) continue;

                // For still image captures, we use the largest available size.
                /*Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());*/
                /**
                 * 替换了寻找最大尺寸的算法.
                 * 从OutputSizes中找到满足16:9比例，且像素数不超过3840*2160的最大Size.
                 * 若找不到，则选择满足16:9比例的最大Size（像素数可能超过3840*2160)，若仍找不到，返回最大Size。
                 */
                Size largest = CameraUtils.findBestSize(map.getOutputSizes(ImageFormat.JPEG), mMaxPicturePixels);
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270)
                            swappedDimensions = true;
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180)
                            swappedDimensions = true;
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH;

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT;

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

//                mCameraId = cameraId;
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the camera.
     */
    void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(4, TimeUnit.SECONDS))
                throw new RuntimeException("Time out waiting to lock camera opening.");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(App.sApp, "相机开启失败，再试一次吧", Toast.LENGTH_LONG).show();
            mFinishCalled = true;
            finish();
        }
    }

    public void switchCamera() {
        if (mCameraId.equals(CAMERA_FRONT)) {
            mCameraId = CAMERA_BACK;
            closeCamera();
            reopenCamera();

        } else if (mCameraId.equals(CAMERA_BACK)) {
            mCameraId = CAMERA_FRONT;
            closeCamera();
            reopenCamera();
        }
    }

    public void reopenCamera() {
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (null == mCameraDevice) return;

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    try {
                        // Auto focus should be continuous for camera preview.
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // Flash is automatically enabled when necessary.
                        setAutoFlash(mPreviewRequestBuilder);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_MODE_OFF);
                        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue);
                        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 50000000L);

//                        expText.setText("Exposure Time " + expTime + " ms");

                        // Finally, we start displaying the camera preview.
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(App.sApp, "开启相机预览失败，再试一次吧", Toast.LENGTH_LONG).show();
                        mFinishCalled = true;
                        finish();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(App.sApp, "开启相机预览失败，再试一次吧", Toast.LENGTH_LONG).show();
                    mFinishCalled = true;
                    finish();
                }
            }, null);
//
//            long expTimeMilliSecond = expTime / 1000;
//            String expTimeString = "Exposure Time " + expTimeMilliSecond + " ms";

//            expText.setText(expTimeString);

//            new Thread() {
//                public void run() {
//                    expTimeContent = expTimeString;
//                    handler.post(runnableUI);
//                    handler.removeCallbacks(runnableUI);
//                }
//            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(App.sApp, "开启相机预览失败，再试一次吧", Toast.LENGTH_LONG).show();
            mFinishCalled = true;
            finish();
        }
    }
//
//    Runnable runnableUI = new Runnable() {
//        @Override
//        public void run() {
//            expText.setText(expTimeContent);
//        }
//    };

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) return;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    void takePicture() {
        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    void captureStillPicture() {
        try {
            if (null == mCameraDevice) return;
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue);
            captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 50000000L);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        /**
         * 若相机支持自动开启/关闭闪光灯，则使用. 否则闪光灯总是关闭的.
         */
        if (mFlashSupported) requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }



    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        final Image mImage;
        /**
         * The file we save the image into.
         */
        final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            try {
                if (mFile.exists()) mFile.delete();
                FileOutputStream output = new FileOutputStream(mFile);
                output.write(bytes);
                try {mImage.close();} catch (Exception ignored) {}
                try {output.close();} catch (Exception ignored) {}
                /**
                 * 拍照完成后返回MainActivity.
                 */
                App.mHandler.post(() -> {
//                    setResult(200, getIntent().putExtra("file", mFile.toString()));

                    Intent intent = new Intent(CameraActivity.this, ImageProcessingActivity.class);

                    mFinishCalled = true;

                    intent.putExtra("file", mFile.toString());
                    intent.putExtra("isFromCamera", true);

                    setResult(200, intent);

                    startActivity(intent);
                    finish();

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    protected int getContentViewResId() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return R.layout.activity_camera;
    }

    private int currentISOValue = 2;
    private int currentExpValue = 5;

    protected void preInitData() {
//        mFile = new File(getIntent().getStringExtra("file"));
        mFile = CommonUtils.createImageFile("mFile");
        mMaxPicturePixels = getIntent().getIntExtra("maxPicturePixels", 3840 * 2160);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");

//        isoText.setText("ISO 400");

        isoButton.setTypeface(font);
        isoButton.setText(R.string.icon_adjust);

        expButton.setTypeface(font);
        expButton.setText(R.string.icon_exptime);

        isoPlusButton.setTypeface(font);
        isoPlusButton.setText(R.string.icon_plus);

        isoMinusButton.setTypeface(font);
        isoMinusButton.setText(R.string.icon_minus);

        isoLineButton.setOnMenuItemClickListener((view, pos) -> {
//                int isoCount = 0;
            switch (pos) {
                case 0:
                    Log.e("ISO", "ISO Add");
                    if(currentISOValue < isoValues.length - 1) {
                        currentISOValue++;
                        isoMinusButton.setText(R.string.icon_minus);
                        isoPlusButton.setText(R.string.icon_plus);
                        isoMinusButton.setTextSize(20);
                        isoPlusButton.setTextSize(20);
                        isoValue = isoValues[currentISOValue];
                        switchISO(isoValue);
                    }

                    if(currentISOValue == isoValues.length - 1) {
                        isoValue = isoValues[currentISOValue];
                        switchISO(isoValue);
                        isoPlusButton.setText(R.string.icon_ban);
                        isoPlusButton.setTextSize(18);
                        isoPlusButton.setClickable(false);
                    } else if(currentISOValue > isoValues.length - 1) {
                        currentISOValue = isoValues.length - 1;
                        isoValue = isoValues[currentISOValue];
                    }
                    break;
                case 1:
                    Log.e("ISO", "ISO Minus");
                    if(currentISOValue > 0) {
                        currentISOValue --;
                        isoPlusButton.setText(R.string.icon_plus);
                        isoMinusButton.setText(R.string.icon_minus);
                        isoMinusButton.setTextSize(20);
                        isoPlusButton.setTextSize(20);
                        isoValue = isoValues[currentISOValue];
                        switchISO(isoValue);
                    }

                    if(currentISOValue == 0) {
                        currentISOValue = 0;
                        isoValue = isoValues[currentISOValue];
                        switchISO(isoValue);
                        isoMinusButton.setText(R.string.icon_ban);
                        isoMinusButton.setTextSize(18);
                        isoMinusButton.setClickable(false);
                    } else if(currentISOValue < 0) {
                        currentISOValue = 0;
                        isoValue = isoValues[currentISOValue];
                    }
                    break;
            }
        });

        expPlusButton.setTypeface(font);
        expPlusButton.setText(R.string.icon_plus);

        expMinusButton.setTypeface(font);
        expMinusButton.setText(R.string.icon_minus);

        expLineButton.setOnMenuItemClickListener((view, pos) -> {
            switch (pos) {
                case 0:
                    Log.e("EXP", "EXP Add");
//                        if(currentExpValue < expTimeValues.length - 1) {
//                            currentExpValue++;
//                            expMinusButton.setText(R.string.icon_minus);
//                            expPlusButton.setText(R.string.icon_plus);
//                            expMinusButton.setTextSize(20);
//                            expPlusButton.setTextSize(20);
//                            expTime = expTimeValues[currentExpValue];
//                            switchExposureTime(500000000L);
//                        }
//
//                        if(currentExpValue == expTimeValues.length - 1) {
//                            expTime = expTimeValues[currentExpValue];
//                            switchExposureTime(expTime);
//                            expPlusButton.setText(R.string.icon_ban);
//                            expPlusButton.setTextSize(18);
//                            expPlusButton.setClickable(false);
//                        } else if(currentExpValue > expTimeValues.length - 1) {
//                            currentExpValue = expTimeValues.length - 1;
//                            expTime = expTimeValues[currentExpValue];
//                        }
                    break;
                case 1:
                    Log.e("EXP", "EXP Minus");
//                        if(currentExpValue > 0) {
//                            currentExpValue --;
//                            expPlusButton.setText(R.string.icon_plus);
//                            expMinusButton.setText(R.string.icon_minus);
//                            expMinusButton.setTextSize(20);
//                            expPlusButton.setTextSize(20);
//                            expTime = expTimeValues[currentExpValue];
//                            switchExposureTime(500000000L);
//                        }
//
//                        if(currentExpValue == 0) {
//                            currentExpValue = 0;
//                            expTime = expTimeValues[currentExpValue];
//                            switchExposureTime(500000000L);
//                            expMinusButton.setText(R.string.icon_ban);
//                            expMinusButton.setTextSize(18);
//                            expMinusButton.setClickable(false);
//                        } else if(currentExpValue < 0) {
//                            currentExpValue = 0;
//                            expTime = expTimeValues[currentExpValue];
//                        }
                    break;
            }
        });


//        flashButton.setTypeface(font);
//        flashButton.setText(R.string.icon_flash);
//
//        flashButton.setOnClickListener(this::switchFlash());

        flashButton.setOnSwitchFlashListener(this::switchFlash);

        changeCameraButton.setTypeface(font);
        changeCameraButton.setText(R.string.icon_change_camera);

        changeCameraButton.setOnClickListener(view -> {
            switchCamera();
        });

        formatButton.setTypeface(font);
        formatButton.setText(R.string.icon_format);


        RxView.clicks(mIvCameraButton)
                /**
                 * 防止手抖连续多次点击造成错误
                 */
                .throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> takePicture());

    }

    private CaptureRequest.Builder setFlashCaptureRequest(CaptureRequest.Builder builder, FlashType flashType) {
        switch (flashType) {
            case AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                break;
            case ON:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                break;
            case OFF:
                builder.set(CaptureRequest.FLASH_MODE, CaptureResult.FLASH_MODE_OFF);
                break;
        }
        return builder;
    }

    private void switchFlash(FlashType flashType) {
        mPreviewRequestBuilder = setFlashCaptureRequest(mPreviewRequestBuilder, flashType);
        try {
            mCaptureSession.stopRepeating();
            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.capture(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            state = StateType.STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CaptureRequest.Builder setISOCaptureRequest(CaptureRequest.Builder builder, int isoValue) {
        builder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue);

        return builder;
    }

    private void switchISO(int isoValue) {
        mPreviewRequestBuilder = setISOCaptureRequest(mPreviewRequestBuilder, isoValue);
        try {
            mCaptureSession.stopRepeating();
            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.capture(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
//            state = StateType.STATE_PREVIEW;
//            state = StateType.STATE_PICTURE_TAKEN;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            String isoString = "ISO " + mPreviewRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY).toString();
            isoText.setText(isoString);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CaptureRequest.Builder setExposureTime(CaptureRequest.Builder builder, long exposureTime) {
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTime);

        return builder;
    }

    private void switchExposureTime(long exposureTime) {
        mPreviewRequestBuilder = setExposureTime(mPreviewRequestBuilder, exposureTime);
        try {
            mCaptureSession.stopRepeating();
            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.capture(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            state = StateType.STATE_PICTURE_TAKEN;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            expText.setText("Exposure Time " + mPreviewRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME).intValue() / 1000 + " ms");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

//        long expTimeMilliSecond = expTime / 1000;
//        String expTimeString = "Exposure Time " + expTimeMilliSecond + " ms";
//
//        expText.setText(expTimeString);

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onBackPressed() {
        mFinishCalled = true;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
        if (!mFinishCalled) finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }
}