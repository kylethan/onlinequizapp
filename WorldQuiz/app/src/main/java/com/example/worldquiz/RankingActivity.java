package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
    LinearLayoutManager layoutManager;
    FirebaseRecyclerAdapter<Ranking, RankingAdapter> adapter;

    FirebaseDatabase database;
    DatabaseReference score,rankingtable;

    int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rankingList = (RecyclerView) findViewById(R.id.rankingList);
        layoutManager = new LinearLayoutManager(this);
        rankingList.setHasFixedSize(true);
        layoutManager.setReverseLayout(true);   //reverse the list because the firebase showing ascending list
        layoutManager.setStackFromEnd(true);
        rankingList.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        score = database.getReference("Score");
        rankingtable = database.getReference("Ranking");

        updateScore(Common.currentUser.getName(), new RankingCallback<Ranking>() {
            @Override
            public void callBack(Ranking ranking) {
                //Updating to ranking table
                rankingtable.child(ranking.getName())
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
            protected void populateViewHolder(RankingAdapter rankingAdapter, Ranking ranking, int i) {
                rankingAdapter.txt_name.setText(ranking.getName());
                rankingAdapter.txt_score.setText(String.valueOf(ranking.getScore()));


                //avoid crashing when clicked
                rankingAdapter.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

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
        score.orderByChild("user").equalTo(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()) {
                            Score score = data.getValue(Score.class);
                            sum+=Integer.parseInt(score.getScore());
                        }

                        //After summary all score, processing sum variable on Firebase

                        Ranking ranking = new Ranking(name,sum);
                        callback.callBack(ranking);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}