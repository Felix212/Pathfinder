package org.tensorflow.lite.examples.detection;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;


public class Soundcontrol {
    private Context context;

    public Soundcontrol(Context context){
        this.context = context;
    }

    MediaPlayer mp;
    public void play_reached_destiation() {
        try{

            AssetFileDescriptor afd = context.getAssets().openFd("success.mp3");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (Exception e){

        }
    }

    public void start_searching(){
        try{

            AssetFileDescriptor afd = context.getAssets().openFd("terminatordrums.mp3");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (Exception e){

        }
    }
}
