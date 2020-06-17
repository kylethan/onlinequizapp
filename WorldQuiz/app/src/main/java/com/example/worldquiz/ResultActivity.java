package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.worldquiz.Adapter.ResultGridAdapter;
import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Common.SpaceDecoration;
import com.example.worldquiz.Model.Ranking;
import com.example.worldquiz.Model.Score;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class ResultActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView txt_timer, txt_result, txt_right_answer, txt_score;
    Button btn_filter_total,btn_filter_right,btn_filter_wrong,btn_filter_no_answer;
    RecyclerView recyclerView_result;

    ResultGridAdapter adapter, filtered_adapter;
    FirebaseDatabase database;
    DatabaseReference score;

    int sum = 0;

    BroadcastReceiver backToQuestion = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {                     //using broadcast receiver to get back to the specific question when click to a Result Grid item (ResultGridAdapter)
            if (intent.getAction().toString().equals(Common.KEY_BACK_FROM_RESULT)) {
                int question = intent.getIntExtra(Common.KEY_BACK_FROM_RESULT,-1);
                goBackActivityWithQuestion(question);
            }
        }


    };
    private void goBackActivityWithQuestion(int question) { //go back to Question Activity with a key - KEY_BACK_FROM_RESULT to specify to a question fragment
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Common.KEY_BACK_FROM_RESULT,question);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        LocalBroadcastManager.getInstance(this)         //declare Broadcast Manager
                .registerReceiver(backToQuestion, new IntentFilter(Common.KEY_BACK_FROM_RESULT));

        toolbar = (Toolbar) findViewById(R.id.toolbar3);
        toolbar.setTitle("RESULT");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_result = (TextView) findViewById(R.id.txt_result);
        txt_right_answer = (TextView) findViewById(R.id.txt_right_answer);
        txt_timer = (TextView) findViewById(R.id.txt_time);
        txt_score = (TextView) findViewById(R.id.txt_score);

        btn_filter_no_answer = (Button) findViewById(R.id.btn_filter_no_answer);
        btn_filter_right = (Button) findViewById(R.id.btn_filter_right_answer);
        btn_filter_wrong = (Button) findViewById(R.id.btn_filter_wrong_answer);
        btn_filter_total = (Button) findViewById(R.id.btn_filter_total);

        recyclerView_result = (RecyclerView) findViewById(R.id.recycler_result);
        recyclerView_result.setHasFixedSize(true);
        recyclerView_result.setLayoutManager(new GridLayoutManager(this,4));

        //Setting Adapter
        adapter = new ResultGridAdapter(this,Common.answerSheetList);
        recyclerView_result.addItemDecoration(new SpaceDecoration(4));
        recyclerView_result.setAdapter(adapter);


        //Calculate the time player used to finish the game
        txt_timer.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Common.timer),
                TimeUnit.MILLISECONDS.toSeconds(Common.timer) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Common.timer))));

        //Calculate score
        int totalscore = Common.right_answer_count*10;
        txt_score.setText(new StringBuilder("").append(totalscore));

        txt_right_answer.setText(new StringBuilder("").append(Common.right_answer_count).append("/")
        .append(Common.questionList.size()));

        //set text on buttons
        btn_filter_total.setText(new StringBuilder("").append(Common.questionList.size()));
        btn_filter_right.setText(new StringBuilder("").append(Common.right_answer_count));
        btn_filter_wrong.setText(new StringBuilder("").append(Common.wrong_answer_count));
        btn_filter_no_answer.setText(new StringBuilder("").append(Common.no_answer_count));

        //Calculate result
        int percent = (Common.right_answer_count*100/Common.questionList.size());
        if (percent > 90)
            txt_result.setText("EXCELLENT");
        else if (percent > 70)
            txt_result.setText("GOOD");
        else if (percent > 50)
            txt_result.setText("PASS");
        else if (percent < 50)
            txt_result.setText("FAIL");

        //Setting filter button event showing related result list such as total, right, wrong, and no answers
        btn_filter_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                           //showing all answer grid items
                if (adapter == null) {
                    adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetList);
                    recyclerView_result.setAdapter(adapter);
                }
                else
                    recyclerView_result.setAdapter(adapter);
            }
        });

        btn_filter_no_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       //showing all non-answer grid items
                Common.answerSheetListFiltered.clear();
                for (int i = 0; i < Common.answerSheetList.size(); i++) {
                    if (Common.answerSheetList.get(i).getType() == Common.ANSWER_TYPE.NO_ANSWER)
                        Common.answerSheetListFiltered.add(Common.answerSheetList.get(i));
                }
                filtered_adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetListFiltered);
                recyclerView_result.setAdapter(filtered_adapter);
            }
        });

        btn_filter_wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                           //showing all wrong-answer grid items
                Common.answerSheetListFiltered.clear();
                for (int i = 0; i < Common.answerSheetList.size(); i++) {
                    if (Common.answerSheetList.get(i).getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                        Common.answerSheetListFiltered.add(Common.answerSheetList.get(i));
                }
                filtered_adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetListFiltered);
                recyclerView_result.setAdapter(filtered_adapter);
            }
        });

        btn_filter_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                           //showing all right-answer grid items
                Common.answerSheetListFiltered.clear();
                for (int i = 0; i < Common.answerSheetList.size(); i++) {
                    if (Common.answerSheetList.get(i).getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
                        Common.answerSheetListFiltered.add(Common.answerSheetList.get(i));
                }
                filtered_adapter = new ResultGridAdapter(ResultActivity.this,Common.answerSheetListFiltered);
                recyclerView_result.setAdapter(filtered_adapter);
            }
        });
        database = FirebaseDatabase.getInstance();
        score = database.getReference("Score");     //getting Score path on database


        score.child(String.format("%s_%s",Common.currentUser.getUserName(),Common.selectedCategory.getId()))        //Create data named by user's username and Category ID and set all the data archieved
                .setValue(new Score(String.format("%s_%s",Common.currentUser.getUserName(),Common.selectedCategory.getId()),
                        Common.currentUser.getName(),
                        String.valueOf(totalscore),
                        String.valueOf(Common.selectedCategory.getId()),
                        Common.selectedCategory.getName(),
                        Common.currentUser.getUserName()));

        score.orderByChild("userName").equalTo(Common.currentUser.getUserName())        //find the user base on user's username
                .addListenerForSingleValueEvent(new ValueEventListener() {      //getting every score in each category stored on Database and sum it)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()) {
                            Score score = data.getValue(Score.class);
                            sum+=Integer.parseInt(score.getScore());

                        }
                        //After summary all score, processing sum variable on Firebase
                        Ranking ranking = new Ranking(Common.currentUser.getName(),sum,Common.rankingimage.getProfileImage());
                        Common.rankingimage = ranking;      //save this one to display the new total score immediately
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())       //getting back to homepage
        {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(),Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                startActivity(intent);
        }
        return true;
    }



    public void Scoreboard(View view) {         //go to Ranking Activity
        Intent intent1 = new Intent(ResultActivity.this,RankingActivity.class);
        startActivity(intent1);
    }

    public void ViewAnswer(View view) {     //return to Question activity with a value to do specific function (which is view all answers)

        new BottomDialog.Builder(ResultActivity.this)
                .setTitle("View all answers?")
                .setIcon(R.drawable.ic_mood_black_24dp)
                .setContent("Are you sure that you want to see all the results...?")
                .setNegativeText("No...")
                .onNegative(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        bottomDialog.dismiss();
                    }
                })
                .setPositiveText("Yes!")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        bottomDialog.dismiss();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("action","viewquizanswer");
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                }).show();

    }

    public void DoAgain(View view) {        //open dialog to ask if user want to do it again
        new BottomDialog.Builder(ResultActivity.this)
                .setTitle("Do quiz again?")
                .setIcon(R.drawable.ic_mood_black_24dp)
                .setContent("Do you you really want to redo the quiz?")
                .setNegativeText("No...")
                .onNegative(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        bottomDialog.dismiss();
                    }
                })
                .setPositiveText("Yes!")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog bottomDialog) {
                        bottomDialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(),QuestionActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                        startActivity(intent);
                    }
                }).show();
    }
}
