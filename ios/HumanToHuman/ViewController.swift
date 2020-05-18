import CoreBluetooth
import Foundation
import SystemConfiguration.CaptiveNetwork
import UIKit

func getConnectedWifiMacAdrees() -> [String: String] {
    var informationDictionary = [String: String]()
    let informationArray: NSArray? = CNCopySupportedInterfaces()
    if let information = informationArray {
        let dict: NSDictionary? = CNCopyCurrentNetworkInfo(information[0] as! CFString)
        if let temp = dict {
            informationDictionary["SSID"] = String(temp["SSID"] as! String)
            informationDictionary["BSSID"] = String(temp["BSSID"] as! String)
            return informationDictionary
        }
    }

    return informationDictionary
}

class BluetoothCell: UITableViewCell {
    @IBOutlet weak var name : UILabel!
    @IBOutlet weak var rssi : UILabel!
    
    
}

class ViewController: UIViewController {
    var manager: CBCentralManager!
    @IBOutlet var table: UITableView!
    var rows : [(name: String, mac: String?, rssi: Float)]!

    override func viewDidLoad() {
        super.viewDidLoad()
        manager = CBCentralManager(delegate: self, queue: nil)
        rows = []
    }

    override func viewDidAppear(_: Bool) {
        manager.scanForPeripherals(withServices: nil, options: nil)
    }

    override func viewDidDisappear(_: Bool) {
        manager.stopScan()
    }
}

extension ViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "BluetoothCell")! as! BluetoothCell
        let row = self.rows[indexPath.row]
        cell.name.text = "\(row.name)"
        cell.rssi.text = "\(row.rssi)"
        return cell
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.rows.count
    }
}

extension ViewController: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_: CBCentralManager) {}

    func centralManager(_ manager: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi: NSNumber) {
        
        var firstIndex : Int?
        let uidOptional = advertisementData["kCBAdvDataManufacturerData"].map({ (value) in "" })
        let peripheralName = peripheral.name ?? "UNNAMED"
        if let uid = uidOptional {
            firstIndex = self.rows.firstIndex(where: { (per) in
                if let perUid = per.mac {
                    return perUid == uid
                } else {
                    return false
                }
            })
        } else if let name = peripheral.name {
            firstIndex = self.rows.firstIndex(where: { (per) in per.name == name })
        } else { // We don't track of unidentifiable objects
            return
        }
        
        if let idx = firstIndex {
            self.rows[idx].name = peripheralName
            self.rows[idx].mac = uidOptional
            self.rows[idx].rssi = rssi.floatValue
        } else {
            self.rows.append((name: peripheralName, mac: uidOptional, rssi: rssi.floatValue))
        }

        self.table.reloadData()
        print("Discovered \(peripheralName) with rssi \(rssi)")
    }
}
