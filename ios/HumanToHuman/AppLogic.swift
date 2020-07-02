//
//  AppLogic.swift
//  HumanToHuman
//
//  Created by Albert Liu on 7/1/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation


let APPSTATE_EXPERIMENT_RUNNING_COLLECTING = 0
let APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING = 1
let APPSTATE_NO_EXPERIMENT = 2
let APPSTATE_LOGGING_IN = 3
let APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING = 4
let APPSTATE_EXPERIMENT_JOINED_ACCEPTED_NOT_RUNNING = 5

class AppLogic {
    
    static var appState : Int = APPSTATE_NO_EXPERIMENT
    
    func startup() {
        guard Database.initDatabase() else { exit(1) }
    }
    
    static func getAppState() -> Int {
        return appState
    }
    
    static func setAppState(_ state: Int) {
        appState = state
        Database.setPropNumeric(prop: KEY_APP_STATE, value: Int64(state))
    }
    
    static func getBluetoothId() -> UInt64 {
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN {
            print("No id to get!")
            exit(1)
        }
        return Database.getPropNumeric(prop: KEY_OWN_ID)!
    }
    
    static func startCollectingData() {
        if appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING {
            print("Can't start collecting data while not in an experiment!")
        }
        
        Bluetooth.startScanning()
        guard Bluetooth.startAdvertising() else {
            print("advertising failed somehow")
            exit(1)
        }
        
        setAppState(APPSTATE_EXPERIMENT_RUNNING_COLLECTING)
    }
    
    static func stopCollectingData() {
        if appState != APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
            print("Can't stop collecting data while not currently collecting!")
            exit(1)
        }
        
        Bluetooth.stopScanning()
        Bluetooth.stopAdvertising()
        setAppState(APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
    }
    
    static func getServerURL() -> String {
        if appState == APPSTATE_NO_EXPERIMENT {
            print("Can't get server URL when there's no experiment!")
            exit(1)
        }

        return Database.getPropText(prop: KEY_SERVER_BASE_URL)!
    }
    
    static func setServerCredentials(urlString: String, callback: (Bool) -> Void) {
        Database.setPropText(prop: KEY_SERVER_BASE_URL, value: urlString)
    }
}
