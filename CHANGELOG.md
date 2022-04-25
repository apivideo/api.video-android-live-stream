# Changelog
All changes to this project will be documented in this file.

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
