![](https://github.com/apivideo/API_OAS_file/blob/master/apivideo_banner.png)
# Api.Video Android Kotlin LiveStream Module

This module is an easy way to broadcast livestream on api.video platform

## Installation
### With maven
On build.gradle add the following code in dependencies:
```xml
dependencies {
    ...
    implementation 'video.api:android-live-stream:0.1.5' // add this line
}
```
### Or import with a local aar

1. Download the [latest release](https://github.com/apivideo/android-live-stream/releases) of the aar file.
2. Go to “File” > “Project Structure...”
3. On "Modules" select the "+" button and select "Import .JAR/.AAR Package" then click "next"
4. Select the AAR file you have just downloaded, and click "Finish"
5. Then go to "Dependencies" select the the app module and add a new dependencies by clicking on the "+" button, then select "Module Dependency"
(if there is no "Module Dependency", close the window and re-open it, it should be fine)
6. select Api.Video module, click "ok"
7. click on "Apply" and then "ok"

### Permissions:
```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
</manifest>
```
### Quick Start
Your class must implement ConnectCheckerRtmp and override all methods 

```kotlin
class FirstFragment : Fragment(), ConnectCheckerRtmp{
  private var openGlView: OpenGlView? = null
  private lateinit var apiVideo: ApiVideoLiveStream
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openGlView = view.findViewById(R.id.surfaceView)
        apiVideo = ApiVideoLiveStream(requireContext(),this,openGlView,null)
    }
    
  override fun onConnectionSuccessRtmp() {
      //Add your code here
  }

  override fun onConnectionFailedRtmp(reason: String?) {
      //Add your code here
  }

  override fun onNewBitrateRtmp(bitrate: Long) {
      //Add your code here
  }

  override fun onDisconnectRtmp() {
      //Add your code here
  }

  override fun onAuthErrorRtmp() {
      //Add your code here
  }

  override fun onAuthSuccessRtmp() {
      //Add your code here
  }
}
```

To start your stream use startStreaming methode 

1. if you are broadcasting on api.video 

```kotlin
apiVideo.startStreaming("YOUR_STREAM_KEY", null)
```
2. else

```kotlin
apiVideo.startStreaming("YOUR_STREAM_KEY", "YOUR_RTMP_URL")
```

### Plugins

We are using external library

| Plugin | README |
| ------ | ------ |
| rtmp-rtsp-stream-client-java | [https://github.com/pedroSG94/rtmp-rtsp-stream-client-java][rtmp-rtsp-stream-client-java] |

### FAQ
If you have any questions, ask us here:  https://community.api.video .
Or use [Issues].

License
----

MIT License
Copyright (c) 2020 api.video

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

   [rtmp-rtsp-stream-client-java]: <https://github.com/pedroSG94/rtmp-rtsp-stream-client-java>
   [Issues]: <https://github.com/apivideo/android-live-stream/issues>
