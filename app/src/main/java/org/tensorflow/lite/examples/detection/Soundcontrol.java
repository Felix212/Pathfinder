package org.tensorflow.lite.examples.detection;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import org.tensorflow.lite.examples.detection.env.Logger;


public class Soundcontrol {
    private Context context;
    private static final Logger LOGGER = new Logger();

    public Soundcontrol(Context context){
        this.context = context;
        player.setOnCompletionListener(mediaPlayer -> {
            player.stop();
            player.reset();
            LOGGER.i("soundreset");
        });
    }

    MediaPlayer mp;
    MediaPlayer player = new MediaPlayer();

    public void play_reached_destiation() {
        try{

            AssetFileDescriptor afd = context.getAssets().openFd("success.mp3");
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (Exception e){

        }
    }

    public void start_searching(){
        try{

            AssetFileDescriptor afd = context.getAssets().openFd("terminatordrums.mp3");

            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (Exception e){

        }
    }
}
