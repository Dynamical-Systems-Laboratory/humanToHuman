import CoreBluetooth
import Foundation
import UIKit

class MainController: UIViewController {
    @IBOutlet var idLabel: UILabel!
    @IBOutlet var experimentDescription: UITextView!

    override func viewDidLoad() {
        print("main controller loading...")
        experimentDescription.isEditable = false
        experimentDescription.text = Database.getPropText(prop: KEY_EXPERIMENT_DESCRIPTION)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        if let id = Database.getPropNumeric(prop: KEY_OWN_ID) {
            idLabel.text = "\(id)"
        }
    }
}

