package com.example.worldquiz.Interface;

import com.example.worldquiz.Model.CurrentQuestion;

public interface IQuestion {
    CurrentQuestion getSelectedAnswer();    //Get selected Answer from user select
    void showCorrectAnswer();   //Bold correct Answer Text
    void disableAnswer();       //Disable all check box

}
