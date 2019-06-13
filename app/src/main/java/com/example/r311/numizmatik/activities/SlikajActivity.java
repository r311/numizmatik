package com.example.r311.numizmatik.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.r311.numizmatik.R;
import com.example.r311.numizmatik.data.Kovanec;
import com.example.r311.numizmatik.data.Lokacija;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SlikajActivity extends AppCompatActivity {

    ImageView imageView;
    private Button btnUpload;
    private Button btnBack;
    private Button btnCompare;
    private Button btnCrop;
    private Button btnOK;
    private EditText txtDrzava;
    private EditText txtVrednost;
    private ProgressBar pgBar;
    private String filePath;

    //lokacija
    Lokacija gLokacija;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;

    // Create a storage reference from our app
    StorageReference storage;

    StorageReference storageRef;
    // Create a reference to "kovanci.jpg"
    StorageReference mountainsRef;
    // Create a reference to 'images/kovanci.jpg'
    StorageReference mountainImagesRef;
    UploadTask uploadTask;
    Uri mImgURI;
    Bitmap bitmap;
    DatabaseReference database;

    Context context;

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

    @SuppressLint("StaticFieldLeak")
    AsyncTask<ArrayList<Kovanec>, Void, Bitmap> compareTask = new AsyncTask<ArrayList<Kovanec>, Void, Bitmap>() {
        @Override
        protected Bitmap doInBackground(ArrayList<Kovanec>... voids) {
            double bestSimiliarity = 0;
            Kovanec bestKovanc = new Kovanec();
            Bitmap bestBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);;
            for(Kovanec k: voids[0]) {
                try {
                    URL url = new URL(k.getSlika());
                    Bitmap image = BitmapFactory.decodeStream(url.openStream());
                    Log.i("ParseIMG", k.getSlika());
                    double similarity = compareHist(image);
                    if(similarity > bestSimiliarity){
                        bestSimiliarity = similarity;
                        bestKovanc = k;
                        bestBitmap = image;
                    }
                } catch (IOException e) {
                    Log.d("ParseIMG", e.toString());
                }
            }

            Log.i("BestKovanc", bestKovanc.getSlika());

            return bestBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap localBitmap) {
            super.onPostExecute(localBitmap);
            pgBar.setVisibility(View.INVISIBLE);
            final Dialog settingsDialog = new Dialog(context);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.popup_image
                    , null));
            ImageView ivPopup = (ImageView) settingsDialog.findViewById(R.id.popupImage);
            ivPopup.setImageBitmap(localBitmap);

            btnOK = (Button) settingsDialog.findViewById(R.id.btnOK);
            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settingsDialog.dismiss();
                }
            });
            settingsDialog.show();
        }
    };

    @SuppressLint("StaticFieldLeak")
    AsyncTask<Void, Void, Void> cropTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            cropPicture(mImgURI);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            imageView.setImageURI(mImgURI);
            pgBar.setVisibility(View.INVISIBLE);
        }
    };

    boolean trigger = true;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slikaj);

        gLokacija = new Lokacija();

        context = this;
        imageView = (ImageView) findViewById(R.id.imageView);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnBack = (Button) findViewById(R.id.btnBackSlikaj);
        btnCompare = (Button) findViewById(R.id.btnCompare);
        btnCrop = (Button) findViewById(R.id.btnCrop);

        txtDrzava = (EditText) findViewById(R.id.txtDrzava);
        txtVrednost = (EditText) findViewById(R.id.txtVrednost);
        pgBar = (ProgressBar) findViewById(R.id.progressBar);

        storage = FirebaseStorage.getInstance().getReference("slike");
        database = FirebaseDatabase.getInstance().getReference("slike");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

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

        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pgBar.setVisibility(View.VISIBLE);
                cropTask.execute();
            }
        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cropPicture(mImgURI);
                pgBar.setVisibility(View.VISIBLE);
                trigger = true;
                final ArrayList<Kovanec> kovSeznam = new ArrayList<Kovanec>();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("slike");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(trigger) {
                            ArrayList<Kovanec> kovancList = new ArrayList<Kovanec>();
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Kovanec k = dataSnapshot1.getValue(Kovanec.class);
                                //compare(k.getSlika());
                                kovancList.add(k);
                            }
                            compareTask.execute(kovancList);
                            trigger = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("DB", "ERROR");
                    }
                });
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
                        Kovanec uploadKovanec = new Kovanec(
                                UUID.randomUUID().toString().replace("-", ""),
                                txtDrzava.getText().toString().trim(),
                                Integer.parseInt(txtVrednost.getText().toString()),
                                uri.toString(),
                                gLokacija
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

    private void cropPicture(Uri pathFile){ //Bitmap
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Log.i("src",String.valueOf(mat));

        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
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
        mImgURI = getImageUri(this, nBit);
        bitmap=nBit;
    }

    private double compareHist(Bitmap compareImage){
        //cropPicture(mImgURI);
        Mat srcBase = new Mat(), hsvBase = new Mat();
        Mat srcTest = new Mat(), hsvTest = new Mat();

        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, srcBase);

        Bitmap bmpp = compareImage.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmpp, srcTest);

        Imgproc.cvtColor(srcBase,hsvBase, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(srcTest,hsvTest, Imgproc.COLOR_BGR2HSV);

        int h_bins = 50, s_bins = 60;
        int[] histSize = {h_bins, s_bins};
        float ranges[] = {0, 180, 0, 256};  //prvi je za HUE, drugi za saturation
        int channels[] = {0, 1};

        Mat histBase = new Mat(), histTest = new Mat();

        List<Mat> hsvBaseList = Arrays.asList(hsvBase);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), histBase, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX);

        List<Mat> hsvTest1List = Arrays.asList(hsvTest);
        Imgproc.calcHist(hsvTest1List, new MatOfInt(channels), new Mat(), histTest, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest, histTest, 0, 1, Core.NORM_MINMAX);

        //double baseBase = Imgproc.compareHist( histBase, histBase, 0);
        double baseTest1 = Imgproc.compareHist( histBase, histTest, 0 );


        //if (1 - baseTest1 >= 1){
        Log.i("Value baseTEST: ", String.valueOf(baseTest1));
        return baseTest1;
       // }
    }

    //za lokacijo
    private void fetchLastLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() +""+
                            currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    gLokacija.setxCrd(currentLocation.getLatitude());
                    gLokacija.setyCrd(currentLocation.getLongitude());
                }
            }
        });
    }
}
