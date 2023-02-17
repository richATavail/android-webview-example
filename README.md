Example of Lean Mobile Web App Running in Android WebView
================================================================================

This app demonstrates how to build a web app using web technologies 
(Javascript, HTML, CSS) that is presented as a mobile web app running in 
[Android's `WebView`](https://developer.android.com/develop/ui/views/layout/webapps).


This example Android app demonstrates:
 1. The setup of a mobile web app provided through `WebView`
 2. Exposing a native Android API that is callable from the web app
 3. Exposing a Javascript API that is callable from native Android code.
 4. A server-client message passing protocol between the web app and the native 
    Android code allowing for asynchronous communication.

## How to Navigate
All example code is well documented. Begin by exploring
`com.bitwisearts.example.MainActivity`.

## Web App
The web app is a plain HTML and Javascript application placed in 
`app/src/main/assets`.