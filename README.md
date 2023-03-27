[![badge](https://img.shields.io/twitter/follow/api_video?style=social)](https://twitter.com/intent/follow?screen_name=api_video)
&nbsp; [![badge](https://img.shields.io/github/stars/apivideo/api.video-android-live-stream?style=social)](https://github.com/apivideo/api.video-android-live-stream)
&nbsp; [![badge](https://img.shields.io/discourse/topics?server=https%3A%2F%2Fcommunity.api.video)](https://community.api.video)
![](https://github.com/apivideo/.github/blob/main/assets/apivideo_banner.png)
<h1 align="center">Android RTMP live stream client</h1>

[api.video](https://api.video) is the video infrastructure for product builders. Lightning fast
video APIs for integrating, scaling, and managing on-demand & low latency live streaming features in
your app.

# Table of contents

- [Table of contents](#table-of-contents)
- [Project description](#project-description)
- [Getting started](#getting-started)
    - [Installation](#installation)
        - [Gradle](#gradle)
    - [Permissions](#permissions)
    - [Code sample](#code-sample)
- [Documentation](#documentation)
- [Dependencies](#dependencies)
- [Sample application](#sample-application)
- [FAQ](#faq)

# Project description

This library is an easy way to broadcast livestream to api.video platform on Android.

# Getting started

## Installation

### Gradle

On build.gradle add the following code in dependencies:

```groovy
dependencies {
    implementation 'video.api:android-live-stream:1.3.1'
}
```

## Permissions

```xml

<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
</manifest>
```

Your application must dynamically require `android.permission.CAMERA`
and `android.permission.RECORD_AUDIO`.

## Code sample

1. Add [permissions](#permissions) to your `AndroidManifest.xml` and request them in your
   Activity/Fragment.
2. Add a `ApiVideoView` to your Activity/Fragment layout for the camera preview.

```xml

<video.api.livestream.views.ApiVideoView
    android:id="@+id/apiVideoView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

3. Implement a `ConnectionChecker`.

```kotlin
val connectionChecker = object : ConnectionChecker {
    override fun onConnectionSuccess() {
        //Add your code here
    }

    override fun onConnectionFailed(reason: String?) {
        //Add your code here
    }

    override fun onDisconnect() {
        //Add your code here
    }
}
```

4. Creates an `ApiVideoLiveStream` instance.

```kotlin
class MyFragment : Fragment(), ConnectionChecker {
    private var apiVideoView: ApiVideoView? = null
    private lateinit var apiVideo: ApiVideoLiveStream

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiVideoView = view.findViewById(R.id.apiVideoView)
        val audioConfig = AudioConfig(
            bitrate = 128 * 1000, // 128 kbps
            sampleRate = 44100, // 44.1 kHz
            stereo = true,
            echoCanceler = true,
            noiseSuppressor = true
        )
        val videoConfig = VideoConfig(
            bitrate = 2 * 1000 * 1000, // 2 Mbps
            resolution = Resolution.RESOLUTION_720,
            fps = 30
        )
        apiVideo =
            ApiVideoLiveStream(
                context = getContext(),
                connectionChecker = this,
                initialAudioConfig = audioConfig,
                initialVideoConfig = videoConfig,
                apiVideoView = apiVideoView
            )
    }
}
```

5. Start your stream with `startStreaming` method

For detailed information on this livestream library API, refers
to [API documentation](https://apivideo.github.io/api.video-android-live-stream/).

# Tips

You can check device supported configurations by using the helper: `Helper`

# Documentation

* [API documentation](https://apivideo.github.io/api.video-android-live-stream/)
* [api.video documentation](https://docs.api.video)

# Dependencies

We are using external library

| Plugin | README |
| ------ | ------ |
| [StreamPack](https://github.com/ThibaultBee/StreamPack) | [README.md](https://github.com/ThibaultBee/StreamPack/blob/master/README.md) |

# Sample application

A demo application demonstrates how to use this livestream library. See `/example` folder.

# FAQ

If you have any questions, ask us here:  https://community.api.video . Or use [Issues].


[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

[Issues]: <https://github.com/apivideo/api.video-android-live-stream/issues>
