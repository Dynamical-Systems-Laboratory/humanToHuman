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
    @IBOutlet var privacyPolicyButton: UIButton!
    @IBOutlet var leaveExperimentButton: UIButton!

    override func viewDidLoad() {
        print("settings controller loading...")

        baseurlField.text = "https://dslserver05.poly.edu/experiment/albert-debug"
    }

    override func viewDidAppear(_: Bool) {
        print("settings controller appearing...")

        switch AppLogic.getAppState() {
        case APPSTATE_EXPERIMENT_RUNNING_COLLECTING:
            toggleCollectButton.setTitle("Stop collection", for: .normal)
            baseurlButton.isEnabled = false
            baseurlField.isEnabled = false
            toggleCollectButton.isEnabled = true
            privacyPolicyButton.isEnabled = true
            leaveExperimentButton.isEnabled = true
        case APPSTATE_EXPERIMENT_RUNNING_NOT_COLLECTING:
            toggleCollectButton.setTitle("Collect data", for: .normal)
            baseurlField.isEnabled = false
            baseurlButton.isEnabled = false
            toggleCollectButton.isEnabled = true
            privacyPolicyButton.isEnabled = true
            leaveExperimentButton.isEnabled = true
        case APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING:
            toggleCollectButton.setTitle("Collect data", for: .normal)
            baseurlButton.isEnabled = false
            baseurlField.isEnabled = false
            toggleCollectButton.isEnabled = false
            privacyPolicyButton.isEnabled = true
            leaveExperimentButton.isEnabled = true
        case APPSTATE_LOGGING_IN:
            toggleCollectButton.setTitle("Collect data", for: .normal)
            baseurlButton.isEnabled = false
            baseurlField.isEnabled = false
            toggleCollectButton.isEnabled = false
            privacyPolicyButton.isEnabled = false
            leaveExperimentButton.isEnabled = false
        case APPSTATE_NO_EXPERIMENT:
            toggleCollectButton.setTitle("Collect data", for: .normal)
            baseurlButton.isEnabled = true
            toggleCollectButton.isEnabled = false
            baseurlField.isEnabled = true
            privacyPolicyButton.isEnabled = false
            leaveExperimentButton.isEnabled = false
        default:
            print("got unexpected state")
            exit(1)
        }
    }

    @IBAction func backToMain() {
        dismiss(animated: true, completion: nil)
    }

    @IBAction func goToPrivacyPolicy() {
        let viewController = storyboard?.instantiateViewController(withIdentifier: "PrivacyPolicyController") as! PrivacyPolicyController
        viewController.modalPresentationStyle = .fullScreen
        present(viewController, animated: true, completion: nil)
    }

    @IBAction func toggleCollection() {
        if AppLogic.getAppState() == APPSTATE_EXPERIMENT_RUNNING_COLLECTING {
            AppLogic.stopCollectingData()
            toggleCollectButton.setTitle("Collect data", for: .normal)
        } else {
            guard AppLogic.startCollectingData() else {
//                let refreshAlert = UIAlertController(title: "Bluetooth not on",
//                                                     message: "This app requires bluetooth to work properly.",
//                                                     preferredStyle: UIAlertController.Style.alert)
//
//                refreshAlert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
//
//                present(refreshAlert, animated: true, completion: nil)
                return
            }
            toggleCollectButton.setTitle("Stop collection", for: .normal)
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

                    self.goToPrivacyPolicy()
                }
            }
        }
    }

    @IBAction func leaveExperiment() {
        AppLogic.leaveExperiment()
        viewDidAppear(false)
    }
}
