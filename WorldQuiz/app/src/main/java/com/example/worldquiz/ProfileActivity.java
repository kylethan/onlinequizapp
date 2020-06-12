package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

    Toolbar toolbar;
    MaterialEditText udName;
    TextView udUserName, udEmail;
    Button udButton;
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference users,score,ranking;


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
            udName.setError("Name required");
            udName.requestFocus();
            return;
        }
        else {
            users.child(Common.currentUser.getUserName()).child("name").setValue(name);
            ranking.child(Common.currentUser.getUserName()).child("name").setValue(name);
            score.orderByChild("userName").equalTo(Common.currentUser.getUserName())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data:dataSnapshot.getChildren()) {
                                String key = data.getKey();
                                score.child(key).child("user").setValue(name);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scoreboard_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                startActivity(intent);
                break;

        }
        return true;
    }

}