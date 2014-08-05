Puzzle Me
=============

This is my first android application.
It is a simple puzzle.
Please do not use this without explicit permission from me. 




Build Instructions
==================
This project uses Google Play Game Services.

***
Copy Google API from
<Google andoird SDK>\extras\google\google_play_services\libproject\google-play-services_lib
to:
<eclipse workspace>\GoogleProjects\google_play_services_lib\
and import using "Existing android code" 

***
Clone https://github.com/playgameservices/android-basic-samples.git into 
<eclipse workspace>\GoogleProjects\android-basic-samples\
and import using "Existing android code" 
<eclipse workspace>\GoogleProjects\android-basic-samples\BasicSamples\libraries\BaseGameUtils\src\main
Rename this project from "main" to "BaseGameUtils"
add java as source folder for this project
under project, properties, android, library, check isLibrary
under project, properties, android, library, add: appcompat_v7 and google-play-services_lib

***
fix GameHelper.Java, line 859, from:
   if (mConnectionResult.hasResolution() ) {
to
   if (mConnectionResult.hasResolution() && (mActivity != null)) {
 

***
For this project:
under project, properties, android, library, add: BaseGameUtils

***
in google play services console add certificate client for both debug and release keys
c:\jdk\1.7_64\bin\keytool -keystore c:\adt-bundle-windows-x86_64-20140702\keystore\googleplay.ks -list
c:\jdk\1.7_64\bin\keytool -keystore C:\Users\atamir\.android\debug.keystore  -list  (password for debug is android)


**
Use the target starting with "Google APIs" in avd creation, not the one starting with "Android"

 
 
 
 Useful links
 ============
 Free Android Icons:  http://developer.android.com/design/downloads/index.html
 Icons generator:     http://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
 