package com.example.worldquiz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.worldquiz.Adapter.ScoreDetailViewHolder;
import com.example.worldquiz.Model.Score;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ScoreDetail extends AppCompatActivity {

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
}