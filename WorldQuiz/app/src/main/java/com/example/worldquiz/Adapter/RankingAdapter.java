package com.example.worldquiz.Adapter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worldquiz.Interface.ItemClickListener;
import com.example.worldquiz.R;

public class RankingAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txt_name,txt_score;

    private ItemClickListener itemClickListener;

    public RankingAdapter(@NonNull View itemView) {
        super(itemView);
        txt_name = (TextView) itemView.findViewById(R.id.txt_name);
        txt_score= (TextView) itemView.findViewById(R.id.txt_score);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
