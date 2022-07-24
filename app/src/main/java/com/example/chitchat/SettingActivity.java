package com.example.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView mCircleImageView;
    private TextView mDisplayName;
    private TextView mStatus;

    private DatabaseReference mDatabaseReference;

    private StorageReference mStorageReference;

    String status = "";

    private static final int GALLERY_PICK = 1;
    String uid;
    ProgressDialog mProgressDialog;
    byte[] thumb_bytes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mCircleImageView = findViewById(R.id.display_image);
        mDisplayName = findViewById(R.id.textViewDisplayname);
        mStatus = findViewById(R.id.textViewStatus);
        mProgressDialog = new ProgressDialog(this);

        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = mFirebaseUser.getUid();

        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mDatabaseReference.keepSynced(true);

        mStorageReference = FirebaseStorage.getInstance().getReference();


        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name  = (String) dataSnapshot.child("name").getValue();
                status = (String)dataSnapshot.child("status").getValue();
                final String image = (String)dataSnapshot.child("image").getValue();
                String thumb = (String)dataSnapshot.child("thumb_image").getValue();

                mDisplayName.setText(name);
                mStatus.setText(status);


                if(!image.equals("default"))
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).
                            placeholder(R.drawable.user_img).into(mCircleImageView, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.user_img).into(mCircleImageView);
                        }

                    });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
   public void buttonIsClicked(View view) {

       switch(view.getId()) {

           case R.id.buttonChangeImage:

               Intent galleryIntent = new Intent();
               galleryIntent.setType("image/*");
               galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
               startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
               break;

           case R.id.buttonChangeStatus:
               Intent intent  = new Intent(SettingActivity.this, StatusActivity.class);
               intent.putExtra("current_status", status);
               startActivity(intent);
               break;

           default:
               break;

       }
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri sourceUri = data.getData();

            CropImage.activity(sourceUri).
                    setAspectRatio(1, 1).
                    setMinCropWindowSize(500, 500).
                    start(SettingActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while we process and upload the image...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.show();


                Uri resultUri = result.getUri();
                File thumb_filepath = new File(resultUri.getPath());
                try {
                    Bitmap thumb_bitmap = new Compressor(this).
                            setMaxWidth(200).
                            setMaxHeight(200).
                            setQuality(75).
                            compressToBitmap(thumb_filepath);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                     thumb_bytes= outputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                StorageReference filepath = mStorageReference.child("profile_image").child(uid+".jpg");
                final StorageReference thumb_file_path = mStorageReference.child("profile_image").child("thumbs").child(uid+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {

                        @SuppressWarnings("VisibleForTests")
                       final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                        UploadTask uploadTask = thumb_file_path.putBytes(thumb_bytes);

                        uploadTask.addOnCompleteListener(thumb_task -> {
                            @SuppressWarnings("VisibleForTests")
                            String thumb_download_url = thumb_task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                            if(thumb_task.isSuccessful()){
                                Map<String, Object> update_HashMap = new HashMap<>();
                                update_HashMap.put("image", downloadUrl);
                                update_HashMap.put("thumb_image", thumb_download_url);

                                mDatabaseReference.updateChildren(update_HashMap).addOnCompleteListener(task1 -> {

                                    if(task1.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingActivity.this, "Uploaded Successfully...", Toast.LENGTH_SHORT).show();

                                    }
                                    else{
                                        mProgressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), " Image is not uploading...", Toast.LENGTH_SHORT).show();
                                    }

                                });

                            }
                            else{
                                mProgressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), " Error in uploading Thumbnail..", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                    else{
                        mProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), " Image is not uploading...", Toast.LENGTH_SHORT).show();
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
