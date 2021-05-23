package com.pucpr.cameraapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity {

    static final int CAMERA_PERMISSION_CODE = 2001;
    static final int CAMERA_INTENT_CODE = 3001;
    static final int SELECT_PICTURE = 200;

    ImageView imageViewCamera;

    String picturePath;
    Bitmap bitmap;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViewCamera = findViewById(R.id.imageViewCamera);
    }

    public void btnSelecionarClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    public void buttonCameraClicked(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestCameraPermission();
        }else{
            sendCameraIntent();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestCameraPermission(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{ Manifest.permission.CAMERA },CAMERA_PERMISSION_CODE);
            }else{
                sendCameraIntent();
            }
        }else{
            Toast.makeText(MainActivity.this,"No camera available",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                sendCameraIntent();
            }else{
                Toast.makeText(MainActivity.this,"Camera Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint({"SimpleDateFormat", "QueryPermissionsNeeded"})
    public void sendCameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION,true);
        if(intent.resolveActivity(getPackageManager()) != null){
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String picName = "pic_"+timeStamp;
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File pictureFile = null;
            try {
                pictureFile = File.createTempFile(picName,".jpg",dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(pictureFile != null){
                picturePath = pictureFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.pucpr.cameraapp.fileprovider", pictureFile
                );
                uri = photoUri;

                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(intent,CAMERA_INTENT_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_INTENT_CODE){
            if(resultCode == RESULT_OK){
                File file = new File(picturePath);
                if(file.exists()){
                    imageViewCamera.setImageURI(Uri.fromFile(file));
                    uri = Uri.fromFile(file);
                }
            }else{
                Toast.makeText(MainActivity.this,"Problem getting the image from the camera app", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    uri = selectedImageUri;

                    imageViewCamera.setImageURI(null);
                    imageViewCamera.setImageURI(selectedImageUri);

                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Cursor c = getContentResolver().query(selectedImageUri,filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    picturePath = c.getString(columnIndex);
                    c.close();
                }
            }
        }
    }

    public void btnConverterClicked(View view){
        try {
/*
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Drawable drawable = Drawable.createFromStream(inputStream, uri.toString() );
            bitmap = ((BitmapDrawable) drawable).getBitmap();
*/
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Bitmap novoBitmap = convertImage(bitmap);
            imageViewCamera.setImageBitmap(novoBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap convertImage(Bitmap original){
        Bitmap imgFinal = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        int alpha, red, green, blue;
        int corPixel;
        int width = original.getWidth();
        int height = original.getHeight();

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                corPixel = original.getPixel(i, j);
                alpha = Color.alpha(corPixel);
                red = Color.red(corPixel);
                green = Color.green(corPixel);
                blue = Color.blue(corPixel);

                red = (red + green + blue) / 3;
                green = red;
                blue = red;

                imgFinal.setPixel(i, j, Color.argb(alpha, red, green, blue));
            }
        }

        return imgFinal;
    }
}