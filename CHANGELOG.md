# Changelog
All changes to this project will be documented in this file.

## [1.3.1] - 2023-03-27
- Return a `onConnectionFailed` when `connectStream` failed.

## [1.3.0] - 2023-01-06
- Add an API to set the interval between to key frames
- Synchronize video and audio RTMP packets
- Fix a crash when microphone is muted on few devices

## [1.2.3] - 2022-10-10
- Fix a crash on `stopStreaming` due to a `free` in `rtmpdroid`

## [1.2.2] - 2022-10-05
- Fix preview when `videoConfig` is set before the `view.display` exists

## [1.2.1] - 2022-09-29
- Fix preview when `ApiVideoView` has already been created 
- Only call `onDisconnect` when application was connected
- Release workflow is triggered on release published (instead of created)
- Example: remove rxpermission usage

## [1.2.0] - 2022-08-18
- Adds API to set zoom ratio

## [1.1.0] - 2022-08-05
- `initialVideoConfig` and `initialAudioConfig` are now optional
- Multiple fixes on RTMP stream (to avoid ANR and to improve compatibility)

## [1.0.4] - 2022-06-28
- Disconnect after a `stopStream`.

## [1.0.3] - 2022-06-13
- Fix stream after a `stopPreview` call.
- Disconnect if `startStream` fails.

## [1.0.2] - 2022-04-25
- Do not remove SurfaceView callbacks when the Surface is destroyed.

## [1.0.1] - 2022-04-13
- Fix audioConfig and videoConfig API
- Improve stop live button look

## [1.0.0] - 2022-04-05
- Add a configuration helper
- Add video and audio configuration default value instead of using a builder
- Change internal RTMP live stream library

## [0.3.3] - 2022-01-24
- Add startPreview/stopPreview API

## [0.3.2] - 2022-01-19
- Catch onConnectionFailed to stop streaming without user
- Throw an exception on `startStreaming` when stream key is empty
- Remove jcenter as a dependency repository

## [0.3.1] - 2021-12-14
- Add a trailing slash at the end of the RTMP url in case it is missing
- Rename project to live-stream

## [0.3.0] - 2021-10-14
- Add/Improve API: introducing videoConfig and audioConfig changes

## [0.3.0] - 2021-10-14
- Add/Improve API: introducing videoConfig and audioConfig changes
  
## [0.2.0] - 2021-10-07
- Sample application

## [0.1.0] - 2021-05-14
- First version
