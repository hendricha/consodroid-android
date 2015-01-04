ConsoDroid Android application
==============================

ConsoDroid is a Android application that let's you access the files on your Android device from a remote webbrowser. However unlike some other similiar apps, this one is completely open source.

How?
====

The ConsoDroid Android application runs a node.js server on your Android device, which itself runs a Consoloid application. Consoloid is a node.js based web application framework, that can be used for creating Post-WIMP applications.

What exactly is this repository?
================================

This repository is the home of the Android application written in Java, and the script that can build the .obb file used by the Java app. The .obb file will contain all the necessarry files needed for running the node.js server, so - Node.js executable compiled for Android ARM Linux - The ConsoDroid Consoloid application - all the necessary node.js modules

How can I build it?
===================

First you need to run the *createobb.sh* shell script, to build the .obb file, then open this directory with Android Studio or use gradle to build the Android package (.apk).

How does the createobb.sh work?
===============================

Please consult *createobb.md* for that.
