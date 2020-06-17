package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Model.Ranking;
import com.example.worldquiz.user.User;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    MaterialEditText newName, newUserName, newPassword, newEmail;   //signup function
    MaterialEditText userName, password;                //signin function
    MaterialEditText forget_pass_info;      //forget password function
    Button bsignin, bsignup;
    ProgressBar progressBar,progressBar1,progressBar_forget, progressBar_auto;
    CheckBox checkBox;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase database;
    DatabaseReference users,ranking;
    FirebaseAuth fAuth;

    LinearLayout linearLayout;

    TextView forgetpass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        ranking = database.getReference("Ranking");
        fAuth = FirebaseAuth.getInstance();

        progressBar_auto = findViewById(R.id.progressBar_auto);
        linearLayout = findViewById(R.id.layout_auto);

        progressBar1 = findViewById(R.id.progressbar1);
        checkBox = findViewById(R.id.checkBox);

        forgetpass = findViewById(R.id.forget_pass);

        userName = (MaterialEditText) findViewById(R.id.UserName);
        password = (MaterialEditText) findViewById(R.id.Password);

        bsignin = findViewById(R.id.signin);
        bsignup = findViewById(R.id.signup);


        //declare using saved preference for Remember Me function
        sharedPreferences = getSharedPreferences("Login",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        checkPreferences();

        final String autologin = sharedPreferences.getString("autologin","");

        if (fAuth.getCurrentUser() != null) {       //if user is not logging out, the app will go straight to homepage
            progressBar_auto.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            //Disable user interaction when auto-logging in
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            users.addListenerForSingleValueEvent(new ValueEventListener() {              //add Listener for "user" path in firebase database
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User login = dataSnapshot.child(autologin).getValue(User.class);           //get user value using their user name in "user" path base on User.class
                    ranking.addListenerForSingleValueEvent(new ValueEventListener() {               //add Listener for "Ranking" path in firebase database
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final Ranking rank = dataSnapshot.child(autologin).getValue(Ranking.class);      //get user value using their user name in "user" path base on User.class
                            // in order to retrieve the photo URL stored in "Ranking" path and display it as user profile photo
                            Common.currentUser = login;                 //saving user value in Common.currentUser (for getting current player's username, name,etc)
                            Common.rankingimage = rank;                 //saving ranking value in Common.rankingimage (for getting user profile photo, total score;  display it in Homepage and UserProfile Activity)
                            startActivity(new Intent(MainActivity.this,Homepage.class));
                            progressBar_auto.setVisibility(View.GONE);
                            linearLayout.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }

        //Open Sign Up dialog
        bsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpDialog();
            }
        });

        //Login function
        bsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("autologin",userName.getText().toString());        //save username for auto-logging in method
                editor.commit();

                if (checkBox.isChecked()) {         //if checkbox is checked, login and password will be stored
                    editor.putString("username",userName.getText().toString());
                    editor.putString("password",password.getText().toString());
                    editor.commit();
                }
                else {
                    editor.putString("username","");
                    editor.putString("password","");
                    editor.commit();
                }
                editor.putBoolean("checkbox",checkBox.isChecked());     //saving preference
                editor.commit();

                signIn(userName.getText().toString().trim(),password.getText().toString().trim());      //Sign In function

            }
        });


        //Forget password function
        forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });
    }


    private void forgetPassword() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);     //Open Alert Dialog
        alertDialog.setTitle("Recover Password");
        alertDialog.setMessage("We are here to help!");

        LayoutInflater inflater = this.getLayoutInflater();
        final View forget_pass_layout = inflater.inflate(R.layout.forget_pass_layout,null);       //get forget_password_layout

        forget_pass_info = (MaterialEditText)forget_pass_layout.findViewById(R.id.forget_pass_info);
        progressBar_forget = forget_pass_layout.findViewById(R.id.progressbar_forget);


        alertDialog.setView(forget_pass_layout);
        alertDialog.setIcon(R.drawable.ic_account_circle_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {        //Negative Button to dismiss dilog
                dialog.dismiss();
            }
        });

        alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {     //Positive Button for Submit
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog dialog = alertDialog.create();            //open dialog for set up Positive button function
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {       //setting up positive button

                final String info = forget_pass_info.getText().toString().trim();

                if (info.isEmpty()) {                                                   //check if edit text is empty
                    forget_pass_info.setError("Username or Email required");
                    forget_pass_info.requestFocus();
                    return;
                }
                else
                {   forget_pass_info.setEnabled(false);     //set edit text disable
                    progressBar_forget.setVisibility(View.VISIBLE);
                    users.addListenerForSingleValueEvent(new ValueEventListener() {             //add Listener for "users" in database

                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            fAuth.sendPasswordResetEmail(info).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {          // open retrieving email for password reset function
                                    progressBar_forget.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {                                          //if the text is an email then send the reset password email
                                        Toast.makeText(MainActivity.this, "Check your email for Password Reset", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    } else if (dataSnapshot.child(info).exists()) {                 //otherwise check the username for child key of "users" path

                                        final User forget = dataSnapshot.child(info).getValue(User.class);              //get user value using their user name in "users" path base on User.class
                                        fAuth.sendPasswordResetEmail(forget.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {      //get email from retrieved value and send the password reset request to that email
                                                progressBar_forget.setVisibility(View.GONE);
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MainActivity.this, "Check your email for Password Reset", Toast.LENGTH_LONG).show();
                                                    dialog.dismiss();
                                                } else {
                                                    forget_pass_info.setEnabled(true);
                                                    Toast.makeText(MainActivity.this,"Username does not exist", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        forget_pass_info.setEnabled(true);  //set edit text enable again
                                        //if the input is not email or username that stored on database then notice to user
                                        Toast.makeText(MainActivity.this, "Email does not register or Username does not exist", Toast.LENGTH_LONG).show();
                                    }
                                }



                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }


    private void signIn(final String user, final String pass) {         //Sign in function

        if (user.isEmpty()) {       //check if the field is empty
            userName.setError("Username required");
            userName.requestFocus();
            return;
        }

        if (pass.isEmpty()) {       //check if the field is empty
            password.setError("Password required");
            password.requestFocus();
            return;
        }

        progressBar1.setVisibility(View.VISIBLE);
        userName.setEnabled(false);     //disable edit text when login button is clicked
        password.setEnabled(false);     //disable edit text when login button is clicked
        users.addListenerForSingleValueEvent(new ValueEventListener() {              //add Listener for "user" path in firebase database

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar1.setVisibility(View.VISIBLE);
                if(dataSnapshot.child(user).exists()) {             //Check if intput exist as a child name in firebase database
                    final User login = dataSnapshot.child(user).getValue(User.class);           //get user value using their user name in "user" path base on User.class
                    ranking.addListenerForSingleValueEvent(new ValueEventListener() {               //add Listener for "Ranking" path in firebase database
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(user).exists()) {

                                final Ranking rank = dataSnapshot.child(user).getValue(Ranking.class);      //get user value using their user name in "user" path base on User.class
                                                                                                            // in order to retrieve the photo URL stored in "Ranking" path and display it as user profile photo

                                fAuth.signInWithEmailAndPassword(login.getEmail(),pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar1.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Common.currentUser = login;                 //saving user value in Common.currentUser (for getting current player's username, name,etc)
                                            Common.rankingimage = rank;                 //saving ranking value in Common.rankingimage (for getting user profile photo and display it in Homepage and UserProfile Activity)
                                            startActivity(new Intent(getApplicationContext(),Homepage.class));      //start activity when authentication successful

                                        }
                                        else
                                        {   progressBar1.setVisibility(View.GONE);
                                            userName.setEnabled(true);     //set edit text enable
                                            password.setEnabled(true);     //set edit text enable
                                            Toast.makeText(MainActivity.this,"Wrong password", Toast.LENGTH_SHORT).show();      //notice user if password is wrong

                                        }
                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                }
                else {
                    userName.setEnabled(true);     //set edit text enable
                    password.setEnabled(true);     //set edit text enable
                    progressBar1.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,"User does not exist", Toast.LENGTH_SHORT).show();         //notice if user does not stored (or register) in firebase

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showSignUpDialog() {               //Sign Up function
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);     //Open dialog for doing the Sign up function
        alertDialog.setTitle("Sign Up");
        alertDialog.setMessage("Please fulfill the information");

        LayoutInflater inflater = this.getLayoutInflater();
        View sign_up_layout = inflater.inflate(R.layout.sign_up_layout,null);       //getting layout from Sign_up_layout

        newName = (MaterialEditText)sign_up_layout.findViewById(R.id.newName);
        newUserName = (MaterialEditText)sign_up_layout.findViewById(R.id.newUserName);
        newPassword = (MaterialEditText)sign_up_layout.findViewById(R.id.newPassword);
        newEmail = (MaterialEditText)sign_up_layout.findViewById(R.id.newEmail);
        progressBar = sign_up_layout.findViewById(R.id.progressbar);



        alertDialog.setView(sign_up_layout);                                //set layout for view
        alertDialog.setIcon(R.drawable.ic_account_circle_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {            //Negative button for dismiss dialog (cancel register function)
                dialog.dismiss();
            }
        });

        alertDialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {           //Setting positive button (OnCLickListener)
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                final String name = newName.getText().toString().trim();                //retrieving data from edit text
                final String username = newUserName.getText().toString().trim();        //retrieving data from edit text
                final String passw = newPassword.getText().toString().trim();       //retrieving data from edit text
                final String email = newEmail.getText().toString().trim();      //retrieving data from edit text

                if (name.isEmpty()) {               //Check if field is empty
                    newName.setError("Name required");
                    newName.requestFocus();
                    return;
                }

                if (username.isEmpty()) {             //Check if field is empty
                    newUserName.setError("Username required");
                    newUserName.requestFocus();
                    return;
                }

                if (passw.isEmpty()) {                //Check if field is empty
                    newPassword.setError("Password required");
                    newPassword.requestFocus();
                    return;
                }

                if (email.isEmpty()) {                //Check if field is empty
                    newEmail.setError("Email required");
                    newEmail.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {         //check if email pattern is correct
                    newEmail.setError("Please enter a valid email");
                    newEmail.requestFocus();
                    return;
                }
                else
                {
                    newName.setEnabled(false);          //disable edit text when register button is clicked
                    newUserName.setEnabled(false);      //disable edit text when register button is clicked
                    newPassword.setEnabled(false);      //disable edit text when register button is clicked
                    newEmail.setEnabled(false);         //disable edit text when register button is clicked
                    progressBar.setVisibility(View.VISIBLE);
                    final User user = new User(newName.getText().toString().trim(),             //Creating user model (User.class model) to retrieve the input
                            newUserName.getText().toString().trim(),
                            newEmail.getText().toString().trim());
                    final Ranking rankinguser = new Ranking(newName.getText().toString(),0,Common.imageurl);        //Creating a rankinguser model to retrieve inpput (create player data in "ranking" path if register successful)
                    users.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.child(user.getUserName()).exists()){            //Check if the user is exist or not
                                progressBar.setVisibility(View.GONE);
                                newName.setEnabled(true);          //enable edit text again
                                newUserName.setEnabled(true);      //enable edit text again
                                newPassword.setEnabled(true);      //enable edit text again
                                newEmail.setEnabled(true);        //enable edit text again
                                Toast.makeText(MainActivity.this,"User already existed!",Toast.LENGTH_SHORT).show();

                            }
                            else {
                                fAuth.createUserWithEmailAndPassword(email,passw)       //if not, creating email/password authentication on Firebase Autentication
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {

                                                if (task.isSuccessful()) {

                                                    users.child(user.getUserName())
                                                            .setValue(user);            //creating user with User.class model in "user" path of Firebase Database
                                                    ranking.child(user.getUserName()).setValue(rankinguser);        //creating user data with Ranking.class model in "ranking" path of Firebase Database
                                                    progressBar.setVisibility(View.GONE);
                                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                    Toast.makeText(MainActivity.this, "User Register successfully!",Toast.LENGTH_SHORT).show();     //Notice user if register successfully
                                                    dialog.dismiss();                       //dismiss dialog after register successful

                                                }
                                                else {
                                                    newName.setEnabled(true);          //enable edit text again
                                                    newUserName.setEnabled(true);      //enable edit text again
                                                    newPassword.setEnabled(true);      //enable edit text again
                                                    newEmail.setEnabled(true);        //enable edit text again
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show(); //notice user if error occur
                                                }
                                            }
                                        });

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

    }

    private void checkPreferences() {           //Get and set data for login field if Remember Me function is enabled
        String mail = sharedPreferences.getString("username","");
        String pass = sharedPreferences.getString("password","");
        userName.setText(mail);
        password.setText(pass);
        boolean valueChecked = sharedPreferences.getBoolean("checkbox",false);
        checkBox.setChecked(valueChecked);
    }



}
