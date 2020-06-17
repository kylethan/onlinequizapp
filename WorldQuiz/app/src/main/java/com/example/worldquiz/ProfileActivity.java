package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Model.Ranking;
import com.example.worldquiz.user.User;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    Toolbar toolbar;
    MaterialEditText udName;
    TextView udUserName, udEmail;
    Button udButton;
    ProgressBar progressBar, progressBar_profile;

    private Uri mImageUri;
    private StorageTask mUploadTask;

    FirebaseDatabase database;
    DatabaseReference users,score,ranking, profileImage;

    StorageReference storageReference;
    CircleImageView circleImageView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbarud);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);




        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        ranking = database.getReference("Ranking");
        score = database.getReference("Score");
        profileImage = database.getReference("ProfileImage");

        storageReference = FirebaseStorage.getInstance().getReference(Common.currentUser.getUserName());

        progressBar = findViewById(R.id.progressbarud);
        progressBar_profile = findViewById(R.id.progressbar_profile);

        circleImageView = (CircleImageView) findViewById(R.id.profile_image);

        udName = (MaterialEditText) findViewById(R.id.udName);
        udUserName = (TextView) findViewById(R.id.udUserName);
        udEmail = (TextView) findViewById(R.id.udEmail);
        udButton = (Button) findViewById(R.id.udButton);

        udName.setText(Common.currentUser.getName());
        udEmail.setText(Common.currentUser.getEmail());
        udUserName.setText(Common.currentUser.getUserName());

        udButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       //setting upload new photo image to Firebase Storage and setting new Name to Firebase Database ("User" path, "Ranking" path,"Score" path if available)
                progressBar.setVisibility(View.VISIBLE);
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(ProfileActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
                updateProfile(udName.getText().toString());
            }
        });

        Picasso.get().load(Common.rankingimage.getProfileImage()).into(circleImageView, new Callback() {        //setting image with imageURL stored in Common.rankingimage using Picasso library
            @Override
            public void onSuccess() {
                progressBar_profile.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {

            }
        });


    }


    private void updateProfile(final String name) {     //Updating name of user in "User" path, "Ranking" path,"Score" path if available
        if (name.isEmpty()) {                   //Check if field is empty
            udName.setError("Name required");
            udName.requestFocus();
            return;
        }
        else {
            users.child(Common.currentUser.getUserName()).child("name").setValue(name);         //setting new name to user user's child which is "name" (the key is their username)
            ranking.child(Common.currentUser.getUserName()).child("name").setValue(name);       //setting new name to user ranking's child which is "name" (the key is their username)


            score.orderByChild("userName").equalTo(Common.currentUser.getUserName())        //child in "score" path is named by their username and their CategoryID that they play, this function helps to set the new name to all of their data in "Score" if available
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data:dataSnapshot.getChildren()) {
                                String key = data.getKey();
                                score.child(key).child("user").setValue(name);          //setting new name
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            users.addListenerForSingleValueEvent(new ValueEventListener() {         //update new data forCommon.currentUser after they change their name

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final User update = dataSnapshot.child(Common.currentUser.getUserName()).getValue(User.class);
                    Common.currentUser = update;
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Toast.makeText(ProfileActivity.this,"Update Name Successfully!", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scoreboard_menu,menu);     //getting menu (this is a empty menu, use it to call out getBack to Home function)
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {         //Returning to Homepage
            case android.R.id.home:

                Intent intent = new Intent(getApplicationContext(), Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                startActivity(intent);
                finish();
                break;

        }
        return true;
    }

    public void BrowseImages(View view) {       //Open Gallery on device to get the image for user profile image (user profile image onClick)
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {         //getting the image and setting it on user profile image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(circleImageView);            //setting new image to user profile image using Picasso library
        }
    }

    private void uploadFile() {             //uploading the image to Firebase Storage
        if (mImageUri != null)
        {
            storageReference.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {       //uploading the image to Firebase Storage
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()          //add on complete listener to get the download Url
            {
                private static final String TAG = "yes";

                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();             //retrieving download Url
                        Log.e(TAG, "then: " + downloadUri.toString());

                        ranking.child(Common.currentUser.getUserName()).child("profileImage").setValue(downloadUri.toString());         //set it to "profile image" child of user ranking data

                        ranking.addListenerForSingleValueEvent(new ValueEventListener() {       //open "ranking" listen to store the new value to Common.rankingimage
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                progressBar.setVisibility(View.GONE);
                                Ranking rank1 = dataSnapshot.child(Common.currentUser.getUserName()).getValue(Ranking.class);           //getting new value
                                Common.rankingimage = rank1;                                                                            //storing it in Common.ranking image to perform new image in homepage and ranking scoreboard
                                Toast.makeText(ProfileActivity.this, "Change image successfully",Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    } else
                    {
                        Toast.makeText(ProfileActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();      //notice if upload function is fail
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();        //notice if there is no filed selected from user
        }
    }

    public void SignOut(View view) {        //Signing out function (Signout textview and imageView OnClick)
        new BottomDialog.Builder(this)
                .setTitle("Signing Out?")
                .setIcon(R.drawable.dissatisfied)
                .setContent("You will be missed...!")
                .setNegativeText("No...")
                .onNegative(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        bottomDialog.dismiss();
                    }
                })
                .setPositiveText("Yes!")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        bottomDialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        Intent intent2 = new Intent(ProfileActivity.this,MainActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                    }
                }).show();

    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(getApplicationContext(), Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
        startActivity(intent);

    }
}

