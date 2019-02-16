package com.samrat.firebaseimageupload;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button btnChooseImg,btnUpload;  //mbuttonChooseImage,mButtonUpload
    private TextView showUpload;            //mTextViewShowUploads
    private EditText fileName;              //mEditTextFileName
    private ImageView imgView;              //mImageName
    private ProgressBar progressBar;        //mProgressBar

    private Uri imgUri;                     //mImageUri

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChooseImg = findViewById(R.id.btn_choose_img);
        btnUpload = findViewById(R.id.btn_upload);
        showUpload = findViewById(R.id.text_view_show_uploads);
        fileName = findViewById(R.id.edit_text_file_name);
        imgView = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);

        //The folder name in Firebase is "uploads"
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                }else {
                    uploadFile();
                }
            }
        });
        showUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesACtivity();
            }
        });
    }

    private  void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST  && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imgUri = data.getData();

            Picasso.with(this).load(imgUri).into(imgView);
            //imgView.setImageURI(imgUri);      //this is used if we are not using picasso
        }
    }

    //This method is used to get file extension from the image
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if (imgUri != null){
            StorageReference fileRef = mStorageRef.child(System.currentTimeMillis()
            +"."+getFileExtension(imgUri));

            uploadTask = fileRef.putFile(imgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //To delay the progessBar coming back to zero after a file upload
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            },2000);

                            Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();

                            Upload upload = new Upload(fileName.getText().toString().trim(),
                                    taskSnapshot.getDownloadUrl().toString());

                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    //The following method only gives progess
                    double progress = (100.0* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                }
            });
        }else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagesACtivity(){
        Intent intent = new Intent(getApplicationContext(),ImagesActivity.class);
        startActivity(intent);
    }
}




