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
    
    @IBOutlet var wifiLabel: UILabel!
    @IBOutlet var toggleAdvertiseButton: UIButton!
    @IBOutlet var toggleScanButton: UIButton!
    @IBOutlet var clearDataButton: UIButton!
    
    override func viewDidLoad() {
        print("settings controller loading...")
        wifiLabel.text = getWiFiAddress() ?? "Unknown"
        clearDataButton.isEnabled = Database.rowCount() != 0

        if Bluetooth.advertising {
            toggleAdvertiseButton.setTitle("stop advertising", for: .normal)
        } else {
            toggleAdvertiseButton.setTitle("advertise", for: .normal)
        }
        
        if Bluetooth.scanning {
            toggleScanButton.setTitle("stop scannning", for: .normal)
        } else {
            toggleScanButton.setTitle("scan", for: .normal)
        }
    }
    
    @IBAction func clearData() {
        Services.popDestroy()
    }
    
    @IBAction func toggleAdvertising() {
        if Bluetooth.advertising {
            Bluetooth.stopAdvertising()
            toggleAdvertiseButton.setTitle("advertise", for: .normal)
        } else {
            guard Bluetooth.startAdvertising() else { return } // this fails if there's no id given
            toggleAdvertiseButton.setTitle("stop advertising", for: .normal)
        }
    }
    
    @IBAction func toggleScanning() {
        if Bluetooth.scanning {
            Bluetooth.stopScanning()
            toggleScanButton.setTitle("scan", for: .normal)
        } else {
            Bluetooth.startScanning()
            toggleScanButton.setTitle("stop scanning", for: .normal)
        }
    }
}
