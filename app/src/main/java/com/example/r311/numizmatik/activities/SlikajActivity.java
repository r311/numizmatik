package com.example.r311.numizmatik.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SlikajActivity extends AppCompatActivity {

    ImageView imageView;
    private Button btnUpload;
    private Button btnBack;
    private Button btnCompare;
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
    Bitmap bitmap;
    DatabaseReference database;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("hi", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slikaj);

        imageView = (ImageView) findViewById(R.id.imageView);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnBack = (Button) findViewById(R.id.btnBackSlikaj);
        btnCompare = (Button) findViewById(R.id.btnCompare);

        txtDrzava = (EditText) findViewById(R.id.txtDrzava);
        txtVrednost = (EditText) findViewById(R.id.txtVrednost);
        pgBar = (ProgressBar) findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance().getReference("slike");
        database = FirebaseDatabase.getInstance().getReference("slike");

        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent,0);
        dispatchTakePictureIntent();

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

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropPicture(mImgURI);
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
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("gg", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("gg", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //mImgURI = getImageUri(this, (Bitmap) data.getExtras().get("data"));
        //bitmap = (Bitmap)data.getExtras().get("data");

        //mImgURI = data.getData();

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImgURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                mImgURI = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
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

    private void cropPicture(Uri slikaPath){ //Bitmap

        pgBar.setVisibility(View.VISIBLE);
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        //Mat src = Imgcodecs.imread(String.valueOf(slikaPath), Imgcodecs.IMREAD_COLOR);
        Log.i("src",String.valueOf(mat));

        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.medianBlur(gray, gray, 5);
        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double)gray.rows()/16, // vrednost za detekcijo kroga v razlicnih razdalijah
                100.0, 30.0, 400, 500); // spremeni zadna dva (min_radius & max_radius) za detekcijo vecjih krogov

        Mat mask = new Mat(mat.rows(), mat.cols(), CvType.CV_8U, Scalar.all(0));

        Point maxPoint = null;
        int maxRadius = 0;
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle outline
            int radius = (int) Math.round(c[2]);
            if (radius > maxRadius){
                maxPoint = center;
                maxRadius = radius;
            }
            Log.d("radius", String.valueOf(radius));
        }
        Imgproc.circle(mask, maxPoint, maxRadius, new Scalar(255,255,255), -1, 8, 0 );

        Mat masked = new Mat();
        mat.copyTo( masked, mask );

        // Apply Threshold
        Mat thresh = new Mat();
        Imgproc.threshold( mask, thresh, 1, 255, Imgproc.THRESH_BINARY );

        // Find Contour
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Crop
        Rect rect = Imgproc.boundingRect(contours.get(0));
        Mat cropped = masked.submat(rect);

        Bitmap nBit = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(cropped, nBit);
        imageView.setImageBitmap(nBit);
        mImgURI = getImageUri(this, nBit);
        bitmap=nBit;

        pgBar.setVisibility(View.INVISIBLE);

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
