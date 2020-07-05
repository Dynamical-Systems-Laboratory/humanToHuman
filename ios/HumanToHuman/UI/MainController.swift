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
            idLabel.text = "ID: No id yet"
            break;
        case APPSTATE_LOGGING_IN:
            idLabel.text = "ID: No id yet"
            break;
        case APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING:
            idLabel.text = "ID: No id yet"
            break;
        default:
            idLabel.text = "ID: \(AppLogic.getBluetoothId())"
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        experimentDescription.text = AppLogic.getDescription()
        switch AppLogic.getAppState() {
        case APPSTATE_NO_EXPERIMENT:
            idLabel.text = "ID: No id yet"
            break;
        case APPSTATE_LOGGING_IN:
            idLabel.text = "ID: No id yet"
            break;
        case APPSTATE_EXPERIMENT_JOINED_NOT_ACCEPTED_NOT_RUNNING:
            idLabel.text = "ID: No id yet"
            break;
        default:
            idLabel.text = "ID: \(AppLogic.getBluetoothId())"
        }
    }
    
    @IBAction func goToSettings() {
        let viewController = self.storyboard?.instantiateViewController(withIdentifier: "SettingsController") as! SettingsController
        viewController.modalPresentationStyle = .fullScreen
        present(viewController, animated: true, completion: nil)
    }
}

