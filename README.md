# Blurred


Android高斯模糊库，调教参数后可实现实时高斯模糊（&lt;16ms，可达2~3ms）

[GitHub主页](https://github.com/goweii/Blurred)

[Demo下载](https://github.com/goweii/Blurred/raw/master/app/release/app-release.apk)


# How to use


To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

gradle
maven
sbt
leiningen
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.goweii:Blurred:1.2.0'
	}
