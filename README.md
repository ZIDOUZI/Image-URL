[![GitHub license](https://img.shields.io/badge/License-MIT-blue)](https://mit-license.org/)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green)](https://www.android.com/)

[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.21-_)](https://developer.android.com/jetpack/compose)
# Image Tool

### 简介

1. B站视频(av,BV)、文章、~~直播~~ [已知问题1](#已知问题) 封面url获取,微信文章封面url获取.支持预览,支持使用本地图片查看器查看,支持分享或保存图片.
2. 解析av,cv,Live(直播间号),uid,pid.uid,pid支持跳转打开对应url.

### 已知问题

1. 加载图片过大时(10M~15M)将导致应用闪退.
   `java.lang.RuntimeException: Canvas: trying to draw too large(139197440bytes) bitmap.`
2. 无法加载直播间封面.加载将导致400错误.

### feature

0. 修复已知问题
1. 支持BV号解析
2. 支持pid预览(真的有必要吗🤔)

### 建议 or bug反馈

请前往[issues](https://github.com/ZIDOUZI/Image-URL/issues)