package com.example.worldquiz;

import  android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;


import com.example.worldquiz.Adapter.AnswerSheetAdapter;
import com.example.worldquiz.Adapter.QuestionFragmentAdapter;
import com.example.worldquiz.Common.Common;
import com.example.worldquiz.DBHelper.DBHelper;
import com.example.worldquiz.Model.CurrentQuestion;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView = false;

    TextView txt_right_answer,txt_timer, txt_wrong_answer;

    RecyclerView answer_sheet_view;
    AnswerSheetAdapter answerSheetAdapter;

    ViewPager viewPager;
    TabLayout tabLayout;

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


            viewPager = (ViewPager) findViewById(R.id.viewpaper);
            viewPager.setOffscreenPageLimit(Common.questionList.size()); // Fixed ViewPager size
            tabLayout = (TabLayout) findViewById(R.id.sliding);

            getFragmentList();

            QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    1,
                    this,
                    Common.fragmentList);
            viewPager.setAdapter(questionFragmentAdapter);
            tabLayout.setupWithViewPager(viewPager);

            //Event
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT = 1;
                int SCROLLING_UNDERTERMINED = 2;

                int currentScrollingDirection = 2;

                private void setScrollingDirection(float positionOffset) {
                    if ((1 - positionOffset) >= 0.5)
                        this.currentScrollingDirection = SCROLLING_RIGHT;
                    else if ((1 - positionOffset) <= 0.5)
                        this.currentScrollingDirection = SCROLLING_LEFT;
                }

                private boolean isScrollingDirectionUndetermined() {
                    return currentScrollingDirection == SCROLLING_UNDERTERMINED;
                }

                private boolean isScrollingRight() {
                    return currentScrollingDirection == SCROLLING_RIGHT;
                }

                private boolean isScrollingLeft() {
                    return currentScrollingDirection == SCROLLING_LEFT;

                }
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (isScrollingDirectionUndetermined())
                        setScrollingDirection(positionOffset);
                }

                @Override
                public void onPageSelected(int position) {
                    QuestionFragment questionFragment;
                    int pos = 0;
                    if (position > 0) {
                        if (isScrollingRight()) {   //if user scroll to right, get last fragment and calculate result
                            questionFragment = Common.fragmentList.get(position - 1);
                            pos = position - 1;
                        }
                        else if (isScrollingLeft()) {//if user scroll to left, get last fragment and calculate result
                            questionFragment = Common.fragmentList.get(position + 1);
                            pos = position + 1;
                        }
                        else {
                            questionFragment = Common.fragmentList.get(pos);
                        }
                    }
                    else {
                        questionFragment = Common.fragmentList.get(0);
                        pos = 0;
                    }

                    //Function to show correct answer
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                    Common.answerSheetList.set(pos,question_state); //Set question answer for answersheet
                    answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet

                    countCorrectAnswer();
                    
                    txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                    .append("/")
                    .append(String.format("%d",Common.questionList.size())).toString());
                    txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                    if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswer();
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE)
                        this.currentScrollingDirection =SCROLLING_UNDERTERMINED;
                }
            });
        }


    }

    private void finishGame() {
        int pos = viewPager.getCurrentItem();
        QuestionFragment questionFragment = Common.fragmentList.get(pos);
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();
        Common.answerSheetList.set(pos,question_state); //Set question answer for answersheet
        answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet


        countCorrectAnswer();

        txt_right_answer.setText(new StringBuilder(String.format("%d",Common.right_answer_count))
                .append("/")
                .append(String.format("%d",Common.questionList.size())).toString());
        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

        if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {
            questionFragment.showCorrectAnswer();
            questionFragment.disableAnswer();
        }
        //Navigating to new Result Activity
    }

    private void countCorrectAnswer() {
        //Reset variable
        Common.right_answer_count = Common.wrong_answer_count = 0;
        
        for (CurrentQuestion item:Common.answerSheetList)
            if (item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
                Common.right_answer_count++;
            else if (item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++;
        
    }

    //Generating question fragment base on number of item in QuestionList
    private void getFragmentList() {
        for (int i = 0; i<Common.questionList.size(); i++) {
            Bundle bundle = new Bundle();
            bundle.putInt("index",i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.fragmentList.add(fragment);
        }

    }

    //Countdown for 20 minutes and reset when redo the quiz or click on another category
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
            new BottomDialog.Builder(this)
                    .setTitle("Sorry!")
                    .setContent("We are preparing questions for this category")
                    .setIcon(R.drawable.dissatisfied)
                    .setPositiveText("I UNDERSTAND")
                    .setPositiveBackgroundColorResource(R.color.colorPrimary)
                    //setPositiveBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary)
                    .setPositiveTextColorResource(android.R.color.white)
                    //setPositiveTextColor(ContextCompat.getColor(this, android.R.color.colorPrimary)
                    .onPositive(new BottomDialog.ButtonCallback() {
                        @Override
                        public void onClick(BottomDialog dialog) {
                            startActivity(new Intent(getApplicationContext(), Homepage.class));
                        }
                    })
                    .setCancelable(false)
                    .show();


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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_wrong_answer);
        ConstraintLayout constraintLayout = (ConstraintLayout) item.getActionView();
        txt_wrong_answer = (TextView)constraintLayout.findViewById(R.id.txt_wrong_answer);
        txt_wrong_answer.setText(String.valueOf(0));

        return true;
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_finish_game) {
            if (!isAnswerModeView) {
                new BottomDialog.Builder(this)
                        .setTitle("Finished?")
                        .setIcon(R.drawable.ic_mood_black_24dp)
                        .setContent("Do you want to submit?")
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
                                finishGame();
                            }
                        }).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
