# opencv-android
android调用opencv小demo
OpenCV是Open Source Computer Vision Library 缩写, 主要由Intel公司的一些大佬写的, 功能是提供大量的图形图形处理(计算机视觉)的库, 并且提供 iOS 和 Android 的 SDK, 由于我主要工作是和Camera以及算法集成有关, 所以经常会用到这个工具, 在此记录下 Android 端的 SDK 使用和配置
OpenCV-android-sdk下载
下载地址(官网): https://opencv.org/releases.html
最新的版本是4.0.0 beta, 我们下载稳定版本3.4.3, 直接点击Android pack进行下载, 下载后解压即可.
导入Java SDK到Android Studio
打开Android Studio后新建一个项目 OpencvDemo,建好后, 依次点击
File  -->  New --> Import Module , 如图所示:
在打开的窗口中选择Opencv-android-sdk文件夹下面的sdk目录, 
将Module name 命名为 opencvSdk, 点击 Finish.
导入过程中 Android Studio 会提示 Manifest 中 sdkVersion 设置的错误, 我们需要直接删除 opencvSdk 中 AndroidManifest.xml 的 minSdkVersion 和 targetSdkVersion, 原因是现在 sdkVersion 的设置现在放到了 gradle 配置文件中, 删除 uses-sdk, 如图所示:
将opencvSdk中 build.gradle 关于android sdk 的相关设置改为和当前项目相同, 只需修改 compileSdkVersion minSdkVersion 和 targeSdkVersion, 如果所示:
改完后重新编译一下, OpenCV 导入就大功告成了, 接下来是在我们自己的项目中进行使用.
使用Opencv中的高斯模糊
接下来我们写个 Demo 来使用 OpenCV 中的高斯模糊, 首先在我们项目的中添加我们导入的 opencvSdk 作为依赖, 在 build.gradle 中加入 implementation project(':opencvSdk') 如图所示:
作者：smewise
链接：https://www.jianshu.com/p/2547743bb61b
来源：简书
简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
