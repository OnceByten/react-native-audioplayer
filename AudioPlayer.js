'use strict';

var { RNAudioPlayer } = require('react-native').NativeModules;
var { Platform } = require('react-native');

var AudioPlayer = {
  play(fileName: string) {
    /*fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "");
    RNAudioPlayer.play(fileName);*/
    if (fileName === undefined) {
      RNAudioPlayer.resume();
    } else {
      fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "").replace("-","_");
      RNAudioPlayer.play(fileName);
    }
  },
  initialise(fileName: string, callback: callback) {
    if (fileName === undefined) {
    //  RNAudioPlayer.resume();
    } else {
      fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "").replace("-","_");
      RNAudioPlayer.initialise(fileName, (response) => callback && callback(response));
    }
  },
  pause() {
    RNAudioPlayer.pause();
  },
  stop() {
    RNAudioPlayer.stop();
  },
  seek(val) {
    RNAudioPlayer.seek(val);
  },
  startFadeOut() {
    RNAudioPlayer.startFadeOut();
  },
  setFadeOutInterval(val) {
    RNAudioPlayer.setFadeOutInterval(val);
  },
  getLastPointInTime(callback) {
    RNAudioPlayer.getLastPointInTime((response) => { callback(response) });
  }
};

module.exports = AudioPlayer;
