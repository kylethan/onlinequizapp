package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.worldquiz.Adapter.ScoreDetailViewHolder;
import com.example.worldquiz.Model.Ranking;
import com.example.worldquiz.Model.Score;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ScoreDetail extends AppCompatActivity {

    Toolbar toolbar;

    FirebaseDatabase database;
    DatabaseReference score;

    RecyclerView scoreList;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Score, ScoreDetailViewHolder> adapter;

    String viewUser="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar_category_sb);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Firebase
        database = FirebaseDatabase.getInstance();
        score =database.getReference("Score");

        //view
        scoreList = (RecyclerView) findViewById(R.id.scoreList);
        scoreList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        scoreList.setLayoutManager(layoutManager);



        if (getIntent()!=null)
            viewUser=getIntent().getStringExtra("viewUser");
        if (!viewUser.isEmpty())
            loadScoreDetail(viewUser);
    }

    private void loadScoreDetail(String viewUser) {
        adapter = new FirebaseRecyclerAdapter<Score, ScoreDetailViewHolder>(
                Score.class,
                R.layout.layout_score_detail,
                ScoreDetailViewHolder.class,
                score.orderByChild("user").equalTo(viewUser)
        ) {
            @Override
            protected void populateViewHolder(ScoreDetailViewHolder scoreDetailViewHolder, Score score, int i) {
                scoreDetailViewHolder.txt_cname.setText(score.getCategoryName());
                scoreDetailViewHolder.txt_cscore.setText(score.getScore());
            }
        };
        adapter.notifyDataSetChanged();
        scoreList.setAdapter(adapter);
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
                Intent intent = new Intent(getApplicationContext(), RankingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                startActivity(intent);
                break;

        }
        return true;
    }
}