package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.worldquiz.Adapter.CategoryAdapter;
import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Common.SpaceDecoration;
import com.example.worldquiz.DBHelper.DBHelper;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Homepage extends AppCompatActivity {


    RecyclerView recycler_category;
    FirebaseDatabase database;
    DatabaseReference ranking;
    TextView txt_player_name, txt_score;
    CircleImageView profile_image;
    CheckBox checkBox;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

       return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        //Declaring variables
        database = FirebaseDatabase.getInstance();
        ranking = database.getReference().child("Ranking");

        //Setting toolbar and get support as an Action Bar for toolbar
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_settings_white_24dp);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar2);
        myToolbar.setOverflowIcon(drawable);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //Setting Layout manager for adapter
        recycler_category = findViewById(R.id.recyler_category);
        recycler_category.setHasFixedSize(true);
        recycler_category.setLayoutManager(new GridLayoutManager(this,2));


        //Setting Adapter for Category Item
        CategoryAdapter adapter = new CategoryAdapter(Homepage.this, DBHelper.getInstance(this).getAllCategories());
        int spaceInPixel = 4;
        recycler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recycler_category.setAdapter(adapter);

        //Declaring variable
        txt_player_name = (TextView) findViewById(R.id.txt_player_name);
        txt_score = (TextView) findViewById(R.id.txt_score);

        //Get the name and score from "User" path and "Ranking" path in Firebase database ( the data has been get and stored in Common.currentUser and Common.rankingimage when logging in)
        txt_player_name.setText("Welcome, "+Common.currentUser.getName());
        txt_score.setText(String.valueOf(Common.rankingimage.getScore()));

        //Declaring profile Image and get the image from Firebase Storage (the URL is stored in Firebase Database "Ranking" path for each user)
        progressBar = findViewById(R.id.progressbar_homeImage);
        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        Picasso.get().load(Common.rankingimage.getProfileImage()).into(profile_image, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {

            }
        });

        //if the checkbox is checked, the how to play dialog wont appear in next time
        sharedPreferences = getSharedPreferences("howtoplay",0);
        editor = sharedPreferences.edit();
        boolean valueChecked = sharedPreferences.getBoolean("checkbox",false);
        if (!(valueChecked)) {
            showHowToPLayDialog();
        }

    }

    @Override
    public void onBackPressed() {       //Back press function Exit the game without signing out
        moveTaskToBack(true);
    }

    private void showHowToPLayDialog() {        //open how to play dialog when open the game for first time
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Homepage.this);
        alertDialog.setTitle("How to play");
        alertDialog.setMessage(R.string.How_to_play);
        alertDialog.setIcon(R.drawable.question);
        LayoutInflater inflater = this.getLayoutInflater();
        View how_to_play = inflater.inflate(R.layout.layout_how_to_play,null);
        checkBox = how_to_play.findViewById(R.id.howtoplay);
        alertDialog.setView(how_to_play);

        alertDialog.setNegativeButton("Got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                        //if checkbox is checked, login and password will be stored
                editor.putBoolean("checkbox",checkBox.isChecked());     //saving preference
                editor.commit();

            }
        }).create().show();
    }


    public void profileUpdate(View view) {      //Getting into user profile Activity (player's image or player's name onClick)
        Intent intent = new Intent(Homepage.this,ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void Scoreboard(View view) {         //Getting to Ranking Activity (showing total score of user) (scoreboard imageView onclick)
        Intent intent1 = new Intent(Homepage.this,RankingActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent1);

    }

    public void Sharing(View view) {            //Sharing function (Sharing ImageView onclick)
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
            String shareMessage= "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    public void Howtoplay(View view) {      //Showing the rule of the game
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Homepage.this);
        alertDialog.setTitle("How to play");
        alertDialog.setMessage(R.string.How_to_play);
        alertDialog.setIcon(R.drawable.question);
        alertDialog.setNegativeButton("Got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {            //Negative button for dismiss dialog
                dialog.dismiss();

            }
        }).create().show();


    }
}
