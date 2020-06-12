package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

public class RankingActivity extends AppCompatActivity {

    RecyclerView rankingList;
    Toolbar toolbar;
    LinearLayoutManager layoutManager;
    FirebaseRecyclerAdapter<Ranking, RankingAdapter> adapter;

    FirebaseDatabase database;
    DatabaseReference score,rankingtable;

    int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

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
        score = database.getReference("Score");
        rankingtable = database.getReference("Ranking");

        updateScore(Common.currentUser.getUserName(), new RankingCallback<Ranking>() {
            @Override
            public void callBack(Ranking ranking) {
                //Updating to ranking table
                rankingtable.child(Common.currentUser.getUserName())
                        .setValue(ranking);
                showRanking();  // Sorting and displaying ranking table
            }
        });

        //Setting adapter

        adapter = new FirebaseRecyclerAdapter<Ranking, RankingAdapter>(
                Ranking.class,
                R.layout.layout_ranking,
                RankingAdapter.class,
                rankingtable.orderByChild("score")
        ) {
            @Override
            protected void populateViewHolder(RankingAdapter rankingAdapter, final Ranking ranking, int i) {
                rankingAdapter.txt_name.setText(ranking.getName());
                rankingAdapter.txt_score.setText(String.valueOf(ranking.getScore()));


                //avoid crashing when clicked
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
        rankingList.setAdapter(adapter);
    }

    private void showRanking() {
        //Print Log to show
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


    private void updateScore(final String name, final RankingCallback<Ranking> callback) {
        score.orderByChild("userName").equalTo(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()) {
                            Score score = data.getValue(Score.class);
                            sum+=Integer.parseInt(score.getScore());
                        }

                        //After summary all score, processing sum variable on Firebase

                        Ranking ranking = new Ranking(Common.currentUser.getName(),sum);
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
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                startActivity(intent);
                break;

        }
        return true;
    }
}