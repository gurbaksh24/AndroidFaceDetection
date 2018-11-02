package com.tavisca.taviscavisitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;


public class FaceDetectorService extends AppCompatActivity {

    static String PREFERENCE = "GuardSessionPref";
    private Camera mCamera;
    private CameraPreview mPreview;
    private StorageReference mStorageRef;
    private static AmazonS3 s3Client;
    private static String bucket = "visitors-bucket";
    private static TransferUtility transferUtility;
    FrameLayout preview;
    SharedPreferences sharedPreferences;
    File uploadToS3;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (!saveImageInDirectory(data)) {
                return;
            }
            File pictureFileDir = getDir();
            File file = new File(pictureFileDir + File.separator + "visitor.jpg");
            uploadToS3 = file;

            s3credentialsProvider();
            setTransferUtility();
            uploadFileToS3();
        }
    };

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            int cameraId = findFrontFacingCamera();
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return c;
    }

    private static int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int index = 0; index < numberOfCameras; index++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(index, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = index;
                break;
            }
        }
        return cameraId;
    }

    private void initializeItems() {
        preview = (FrameLayout) findViewById(R.id.camera_preview);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#161731")));
        setContentView(R.layout.activity_face_detector_service);

        checkSessionValidity();

        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        initializeItems();
        preview.addView(mPreview);

        listenerCaller();
    }

    private void listenerCaller() {

        //Face Detection Listener
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mCamera.setFaceDetectionListener(
                        new Camera.FaceDetectionListener() {
                            @Override
                            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                                if (faces.length > 0) {
                                    mCamera.takePicture(null, null, mPicture);
                                }
                            }
                        }
                );
            }
        }, 1000);

        //On Button Click Listener
        FloatingActionButton captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    public void s3credentialsProvider() {
        // Initialize the AWS Credential
        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(),
                "ap-south-1:47114000-489c-42bc-9ad7-5efe4d3de442",
                Regions.AP_SOUTH_1
        );
        createAmazonS3Client(cognitoCachingCredentialsProvider);
    }

    public void createAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
        s3Client = new AmazonS3Client(credentialsProvider);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));                                   // Set the region of your S3 bucket
    }

    public void setTransferUtility() {
        transferUtility = new TransferUtility(s3Client, getApplicationContext());
    }

    public void uploadFileToS3() {
        TransferObserver transferObserver = transferUtility.upload(
                bucket,      //The bucket to upload to
                "new_visitor.jpg",     //The key for the uploaded object
                uploadToS3        //The file where the data to upload exists
        );
        transferObserverListener(transferObserver);
    }

    public void transferObserverListener(TransferObserver transferObserver) {
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                FaceRecognitionApiCaller faceRecognitionApiCaller = new FaceRecognitionApiCaller();
                String response = null;
                try {
                    response = faceRecognitionApiCaller.execute().get();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (state.toString().equals("COMPLETED")) {
                    /*if (response!=null && response.equals("true")) {
                        Intent intent = new Intent(FaceDetectorService.this, WelcomeActivity.class);
                        startActivity(intent);
                        Toast.makeText(FaceDetectorService.this, "Face Recognised", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(FaceDetectorService.this, NewVisitorEntry.class);
                        startActivity(intent);
                        Toast.makeText(FaceDetectorService.this, "Face Not Recognised", Toast.LENGTH_SHORT).show();
                    }*/
                    if (response != null && !response.equals("\"No Match Found\"") && !response.equals("\"Exception Occurred\"")) {
                        Intent intent = new Intent(FaceDetectorService.this, WelcomeActivity.class);
                        intent.putExtra("Visitor Name", response);
                        startActivity(intent);
                        Toast.makeText(FaceDetectorService.this, "Face Recognised", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(FaceDetectorService.this, NewVisitorEntry.class);
                        startActivity(intent);
                        Toast.makeText(FaceDetectorService.this, "Face Not Recognised", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %" + percentage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error", "error");
            }
        });
    }

    private boolean saveImageInDirectory(byte[] data) {
        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d("Camera Error", "Can't create directory to save image.");
            Toast.makeText(FaceDetectorService.this, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            return false;
        }

        String photoFileName = "visitor.jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFileName;
        File pictureFile = new File(filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            return true;
        } catch (Exception error) {
            Log.d("Camera Error", "File" + filename + "not saved: " + error.getMessage());
            Toast.makeText(FaceDetectorService.this, "Image could not be saved.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Tavisca_Visitors");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_password) {
            Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new LogoutApiCaller().execute(sharedPreferences.getString("sessionId", "default"));
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sessionId", "default");
        editor.commit();
        Intent intent = new Intent(FaceDetectorService.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkSessionValidity() {
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        if (sharedPreferences.getString("sessionId", "default").equals("default")) {
            Intent faceDetectIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(faceDetectIntent);
            finish();
        }
    }

}
