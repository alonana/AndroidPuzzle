AndroidPuzzle
=============

This is my first android application.
It is a simple puzzle.
Please do not use this without explicit permission from me. 


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

