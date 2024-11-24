package com.example.musicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class Songinfo extends ArrayAdapter<String> {
    private String[] artist;
    private Bitmap[] Art;
    private String[] NameMusic;
    private Context context;
    private String[] songs;
    private ArrayList<File> musics;
    private String noart ="ไม่ทราบศิลปิน";
    private SongmenuFragment songmenuFragment;  // เพิ่มตัวแปร
    public Songinfo(Context context, String[] songs, ArrayList<File> musics, SongmenuFragment songmenuFragment, String[] artist, String[] Name, Bitmap[] art) {
        super(context, R.layout.list_item_layout, songs);
        this.context = context;
        this.songs = songs;
        this.musics = musics;
        this.artist = artist;
        this.NameMusic = Name;
        this.songmenuFragment = songmenuFragment;  // กำหนดค่าตัวแปร
        this.Art = art;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_layout, parent, false);

        ImageView imageView = rowView.findViewById(R.id.imageView);
        TextView textView = rowView.findViewById(R.id.textView);
        TextView Timemusic = rowView.findViewById(R.id.TimeM);
        TextView Artist = rowView.findViewById(R.id.auther);

        // กำหนดรูปภาพและข้อความ

        imageView.setImageBitmap((Art != null && position < Art.length && Art[position] != null) ? Art[position] : BitmapFactory.decodeResource(context.getResources(), R.drawable.nomal));
        textView.setText(TextUtils.isEmpty(NameMusic[position]) ? songs[position] : NameMusic[position]);
        Artist.setText(TextUtils.isEmpty(artist[position]) ? noart : artist[position]);


        // เรียกใช้เมธอดในการดึงข้อมูลเวลาและกำหนดให้ TextView
        String time = songmenuFragment.getSongDuration(musics.get(position).getAbsolutePath());
        Timemusic.setText(time);

        return rowView;
    }

}
