package com.example.r311.numizmatik.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.r311.numizmatik.R;
import com.example.r311.numizmatik.data.Kovanci;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SlikajActivity extends AppCompatActivity {

    ImageView imageView;
    private Button btnUpload;
    private Button btnBack;
    private EditText txtDrzava;
    private EditText txtVrednost;
    private ProgressBar pgBar;
    private String filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    StorageReference storage;
    // Create a storage reference from our app
    StorageReference storageRef;
    // Create a reference to "kovanci.jpg"
    StorageReference mountainsRef;
    // Create a reference to 'images/kovanci.jpg'
    StorageReference mountainImagesRef;
    UploadTask uploadTask;
    Uri mImgURI;

    DatabaseReference database;


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slikaj);

        imageView = (ImageView) findViewById(R.id.imageView);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnBack = (Button) findViewById(R.id.btnBackSlikaj);
        txtDrzava = (EditText) findViewById(R.id.txtDrzava);
        txtVrednost = (EditText) findViewById(R.id.txtVrednost);
        pgBar = (ProgressBar) findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance().getReference("slike");
        database = FirebaseDatabase.getInstance().getReference("slike");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,0);

        btnUpload.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    upload2();
                } else {
                    signInAnonymously();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SlikajActivity.this, StartActivity.class));
            }
        });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                upload2();
            }
        })
        .addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("login", "signInAnonymously:FAILURE", exception);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        mImgURI = getImageUri(this, (Bitmap) data.getExtras().get("data"));
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private StorageTask mUploadTask;
    private void upload2(){

        pgBar.setVisibility(View.VISIBLE);
        final DateFormat df = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
        final Date date = new Date();
        StorageReference mStorageRef = storage;
        final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                + "." + getFileExtension(mImgURI));
        fileReference.putFile(mImgURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Kovanci uploadKovanec = new Kovanci(
                                UUID.randomUUID().toString().replace("-", ""),
                                txtDrzava.getText().toString().trim(),
                                Integer.parseInt(txtVrednost.getText().toString()),
                                uri.toString()
                        );

                        database.child(String.valueOf(System.currentTimeMillis()).replace("."+"#"+"$"+","+"["+"]", "")
                                + df.format(date).toString().replace("-","")).setValue(uploadKovanec);

                        Toast.makeText(SlikajActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        });
    }

    private void upload(){
        DateFormat df = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
        Date date = new Date();
        //////////////////////////////////
        String cas = df.toString();
        storage = FirebaseStorage.getInstance().getReference("slike");
        storageRef = storage;
        mountainsRef = storageRef.child("kovanci"+ df.format(date).toString().replace("-","") +".jpg");
        mountainImagesRef = storageRef.child("slike/kovanci.jpg");

        // While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // falseStorageActivity.java

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        uploadTask = mountainsRef.putBytes(data);
        Log.i("path", String.valueOf(mountainsRef.getPath()));

        //filePath = "gs://numizmatik-996.appspot.com"+mountainsRef.getPath();

        Log.i("path", String.valueOf(filePath));
        //////////////////////////////////
        if (filePath != null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {    //delaya za 0.3 sekunde
                @Override
                public void run() {
                    pgBar.setProgress(0);
                }
            }, 300);
            pgBar.setVisibility(View.INVISIBLE);

            //int vrednost = Integer.parseInt(txtVrednost.getText().toString());

            //////////
            /*
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference imagesRef = rootRef.child("slike");
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String url = ds.getValue(String.class);
                        Log.d("TAG", url);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            imagesRef.addListenerForSingleValueEvent(valueEventListener);
            */
            //////////

            Toast.makeText(SlikajActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
            Kovanci uploadKovanec = new Kovanci(
                    UUID.randomUUID().toString().replace("-", ""),
                    txtDrzava.getText().toString().trim(),
                    Integer.parseInt(txtVrednost.getText().toString()),
                    filePath
            );

            database.child(String.valueOf(System.currentTimeMillis()).replace("."+"#"+"$"+","+"["+"]", "")
                    + df.format(date).toString().replace("-","")).setValue(uploadKovanec);
        }
        else{
            Toast.makeText(this, "Ni slike", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFromServer(){
        Uri file = Uri.fromFile(new File("path/to/kovanci.jpg"));
        final StorageReference ref = storageRef.child("slike/kovanci.jpg");
        uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }
}
