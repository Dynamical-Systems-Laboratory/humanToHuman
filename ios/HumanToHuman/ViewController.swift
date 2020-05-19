import CoreBluetooth
import Foundation
import SystemConfiguration.CaptiveNetwork
import UIKit

// Return IP address of WiFi interface (en0) as a String, or `nil`
func getWiFiAddress() -> String? {
    var address : String?

    // Get list of all interfaces on the local machine:
    var ifaddr : UnsafeMutablePointer<ifaddrs>?
    guard getifaddrs(&ifaddr) == 0 else { return nil }
    guard let firstAddr = ifaddr else { return nil }

    // For each interface ...
    for ifptr in sequence(first: firstAddr, next: { $0.pointee.ifa_next }) {
        let interface = ifptr.pointee

        // Check for IPv4 or IPv6 interface:
        let addrFamily = interface.ifa_addr.pointee.sa_family
        if addrFamily == UInt8(AF_INET) || addrFamily == UInt8(AF_INET6) {

            // Check interface name:
            let name = String(cString: interface.ifa_name)
            if  name == "en0" {

                // Convert interface address to a human readable string:
                var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                getnameinfo(interface.ifa_addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                            &hostname, socklen_t(hostname.count),
                            nil, socklen_t(0), NI_NUMERICHOST)
                address = String(cString: hostname)
            }
        }
    }
    freeifaddrs(ifaddr)

    return address
}

class BluetoothCell: UITableViewCell {
    @IBOutlet var name: UILabel!
    @IBOutlet var rssi: UILabel!
}

class ViewController: UIViewController {
    @IBOutlet var table: UITableView!
    @IBOutlet var wifiLabel: UILabel!
    
    var manager: CBCentralManager!
    var rows: [(name: String, mac: String?, rssi: Float)]!

    override func viewDidLoad() {
        super.viewDidLoad()
        manager = CBCentralManager(delegate: self, queue: nil)
        rows = []
        Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: { (timer) in
            self.wifiLabel.text = getWiFiAddress() ?? "Unknown"
        })
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
        let row = rows[indexPath.row]
        cell.name.text = "\(row.name)"
        cell.rssi.text = "\(row.rssi)"
        return cell
    }

    func tableView(_: UITableView, numberOfRowsInSection _: Int) -> Int {
        return rows.count
    }
}

extension ViewController: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_: CBCentralManager) {}

    func centralManager(_: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi: NSNumber) {
        var firstIndex: Int?
        let uidOptional = advertisementData["kCBAdvDataManufacturerData"].map { _ in "" }
        let peripheralName = peripheral.name ?? "UNNAMED"
        if let uid = uidOptional {
            firstIndex = rows.firstIndex(where: { per in
                if let perUid = per.mac {
                    return perUid == uid
                } else {
                    return false
                }
            })
        } else if let name = peripheral.name {
            firstIndex = rows.firstIndex(where: { per in per.name == name })
        } else { // We don't track of unidentifiable objects
            return
        }

        if let idx = firstIndex {
            rows[idx].name = peripheralName
            rows[idx].mac = uidOptional
            rows[idx].rssi = rssi.floatValue
        } else {
            rows.append((name: peripheralName, mac: uidOptional, rssi: rssi.floatValue))
        }

        table.reloadData()
//        print("Discovered \(peripheralName) with rssi \(rssi)")
    }
}
