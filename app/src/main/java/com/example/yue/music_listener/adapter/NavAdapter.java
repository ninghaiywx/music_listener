package com.example.yue.music_listener.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yue.music_listener.R;
import com.example.yue.music_listener.bean.NavBean;
import java.util.List;

/**
 * Created by yue on 2017/9/9.
 */

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder>{
    private List<NavBean>navBeanList;
    private Context mContext;

    public NavAdapter(Context context,List<NavBean>list){
        mContext=context;
        navBeanList=list;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.main_nav_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NavBean navBean=navBeanList.get(position);
        holder.item.setText(navBean.getText());
        Glide.with(mContext).load(navBean.getPic()).into(holder.pic);

    }

    @Override
    public int getItemCount() {
        return navBeanList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        TextView item;
        public ViewHolder(View itemView) {
            super(itemView);
            pic=(ImageView)itemView.findViewById(R.id.nav_item_pic);
            item=(TextView)itemView.findViewById(R.id.nav_item_text);
        }
    }
}
