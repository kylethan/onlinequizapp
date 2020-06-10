package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.user.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {
    MaterialEditText newName, newUserName, newPassword, newEmail;   //signup function
    MaterialEditText userName, password;                //signin function
    Button bsignin, bsignup;
    ProgressBar progressBar,progressBar1;
    CheckBox checkBox;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase database;
    DatabaseReference users;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        fAuth = FirebaseAuth.getInstance();

        progressBar1 = findViewById(R.id.progressbar1);
        checkBox = findViewById(R.id.checkBox);



        userName = (MaterialEditText) findViewById(R.id.UserName);
        password = (MaterialEditText) findViewById(R.id.Password);

        bsignin = findViewById(R.id.signin);
        bsignup = findViewById(R.id.signup);

        sharedPreferences = getSharedPreferences("Login",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        checkPreferences();

        bsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignUpDialog();
            }
        });

        bsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkBox.isChecked()) {
                    editor.putString("email",userName.getText().toString());
                    editor.putString("password",password.getText().toString());
                    editor.commit();
                }
                else {
                    editor.putString("email","");
                    editor.putString("password","");
                    editor.commit();
                }
                editor.putBoolean("checkbox",checkBox.isChecked());
                editor.commit();

                signIn(userName.getText().toString().trim(),password.getText().toString().trim());

            }
        });

    }



    private void signIn(final String user, final String pass) {

        if (user.isEmpty()) {
            userName.setError("Username required");
            userName.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            password.setError("Password required");
            password.requestFocus();
            return;
        }
        progressBar1.setVisibility(View.VISIBLE);
        users.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar1.setVisibility(View.VISIBLE);
                if(dataSnapshot.child(user).exists()) {

                        final User login = dataSnapshot.child(user).getValue(User.class);
                        fAuth.signInWithEmailAndPassword(login.getEmail(),pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar1.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Common.currentUser = login;
                                    startActivity(new Intent(getApplicationContext(),Homepage.class));

                                }
                                else
                                    Toast.makeText(MainActivity.this,"Wrong password", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
                else {
                    progressBar1.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,"User does not exist", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showSignUpDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Sign Up");
        alertDialog.setMessage("Please fulfill the information");

        LayoutInflater inflater = this.getLayoutInflater();
        View sign_up_layout = inflater.inflate(R.layout.sign_up_layout,null);

        newName = (MaterialEditText)sign_up_layout.findViewById(R.id.newName);
        newUserName = (MaterialEditText)sign_up_layout.findViewById(R.id.newUserName);
        newPassword = (MaterialEditText)sign_up_layout.findViewById(R.id.newPassword);
        newEmail = (MaterialEditText)sign_up_layout.findViewById(R.id.newEmail);
        progressBar = sign_up_layout.findViewById(R.id.progressbar);


        alertDialog.setView(sign_up_layout);
        alertDialog.setIcon(R.drawable.ic_account_circle_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = alertDialog.create();
        dialog.show();
//Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                final String name = newName.getText().toString().trim();
                final String username = newUserName.getText().toString().trim();
                final String passw = newPassword.getText().toString().trim();
                final String email = newEmail.getText().toString().trim();

                if (name.isEmpty()) {
                    newName.setError("Name required");
                    newName.requestFocus();
                    return;
                }

                if (username.isEmpty()) {
                    newUserName.setError("Username required");
                    newUserName.requestFocus();
                    return;
                }

                if (passw.isEmpty()) {
                    newPassword.setError("Password required");
                    newPassword.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    newEmail.setError("Email required");
                    newEmail.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    newEmail.setError("Please enter a valid email");
                    newEmail.requestFocus();
                    return;
                }
                else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    final User user = new User(newName.getText().toString().trim(),
                            newUserName.getText().toString().trim(),
                            newEmail.getText().toString().trim());
                    users.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.child(user.getUserName()).exists()){
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this,"User already existed!",Toast.LENGTH_SHORT).show();

                            }
                            else {
                                fAuth.createUserWithEmailAndPassword(email,passw)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {

                                                if (task.isSuccessful()) {
                                                    users.child(user.getUserName())
                                                            .setValue(user);
                                                    progressBar.setVisibility(View.GONE);
                                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                    Toast.makeText(MainActivity.this, "User Register successfully!",Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();

                                                }
                                                else {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void checkPreferences() {
        String mail = sharedPreferences.getString("email","");
        String pass = sharedPreferences.getString("password","");
        userName.setText(mail);
        password.setText(pass);
        boolean valueChecked = sharedPreferences.getBoolean("checkbox",false);
        checkBox.setChecked(valueChecked);
    }

}
