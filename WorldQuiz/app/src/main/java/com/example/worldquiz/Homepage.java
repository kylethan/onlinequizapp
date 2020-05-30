package com.example.worldquiz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.Menu;
import android.widget.Space;

import androidx.appcompat.widget.Toolbar;

import com.example.worldquiz.Adapter.CategoryAdapter;
import com.example.worldquiz.Common.SpaceDecoration;
import com.example.worldquiz.DBHelper.DBHelper;


public class Homepage extends AppCompatActivity {


    RecyclerView recycler_category;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu,menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(myToolbar);


        recycler_category = findViewById(R.id.recyler_category);
        recycler_category.setHasFixedSize(true);
        recycler_category.setLayoutManager(new GridLayoutManager(this,2));

        //Getting Screen height

        CategoryAdapter adapter = new CategoryAdapter(Homepage.this, DBHelper.getInstance(this).getAllCategories());
        int spaceInPixel = 4;
        recycler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recycler_category.setAdapter(adapter);



    }



}
