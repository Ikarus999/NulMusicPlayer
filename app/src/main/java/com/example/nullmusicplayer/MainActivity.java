package com.example.nullmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bullhead.equalizer.DialogEqualizerFragment;
import com.example.nullmusicplayer.Utils.AppUtils;
import com.example.nullmusicplayer.constants.AppConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private MediaPlayer mediaPlayer;
    private static final int PERMISSION_REQUESTS = 1;
    private DJ dj;
    View rewindView,forwardsView;

    TextView musicTitleText, songNameText, songLengthText, songProgressText;
    ImageView playButton, nextButton, previousButton, shuffleButton, repeatButton, randomImageSelectionView, playlistButton,equalizerButton;
    SeekBar playbackSeek;
    Boolean success = false;
    /**TODO
     * modify the UI
     */
    private Handler mHandler = new Handler();
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    AppUtils appUtils;
    List<String> statusList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusList.add(AppConstants.defaultStatus);
        statusList.add(AppConstants.altStatus);
        statusList.add(AppConstants.altStatus1);
        statusList.add(AppConstants.altStatus2);
        statusList.add(AppConstants.altStatus3);
        statusList.add(AppConstants.altStatus4);
        statusList.add(AppConstants.altStatus5);
        statusList.add(AppConstants.altStatus6);
        statusList.add(AppConstants.altStatus7);
        statusList.add(AppConstants.altStatus8);
        statusList.add(AppConstants.altStatus9);
        statusList.add(AppConstants.altStatus10);
        statusList.add(AppConstants.altStatus11);
        mediaPlayer = new MediaPlayer();
        dj = new DJ();
        appUtils = new AppUtils();
        setContentView(R.layout.activity_main);
        rewindView = findViewById(R.id.rewindButton);
        forwardsView = findViewById(R.id.forwardsButton);
        equalizerButton = findViewById(R.id.equalizer);
        playlistButton = findViewById(R.id.playlistButton);
        songNameText = findViewById(R.id.songNameText);
        songLengthText = findViewById(R.id.trackLength);
        songProgressText = findViewById(R.id.seekProgressText);
        playButton = findViewById(R.id.playPause);
        nextButton = findViewById(R.id.nextSong);
        musicTitleText = findViewById(R.id.musicTitle);
        previousButton = findViewById(R.id.previousSong);
        shuffleButton = findViewById(R.id.shuffle);
        repeatButton = findViewById(R.id.repeat);
        randomImageSelectionView = findViewById(R.id.randomImageSelection);
        playbackSeek = findViewById(R.id.seekBar);
        playbackSeek.setOnSeekBarChangeListener(this);
        mediaPlayer.setOnCompletionListener(this);
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
            if (success) {
                finish();
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                startActivity(i);
            }
//            else {
//                finish();
//                finishAffinity();
//                System.exit(0);
//            }

        }

        songsList = dj.getPlayList(Environment.getStorageDirectory().getPath());
        rewindView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get current song position
                int currentPosition = mediaPlayer.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // forward song
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                    Toast.makeText(MainActivity.this,"Rewinding!",Toast.LENGTH_SHORT).show();
                }else{
                    // backward to starting position
                    mediaPlayer.seekTo(0);
                    Toast.makeText(MainActivity.this,"Rewinding!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        forwardsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + seekForwardTime <= mediaPlayer.getDuration()){
                    // forward song
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                    Toast.makeText(MainActivity.this,"Skipping ahead!",Toast.LENGTH_SHORT).show();
                }else{
                    // forward to end position
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                    Toast.makeText(MainActivity.this,"Skipping ahead!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        equalizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogEqualizerFragment fragment = DialogEqualizerFragment.newBuilder()
                        .setAudioSessionId(mediaPlayer.getAudioSessionId())
                        .themeColor(ContextCompat.getColor(MainActivity.this, R.color.primaryColor))
                        .textColor(ContextCompat.getColor(MainActivity.this, R.color.primaryTextColor))
                        .accentAlpha(ContextCompat.getColor(MainActivity.this, R.color.secondaryLightColor))
                        .darkColor(ContextCompat.getColor(MainActivity.this, R.color.primaryDarkColor))
                        .setAccentColor(ContextCompat.getColor(MainActivity.this, R.color.secondaryColor))
                        .build();
                fragment.show(getSupportFragmentManager(), "eq");
            }
        });

//        songsList = dj.getPlayList(Environment.getStorageDirectory().getPath());
        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MusicListActivity.class);
                startActivityForResult(i, 100);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer != null) {
                        mediaPlayer.pause();
                        // Changing button image to play button
                        playButton.setImageResource(R.drawable.round_play_arrow_24);
                    }
                } else {
                    // Resume song
                    if (mediaPlayer != null) {
                        mediaPlayer.start();

//                        playSong(0);

                        // Changing button image to pause button
                        playButton.setImageResource(R.drawable.round_pause_24);
                    }
                }

            }

        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if next song is there or not
                // check for repeat is ON or OFF
                if (isRepeat) {
                    // repeat is on play same song again
                    repeatButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    playSong(currentSongIndex);
                } else if (isShuffle) {

                    shuffleButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);

                    // shuffle is on - play a random song
                    Random rand = new Random();
                    currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
                    playSong(currentSongIndex);
                } else {
                    // no repeat or shuffle ON - play next song
                    if (currentSongIndex < (songsList.size() - 1)) {
                        playSong(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        // play first song
                        playSong(0);
                        currentSongIndex = 0;
                    }
                }


            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get current song position
                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shuffleButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);

                if (isShuffle) {
                    isShuffle = false;
                    shuffleButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryColor), android.graphics.PorterDuff.Mode.SRC_IN);

                } else {
                    // make repeat to true
                    isShuffle = true;
                    shuffleButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);

                    // make shuffle to false
                    isRepeat = false;
                    repeatButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryColor), android.graphics.PorterDuff.Mode.SRC_IN);

                }
            }
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                repeatButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);

                if (isRepeat) {
                    isRepeat = false;
                    repeatButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryColor), android.graphics.PorterDuff.Mode.SRC_IN);

                } else {
                    // make repeat to true
                    isRepeat = true;
                    repeatButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);

                    // make shuffle to false
                    isShuffle = false;
                    shuffleButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryColor), android.graphics.PorterDuff.Mode.SRC_IN);

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex);
        }

    }

    public void playSong(int songIndex) {
        // Play song
        try {
            mediaPlayer.reset();

            mediaPlayer.setDataSource(songsList.get(songIndex).get("file_path"));

            mediaPlayer.prepare();
            mediaPlayer.start();
            musicTitleText.setText(songsList.get(songIndex).get("file_name"));
            int[] res = {R.drawable.round_theater_comedy_24, R.drawable.round_token_24, R.drawable.round_sentiment_very_dissatisfied_24, R.drawable.round_sentiment_very_satisfied_24, R.drawable.round_whatshot_24, R.drawable.round_psychology_24};


            Random random = new Random();
            int rndInt = random.nextInt(res.length);


            randomImageSelectionView.setImageDrawable(getResources().getDrawable(res[rndInt]));
            // Displaying Song title
            String songTitle = appUtils.getRandomElement(statusList);
            songNameText.setText(songTitle);

            // Changing Button Image to pause image
            playButton.setImageResource(R.drawable.round_pause_24);

            // set Progress bar values
            playbackSeek.setProgress(0);
            playbackSeek.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            songLengthText.setText("" + appUtils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songProgressText.setText("" + appUtils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (appUtils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            playbackSeek.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = appUtils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            repeatButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);
            playSong(currentSongIndex);
        } else if (isShuffle) {

            shuffleButton.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.secondaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);

            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < (songsList.size() - 1)) {
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        success = true;
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {

            //Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        // Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

}