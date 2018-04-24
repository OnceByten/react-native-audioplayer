'use strict';

var { RNAudioPlayer } = require('react-native').NativeModules;
var { Platform } = require('react-native');

var AudioPlayer = {
  play(fileName: string, filePath: string,) {
    /*fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "");
    RNAudioPlayer.play(fileName);*/
    if (fileName === undefined) {
      RNAudioPlayer.resume();
    } else {
      fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "").replace("-","_");
      RNAudioPlayer.play(fileName, filePath);
    }
  },
  playFromExpansion(fileName: string, mainVersion: int, patchVersion: int) {
    if (fileName === undefined) {
      RNAudioPlayer.resume();
    } else {
      fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "").replace("-","_");
      RNAudioPlayer.playFromExpansion(fileName.toLowerCase(), mainVersion, patchVersion);
    }
  },
  initialise(fileName: string, filePath: string, callback: callback) {
    if (fileName === undefined) {
    //  RNAudioPlayer.resume();
    } else {
      fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "").replace("-","_");
      RNAudioPlayer.initialise(fileName, filePath, (response) => callback && callback(response));
    }
  },
  initialiseFromExpansion(fileName: string, mainVersion: int, patchVersion: int, callback: callback) {
    if (fileName === undefined) {
    //  RNAudioPlayer.resume();
    } else {
      fileName = Platform.OS === 'ios' ? fileName : fileName.replace(/\.[^/.]+$/, "").replace("-","_");
      RNAudioPlayer.initialiseFromExpansion(fileName.toLowerCase(), mainVersion, patchVersion, (response) => callback && callback(response));
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
  setFadeOutIntervalWithOptions(fadeOutTime, fadeDuration) {
    RNAudioPlayer.setFadeOutIntervalWithOptions(fadeOutTime, fadeDuration);
  },
  getLastPointInTime(callback) {
    RNAudioPlayer.getLastPointInTime((response) => { callback(response) });
  },
  setVolume(val) {
    RNAudioPlayer.setVolume(val);
  }
};

module.exports = AudioPlayer;
