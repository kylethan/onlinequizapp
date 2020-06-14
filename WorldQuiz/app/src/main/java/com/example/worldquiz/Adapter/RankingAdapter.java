package com.example.worldquiz.Adapter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worldquiz.Interface.ItemClickListener;
import com.example.worldquiz.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RankingAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txt_name,txt_score,txt_ranking;
    public CircleImageView imageView;
    public ProgressBar progressBar;

    private ItemClickListener itemClickListener;

    public RankingAdapter(@NonNull View itemView) {
        super(itemView);
        txt_ranking = (TextView) itemView.findViewById(R.id.txt_ranking);
        txt_name = (TextView) itemView.findViewById(R.id.txt_name);
        txt_score= (TextView) itemView.findViewById(R.id.txt_score);
        imageView = (CircleImageView) itemView.findViewById(R.id.profile_image1);
        progressBar = itemView.findViewById(R.id.progressbarR);
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
