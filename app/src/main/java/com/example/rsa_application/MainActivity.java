package com.example.rsa_application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rsa_application.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;



public class MainActivity extends AppCompatActivity {

    public byte[] imageToByte(String image) throws IOException {
        InputStream is = null;
        byte[] buffer = null;
        is = new FileInputStream(image);
        buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return buffer;
    }

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_GALLERY = 200;
    private long downloadId;

    TextView file_name;
    String file_path=null;
    Button upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button upload_file=findViewById(R.id.upload_file);
        upload_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permission greater than equal to marshmeellow we used run time permission
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission()){
                        filePicker();
                    }
                    else{
                        requestPermission();
                    }
                }
                else{
                    filePicker();
                }
            }
        });


        upload=findViewById(R.id.upload);
        file_name=findViewById(R.id.filename);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file_path!=null){
                    try {
                        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                        Cipher cipher = Cipher.getInstance("RSA");

                        kpg.initialize(1024);
                        KeyPair kp = kpg.generateKeyPair();
                        PrivateKey prKey = kp.getPrivate();
                        PublicKey puKey = kp.getPublic();

                        byte[] array = imageToByte(file_path); // Imagem transformada para array de bytes

                        SecretKeySpec key = new SecretKeySpec(array,"RSA");

                        cipher.init(Cipher.ENCRYPT_MODE ,key);

                        //byte[] teste = cipher.doFinal();

                    } catch (FileNotFoundException | InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Please Select File First", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void filePicker(){
        //.Now Permission Working
        Toast.makeText(MainActivity.this, "File Picker Call", Toast.LENGTH_SHORT).show();
        //Let's Pick File
        Intent opengallery=new Intent(Intent.ACTION_PICK);
        opengallery.setType("image/*");
        startActivityForResult(opengallery,REQUEST_GALLERY);
    }




    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this, "Please Give Permission to Upload File", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission(){
        int result= ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission Successfull", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Permission Failed", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_GALLERY && resultCode== Activity.RESULT_OK){
            String filePath=getRealPathFromUri(data.getData(),MainActivity.this);
            Log.d("File Path : "," "+filePath);
            //now we will upload the file
            //lets import okhttp first

            File file=new File(filePath);
            file_name.setText(file.getAbsolutePath());
            file_path = file.getAbsolutePath();

        }
    }

    public String getRealPathFromUri(Uri uri,Activity activity){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor=activity.getContentResolver().query(uri,proj,null,null,null);
        if(cursor==null){
            return uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int id=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(id);
        }
    }


}