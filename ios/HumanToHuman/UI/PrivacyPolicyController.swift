//
//  PrivacyPolicyController.swift
//  HumanToHuman
//
//  Created by Albert Liu on 6/22/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation
import UIKit

class PrivacyPolicyController: UIViewController {

    @IBOutlet var privacyPolicy: UITextView!
    @IBOutlet var privacyPolicySwitch: UISwitch!
    
    override func viewDidLoad() {
        print("privacy policy controller loading...")
        privacyPolicy.isEditable = false
        privacyPolicy.text = AppLogic.getPolicy()
        print("Appstate is: \(AppLogic.getAppState())")
        switch AppLogic.getAppState() {
        case APPSTATE_NO_EXPERIMENT:
            print("why did this happen?")
            exit(1)
            break
        case APPSTATE_LOGGING_IN:
            print("why did this happen?")
            exit(1)
            break
        case APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING:
            privacyPolicySwitch.isEnabled = true
            privacyPolicySwitch.isOn = false
            break
        default:
            privacyPolicySwitch.isEnabled = false
            privacyPolicySwitch.isOn = true
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        if AppLogic.getAppState() == APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING {
            
        }
    }
    
    @IBAction func acceptPrivacyPolicy() {
        AppLogic.acceptPrivacyPolicy() { errorString in
            DispatchQueue.main.async {
                if let errorString = errorString {
                    print(errorString)
                    self.privacyPolicySwitch.isOn = false
                    self.dismiss(animated: true, completion: nil)
                    return
                }
                
                self.privacyPolicySwitch.isEnabled = false
                self.privacyPolicySwitch.isOn = true
                self.dismiss(animated: true, completion: nil)
            }
        }
    }
}

