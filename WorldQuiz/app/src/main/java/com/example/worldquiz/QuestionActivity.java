package com.example.worldquiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.worldquiz.Adapter.AnswerSheetAdapter;
import com.example.worldquiz.Common.Common;
import com.example.worldquiz.DBHelper.DBHelper;
import com.example.worldquiz.Model.CurrentQuestion;
import com.example.worldquiz.Model.Question;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView = false;

    TextView txt_right_answer,txt_timer;

    RecyclerView answer_sheet_view;
    AnswerSheetAdapter answerSheetAdapter;

    @Override
    protected void onDestroy() {
        if (Common.countDownTimer != null)
            Common.countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectedCategory.getName());
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        takeQuestion();     //taking questions from DB

        if (Common.questionList.size() > 0) {

            //show right answer signal and timer
            txt_right_answer = (TextView) findViewById(R.id.txt_question_right);
            txt_timer = (TextView) findViewById(R.id.txt_timer);

            txt_timer.setVisibility(View.VISIBLE);
            txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText((new StringBuilder(String.format("%d/%d",Common.right_answer_count,Common.questionList.size()))));
            
            countTimer();
            
            //answer sheet view
            answer_sheet_view = (RecyclerView) findViewById(R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);
            if(Common.questionList.size() > 5)      //separate into 2 rows  if the question list has more than 5 questions.
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this,Common.questionList.size()/2));
            answerSheetAdapter = new AnswerSheetAdapter(this,Common.answerSheetList);
            answer_sheet_view.setAdapter(answerSheetAdapter);
        }




    }


    private void countTimer() {
        if (Common.countDownTimer == null) {
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    txt_timer.setText(""+String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                    time_play-=1000;
                }

                @Override
                public void onFinish() {
                    //Finish Game
                }
            }.start();
        } else {
            Common.countDownTimer.cancel();
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    txt_timer.setText(""+String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                    time_play-=1000;
                }

                @Override
                public void onFinish() {
                    //Finish Game
                }
            }.start();
        }
    }

    private void takeQuestion()  {
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectedCategory.getId());
        if (Common.questionList.size() == 0) {
            new MaterialStyledDialog.Builder(this)
                    .setTitle(R.string.Sorry)
                    .setIcon(R.drawable.dissatisfied)
                    .setDescription(R.string.NoQuestion)
                    .setHeaderColor(R.color.colorPrimary)
                    .setPositiveText("I understand")
                    .show()
                    .setCancelable(true);


        } else {

            if (Common.answerSheetList.size()>0)
                Common.answerSheetList.clear();
            //Get answerSheet item from question
            //30 question = 30 answer sheet item
            for (int i = 0; i<Common.questionList.size();i++) {

                //use for i to take index of question in list
                Common.answerSheetList.add(new CurrentQuestion(i,Common.ANSWER_TYPE.NO_ANSWER));    //default answer is no answer
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
