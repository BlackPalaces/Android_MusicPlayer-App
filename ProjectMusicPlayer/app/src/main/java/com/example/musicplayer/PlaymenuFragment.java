package com.example.musicplayer;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.content.res.TypedArray;
import java.util.Random;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaymenuFragment extends Fragment {

    private TypedArray colorArray;
    private FrameLayout frameLayout;
    public PlaymenuFragment() {
        // Required empty public constructor
    }
    Bundle songExtraData;
    ImageView prev,play,next,skip,pic;
    private int position;
    private String songs[],cName[],cartist[];
    TextView songtitle,songname,artist;
    TextView startTimeTextView, endTimeTextView;
    static MediaPlayer mediaPlayer;
    private ArrayList<File> musicList;
    SeekBar progressBar;
    Handler handler;

    private Bitmap[] albumArt;
    public static PlaymenuFragment newInstance() {
        return new PlaymenuFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playmenu, container, false);
        colorArray = getResources().obtainTypedArray(R.array.colorArray);
        frameLayout = view.findViewById(R.id.flame1layout);
        prev = view.findViewById(R.id.imageView0);
        play = view.findViewById(R.id.imageView1);
        next = view.findViewById(R.id.imageView2);
        skip = view.findViewById(R.id.imageView3);
        songtitle = view.findViewById(R.id.MusicPlay);
        songname = view.findViewById(R.id.MetaName);
        artist = view.findViewById(R.id.MetaArtist);
        pic = view.findViewById(R.id.imageView4);
        startTimeTextView = view.findViewById(R.id.timestart);
        endTimeTextView = view.findViewById(R.id.timeend);
        if(mediaPlayer!=null){
            mediaPlayer.stop();
        }


        Bundle bundle = getArguments();
        if (bundle != null ) {
            Parcelable[] parcelables = bundle.getParcelableArray("albumArt");
            if (parcelables != null) {
                albumArt = new Bitmap[parcelables.length];
                for (int i = 0; i < parcelables.length; i++) {
                    albumArt[i] = (Bitmap) parcelables[i];
                }
                position = bundle.getInt("position", 0);
                songs = bundle.getStringArray("songs");
                cName = bundle.getStringArray("Name");
                cartist = bundle.getStringArray("artist");
                musicList = (ArrayList<File>) bundle.getSerializable("songslist");
                if (albumArt != null && albumArt.length > position && albumArt[position] != null) {
                    pic.setImageBitmap(albumArt[position]);
                }

                songtitle.setText(songs[position]);
                songname.setText(cName[position]);
                artist.setText(cartist[position]);
                Log.d("PlaymenuFragment", "มีเพลงอยู่" + musicList);
                initializeMusicPlayer(position);
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        play();
                    }
                });

                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position<musicList.size()-1){
                            position++;
                        }else {
                            position = 0;
                        }
                        int randomColor = getRandomColor();
                        frameLayout.setBackgroundColor(randomColor);
                        initializeMusicPlayer(position);
                    }
                });


                prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position<=0){
                            position = musicList.size()-1;
                        } else {
                            position--;
                        }
                        int randomColor = getRandomColor();
                        frameLayout.setBackgroundColor(randomColor);
                        initializeMusicPlayer(position);
                    }
                });

            }
        } else{
            Log.d("PlaymenuFragment", "มีปัญหาบางอย่าง"+musicList);

        }
        // อ้างอิงถึง ProgressBar
        progressBar = view.findViewById(R.id.seekBar);
        progressBar.setMax(100); // ตั้งค่าค่าสูงสุดของ ProgressBar เป็น 100
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // ใส่โค้ดที่ต้องการทำเมื่อมีการเปลี่ยนแปลงใน SeekBar
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ใส่โค้ดที่ต้องการทำเมื่อผู้ใช้แตะที่ SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // ใส่โค้ดที่ต้องการทำเมื่อผู้ใช้ปล่อยมือจาก SeekBar
                if (mediaPlayer != null) {
                    int totalDuration = mediaPlayer.getDuration();
                    int currentPosition = (int) (((double) progressBar.getProgress() / 100) * totalDuration);
                    mediaPlayer.seekTo(currentPosition);
                }
            }

        });

        // โค้ดอื่น ๆ

        handler = new Handler();

        // เรียกเมทอด updateProgressBar เพื่ออัปเดต ProgressBar ทุก 500 milliseconds
        handler.postDelayed(updateProgressBar, 500);
        return view;
    }
    private int getRandomColor() {
        Random random = new Random();
        int randomIndex = random.nextInt(colorArray.length());
        return colorArray.getColor(randomIndex, 0);
    }

    private void initializeMusicPlayer(int position) {

        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }
        if (albumArt != null && albumArt.length > position && albumArt[position] != null) {
            pic.setImageBitmap(albumArt[position]);
        } else {
            pic.setImageResource(R.drawable.nomal);
        }
        songtitle.setText(songs[position]);
        songname.setText(cName[position] != null ? cName[position]:songs[position]);
        artist.setText(cartist[position] != null ? cartist[position] : "ไม่ทราบศิลปิน");

        mediaPlayer = new MediaPlayer();
        Uri uri = Uri.parse(musicList.get(position).toString());
        mediaPlayer = MediaPlayer.create(getActivity(),uri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                play.setImageResource(R.drawable.baseline_pause_24);
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // นี่คือโค้ดที่จะถูกเรียกเมื่อ MediaPlayer พร้อมที่จะเล่น
                play.setImageResource(R.drawable.baseline_pause_24);
                mediaPlayer.start();
            }
        });
    }
    private Runnable updateProgressBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                int currentDuration = mediaPlayer.getCurrentPosition();
                int totalDuration = mediaPlayer.getDuration();

                // คำนวณค่า progress และตั้งค่าให้ ProgressBar
                int progress = (int) (((double) currentDuration / totalDuration) * 100);
                progressBar.setProgress(progress);
                startTimeTextView.setText(formatDuration(currentDuration));
                endTimeTextView.setText(formatDuration(totalDuration));

            }

            handler.postDelayed(this, 500);
        }
    };

    private void play(){
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            play.setImageResource(R.drawable.baseline_play_arrow_24);
        }else {
            mediaPlayer.start();
            play.setImageResource(R.drawable.baseline_pause_24);
        }
    }
    private String formatDuration(int duration) {
        int seconds = (duration / 1000) % 60;
        int minutes = (duration / (1000 * 60)) % 60;
        int hours = duration / (1000 * 60 * 60);

        return String.format("%02d:%02d",minutes, seconds);
    }
}