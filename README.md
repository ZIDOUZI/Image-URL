[![GitHub license](https://img.shields.io/badge/License-MIT-blue)](https://mit-license.org/)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green)](https://www.android.com/)

[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.21-_)](https://developer.android.com/jetpack/compose)
# Image Tool

### 简介

主要用于B站封面的获取,不过其实微信文章也可以.是我[另一个项目](https://github.com/ZIDOUZI/Bilibili-Cover-Getter)的Android版

改版后也适用于处理uid和pid.

包括两个小彩蛋XD

### 已知问题

1. 加载图片过大时(10M~15M)将导致应用闪退.  
  `java.lang.RuntimeException: Canvas: trying to draw too large(139197440bytes) bitmap.`
2. 直播间封面无法加载,加载将导致400错误.
  


### 建议 or bug反馈

请前往[issues](https://github.com/ZIDOUZI/Image-URL/issues)
