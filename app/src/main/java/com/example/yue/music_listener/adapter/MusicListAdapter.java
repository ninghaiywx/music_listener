package com.example.yue.music_listener.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yue.music_listener.R;
import com.example.yue.music_listener.bean.Mp3Info;
import com.example.yue.music_listener.bean.OnItemClickListener;
import com.example.yue.music_listener.utils.AlbumUtil;

import java.util.List;

/**
 * Created by yue on 2017/9/11.
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {
    private List<Mp3Info>mp3InfoList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public MusicListAdapter(Context context,List<Mp3Info>mp3Infos){
        mContext=context;
        mp3InfoList=mp3Infos;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.music_list_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Mp3Info mp3Info=mp3InfoList.get(position);
        holder.album.setImageBitmap(AlbumUtil.getArtwork(mContext,mp3Info.getSong_id(),mp3Info.getAlbum_id() ,true));
        holder.song.setText(mp3Info.getTitle());
        holder.name.setText(mp3Info.getArtist()+"."+mp3Info.getAlbum());
    }

    @Override
    public int getItemCount() {
        return mp3InfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView album,more;
        TextView song,name;
        public ViewHolder(final View itemView) {
            super(itemView);
            album=(ImageView)itemView.findViewById(R.id.music_list_item_pic);
            more=(ImageView)itemView.findViewById(R.id.music_list_item_more);
            song=(TextView)itemView.findViewById(R.id.music_list_item_song);
            name=(TextView)itemView.findViewById(R.id.music_list_item_name_album);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemClickListener!=null){
                        onItemClickListener.onItemClick(itemView,getAdapterPosition());
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
