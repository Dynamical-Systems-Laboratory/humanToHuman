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
    
    override func viewDidLoad() {
        print("privacy policy controller loading...")
        privacyPolicy.isEditable = false
        privacyPolicy.text = Database.getPropText(prop: KEY_PRIVACY_POLICY)
    }
}

