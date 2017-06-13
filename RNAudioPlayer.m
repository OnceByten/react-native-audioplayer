#import "RNAudioPlayer.h"
#import "RCTConvert.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"

@implementation RNAudioPlayer

NSString *const AudioPlayerEventFinished = @"playerFinished";
NSString *const AudioPlayerEventProgress= @"playerProgress";
NSTimeInterval _currentTime;
NSTimeInterval _currentDuration;
id _progressUpdateTimer;
int _progressUpdateInterval;
NSDate *_prevProgressUpdateTime;

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(play:(NSString *)fileName)
{
    NSError *error;
    
    AVAudioSession *session = [AVAudioSession sharedInstance];
    [session setCategory: AVAudioSessionCategoryPlayback error: nil];
    [session setActive: YES error: nil];
    
    NSURL *soundURL = [[NSBundle mainBundle] URLForResource:[[fileName lastPathComponent] stringByDeletingPathExtension]
                                             withExtension:[fileName pathExtension]];
    
    self.audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:soundURL error:&error];
    self.audioPlayer.delegate = self;
    
    if(error) {
        [self stopProgressTimer];
    } else {
        [self startProgressTimer];
    
        [self.audioPlayer play];
    }
}

RCT_EXPORT_METHOD(pause)
{
    [self.audioPlayer pause];
}

RCT_EXPORT_METHOD(stop)
{
    [self.audioPlayer stop];
    
    [self stopProgressTimer];
    [self sendProgressUpdate];
}
RCT_EXPORT_METHOD(resume)
{
    [self.audioPlayer play]; // yes, play not resume.. the normal intermediary play function has no ability to call play without breaking the existing player, this gets around it.
}

RCT_EXPORT_METHOD(seek:(NSNumber *) gotoTime) {
    
    NSTimeInterval intv = [gotoTime doubleValue];
    
    if (self.audioPlayer && intv>=0 && intv<=_currentDuration) {
        [self.audioPlayer stop];
        [self.audioPlayer setCurrentTime:intv];
        [self.audioPlayer prepareToPlay];
        [self.audioPlayer play];
    }
}

RCT_EXPORT_METHOD(startFadeOut) {
    if (self.audioPlayer) {
        
        [self doVolumeFade];
        
        
    }
}

- (void)doVolumeFade {
    if (self.audioPlayer.volume > 0.1) {
        self.audioPlayer.volume = self.audioPlayer.volume - 0.1;
        [self performSelector:@selector(doVolumeFade) withObject:nil afterDelay:0.1];
    } else {
        //go to the very end - complete!
        self.audioPlayer.volume = 0;
        self.audioPlayer.currentTime = _currentDuration;
        //[self seek:([NSNumber numberWithDouble:_currentDuration])];
        
        
//        [self.audioPlayer stop];
//        self.audioPlayer.currentTime = 0;
//        [self.audioPlayer prepareToPlay];
    }
}

- (void)sendProgressUpdate {
    
    
    if (self.audioPlayer && self.audioPlayer.playing) {
        _currentTime = _audioPlayer.currentTime;
        _currentDuration = _audioPlayer.duration;
    }
    
    // If audioplayer stopped, reset current time to 0
    if (self.audioPlayer && !self.audioPlayer.playing) {
        _currentTime = 0;
    }
    
    if (_prevProgressUpdateTime == nil ||
        (([_prevProgressUpdateTime timeIntervalSinceNow] * -1000.0) >= _progressUpdateInterval)) {
        
        [self.bridge.eventDispatcher sendDeviceEventWithName:AudioPlayerEventProgress body:@{
                                                                                         @"currentTime": [NSNumber numberWithFloat:_currentTime],
                                                                                         @"currentDuration": [NSNumber numberWithFloat:_currentDuration]
                                                                                         }];
        _prevProgressUpdateTime = [NSDate date];
    }
}

- (void)stopProgressTimer {
    [_progressUpdateTimer invalidate];
}

- (void)startProgressTimer {
    _progressUpdateInterval = 250;
    _prevProgressUpdateTime = nil;
    
    [self stopProgressTimer];
    
    _progressUpdateTimer = [CADisplayLink displayLinkWithTarget:self selector:@selector(sendProgressUpdate)];
    [_progressUpdateTimer addToRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
}

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)audioPlayer successfully:(BOOL)flag {
    [self stopProgressTimer];
    [self sendProgressUpdate];
    
    self.audioPlayer.volume = 1;
    
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"playerFinished" body:@{
                                                                                         @"finished": @TRUE
                                                                                         }];
}

@end