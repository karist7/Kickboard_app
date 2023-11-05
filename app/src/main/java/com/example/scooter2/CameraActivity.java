package com.example.scooter2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.Toast;

import com.example.scooter2.server.RetrofitManager;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "CameraActivity";
    private RetrofitManager retrofitManager = new RetrofitManager();
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean pictureTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // Open the front camera
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                try {
                    camera.setPreviewDisplay(holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);

                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;

                switch (rotation) {
                    case Surface.ROTATION_0:
                        degrees = 0;
                        break;
                    case Surface.ROTATION_90:
                        degrees = 90;
                        break;
                    case Surface.ROTATION_180:
                        degrees = 180;
                        break;
                    case Surface.ROTATION_270:
                        degrees = 270;
                        break;
                }

                int result;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (info.orientation + degrees) % 360;
                    result = (360 - result) % 360;  // compensate for the mirror
                } else {
                    result = (info.orientation - degrees + 360) % 360;
                }

                camera.setDisplayOrientation(result);

                // Set camera parameters and start preview
                Camera.Parameters parameters = camera.getParameters();
                parameters.set("orientation", "portrait");
                camera.setParameters(parameters);
                camera.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // Release the camera when the surface is destroyed
                camera.stopPreview();
                camera.release();
            }
        });

        Button captureButton = findViewById(R.id.btnCapture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureButton.setEnabled(false); // 버튼 비활성화

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        captureButton.setEnabled(true); // 1초 후에 버튼 활성화
                    }
                }, 1500); // 1000 밀리초 = 1초
                camera.takePicture(null, null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap capturedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap rotatedBitmap = rotateBitmap(capturedBitmap, -90); // 90도 회전
                        Bitmap finalBitmap = flipImage(rotatedBitmap); // 좌우 반전
                        Bitmap resizedBitmap = resizeBitmap(finalBitmap, 800, 1200); // 크기 조정

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        sendPhoto(stream.toByteArray());



                    }
                });
            }
        });
    }
    public void sendPhoto(final byte[] comment){
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), comment);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("imageFile", "image.jpg", requestFile);
        retrofitManager.getApiService().requestPhoto(imagePart).enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code()==201){
                    Toast.makeText(CameraActivity.this, "인증에 성공했습니다.", Toast.LENGTH_SHORT).show();
                    Log.d("성공","성공");
                    Intent intent = new Intent(getApplicationContext(), ScanQR.class);
                    intent.putExtra("finish","finish");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }
                else{
                    Toast.makeText(CameraActivity.this, "인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    pictureTaken = true;

                    // 다시 사진을 찍을 수 있도록 미리보기 재시작
                    camera.startPreview();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("전송오류",t.toString());
            }
        });
    }
    public Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private Bitmap flipImage(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    public Bitmap resizeBitmap(Bitmap source, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
}
