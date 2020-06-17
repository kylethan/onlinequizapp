package com.example.worldquiz;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Interface.IQuestion;
import com.example.worldquiz.Model.CurrentQuestion;
import com.example.worldquiz.Model.Question;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionFragment extends Fragment implements IQuestion {

    TextView txt_question_text;
    RadioButton ckbA,ckbB,ckbC,ckbD;
    FrameLayout layout_image;
    ProgressBar progressBar;

    Question question;
    int questionIndex =-1;

    public QuestionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_question, container, false);

        //Get Question
        questionIndex = getArguments().getInt("index",-1);
        question = Common.questionList.get(questionIndex);

        //Get image of question if available
        if (question != null) {
            layout_image = (FrameLayout) itemView.findViewById(R.id.layout_image);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbarI);
            if (question.isImageQuestion()) {       //if it is a Image question, you Picasso to get the image and set it in Image frame layout
                ImageView image_question = (ImageView) itemView.findViewById(R.id.image_question);
                Picasso.get().load(question.getQuestionImage()).into(image_question, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(),"Some images are unavailable to load",Toast.LENGTH_SHORT).show();
                    }
                });
            } else
                layout_image.setVisibility(View.GONE);

            //View
            txt_question_text = (TextView) itemView.findViewById(R.id.txt_question_text);
            txt_question_text.setText(question.getQuestionText());


            //Making checkbox function
            ckbA = (RadioButton) itemView.findViewById(R.id.ckbA);
            ckbA.setText(question.getAnswerA());
            ckbA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        ckbB.setChecked(false);
                        ckbC.setChecked(false);
                        ckbD.setChecked(false);
                        Common.selected_values.add(ckbA.getText().toString());          //store selected value in Common.selected value to compare with the answer later
                        ckbA.setBackground(getResources().getDrawable(R.drawable.answer_choose_bg));

                    }
                    else
                    {
                        Common.selected_values.remove(ckbA.getText().toString());
                        ckbA.setTypeface(null,Typeface.NORMAL);
                        ckbA.setBackground(getResources().getDrawable(R.drawable.border_bg_white));

                    }

                }
            });

            //Making checkbox function
            ckbB = (RadioButton) itemView.findViewById(R.id.ckbB);
            ckbB.setText(question.getAnswerB());
            ckbB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        ckbA.setChecked(false);
                        ckbC.setChecked(false);
                        ckbD.setChecked(false);
                        Common.selected_values.add(ckbB.getText().toString());          //store selected value in Common.selected value to compare with the answer later
                        ckbB.setBackground(getResources().getDrawable(R.drawable.answer_choose_bg));

                    }
                    else
                    {
                        Common.selected_values.remove(ckbB.getText().toString());
                        ckbB.setTypeface(null,Typeface.NORMAL);
                        ckbB.setBackground(getResources().getDrawable(R.drawable.border_bg_white));

                    }



                }
            });

            //Making checkbox function
            ckbC = (RadioButton) itemView.findViewById(R.id.ckbC);
            ckbC.setText(question.getAnswerC());
            ckbC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        ckbA.setChecked(false);
                        ckbB.setChecked(false);
                        ckbD.setChecked(false);
                        Common.selected_values.add(ckbC.getText().toString());          //store selected value in Common.selected value to compare with the answer later
                        ckbC.setBackground(getResources().getDrawable(R.drawable.answer_choose_bg));

                    }
                    else
                    {
                        Common.selected_values.remove(ckbC.getText().toString());
                        ckbC.setTypeface(null,Typeface.NORMAL);
                        ckbC.setBackground(getResources().getDrawable(R.drawable.border_bg_white));

                    }


                }
            });

            //Making checkbox function
            ckbD = (RadioButton) itemView.findViewById(R.id.ckbD);
            ckbD.setText(question.getAnswerD());
            ckbD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        ckbA.setChecked(false);
                        ckbB.setChecked(false);
                        ckbC.setChecked(false);
                        Common.selected_values.add(ckbD.getText().toString());          //store selected value in Common.selected value to compare with the answer later
                        ckbD.setBackground(getResources().getDrawable(R.drawable.answer_choose_bg));

                    }
                    else
                    {
                        ckbD.setTypeface(null,Typeface.NORMAL);
                        Common.selected_values.remove(ckbD.getText().toString());
                        ckbD.setBackground(getResources().getDrawable(R.drawable.border_bg_white));

                    }

                }
            });


        }



        return itemView;
    }

    @Override
    public CurrentQuestion getSelectedAnswer() {

        if (Common.answerSheetList.get(questionIndex).getType() == Common.ANSWER_TYPE.NO_ANSWER) {      //check if that question is answer or not, if not, continue to this function
            //This function will return state of answer: right, wrong and non-answer
            CurrentQuestion currentQuestion = new CurrentQuestion(questionIndex, Common.ANSWER_TYPE.NO_ANSWER);  //creating new currentQuestion base on CurrentQuestion.class model Default no answer
            StringBuilder result = new StringBuilder();

            //put answer to array, Example: arr[0] = A. Melbourne; then take the first letter A
            if (Common.selected_values.size()==1) {
                Object[] arrayAnswer = Common.selected_values.toArray();
                result.append(((String)arrayAnswer[0]).substring(0,1));
            }

            if (question != null) {
                //Compare user answer with correct answer
                if (!TextUtils.isEmpty(result))
                {
                    if (result.toString().equals(question.getCorrectAnswer()))      //get the current question answer type and set to Common.currentQuestion
                    {
                        currentQuestion.setType(Common.ANSWER_TYPE.RIGHT_ANSWER);
                        Common.currentQuestion = currentQuestion;
                    }

                    else{
                        currentQuestion.setType(Common.ANSWER_TYPE.WRONG_ANSWER);   //get the current question answer type and set to Common.currentQuestion
                        Common.currentQuestion = currentQuestion;
                    }

                }
                else
                {
                    currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);      //get the current question answer type and set to Common.currentQuestion
                    Common.currentQuestion = currentQuestion;
                }



            }
            else {
                Toast.makeText(getContext(), "Cannot get question", Toast.LENGTH_SHORT).show();
                currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
            }
            Common.selected_values.clear();//always clear selected_value when compare done
            return currentQuestion;
        }
        else
            return Common.answerSheetList.get(questionIndex);   //if yes, return the previous status of the question

    }

    @Override
    public void showCorrectAnswer() {
        //Bold the correct answer
        //Pattern A,B
        String[] correctAnswer = question.getCorrectAnswer().split(",");
        for (String answer:correctAnswer) {
            if (answer.equals("A")) {
                ckbA.setTypeface(null,Typeface.BOLD);
                ckbA.setBackground(getResources().getDrawable(R.drawable.selected_answer_bg));
            }
            else if (answer.equals("B")) {
                ckbB.setTypeface(null,Typeface.BOLD);
                ckbB.setBackground(getResources().getDrawable(R.drawable.selected_answer_bg));
            }
            else if (answer.equals("C")) {
                ckbC.setTypeface(null,Typeface.BOLD);
                ckbC.setBackground(getResources().getDrawable(R.drawable.selected_answer_bg));
            }
            else if (answer.equals("D")) {
                ckbD.setTypeface(null,Typeface.BOLD);
                ckbD.setBackground(getResources().getDrawable(R.drawable.selected_answer_bg));
            }


        }

    }

    @Override
    public void disableAnswer() {       //disable answer (uncheckable checkbox)
        ckbA.setEnabled(false);
        ckbB.setEnabled(false);
        ckbC.setEnabled(false);
        ckbD.setEnabled(false);

    }

}
