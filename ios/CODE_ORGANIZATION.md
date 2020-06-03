# Code Organization
The code is laid out as follows:

```
HumanToHuman
├── AppDelegate.swift
├── Assets
├── Assets.xcassets
│  ├── AppIcon.appiconset
│  │  └── Contents.json
│  └── Contents.json
├── Bridging.h
├── FMDB
│  └── ...
├── Info.plist
├── LaunchScreen.storyboard
├── Main.storyboard
├── Util
│  ├── Bluetooth.swift
│  ├── Database.swift
│  ├── OverflowAreaUtils.swift
│  ├── Server.swift
│  └── Wifi.swift
└── ViewController.swift
```

## FMDB
FMDB is a library we use for SQLite database interaction. This code is **read only**.

## Bridging.h
`Bridging.h` is a file that bridges the Objective-C code (namely FMDB code) to the
rest of the codebase, which is written in Swift.

## Util
This is where all the utility code lives. It's all the stuff that isn't UI code.

## ViewController.swift
This is where the application logic is.


