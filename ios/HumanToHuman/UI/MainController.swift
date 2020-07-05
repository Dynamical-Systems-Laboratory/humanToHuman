import CoreBluetooth
import Foundation
import UIKit

class MainController: UIViewController {
    @IBOutlet var idLabel: UILabel!
    @IBOutlet var experimentDescription: UITextView!

    override func viewDidLoad() {
        print("main controller loading...")
        experimentDescription.isEditable = false
        experimentDescription.text = AppLogic.getDescription()
        
        switch AppLogic.getAppState() {
        case APPSTATE_NO_EXPERIMENT:
            idLabel.text = "No id yet"
            break;
        case APPSTATE_LOGGING_IN:
            idLabel.text = "No id yet"
            break;
        default:
            idLabel.text = "\(AppLogic.getBluetoothId())"
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        switch AppLogic.getAppState() {
        case APPSTATE_NO_EXPERIMENT:
            idLabel.text = "No id yet"
            break;
        case APPSTATE_LOGGING_IN:
            idLabel.text = "No id yet"
            break;
        default:
            idLabel.text = "\(AppLogic.getBluetoothId())"
        }
    }
}

