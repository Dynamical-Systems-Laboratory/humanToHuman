# Code Organization
The code is laid out as follows:

```

HumanToHuman
├── AppDelegate.swift
├── AppLogic.swift
├── Assets
├── Assets.xcassets
│  ├── AppIcon.appiconset
│  │  └── ...
│  └── Contents.json
├── Bridging.h
├── FMDB
│  └── ...
├── Info.plist
├── LaunchScreen.storyboard
├── UI
│  ├── Main.storyboard
│  ├── MainController.swift
│  ├── PrivacyPolicyController.swift
│  └── SettingsController.swift
└── Util
   ├── Bluetooth.swift
   ├── Database.swift
   ├── OverflowAreaUtils.swift
   ├── Server.swift
   └── Wifi.swift
```

## FMDB
FMDB is a library we use for SQLite database interaction. This code is **read only**.

## Bridging.h
`Bridging.h` is a file that bridges the Objective-C code (namely FMDB code) to the
rest of the codebase, which is written in Swift.

## Util
This is where all the utility code lives. It's stuff like sending/receiving Bluetooth
data (`Bluetooth.swift`), reading and writing to the SQLite Database (`Database.swift`),
and sending and receiving data from the server (`Server.swift`). It also includes
other utilities related to wifi and the Overflow Area.

## UI
This is where the user interface is managed.

## AppLogic.swift
This is where the application's state is managed. Almost all of the state is handled
here.


