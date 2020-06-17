package com.example.worldquiz;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.worldquiz.Adapter.AnswerSheetAdapter;
import com.example.worldquiz.Adapter.QuestionFragmentAdapter;
import com.example.worldquiz.Common.Common;
import com.example.worldquiz.DBHelper.DBHelper;
import com.example.worldquiz.Model.CurrentQuestion;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CODE_GET_RESULT = 9999 ;
    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView = false;

    TextView txt_right_answer,txt_timer, txt_wrong_answer;

    RecyclerView answer_sheet_view;
    AnswerSheetAdapter answerSheetAdapter;

    ImageView back_arrow, forward_arrow;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);






        takeQuestion();     //taking questions from DB

        if (Common.questionList.size() > 0) {

                forward_arrow = (ImageView) findViewById(R.id.forward_arrow);
                back_arrow = (ImageView) findViewById(R.id.back_arrow);

            //show right answer signal and timer
                txt_right_answer = (TextView) findViewById(R.id.txt_question_right);
                txt_timer = (TextView) findViewById(R.id.txt_timer);

                txt_timer.setVisibility(View.VISIBLE);
                txt_right_answer.setVisibility(View.VISIBLE);
                Common.right_answer_count = 0;
                txt_right_answer.setText(String.valueOf(Common.right_answer_count));

                //showing wrong answer

                txt_wrong_answer = (TextView)findViewById(R.id.txt_wrong_answer);
                txt_wrong_answer.setVisibility(View.VISIBLE);
                Common.wrong_answer_count = 0;
                txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                countTimer();


                //answer sheet view
                answer_sheet_view = (RecyclerView) findViewById(R.id.grid_answer);
                answer_sheet_view.setHasFixedSize(true);
                if(Common.questionList.size() >= 12)      //separate into 2 rows  if the question list has more than 5 questions.
                    answer_sheet_view.setLayoutManager(new GridLayoutManager(this,Common.questionList.size()/2));
                answerSheetAdapter = new AnswerSheetAdapter(this,Common.answerSheetList);
                answer_sheet_view.setAdapter(answerSheetAdapter);           //setting adapter for answer sheet


                viewPager = (ViewPager) findViewById(R.id.viewpaper);
                tabLayout = (TabLayout) findViewById(R.id.sliding);

                getFragmentList();

                //setting question Fragment Adapter and add it to viewPager
                QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                        1,
                        this,
                        Common.fragmentList);
                viewPager.setOffscreenPageLimit(Common.questionList.size());            //saving state for viewPager
                viewPager.setAdapter(questionFragmentAdapter);
                tabLayout.setupWithViewPager(viewPager);

                //Event
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));          //viewPager listener base on tabLayout selected

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {          //TabLayout selected function - getting the data in the previous viewpager to check for the answer
                    int pos = 0;
                       @Override
                       public void onTabSelected(TabLayout.Tab tab) {

                           QuestionFragment fragment;
                           viewPager.setCurrentItem(tab.getPosition());

                           if (pos < tab.getPosition()) {           //if the previous position smaller than current position
                               // then we calculate to take the fragment from previous fragment to check answer
                               int a = tab.getPosition()-(tab.getPosition()-pos);
                               fragment = Common.fragmentList.get(a);   //get the previous fragment

                               //Function to show correct answer
                               CurrentQuestion question_state = fragment.getSelectedAnswer();
                               Common.answerSheetList.set(a, question_state); //Set question answer type for answersheet
                               answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet


                               countCorrectAnswer();

                               //showing correct answer over total question and wrong answer
                               txt_right_answer.setText(String.valueOf(Common.right_answer_count));

                               txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                               if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {     //if question is answered and once user scroll, the answer will be taken to check and show correct answer immediately
                                   // immediate and user are unable to do it again
                                   fragment.showCorrectAnswer();
                                   fragment.disableAnswer();
                               }
                               pos = tab.getPosition();
                           }

                           if (pos > tab.getPosition()) {           //if the previous position bigger than current position
                               // then we calculate to take the fragment from previous fragment to check answer
                               int a = tab.getPosition()+(pos - tab.getPosition());
                               fragment = Common.fragmentList.get(a);   //get the previous fragment

                               //Function to show correct answer
                               CurrentQuestion question_state = fragment.getSelectedAnswer();
                               Common.answerSheetList.set(a, question_state); //Set question answer type for answersheet
                               answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet


                               countCorrectAnswer();

                               //showing correct answer  wrong answer
                               txt_right_answer.setText(String.valueOf(Common.right_answer_count));

                               txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                               if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {     //if question is answered and once user scroll, the answer will be taken to check and show correct answer immediately
                                   // immediate and user are unable to do it again
                                   fragment.showCorrectAnswer();
                                   fragment.disableAnswer();
                               }
                               pos = tab.getPosition();
                           }

                       }

                       @Override
                       public void onTabUnselected(TabLayout.Tab tab) {

                       }

                       @Override
                       public void onTabReselected(TabLayout.Tab tab) {


                       }
                   });

            forward_arrow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {        //right arrow button function (having the same function with scrolling right)
                    viewPager.setCurrentItem(getItem(+1), true);
                    QuestionFragment questionFragment;
                    questionFragment = Common.fragmentList.get(getItem(-1));        //get the previous fragment

                    //Function to show correct answer
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                    Common.answerSheetList.set(getItem(-1), question_state); //Set question answer type for answersheet
                    answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet


                    countCorrectAnswer();

                    //showing correct answer and wrong answer
                    txt_right_answer.setText(String.valueOf(Common.right_answer_count));

                    txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                    if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {     //if question is answered and once user scroll, the answer will be taken to check and show correct answer immediately
                        // immediate and user are unable to do it again
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswer();
                    }
                }
            });


            back_arrow.setOnClickListener(new View.OnClickListener() {           //left arrow button function (having the same function with scrolling left)

                @Override
                public void onClick(View view) {
                    viewPager.setCurrentItem(getItem(-1), true);
                    QuestionFragment questionFragment;
                    questionFragment = Common.fragmentList.get(getItem(1));      //get the previous fragment

                    //Function to show correct answer
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                    Common.answerSheetList.set(getItem(1),question_state); //Set question answer type for answersheet
                    answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet


                    countCorrectAnswer();

                    //showing correct answer  and wrong answer
                    txt_right_answer.setText(String.valueOf(Common.right_answer_count));

                    txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                    if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {     //if question is answered and once user scroll, the answer will be taken to check and show correct answer immediately
                        // immediate and user are unable to do it again
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswer();
                    }

                }
            });
        }

    }
    private int getItem(int i) {            //getItem to get the previous our forward fragment of the current fragment (base on number of i)
        return viewPager.getCurrentItem() + i;
    }

    private void finishGame() {

            //get the last answer of question to compare it in case user have not scrolled yet or user want to end earlier
            int pos = viewPager.getCurrentItem();
            QuestionFragment questionFragment = Common.fragmentList.get(pos);
            CurrentQuestion question_state = questionFragment.getSelectedAnswer();
            Common.answerSheetList.set(pos,question_state); //Set question answer for answersheet
            answerSheetAdapter.notifyDataSetChanged();  //Change color in answer sheet
            countCorrectAnswer();

            //showing correct answer and wrong answer
        txt_right_answer.setText(String.valueOf(Common.right_answer_count));

        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

            //if that question is answer then disableAnswer and showCorrectAnswer
            if (question_state.getType() != Common.ANSWER_TYPE.NO_ANSWER) {
                questionFragment.showCorrectAnswer();
                questionFragment.disableAnswer();

            }

        //Navigating to new Result Activity
        Intent intent = new Intent(QuestionActivity.this,ResultActivity.class);
        Common.timer = Common.TOTAL_TIME - time_play;
        Common.no_answer_count = Common.questionList.size() - (Common.wrong_answer_count+Common.right_answer_count);
        Common.data_question = new StringBuilder(new Gson().toJson(Common.answerSheetList));        //using GSON to store data and use it Result Activity

        startActivityForResult(intent,CODE_GET_RESULT);

    }

    private void countCorrectAnswer() {     //Counting correct answer
        if (Common.fragmentList.size()>0) {
            Common.right_answer_count = 0;
            Common.wrong_answer_count = 0;
        }

        for (CurrentQuestion item:Common.answerSheetList)      //counting correct and wrong answer
            if (item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
                Common.right_answer_count++;
            else if (item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                Common.wrong_answer_count++;
    }

    //Generating question fragment base on number of item in QuestionList
    private void getFragmentList() {
        if (Common.fragmentList.size()>0)
            Common.fragmentList.clear();
        for (int i = 0; i<Common.questionList.size(); i++) {
            Bundle bundle = new Bundle();
            bundle.putInt("index",i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.fragmentList.add(fragment);
        }

    }

    //Countdown for 10 minutes and reset when redo the quiz or click on another category
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
                    finishGame();
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
                    finishGame();
                }       //finish game function if the time is over
            }.start();
        }
    }

    private void takeQuestion()  {
        //taking question from DBHelper base on CategoryID
        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectedCategory.getId());
        if (Common.questionList.size() == 0) {      //Open a dialog to notice user if there is no question in the category
            new BottomDialog.Builder(this)
                    .setTitle("Sorry!")
                    .setContent("We are preparing questions for this category")
                    .setIcon(R.drawable.dissatisfied)
                    .setPositiveText("I UNDERSTAND")
                    .setPositiveBackgroundColorResource(R.color.colorPrimary)
                    .setPositiveTextColorResource(android.R.color.white)
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
    public boolean onPrepareOptionsMenu(Menu menu) {        //Menu will have wrong answer display and submission button


        return true;
    }

    @Override
    public void onBackPressed() {
        new BottomDialog.Builder(this)
                .setTitle("Do you wanna quit?")
                .setIcon(R.drawable.ic_mood_black_24dp)
                .setContent("You can get back anytime!")
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
                        Intent intent = new Intent(getApplicationContext(),Homepage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                        startActivity(intent);
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:         //open a dialog for user to confirm quit game
                new BottomDialog.Builder(this)
                        .setTitle("Do you wanna quit?")
                        .setIcon(R.drawable.ic_mood_black_24dp)
                        .setContent("You can get back anytime!")
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
                                Intent intent = new Intent(getApplicationContext(),Homepage.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //Delete all activity
                                startActivity(intent);
                            }
                        }).show();
                break;
            case R.id.menu_finish_game:     //open dialog to confirm submission
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
                    else
                        finishGame();
            }

            return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {       //getting On activity Result from Result Activity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_GET_RESULT)
        {
            if (resultCode == Activity.RESULT_OK) {
                String action = data.getStringExtra("action");     //get to specific question when clicked on the answer sheet in result activity
                if (action == null || TextUtils.isEmpty(action)) {
                    int questionNum = data.getIntExtra(Common.KEY_BACK_FROM_RESULT,-1);
                    viewPager.setCurrentItem(questionNum);

                    isAnswerModeView = true;
                    Common.countDownTimer.cancel();
                    txt_wrong_answer.setVisibility(View.GONE);
                    txt_right_answer.setVisibility(View.GONE);
                    txt_timer.setVisibility(View.GONE);
                    for (int i = 0; i < Common.fragmentList.size();i++) {           //disable every questions - user cannot answer anymore
                        Common.fragmentList.get(i).disableAnswer();
                    }
                }
                else {
                    if (action.equals("viewquizanswer")) {
                        viewPager.setCurrentItem(0);            //showing all answers and disable everything

                        isAnswerModeView = true;
                        Common.countDownTimer.cancel();
                        txt_wrong_answer.setVisibility(View.GONE);
                        txt_right_answer.setVisibility(View.GONE);
                        txt_timer.setVisibility(View.GONE);

                        for (int i = 0; i < Common.fragmentList.size();i++) {
                            Common.fragmentList.get(i).showCorrectAnswer();       //showing all correct answers
                            Common.fragmentList.get(i).disableAnswer();         //disable every questions - user cannot answer anymore
                        }
                    }

                }
            }
        }
    }
}
