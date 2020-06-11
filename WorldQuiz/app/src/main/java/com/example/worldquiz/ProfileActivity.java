package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Model.Ranking;
import com.example.worldquiz.Model.Score;
import com.example.worldquiz.user.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    MaterialEditText udName;
    TextView udUserName, udEmail;
    ProgressBar udprogressBar;
    Button udButton;
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference users,score,ranking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        ranking = database.getReference("Ranking");
        score = database.getReference("Score");

        progressBar = findViewById(R.id.progressbarud);

        udName = (MaterialEditText) findViewById(R.id.udName);
        udUserName = (TextView) findViewById(R.id.udUserName);
        udEmail = (TextView) findViewById(R.id.udEmail);
        udButton = (Button) findViewById(R.id.udButton);

        udName.setText(Common.currentUser.getName());
        udEmail.setText(Common.currentUser.getEmail());
        udUserName.setText(Common.currentUser.getUserName());

        udButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(udName.getText().toString());
            }
        });


    }

    private void updateProfile(final String name) {
        if (name.isEmpty()) {
            udName.setError("Username required");
            udName.requestFocus();
            return;
        }
        else {
            users.child(Common.currentUser.getUserName()).child("name").setValue(name);
            ranking.child(Common.currentUser.getUserName()).child("name").setValue(name);
            score.orderByChild("UserName").equalTo(name)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data:dataSnapshot.getChildren()) {
                                Score score = data.getValue(Score.class);
                                score.setUser(name);
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            users.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final User update = dataSnapshot.child(Common.currentUser.getUserName()).getValue(User.class);
                    Common.currentUser = update;
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Toast.makeText(ProfileActivity.this,"Update Successfully!", Toast.LENGTH_SHORT).show();
        }



    }
}