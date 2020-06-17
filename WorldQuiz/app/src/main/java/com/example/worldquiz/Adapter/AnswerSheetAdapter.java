package com.example.worldquiz.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Model.CurrentQuestion;
import com.example.worldquiz.R;

import java.util.List;

public class AnswerSheetAdapter extends RecyclerView.Adapter<AnswerSheetAdapter.MyViewHolder> {         //AnswerSheetAdapter showing the condition of the Answer Sheet in Question Activity

    Context context;
    List<CurrentQuestion> currentQuestionList;

    public AnswerSheetAdapter(Context context, List<CurrentQuestion> currentQuestionList) {
        this.context = context;
        this.currentQuestionList = currentQuestionList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_grid_answer_sheet_item,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {      //Chaning color of the sheet base on Answer type
        holder.txt_question_number.setText(String.valueOf(position+1));
        if (currentQuestionList.get(position).getType() == Common.ANSWER_TYPE.RIGHT_ANSWER)
            holder.question_item.setBackgroundResource(R.drawable.grid_question_right_answer);
        else if (currentQuestionList.get(position).getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
            holder.question_item.setBackgroundResource(R.drawable.grid_question_wrong_answer);
        else
            holder.question_item.setBackgroundResource(R.drawable.grid_question_no_answer);

    }

    @Override
    public int getItemCount() {
        return currentQuestionList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View question_item;
        TextView txt_question_number;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            question_item = itemView.findViewById(R.id.question_item);
            txt_question_number = itemView.findViewById(R.id.txt_question_number);
        }

    }
}
