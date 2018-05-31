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

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class RNAudioPlayerModule extends ReactContextBaseJavaModule implements Runnable {
  ReactApplicationContext reactContext;
  MediaPlayer mp;
  private static AudioManager audioManager;
  private float mVolume = 0.5f;
  private int mFadeOutTime = 0;
  private boolean _isFading = false;
  private double fadeOutInterval;
  private double fadeOutEndPoint;
  private float _lowerLimit = 0.01f;
  private  double _lastTime = 0;

  public RNAudioPlayerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    audioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
  }

  @Override
  public String getName() {
    return "RNAudioPlayer";
  }

  private static float getDeviceVolume() {
        int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return (float) volumeLevel / maxVolume;
  }

  @ReactMethod
  public void setFadeOutInterval(final int fadeOutTime) {
      if(mp!=null) {
          try {
              //Log.d("AUDIO", "duration: " + String.valueOf(mp.getDuration()) + ", fadeOutTime: " + String.valueOf(fadeOutTime));

              if (mp.getDuration() > fadeOutTime) {
                  mFadeOutTime = fadeOutTime * 1000;
              }
          }
          catch (Exception e){
              try {
                  Log.d("AUDIO", e.getMessage());
              }
              catch(Exception ex) {}
          }
      }
  }

    @ReactMethod
    public void setFadeOutIntervalWithOptions(final int fadeOutTime, final int fadeDuration) {
        if(mp!=null) {
            try {

                double intv = (double)(fadeOutTime * 1000);
                double _fadeDuration = (double)(fadeDuration*1000);
                double fadeOutPoint = (intv+_fadeDuration);

                _isFading = false;

                if(intv>0 && intv<mp.getDuration()) {
                    fadeOutInterval = intv;
                    fadeOutEndPoint = fadeOutPoint;
                }
            }
            catch (Exception e){
                try {
                    Log.d("AUDIO", e.getMessage());
                }
                catch(Exception ex) {}
            }
        }
    }

  private void doVolumeFade() {
    try {
        float curVol = getDeviceVolume();
        int curDuration = mp.getDuration();
        if(curVol>_lowerLimit) {
            if(_isFading) {

            }
        }
        else {
            mp.setVolume(0,0);
            mp.seekTo((int)(curDuration-200));
            fadeOutInterval = 0;
        }
    }
    catch (Exception e) {
        try {
            Log.d("AUDIO", e.getMessage());
        }
        catch(Exception ex) {}
    }
  }

  @ReactMethod
  public void startFadeOut() {

      if(mp!=null) {
          doVolumeFade();
      }
//      if(mp!=null) {
//          final int duration = 2000;    //2sec
//          final float deviceVolume = getDeviceVolume();
//
//          //Log.d("AUDIO", "deviceVolume: " + String.valueOf(deviceVolume));
//
//          final Handler h = new Handler();
//          h.postDelayed(new Runnable() {
//              private float time = duration;
//              private float volume = 0.0f;
//
//              @Override
//              public void run() {
//                  try {
//                      if (!mp.isPlaying())
//                          mp.start();
//                      time -= 100;
//                      volume = (deviceVolume * time) / duration;
//
//
//                      //Log.d("AUDIO", "deviceVolume: " + String.valueOf(deviceVolume) + ", new vol: " + String.valueOf(volume));
//
//                      mp.setVolume(volume, volume);
//                      if (time > 0)
//                          h.postDelayed(this, 100);
//                      else {
//                          mp.seekTo(mp.getDuration());
//                          mp.stop();
//                          mp.release();
//                      }
//                  }
//                  catch (Exception e) {}
//              }
//          }, 100);
//
////          final Timer timer = new Timer(true);
////          TimerTask timerTask = new TimerTask()
////          {
////              @Override
////              public void run()
////              {
////                  //Log.i("RNAudio - mVolume: ", String.valueOf(mVolume));
////                  if(mVolume>0.1) {
////                      mVolume = mVolume-0.1f;
////                      mp.setVolume(mVolume,mVolume);
////                  }
////                  else {
////                      try {
////                          mp.setVolume(0, 0);
////
////
////                          mp.seekTo(mp.getDuration() - 10);
////                      }
////                      catch(Exception e) {}
////                      timer.cancel();
////                      timer.purge();
////
////                  }
////              }
////          };
////
////          timer.schedule(timerTask,250,250);
//      }
  }

  @ReactMethod
  public void seek(int val) {
    if(mp!=null) {
        Float newval = val*1000.0f;

        //Log.d("RNAudioPlayer", newval + " of " + mp.getDuration());

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
    mVolume = 0.5f;
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

                mVolume = 0.5f;
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
    public void getLastPointInTime(final Callback callback) {
        callback.invoke((_lastTime/1000.0f));
    }

    @ReactMethod
    public void initialise(String audio, final Callback callback) {
        if(audio!=null) {
            String fname = audio.toLowerCase();

            int resID = this.reactContext.getResources().getIdentifier(fname, "raw", this.reactContext.getPackageName());
            mp = MediaPlayer.create(this.reactContext, resID);
        }
        mVolume = 0.5f;
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

                mVolume = 0.5f;
                mp.setVolume(mVolume, mVolume);

                WritableMap map = Arguments.createMap();

                map.putDouble("duration", (mp.getDuration() / 1000.0f));

                callback.invoke(map);
            }
        }



    }

    @ReactMethod
    public void setVolume(float value) {
        if(mp!=null) {
            mVolume = value;
            mp.setVolume(mVolume,mVolume);
        }
    }

    public void run() {
        int _currentTime = 0;
        int _currentDuration = 0;
        try {
            if(mp!=null) {
                _currentDuration = mp.getDuration();

                while (mp != null && _currentTime < _currentDuration) {
                    try {
                        Thread.sleep(10);
                        _currentTime = mp.getCurrentPosition();
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }

                    if (_currentTime > 0) {
                        _lastTime = mp.getCurrentPosition();
                    }

                    if (fadeOutInterval > 0 && fadeOutInterval <= _currentTime) {
                        if (_currentTime >= fadeOutEndPoint && fadeOutEndPoint > 0) {
                            mp.setVolume(0, 0);
                            mp.seekTo((int) (_currentDuration - 200));
                            fadeOutInterval = 0;
                        } else {
                            if ((fadeOutEndPoint - fadeOutInterval) > 0) {

                                double _cTime = _currentTime/1000.0f;
                                double _foInt = fadeOutInterval/1000.0f;
                                double _foEP = fadeOutEndPoint/1000.0f;



                                double _curPercentagePoint = (_cTime-_foInt)/(_foEP-_foInt); //(_currentTime - fadeOutInterval) / (fadeOutEndPoint - fadeOutInterval);

                                float _curLogPoint = (float) ((_curPercentagePoint * 9.0f) + 1.0f);
                                float _curScaledVolume = (float) (mVolume * (1.0f - Math.log10(_curLogPoint)));

                                //Log.d("AUDIO", "Scaling volume...perc:" + String.valueOf(_curPercentagePoint) + ", pos:" + String.valueOf(_curLogPoint) + ", vol: " + String.valueOf(_curScaledVolume));
                                mp.setVolume(_curScaledVolume, _curScaledVolume);
                            }
                        }
                    }


                    WritableMap map = Arguments.createMap();

                    map.putDouble("currentTime", (_currentTime / 1000.0f));
                    map.putDouble("currentDuration", (_currentDuration / 1000.0f));

                    reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("playerProgress", map);
                }
            }
        }
        catch (Exception e) {
            try {
                Log.d("AUDIO", e.getMessage());
            }
            catch(Exception ex) {}
        }


//        int currentPosition= 0;
//        int total = mp.getDuration();
//        while (mp!=null && currentPosition<total) {
//            try {
//                Thread.sleep(250);
//                currentPosition= mp.getCurrentPosition();
//            } catch (InterruptedException e) {
//                return;
//            } catch (Exception e) {
//                return;
//            }
//
//            try {
//                //Log.d("AUDIO", "pos: " + String.valueOf(currentPosition) + ", fade: " + String.valueOf(mFadeOutTime));
//                if(currentPosition > mFadeOutTime && mFadeOutTime>0) {
//                    this.startFadeOut();
//                }
//            }
//            catch (Exception e) {}
//
//            WritableMap map = Arguments.createMap();
//
//            map.putDouble("currentTime", (currentPosition/1000.0f));
//            map.putDouble("currentDuration", (total/1000.0f));
//
//            reactContext
//                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                    .emit("playerProgress", map);
//        }
    }
}
