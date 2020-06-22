//
//  SettingsController.swift
//  HumanToHuman
//
//  Created by Albert Liu on 6/16/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation
import UIKit

class SettingsController: UIViewController {

    @IBOutlet var toggleCollectButton: UIButton!
    @IBOutlet var baseurlField: UITextField!

    override func viewDidLoad() {
        print("settings controller loading...")
        if Bluetooth.advertising {
            toggleCollectButton.setTitle("stop collection", for: .normal)
        } else {
            toggleCollectButton.setTitle("collect data", for: .normal)
        }
        baseurlField.text = Database.getPropText(prop: KEY_SERVER_BASE_URL)
    }
    
    @IBAction func toggleCollection() {
        if Bluetooth.advertising {
            Bluetooth.stopAdvertising()
            Bluetooth.stopScanning()
            toggleCollectButton.setTitle("collect data", for: .normal)
        } else {
            guard Bluetooth.startAdvertising() else { return } // this fails if there's no id given
            Bluetooth.startScanning()
            toggleCollectButton.setTitle("stop collection", for: .normal)
        }
    }
    
    @IBAction func setBaseurl() {
        if let baseurl = baseurlField.text {
            guard URL(string: baseurl) != nil else {
                print("error, baseurl is not properly formatted")
                return
            }
            
            Database.setPropText(prop: KEY_SERVER_BASE_URL, value: baseurl)
            Services.popToServer(baseurl: baseurl)
            if let id = Database.getPropNumeric(prop: KEY_OWN_ID) {
                print("init with saved id \(id)")
                Bluetooth.id = id
            } else {
                Server.getUserId(baseurl: baseurl) { id in
                    guard let id = id else { exit(1) }
                    print("got id \(id)")
                    Database.setPropNumeric(prop: KEY_OWN_ID, value: Int64(bitPattern: id))
                    Bluetooth.id = id
                }
            }
        }
    }
}
