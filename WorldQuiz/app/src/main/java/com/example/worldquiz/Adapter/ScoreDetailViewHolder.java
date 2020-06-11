package com.example.worldquiz.Adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worldquiz.R;

public class ScoreDetailViewHolder extends RecyclerView.ViewHolder {
    public TextView txt_cname,txt_cscore;

    public ScoreDetailViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_cname = (TextView) itemView.findViewById(R.id.txt_cname);
        txt_cscore = (TextView) itemView.findViewById(R.id.txt_cscore);
    }
}
