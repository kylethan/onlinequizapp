package com.example.worldquiz.Common;

import android.os.CountDownTimer;

import com.example.worldquiz.Model.Category;
import com.example.worldquiz.Model.CurrentQuestion;
import com.example.worldquiz.Model.Question;
import com.example.worldquiz.QuestionFragment;
import com.example.worldquiz.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Common {

    public static final int TOTAL_TIME = 10*60*1000;    //10 minutes
    public static final String KEY_BACK_FROM_RESULT = "BACK_FROM_RESULT";
    public static List<Question> questionList = new ArrayList<>();
    public static List<CurrentQuestion> answerSheetList = new ArrayList<>();
    public static List<CurrentQuestion> answerSheetListFiltered = new ArrayList<>();
    public static Category selectedCategory = new Category();

    public static int timer = 0;
    public static CountDownTimer countDownTimer;
    public static int right_answer_count = 0;
    public static int wrong_answer_count = 0;
    public static int no_answer_count = 0;
    public static StringBuilder data_question = new StringBuilder();
    public static List<QuestionFragment> fragmentList = new ArrayList<>();
    public static TreeSet<String> selected_values = new TreeSet<>();
    public static User currentUser;
    public static CurrentQuestion currentQuestion;



    public enum ANSWER_TYPE{
        NO_ANSWER,
        WRONG_ANSWER,
        RIGHT_ANSWER
    }
}
