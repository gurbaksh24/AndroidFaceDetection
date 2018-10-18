package com.tavisca.taviscavisitor;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;


public class FaceDetectorService extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private StorageReference mStorageRef;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (!saveImageInDirectory(data)) {
                return;
            }
            File pictureFileDir = getDir();
            final ProgressDialog progressDialog = new ProgressDialog(FaceDetectorService.this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            File file = new File(pictureFileDir + File.separator + "visitor.jpg");
            Uri uri = Uri.fromFile(file);
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            mStorageRef = firebaseStorage.getReference();

            StorageReference visitorRef = mStorageRef.child("Visitors/visitor.jpg");
            final FaceRecognitionApiCaller faceRecognitionApiCaller = new FaceRecognitionApiCaller();

            visitorRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(FaceDetectorService.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                            //faceRecognitionApiCaller.execute();
                            //if(faceRecognitionApiCaller.doInBackground(null).equals("true"))
                            String response = null;
                            try {
                                response = faceRecognitionApiCaller.execute().get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (response.equals("true")) {
                                    Dialog dialog = new Dialog(FaceDetectorService.this);
                                    dialog.setContentView(R.layout.dialog_visitor);
                                    dialog.setTitle("Message");
                                    dialog.setCancelable(true);
                                    TextView message = (TextView) dialog.findViewById(R.id.textMsg);
                                    message.setText("Welcome Visitor");
                                    dialog.show();
                                    // Toast.makeText(FaceDetectorService.this, "Welcome Visitor", Toast.LENGTH_SHORT).show();
                                } else {
                                    Dialog dialog = new Dialog(FaceDetectorService.this);
                                    dialog.setContentView(R.layout.dialog_visitor);
                                    dialog.setTitle("Message");
                                    dialog.setCancelable(true);
                                    TextView message = (TextView) dialog.findViewById(R.id.textMsg);
                                    message.setText("Unknown Visitor");
                                    dialog.show();
                                    //Toast.makeText(FaceDetectorService.this, "Unknown Visitor", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.setProgress((int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                        }
                    });
            //mCamera.release();
            //Toast.makeText(FaceDetectorService.this, "Picture Taken", Toast.LENGTH_SHORT).show();
        }
    };

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            int cameraId = findFrontFacingCamera();
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private static int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector_service);

        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        FloatingActionButton captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
