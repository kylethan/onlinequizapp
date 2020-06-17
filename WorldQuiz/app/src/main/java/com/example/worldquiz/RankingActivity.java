package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.worldquiz.Adapter.RankingAdapter;
import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Interface.ItemClickListener;
import com.example.worldquiz.Interface.RankingCallback;
import com.example.worldquiz.Model.Ranking;
import com.example.worldquiz.Model.Score;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class RankingActivity extends AppCompatActivity {

    RecyclerView rankingList;
    Toolbar toolbar;
    LinearLayoutManager layoutManager;
    FirebaseRecyclerAdapter<Ranking, RankingAdapter> adapter;       //Firebase recycler adapter for displaying score of all users

    FirebaseDatabase database;
    DatabaseReference score,rankingtable;

    int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        //declaring toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarsb);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        rankingList = (RecyclerView) findViewById(R.id.rankingList);
        layoutManager = new LinearLayoutManager(this);
        rankingList.setHasFixedSize(true);
        layoutManager.setReverseLayout(true);   //reverse the list because the firebase showing ascending list
        layoutManager.setStackFromEnd(true);
        rankingList.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        score = database.getReference("Score");             //get database path
        rankingtable = database.getReference("Ranking");      //get database path


        updateScore(Common.currentUser.getUserName(), new RankingCallback<Ranking>() {
            @Override
            public void callBack(Ranking ranking) {
                //Updating to ranking table
                rankingtable.child(Common.currentUser.getUserName())
                        .setValue(ranking);
                showRanking();  // Sorting and displaying ranking table
            }
        });


        //setting Firebase Recycler Adapter version 2.3.0.
        adapter = new FirebaseRecyclerAdapter<Ranking, RankingAdapter>(
                Ranking.class,
                R.layout.layout_ranking,
                RankingAdapter.class,
                rankingtable.orderByChild("score").limitToLast(6)       //Limited 6 users in the ranking scoreboard
        ) {
            @Override
            protected void populateViewHolder(final RankingAdapter rankingAdapter, final Ranking ranking, int i) {

                Picasso.get().load(ranking.getProfileImage()).into(rankingAdapter.imageView, new Callback() {          //Loading user profile image.
                    @Override
                    public void onSuccess() {
                        rankingAdapter.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

                int realRank = 6 - rankingAdapter.getAdapterPosition();
                rankingAdapter.txt_ranking.setText(String.valueOf(realRank));           //setting ranking number

                rankingAdapter.txt_name.setText(ranking.getName());             //setting user's name
                rankingAdapter.txt_score.setText(String.valueOf(ranking.getScore()));           // user's total score

                //moving to Score Detail Activity with name of user (showing their score in every category they played)
                rankingAdapter.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent scoreDetail = new Intent(RankingActivity.this, ScoreDetail.class);
                        scoreDetail.putExtra("viewUser",ranking.getName());
                        startActivity(scoreDetail);

                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        rankingList.setAdapter(adapter);    //setting adapter

    }

    private void showRanking() {
        //Checking Log if the user is updated (testing to check if user is updated, this function can be disable)
        rankingtable.orderByChild("score")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()) {
                            Ranking local = data.getValue(Ranking.class);
                            Log.d("DEBUG",local.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    //updating score for user
    private void updateScore(final String name, final RankingCallback<Ranking> callback) {
        score.orderByChild("userName").equalTo(name)        //find the user base on user's username
                .addListenerForSingleValueEvent(new ValueEventListener() {      //getting every score in each category stored on Database and sum it)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()) {
                            Score score = data.getValue(Score.class);
                            sum+=Integer.parseInt(score.getScore());

                        }

                        //After summary all score, processing sum variable on Firebase
                        Ranking ranking = new Ranking(Common.currentUser.getName(),sum,Common.rankingimage.getProfileImage());
                        callback.callBack(ranking);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scoreboard_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:     //getting back to Homepage
                finish();
                Intent intent = new Intent(getApplicationContext(), Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                startActivity(intent);
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(getApplicationContext(), Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
        startActivity(intent);

    }
}