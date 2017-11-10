package com.sh3rawi.RNAudioPlayer;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class RNAudioPlayerModule extends ReactContextBaseJavaModule implements Runnable {
  ReactApplicationContext reactContext;
  MediaPlayer mp;
  private float mVolume = 1;

  public RNAudioPlayerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNAudioPlayer";
  }

  @ReactMethod
  public void startFadeOut() {
      if(mp!=null) {
          final Timer timer = new Timer(true);
          TimerTask timerTask = new TimerTask()
          {
              @Override
              public void run()
              {
                  //Log.i("RNAudio - mVolume: ", String.valueOf(mVolume));
                  if(mVolume>0.1) {
                      mVolume = mVolume-0.1f;
                      mp.setVolume(mVolume,mVolume);
                  }
                  else {
                      try {
                          mp.setVolume(0, 0);


                          mp.seekTo(mp.getDuration() - 10);
                      }
                      catch(Exception e) {}
                      timer.cancel();
                      timer.purge();

                  }
              }
          };

          timer.schedule(timerTask,250,250);
      }
  }

  @ReactMethod
  public void seek(int val) {
    if(mp!=null) {
        Float newval = val*1000.0f;

        Log.d("RNAudioPlayer", newval + " of " + mp.getDuration());

        if(newval>=0 && newval<=mp.getDuration()) {
            mp.seekTo(newval.intValue());
        }
    }
  }

  @ReactMethod
  public void resume() {
      play(null);
  }
    @ReactMethod
  public  void stop() {
        if(mp!=null) {
            try {
                mp.stop();
                mp.reset();
                mp.release();
            }
            catch (Exception e){}

            mp = null;
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("playerFinished", true);
        }
    }

  @ReactMethod
  public void pause() {
      if(mp!=null) {
          mp.pause();
      }
  }

  @ReactMethod
  public void play(String audio) {
    if(audio!=null) {
      String fname = audio.toLowerCase();
      int resID = this.reactContext.getResources().getIdentifier(fname, "raw", this.reactContext.getPackageName());
      mp = MediaPlayer.create(this.reactContext, resID);
    }
    mVolume = 1;
    mp.setVolume(mVolume,mVolume);
    mp.start();
    new Thread(this).start();
    mp.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        mp.reset();
        mp.release();
        mp = null;
          reactContext
                  .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                  .emit("playerFinished", true);
      }
    });
  }

    @ReactMethod
    public void playFromExpansion(String audio, int mainVersion, int patchVersion) {
        if(audio!=null) {
            Log.d("AudioDEBUG", "audio: " + audio);
            Log.d("AudioDEBUG", "mainVersion: " + String.valueOf(mainVersion));
            Log.d("AudioDEBUG", "patchVersion: " + String.valueOf(patchVersion));
            String fname = audio.toLowerCase().replace(".mp3","") + ".mp3";

            ZipResourceFile expansionFile= null;
            AssetFileDescriptor fd= null;
            try {
                expansionFile = APKExpansionSupport.getAPKExpansionZipFile(this.reactContext, mainVersion, patchVersion);
                if(expansionFile!=null) {
                    Log.d("AudioDEBUG", "expansionFile NOT null: " + fname);
                    fd = expansionFile.getAssetFileDescriptor(fname);
                }
                else {
                    Log.d("AudioDEBUG", "expansionFile null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if(fd==null) {
                //ERROR!
                Log.d("AudioDEBUG", "File error!");
            }
            else {
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mVolume = 1;
                mp.setVolume(mVolume,mVolume);
                mp.start();
                new Thread(this).start();
                mp.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.reset();
                        mp.release();
                        mp = null;
                        reactContext
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("playerFinished", true);
                    }
                });
            }

        }

    }

    @ReactMethod
    public void initialise(String audio, final Callback callback) {
        if(audio!=null) {
            String fname = audio.toLowerCase();

            int resID = this.reactContext.getResources().getIdentifier(fname, "raw", this.reactContext.getPackageName());
            mp = MediaPlayer.create(this.reactContext, resID);
        }
        mVolume = 1;
        mp.setVolume(mVolume, mVolume);

        WritableMap map = Arguments.createMap();

        map.putDouble("duration", (mp.getDuration() / 1000.0f));

        callback.invoke(map);
    }

    @ReactMethod
    public void initialiseFromExpansion(String audio, int mainVersion, int patchVersion, final Callback callback) {

        if(audio!=null) {
//            Log.d("AudioDEBUG", "audio: " + audio);
//            Log.d("AudioDEBUG", "mainVersion: " + String.valueOf(mainVersion));
//            Log.d("AudioDEBUG", "patchVersion: " + String.valueOf(patchVersion));
            String fname = audio.toLowerCase().replace(".mp3","") + ".mp3";

            ZipResourceFile expansionFile= null;
            AssetFileDescriptor fd= null;
            try {
                expansionFile = APKExpansionSupport.getAPKExpansionZipFile(this.reactContext, mainVersion, patchVersion);
                if(expansionFile!=null) {
                    //Log.d("AudioDEBUG", "expansionFile NOT null: " + fname);
                    fd = expansionFile.getAssetFileDescriptor(fname);
                }
                else {
                    //Log.d("AudioDEBUG", "expansionFile null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if(fd==null) {
                //ERROR!
                Log.d("AudioDEBUG", "File error!");
            }
            else {
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mVolume = 1;
                mp.setVolume(mVolume, mVolume);

                WritableMap map = Arguments.createMap();

                map.putDouble("duration", (mp.getDuration() / 1000.0f));

                callback.invoke(map);
            }
        }



    }

    public void run() {
        int currentPosition= 0;
        int total = mp.getDuration();
        while (mp!=null && currentPosition<total) {
            try {
                Thread.sleep(250);
                currentPosition= mp.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            WritableMap map = Arguments.createMap();

            map.putDouble("currentTime", (currentPosition/1000.0f));
            map.putDouble("currentDuration", (total/1000.0f));

            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("playerProgress", map);
        }
    }
}
