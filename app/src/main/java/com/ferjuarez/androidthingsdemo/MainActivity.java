package com.ferjuarez.androidthingsdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import com.ferjuarez.emotions.Emotion;
import com.ferjuarez.emotions.JoyEmotion;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import net.trippedout.cloudvisionlib.CloudVisionApi;
import net.trippedout.cloudvisionlib.CloudVisionService;
import net.trippedout.cloudvisionlib.FacesFeature;
import net.trippedout.cloudvisionlib.ImageUtil;
import net.trippedout.cloudvisionlib.VisionCallback;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import vaf.vishal.hcsr04.Hcsr04UltrasonicDriver;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "AndroidThingsDemo";

    private static final int MAX_FACE_RESULTS = 3;

    private final String BUTTON_GPIO_PIN = "BCM21";
    private final String PIN_LED = "BCM6";
    private final String PIN_TRIGGER = "BCM23";
    private final String PIN_ECHO = "BCM25";

    private Gpio mLedGpio;
    private Button mButton;
    private PeripheralManagerService mPeriphericalService;
    private SensorManager mSensorManager;
    private Hcsr04UltrasonicDriver mSensorDriver;

    private CameraHandler mCamera;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private boolean isCameraEnabled = true;

    private CloudVisionService mCloudVisionService;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"APP RUNNING");
        //setContentView(R.layout.activity_main);

        // We need permission to access the camera
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // first running need to restart rspi (check in https://developer.android.com/things/training/doorbell/camera-input.html)
            return;
        }

        mDatabase = FirebaseDatabase.getInstance();
        mPeriphericalService = new PeripheralManagerService();
        mCloudVisionService = CloudVisionApi.getCloudVisionService();

        setupButton();
        setupCamera();
        setupIndicatorLed();
        setupProximitySensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
        closeButton();
        closeLedIndicator();
        closeProximitySensor();
    }

    private void getFaces(final byte[] imageBytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes.length);
        List<CloudVisionApi.Feature> features = new ArrayList<>();
        features.add(new CloudVisionApi.Feature("FACE_DETECTION", MAX_FACE_RESULTS));

        List<CloudVisionApi.Request> requests = new ArrayList<>();
        requests.add(
                new CloudVisionApi.Request(
                        new CloudVisionApi.Image(ImageUtil.getEncodedImageData(bitmap)),
                        features
                )
        );
        mCloudVisionService.getAnnotations(
                Const.CLOUD_VISION_API_KEY,
                new CloudVisionApi.VisionRequest(requests)
        ).enqueue(new VisionCallback(CloudVisionApi.getRetrofit()) {
            @Override
            public void onApiResponse(CloudVisionApi.VisionResponse response) {
                Emotion emotion = detectEmotions((CloudVisionApi.FaceDetectResponse) response.getResponseByType(CloudVisionApi.FEATURE_TYPE_FACE_DETECTION));
                updateLastPictureInFirebase(imageBytes, emotion);
                setLedState(false);
            }

            @Override
            public void onApiError(CloudVisionApi.Error error) {
                Log.e(TAG,"Error on communicate with CloudVision");
            }
        });
    }



    private void setupCamera(){
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        mCamera = CameraHandler.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
    }

    private void setupButton(){
        try {
            mButton = new Button(BUTTON_GPIO_PIN, Button.LogicState.PRESSED_WHEN_HIGH);
            mButton.setOnButtonEventListener(mButtonCallback);
        } catch (IOException e) {
        }
    }

    private void setupIndicatorLed(){
        try {
            mLedGpio = mPeriphericalService.openGpio(PIN_LED);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e){
        }
    }

    private void setupProximitySensor(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    mSensorManager.registerListener(MainActivity.this, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        try {
            mSensorDriver = new Hcsr04UltrasonicDriver(PIN_TRIGGER, PIN_ECHO);
            mSensorDriver.register();
        } catch (IOException e) {
            Log.e(TAG,"Error on initialize proximity sensor");
        }
    }

    private void closeProximitySensor(){
        mSensorManager.unregisterListener(this);
        mSensorDriver.unregister();
        try {
            mSensorDriver.close();
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableCameraTemporarily(){
        isCameraEnabled = false;
    }

    private void reenableCamera(){
        // delay to prevent firebase and cloud vision flooding

        isCameraEnabled = true;
    }

    private void setLedState(boolean isOn) {
        try {
            mLedGpio.setValue(isOn);
        } catch (IOException e) {

        }
    }

    private void closeLedIndicator(){
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
            } finally{
                mLedGpio = null;
            }
        }
    }

    private void closeCamera(){
        mCamera.shutDown();
        mCameraThread.quitSafely();
    }

    private void closeButton(){
        try {
            mButton.close();
        } catch (IOException e) {
        }
    }

    private void updateLastPictureInFirebase(final byte[] imageBytes, final Emotion emotion){
        mDatabase.getReference("logs").removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.e(TAG,"Removed last update from Firebase");
                uploadImageToFirebase(imageBytes, emotion);
            }
        });
    }

    private void uploadImageToFirebase(final byte[] imageBytes, Emotion emotion){
        // upload image to firebase
        final DatabaseReference log = mDatabase.getReference("logs").push();
        String imageStr = Base64.encodeToString(imageBytes, Base64.NO_WRAP | Base64.URL_SAFE);

        log.child("timestamp").setValue(ServerValue.TIMESTAMP);
        //log.child("image").setValue(imageStr);
        log.child("emotion").setValue(emotion.getState().getLikelihood());
        log.child("image").setValue(imageStr, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setLedState(false);
                reenableCamera();
            }
        });
        //isCameraEnabled = true;
    }


    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    android.media.Image image = reader.acquireLatestImage();
                    //int width = image.getWidth();
                    //int height = image.getHeight();

                    // get image bytes
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();

                    onPictureTaken(imageBytes);
                }
            };


    private Button.OnButtonEventListener mButtonCallback = new Button.OnButtonEventListener() {
        @Override

        public void onButtonEvent(Button button, boolean pressed) {
            if (pressed) {
                //closeProximitySensor();
                //setupProximitySensor();
                Log.e(TAG,"Button Pressed!");
            }
        }
    };

    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
            getFaces(imageBytes);
        }
    }

    private Emotion detectEmotions(CloudVisionApi.FaceDetectResponse response) {
        JoyEmotion joyEmotion = null;
        if(response != null){
            List<FacesFeature.FaceAnnotations> faceAnnotations = response.faceAnnotations;

            if (faceAnnotations != null) {
                for (FacesFeature.FaceAnnotations annotation : faceAnnotations) {
                    joyEmotion = new JoyEmotion(annotation.joyLikelihood);
                }
            } else {
                joyEmotion = new JoyEmotion(null);

            }
        } else {
            joyEmotion = new JoyEmotion(null);
        }
        return joyEmotion;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float value = sensorEvent.values[0];
        Log.e(TAG,"Sensor Values: " + String.valueOf(value));
        if(value > 40 && value < 50 && isCameraEnabled){
            setLedState(true);
            mCamera.takePicture();
            disableCameraTemporarily();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
