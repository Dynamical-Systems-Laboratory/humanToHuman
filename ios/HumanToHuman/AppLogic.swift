//
//  AppLogic.swift
//  HumanToHuman
//
//  Created by Albert Liu on 7/1/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation
import BackgroundTasks
import UIKit
import CoreBluetooth


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
    private static var token : String! = ""
    private static var data : Data? = nil
    private static var serverSendTimer : Timer!
    
    static func startup() {
        guard Database.initDatabase() else { exit(1) }
        appState = Int(Database.getPropNumeric(prop: KEY_APP_STATE) ?? UInt64(APPSTATE_NO_EXPERIMENT))
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.humantohuman.fetcher", using: nil) { task in
            let bgrequest = BGAppRefreshTaskRequest(identifier: "com.humantohuman.fetcher")
            bgrequest.earliestBeginDate = Date(timeIntervalSinceNow: 5)
            try? BGTaskScheduler.shared.submit(bgrequest)
            
            
            print("Sending data in the background")
            if data == nil {
                data = Server.formatConnectionData(id: bluetoothId, token: token, rows: Database.popRows())
                if data == nil {
                    task.setTaskCompleted(success: true)
                    return
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
                token = Database.getPropText(prop: KEY_TOKEN)!
            }
        
            if appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
                guard startCollectingDataStateless() else {
                    setAppState(APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
                    return
                }
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
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN
        || appState == APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING {
            print("No id to get!")
            exit(1)
        }
        return bluetoothId
    }
    
    static func getToken() -> String {
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN
            || appState == APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING {
            print("No token to get!")
            exit(1)
        }
        return token
    }
    
    static func getDescription() -> NSAttributedString {
        let descriptionString : String
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN {
            descriptionString = """
            <p><strong>What is this App?</strong></p>
            <p><span>This App has been developed by the Dynamical Systems Lab @ NYU and the PoliTo Complex System Lab to study the dynamics of close-range interaction between people. </span></p>
            <p><span style="font-weight: 400;">Our goal is to model and identify the dynamics of interactions in real-world contexts using temporal network frameworks. </span></p>
            <p><strong>How does it work?</strong></p>
            <p><span style="font-weight: 400;">This App will use the Bluetooth antenna of your smartphone to infer the distance to other participants&rsquo; phones without interfering with the normal use of any other device such as earphones, smartwatches, etc. and to collect the interaction time and intensity through the analysis of the Bluetooth signal. </span></p>
            <p><span style="font-weight: 400;">The app interface is essential and is optimized to save battery life and guarantee the normal performance of your phone. </span></p>
            <p><span style="font-weight: 400;">All data are anonymised and securely stored in servers at the Polytechnic of Torino.</span></p>
            <p><strong>How do I connect to a server?</strong></p>
            <p><span style="font-weight: 400;">Please go into Settings and type in the server URL in the text box, then press the &ldquo;SET URL&rdquo; button.</span></p>
            <p><strong>How will data be transmitted to our servers?</strong></p>
            <p><span style="font-weight: 400;">We give you the option to turn on and off the Bluetooth data collection and the option to send data to our server only through WiFi or using your mobile plan.</span></p>
            <p><strong>What will you do with the data?</strong></p>
            <p><span style="font-weight: 400;">All the data will be analysed and anonymously used for research activity in the field of temporal networks and interaction dynamics.</span></p>
            <p><strong>Contacts:</strong></p>
            <p><span style="font-weight: 400;">PoliTo Complex System Laboratory</span></p>
            <p><span style="font-weight: 400;">Politecnico di Torino, Corso Duca degli Abruzzi 24, Torino, Italy</span></p>
            <p><a href="mailto:xxxxxx@xxxx.xx"><span style="font-weight: 400;">humantohuman.polito@gmail.com</span></a></p>
            <p><span style="font-weight: 400;">Head of Laboratory: Professor Alessandro Rizzo</span></p>
            <p><span style="font-weight: 400;">Realized by: Albert Liu, Hugo Ramon Pascual, Francesco Vincenzo Surano</span></p>
            <p><span style="font-weight: 400;">Funded under the projects:</span></p>
            <p style="font-weight: 400;"><em><span style="font-weight: 400;">&ldquo;Hacking a complex world: unraveling the machanism underlying complex social and technological phenomena&rdquo;, </span></em><span style="font-weight: 400;">awarded</span> <span style="font-weight: 400;">by Compagnia di San Paolo. </span></p>
            <p style="font-weight: 400;"><span style="font-weight: 400;"><em>&ldquo;Macro to Micro: uncovering the hidden mechanisms driving network dynamics&rdquo;</em> (Mac2Mic), awarded by the Italian Ministry of Foreign Affairs and International Cooperation in the framework of the bilateral Italy-Israel scientific cooperation agreement. </span></p>
            """
        } else {
            descriptionString = Database.getPropText(prop: KEY_EXPERIMENT_DESCRIPTION)!
        }
        let htmlData = NSString(string: descriptionString).data(using: String.Encoding.unicode.rawValue)!
        
        let str = try! NSMutableAttributedString(data: htmlData, options: [.documentType: NSAttributedString.DocumentType.html], documentAttributes: nil)
        
        let fgColor : UIColor
        if #available(iOS 13.0, *) {
            fgColor = .label
        } else {
            fgColor = .black
        }
        
        str.addAttribute(NSAttributedString.Key.foregroundColor, value: fgColor, range: NSRange(location: 0, length: str.length))
        return str
    }
    
    static func getPolicy() -> NSAttributedString {
        if appState == APPSTATE_NO_EXPERIMENT || appState == APPSTATE_LOGGING_IN {
            print("we don't have a privacy policy to display!")
            exit(1)
        }
        
        let policyString = Database.getPropText(prop: KEY_PRIVACY_POLICY)!
        let htmlData = NSString(string: policyString).data(using: String.Encoding.unicode.rawValue)!
        let str = try! NSMutableAttributedString(data: htmlData, options: [.documentType: NSAttributedString.DocumentType.html], documentAttributes: nil)
        
        let fgColor : UIColor
        if #available(iOS 13.0, *) {
            fgColor = .label
        } else {
            fgColor = .black
        }
        
        str.addAttribute(NSAttributedString.Key.foregroundColor, value: fgColor, range: NSRange(location: 0, length: str.length))
        return str
    }
    
    private static func startCollectingDataStateless() -> Bool {
        let scanState = Bluetooth.startScanning()
        let advertiseState = Bluetooth.startAdvertising()
        guard scanState != .poweredOff && scanState != .unsupported else {
            return false
        }
        guard advertiseState != .poweredOff && advertiseState != .unsupported else {
            return false
        }
        
        print("making bgprocessing request")
        let request = BGAppRefreshTaskRequest(identifier: "com.humantohuman.fetcher")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 2)
        do {
            BGTaskScheduler.shared.cancelAllTaskRequests()
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("BGTaskScheduler errored \(error)")
            exit(1)
        }

        guard serverSendTimer == nil else { return true }
        
        serverSendTimer = Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { _ in
            if data == nil {
                data = Server.formatConnectionData(id: getBluetoothId(), token: getToken(), rows: Database.popRows())
                guard data != nil else { return }
            }
            
            Server.sendConnectionData(data: data!) {
                data = nil
            }
        }
        
        return true
    }
    
    static func startCollectingData() -> Bool {
        if appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING {
            print("Can't start collecting data while not in an experiment!")
            exit(1)
        }
        guard startCollectingDataStateless() else { return false }
        setAppState(APPSTATE_EXPERIMENT_RUNNING_COLLECTING)
        return true
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
        if appState != APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING
            && appState != APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING
            && appState != APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
            print("should be using leave experiment, not ignorePrivacyPolicy")
            exit(1)
        }
        
        setAppState(APPSTATE_NO_EXPERIMENT)
        data = nil
        token = nil
        BGTaskScheduler.shared.cancelAllTaskRequests()
        if serverSendTimer != nil {
            serverSendTimer.invalidate()
            serverSendTimer = nil
        }
        Database.popRows()
    }
    
    static func rejectPrivacyPolicy(callback: @escaping (String?) -> Void) {
        if appState == APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
            Bluetooth.stopScanning()
            Bluetooth.stopAdvertising()
        }
        setAppState(APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING)
        Server.removeUser { str in
            setAppState(APPSTATE_NO_EXPERIMENT)
            callback(str)
        }
    }
    
    static func acceptPrivacyPolicy(callback: @escaping (String?) -> Void) {
        Server.getUserId() { uid, tokenOrError in
            guard let uid = uid else {
                callback("Failed to get uid from server (\(tokenOrError))")
                setAppState(APPSTATE_NO_EXPERIMENT)
                return
            }

            print("Got bluetooth id: \(uid)")
            print("Got token: \(tokenOrError)")
            self.bluetoothId = uid
            self.token = tokenOrError
            Database.setPropNumeric(prop: KEY_OWN_ID, value: Int64(self.bluetoothId))
            Database.setPropText(prop: KEY_TOKEN, value: self.token)
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
