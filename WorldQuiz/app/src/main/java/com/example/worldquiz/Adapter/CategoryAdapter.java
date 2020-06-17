package com.example.worldquiz.Adapter;

import android.content.Context;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worldquiz.Common.Common;
import com.example.worldquiz.Model.Category;
import com.example.worldquiz.QuestionActivity;
import com.example.worldquiz.R;

import java.util.List;



public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {       //Category Adapter showing all category name and image

    Context context;
    List<Category> categories;
    int[] categoryImage = {R.drawable.animal,R.drawable.geography,R.drawable.music,R.drawable.sports,R.drawable.history,R.drawable.nature,R.drawable.film,R.drawable.technology,R.drawable.math,R.drawable.science,R.drawable.cuisine,R.drawable.economy};

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false);


        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_category_name.setText(categories.get(position).getName());
        holder.txt_category_image.setImageResource(categoryImage[position]);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView card_category;
        TextView txt_category_name;
        ImageView txt_category_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_category = (CardView) itemView.findViewById(R.id.card_category);
            txt_category_image = (ImageView) itemView.findViewById(R.id.txt_category_image);
            txt_category_name = (TextView) itemView.findViewById(R.id.txt_category_name);
            card_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Common.selectedCategory = categories.get(getAdapterPosition());     //Assign current category
                    Intent intent = new Intent (context, QuestionActivity.class);
                    context.startActivity(intent);

                }
            });
        }
    }
}
