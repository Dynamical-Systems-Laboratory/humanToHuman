import UIKit
import Foundation
import CoreBluetooth
import SystemConfiguration.CaptiveNetwork

func getConnectedWifiMacAdrees() -> [String:String] {
    var informationDictionary = [String:String]()
    let informationArray: NSArray? = CNCopySupportedInterfaces()
    if let information = informationArray {
        let dict: NSDictionary? = CNCopyCurrentNetworkInfo(information[0] as! CFString)
        if let temp = dict {
            informationDictionary["SSID"] = String(temp["SSID"]as!String)
            informationDictionary["BSSID"] = String(temp["BSSID"]as!String)
            return informationDictionary
        }
    }

    return informationDictionary
}

class ViewController: UIViewController {
    
    var centralManager : CBCentralManager!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        
    }
    
    override func viewDidDisappear(_ animated: Bool) {
    }
    
    
}

extension ViewController : CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        central.scanForPeripherals(withServices: nil, options: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,
                        advertisementData: [String : Any], rssi: NSNumber) {
        let localname: String = peripheral.name ?? ""
        
        print("Discovered \(localname) with rssi \(rssi)")
    }
}
