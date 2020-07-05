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
    @IBOutlet var baseurlButton: UIButton!
    @IBOutlet var privacyPolicyButton : UIButton!

    override func viewDidLoad() {
        print("settings controller loading...")
        baseurlField.text = "http://192.168.1.151:8080/experiment/password"
        switch AppLogic.getAppState() {
        case APPSTATE_EXPERIMENT_RUNNING_COLLECTING:
            toggleCollectButton.setTitle("stop collection", for: .normal)
            baseurlButton.isEnabled = false
            baseurlField.isEnabled = false
            toggleCollectButton.isEnabled = true
            privacyPolicyButton.isEnabled = true
            break
        case APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING:
            toggleCollectButton.setTitle("collect data", for: .normal)
            baseurlField.isEnabled = false
            baseurlButton.isEnabled = false
            toggleCollectButton.isEnabled = true
            privacyPolicyButton.isEnabled = true
            break
        case APPSTATE_NO_EXPERIMENT:
            toggleCollectButton.setTitle("collect data", for: .normal)
            baseurlButton.isEnabled = true
            toggleCollectButton.isEnabled = false
            privacyPolicyButton.isEnabled = false
            break
        default:
            toggleCollectButton.setTitle("collect data", for: .normal)
            baseurlField.isEnabled = false
            baseurlButton.isEnabled = false
            toggleCollectButton.isEnabled = false
            privacyPolicyButton.isEnabled = true
        }
    }
    
    @IBAction func toggleCollection() {
        if AppLogic.getAppState() == APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
            AppLogic.stopCollectingData()
            toggleCollectButton.setTitle("collect data", for: .normal)
        } else {
            AppLogic.startCollectingData()
            toggleCollectButton.setTitle("stop collection", for: .normal)
        }
    }
    
    @IBAction func setBaseurl() {
        if let baseurl = baseurlField.text {
            baseurlButton.isEnabled = false
            AppLogic.setServerCredentials(urlString: baseurl) { errorString in
                DispatchQueue.main.async {
                    if let errorString = errorString {
                        print("Got error: \(errorString)")
                        self.baseurlButton.isEnabled = true
                        return
                    }
                    
                    self.performSegue(withIdentifier: "privacyPolicySegue", sender: self)
                }
            }
        }
    }
}
