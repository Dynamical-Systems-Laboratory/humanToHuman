//
//  AppLogic.swift
//  HumanToHuman
//
//  Created by Albert Liu on 7/1/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation
import BackgroundTasks


let APPSTATE_EXPERIMENT_RUNNING_COLLECTING = 0
let APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING = 1
let APPSTATE_NO_EXPERIMENT = 2
let APPSTATE_LOGGING_IN = 3
let APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING = 4
let APPSTATE_EXPERIMENT_JOINED_ACCEPTED_NOT_RUNNING = 5

class AppLogic {
    
    private static var appState : Int = APPSTATE_NO_EXPERIMENT
    private static var serverURL : String = ""
    private static var bluetoothId : UInt64 = 0
    private static var data : Data? = nil
    
    static func startup() {
        guard Database.initDatabase() else { exit(1) }
        appState = Int(Database.getPropNumeric(prop: KEY_APP_STATE) ?? UInt64(APPSTATE_NO_EXPERIMENT))
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.humantohuman.fetcher", using: nil) { task in
            let bgrequest = BGProcessingTaskRequest(identifier: "com.humantohuman.fetcher")
            bgrequest.earliestBeginDate = Date(timeIntervalSinceNow: 5)
            bgrequest.requiresNetworkConnectivity = true
            try? BGTaskScheduler.shared.submit(bgrequest)
            
            
            print("Sending data in the background")
            if data == nil {
                data = Server.formatConnectionData(id: bluetoothId, rows: Database.popRows())
                if data == nil {
                    task.setTaskCompleted(success: true)
                }
            }
            
            Server.sendConnectionData(data: data!) {
                print("data finished sending")
                data = nil
                task.setTaskCompleted(success: true)
            }
        }
        
        Bluetooth.delegate = AppLogic()
        
        if appState == APPSTATE_LOGGING_IN {
            setAppState(APPSTATE_NO_EXPERIMENT)
            return
        }
        
        if appState != APPSTATE_NO_EXPERIMENT {
            serverURL = Database.getPropText(prop: KEY_SERVER_BASE_URL)!
            
            if appState != APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING {
                bluetoothId = Database.getPropNumeric(prop: KEY_OWN_ID)!
            }
        
            if appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
                Bluetooth.startScanning()
                guard Bluetooth.startAdvertising() else { exit(1) } // TODO handle this condition
            }
        }

    }
    
    static func getAppState() -> Int {
        return appState
    }
    
    private static func setAppState(_ state: Int) {
        appState = state
        Database.setPropNumeric(prop: KEY_APP_STATE, value: Int64(state))
    }
    
    static func getBluetoothId() -> UInt64 {
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN {
            print("No id to get!")
            exit(1)
        }
        return bluetoothId
    }
    
    static func getDescription() -> String {
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN {
            return ""
        }
        return Database.getPropText(prop: KEY_EXPERIMENT_DESCRIPTION)!
    }
    
    static func getPolicy() -> String {
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN {
            print("we don't have a privacy policy to display!")
            exit(1)
        }
        return Database.getPropText(prop: KEY_PRIVACY_POLICY)!
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
        
        let request = BGProcessingTaskRequest(identifier: "com.humantohuman.fetcher")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 2)
        request.requiresNetworkConnectivity = true
        do {
            BGTaskScheduler.shared.cancelAllTaskRequests()
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("BGTaskScheduler errored \(error)")
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
    
    static func setServerCredentials(urlString: String, callback: @escaping (String?) -> Void) {
        if appState != APPSTATE_NO_EXPERIMENT {
            print("Can't set URL while already in an experiment!")
            exit(1)
        }
        
        if URL(string: urlString) == nil {
            callback("url failed to parse")
            return
        }
        
        serverURL = urlString
        Database.setPropText(prop: KEY_SERVER_BASE_URL, value: serverURL)
        setAppState(APPSTATE_LOGGING_IN)
        
        var countdown : Int32 = 2
        var errorSent : Int32 = 0
        
        Server.getDescription(callback: { description in
            guard let description = description else {
                if OSAtomicCompareAndSwap32(0, 1, &errorSent) {
                    setAppState(APPSTATE_NO_EXPERIMENT)
                    callback("Failed to get description")
                }
                return
            }
            
            Database.setPropText(prop: KEY_EXPERIMENT_DESCRIPTION, value: description)
            if OSAtomicDecrement32(&countdown) == 0 {
                setAppState(APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING)
                callback(nil)
            }
        })
        
        Server.getPrivacyPolicy(callback: { privacyPolicy in
            guard let privacyPolicy = privacyPolicy else {
                if OSAtomicCompareAndSwap32(0, 1, &errorSent) {
                    setAppState(APPSTATE_NO_EXPERIMENT)
                    callback("Failed to get privacy policy")
                }
                return
            }
            
            Database.setPropText(prop: KEY_PRIVACY_POLICY, value: privacyPolicy)
            if OSAtomicDecrement32(&countdown) == 0 {
                setAppState(APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING)
                callback(nil)
            }
        })
    }
    
    static func leaveExperiment() {
        if appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
            Bluetooth.stopScanning()
            Bluetooth.stopAdvertising()
        }
        setAppState(APPSTATE_NO_EXPERIMENT)
    }
    
    static func acceptPrivacyPolicy(callback: @escaping (String?) -> Void) {
        Server.getUserId() { uid in
            guard let uid = uid else {
                callback("Failed to get uid from server")
                setAppState(APPSTATE_NO_EXPERIMENT)
                return
            }

            print("Got bluetooth id: \(uid)")
            bluetoothId = uid
            Database.setPropNumeric(prop: KEY_OWN_ID, value: Int64(uid))
            setAppState(APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
            callback(nil)
        }
    }
}

extension AppLogic: BTDelegate {
    func discoveredDevice(_ device: Device) {
        guard Database.writeRow(device: device) else {
            print("something went wrong with sql")
            return
        }
    }
}
