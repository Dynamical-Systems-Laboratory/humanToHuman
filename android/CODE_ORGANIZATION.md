# Code Organization
The code is laid out as follows:

```
app
├── android-app.iml
├── app.iml
├── build
│  └── ...
├── build.gradle
├── release
│  └── ...
└── src
   └── main
      ├── AndroidManifest.xml
      ├── ic_launcher-web.png
      ├── java
      │  └── com
      │     └── polito
      │        └── humantohuman
      │           ├── Activities
      │           │  ├── PolicyActivity.java
      │           │  ├── ScanActivity.java
      │           │  └── SettingsActivity.java
      │           ├── AppLogic.java
      │           ├── Bluetooth.java
      │           ├── Database.java
      │           ├── Server.java
      │           └── utils
      │              ├── OverflowAreaUtils.java
      │              ├── PermissionUtils.java
      │              └── Polyfill.java
      └── res
         └── ...
```

## com.polito.humantohuman.utils
Utilities for handling permissions, the Overflow Area, and miscellaneous problems.

## com.polito.humantohuman.Bluetooth
This is where bluetooth scanning and advertising is implemented

## com.polito.humantohuman.Server
This is where communication to and from the server is implemented

## com.polito.humantohuman.Database
This is where reading and writing to the SQLite database is implemented

## com.polito.humantohuman.AppLogic
This is where the application's state is managed. Almost all of the state is handled
here.

## com.polito.humantohuman.Activities
This is where the user interface code lives.
