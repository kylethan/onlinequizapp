package com.example.worldquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Space;

import androidx.appcompat.widget.Toolbar;

import com.example.worldquiz.Adapter.CategoryAdapter;
import com.example.worldquiz.Common.Common;
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings)
        {
        }
        return true;
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_settings_white_24dp);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar2);
        myToolbar.setTitle("");
        myToolbar.setOverflowIcon(drawable);
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
