Creating the .obb file
======================

The *creat_obb.sh* is the shell script used for creating the .obb file which will contain all the necessary stuff for running the ConsoDroid Consoloid application from your mobile device.

Since it is a bash script file, it requires *bash*, and a *POSIX* like environment. The script was only tested on a *64 bit Linux system* (namely an *Ubuntu 14.04* derivative distro), however it should probably work on OS X too. (And you might even be able to run it in *CYGWIN* if you are lucky enough.)

Other than that you will need the following:

-	wget
-	git
-	npm
-	jobb (it comes with the Android SDK, but createobb.sh asumes that you can access it from your current path)
-	the Android NDK (if you are on a 64 bit Linux, it can download it for you)

Usage
=====

Build with pr-edownloaded and extracted Android NDK

```
./createobb /path/to/Android/NDK
```

Also download Android NDK (only for 64 bit Linux)

```
./createobb --download-ndk
```

Build it with mock Consodroid application for doing a manual integrity test

```
./createobb --mock /path/to/Android/NDK
```

Build everything from predefined source folders. (*node-source* for the node.js source, *consoloid-consodroid* for the Consoloid application)

```
./createobb --use-local-dir /path/to/Android/NDK
```
