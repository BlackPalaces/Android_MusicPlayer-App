package com.example.musicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.Manifest;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.media.MediaMetadataRetriever;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SongmenuFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    String songs[];
    String Name[];
    String artist[];
    private Bitmap[] albumArt;
    ArrayList<File> musics;
    private OnSongClickListener songClickListener;
    private ArrayList<File> findMusicFiles(File directory) {
        ArrayList<File> musicFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    musicFiles.addAll(findMusicFiles(currentFile));
                } else {
                    if (currentFile.getName().endsWith(".mp3")) {
                        musicFiles.add(currentFile);
                    }
                }
            }
        }
        return musicFiles;
    }
    // เพิ่มเมธอดในการดึงข้อมูลเวลา
    String getSongDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        // ดึงข้อมูลเวลา (microseconds)
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        // แปลงเวลาเป็นนาทีและวินาที
        long durationInMillis = Long.parseLong(duration);
        long minutes = (durationInMillis / 1000) / 60;
        long seconds = (durationInMillis / 1000) % 60;

        // สร้างรูปแบบข้อความ HH:MM:SS
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_songmenu, container, false);

        listView = view.findViewById(R.id.list);
        SearchView searchView = view.findViewById(R.id.searchView);
        musics = findMusicFiles(Environment.getExternalStorageDirectory());
        // ตั้งค่า Listener สำหรับการค้นหา
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // ทำการค้นหาเมื่อผู้ใช้กด Enter
                filterSongs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // ทำการค้นหาเมื่อข้อความเปลี่ยนแปลง
                filterSongs(newText);
                return true;
            }
        });

        Dexter.withContext(getActivity())
                .withPermission(Manifest.permission.READ_MEDIA_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Log.d("Permission555", "Granted");
                        musics = findMusicFiles(Environment.getExternalStorageDirectory());

                        // Use String[] for songs
                        songs = new String[musics.size()];
                        Name = new String[musics.size()];
                        artist = new String[musics.size()];
                        albumArt = new Bitmap[musics.size()];
                        for (int i = 0; i < musics.size(); i++) {
                            songs[i] = musics.get(i).getName();
                            Name[i] = getSongInfo(musics.get(i).getPath(), MediaMetadataRetriever.METADATA_KEY_TITLE);
                            artist[i] = getSongInfo(musics.get(i).getPath(), MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            try {
                                albumArt[i] = extractCoverArt(musics.get(i).getPath());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        // Use Songinfo adapter
                        Songinfo adapter = new Songinfo(getActivity(), songs, musics, SongmenuFragment.this,artist,Name,albumArt);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                               Fragment PlaymenuFragment = new PlaymenuFragment();
                                Bundle bundle = new Bundle();
                                bundle.putInt("position", position);
                                bundle.putStringArray("songs",songs);
                                bundle.putStringArray("Name",Name);
                                bundle.putStringArray("artist",artist);
                                bundle.putSerializable("songslist", musics);
                                bundle.putParcelableArray("albumArt", albumArt);
                                PlaymenuFragment.setArguments(bundle);
                                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
                                bottomNavigationView.setSelectedItemId(R.id.PlayMenu);
                                FragmentTransaction playf = getActivity().getSupportFragmentManager().beginTransaction();
                                playf.replace(R.id.frame_layout, PlaymenuFragment).commit();
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        // ทำงานเมื่อไม่ได้รับอนุญาต
                        Log.d("Permission", "Denied");

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        // แสดงคำอภิปรายในการขออนุญาต
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
        return view;
    }
    // เมธอดในการดึงข้อมูล tag จากไฟล์ mp3
    private String getSongInfo(String path, int key) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        return retriever.extractMetadata(key);
    }
    private Bitmap extractCoverArt(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(filePath);

            // ดึงภาพจาก Metadata
            byte[] rawArt = retriever.getEmbeddedPicture();

            // แปลงข้อมูลภาพเป็น Bitmap
            if (rawArt != null) {
                return BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length);
            }

        } catch (Exception e) {
            Log.e("CoverArt", "Error extracting cover art: " + e.getMessage());
        } finally {
            retriever.release();
        }

        return null;
}
    private void updateMusicFiles() {
        musics = findMusicFiles(Environment.getExternalStorageDirectory());
    }
    // ใน SongmenuFragment.java

    private void filterSongs(String searchText) {
        updateMusicFiles();
        Log.d("เข้ามาแล้ว", "filterSongs: ");

        ArrayList<File> filteredMusicFiles = new ArrayList<>();

        // ทำการกรองเพลงตามชื่อหรือนักร้องที่ผู้ใช้พิมพ์
        for (File musicFile : musics) {
            if (musicFile.getName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredMusicFiles.add(musicFile);
            }
        }

        // กำหนดค่าใหม่ให้กับ songs, artist, Name, และ albumArt
        songs = new String[filteredMusicFiles.size()];
        Name = new String[filteredMusicFiles.size()];
        artist = new String[filteredMusicFiles.size()];
        albumArt = new Bitmap[filteredMusicFiles.size()];
        for (int i = 0; i < filteredMusicFiles.size(); i++) {
            songs[i] = filteredMusicFiles.get(i).getName();
            Name[i] = getSongInfo(filteredMusicFiles.get(i).getPath(), MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist[i] = getSongInfo(filteredMusicFiles.get(i).getPath(), MediaMetadataRetriever.METADATA_KEY_ARTIST);
            try {
                albumArt[i] = extractCoverArt(filteredMusicFiles.get(i).getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // ใช้ Songinfo adapter สร้างข้อมูลใหม่
        Songinfo adapter = new Songinfo(getActivity(), songs, filteredMusicFiles, SongmenuFragment.this, artist, Name, albumArt);
        listView.setAdapter(adapter);
    }
    public interface OnSongClickListener {
        void onSongClicked();
    }
}
