![](https://github.com/apivideo/API_OAS_file/blob/master/apivideo_banner.png)

# [api.video](https://api.video) Android Kotlin Livestream library

This library is an easy way to broadcast livestream to api.video platform on Android.

## Installation

### Gradle

On build.gradle add the following code in dependencies:

```xml
dependencies {
        ...
        implementation 'video.api:android-livestream:0.3.0'
}
```

## Sample

A demo application demonstrates how to use this livestream library. See `/app` folder.

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

## Getting Started

1. Add [permissions](#permissions) to your `AndroidManifest.xml` and request them in your
   Activity/Fragment.
2. Add a `ApiVideoView` to your Activity/Fragment layout for the camera preview.

```xml
<video.api.livestream.views.ApiVideoView 
    android:id="@+id/apiVideoView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:aspectRatioMode="adjust"
    app:isFlipHorizontal="false"
    app:isFlipVertical="false"
    app:keepAspectRatio="true" />
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

    override fun onAuthError() {
        //Add your code here
    }

    override fun onAuthSuccess() {
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
            bitrate = 128 * 1024, // 128 kbps
            sampleRate = 48000,
            stereo = true,
            echoCanceler = true,
            noiseSuppressor = true
        )
        val videoConfig = VideoConfig(
            bitrate = 2 * 1024 * 1024, // 2 Mbps
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
to [API documentation](https://apivideo.github.io/android-livestream/).

### Documentation

* [API documentation](https://apivideo.github.io/android-livestream/)
* [api.video documentation](https://docs.api.video)

### Dependencies

We are using external library

| Plugin | README |
| ------ | ------ |
| rtmp-rtsp-stream-client-java | [https://github.com/pedroSG94/rtmp-rtsp-stream-client-java][rtmp-rtsp-stream-client-java] |

### FAQ

If you have any questions, ask us here:  https://community.api.video . Or use [Issues].

License
----

MIT License Copyright (c) 2020 api.video

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

[rtmp-rtsp-stream-client-java]: <https://github.com/pedroSG94/rtmp-rtsp-stream-client-java>

[Issues]: <https://github.com/apivideo/android-livestream/issues>
