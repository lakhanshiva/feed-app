package com.example.myfeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.util.List;


public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private List<XmlParser.Entry> list;
    private Context context;

    //Create the builder
    public Adapter(Context context) {
        this.context = context;
    }

    //Create new files (invokes the layout manager). Here we refer to the layout row.xml
    @Override
    public Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, null);
        // create ViewHolder
        MyViewHolder viewHolder = new MyViewHolder(itemLayoutView);
        return viewHolder;
    }

    //We load the widgets with the data (it invokes the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        String url = list.get(position).image;
        String fileName;
        if(url != null){
            fileName = url.substring(url.lastIndexOf('/')+1, url.length() );
        }
        else{
            fileName = "";
        }
        String path = context.getCacheDir().toString()+ File.separator+fileName;
        Drawable drawable = Drawable.createFromPath(path);
        if (drawable != null){
            viewHolder.imageView.setImageDrawable(drawable);
        }
        viewHolder.vTitle.setText(list.get(position).title);
    }

    //Returns the number of items in the news
    @Override
    public int getItemCount() {
        if(list == null) return 0;
        else return list.size();
    }

    public void setList(List list) {
        this.list = list;
    }

    public List<XmlParser.Entry> getList(){
        return list;
    }

    //We define our ViewHolder, that is, an element in the list in question
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected ImageView imageView;
        protected TextView vTitle;
        public MyViewHolder(View v) {
            super(v);
            // Image and title for feed items
            imageView = (ImageView) v.findViewById(R.id.imageView);
            vTitle = (TextView) v.findViewById(R.id.title);

            // Set up the listener
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, SingleNews.class);
            intent.putExtra("item", list.get(getAdapterPosition()));
            //Send extra information - send item object (with link to the news)
            System.out.println(list.get(getAdapterPosition()).link);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            list.remove(position);
            notifyItemRemoved(position);
            return true;
        }

    }

}