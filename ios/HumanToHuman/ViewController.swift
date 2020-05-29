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
    
    var beacon: AltBeacon!
    var manager: CBCentralManager!
    var rows: [(name: String, rssi: Float, lastSeen: Date)] = []

    override func viewDidLoad() {
        manager = CBCentralManager(delegate: self, queue: nil)
        beacon = AltBeacon(identifier: UIDevice.current.name)
        beacon.add(self)
        rows = []
        Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: { (timer) in
            let currentTime = Date()
            self.rows = self.rows.filter({ row in
                row.lastSeen.addingTimeInterval(1.0).compare(currentTime) != .orderedAscending
            })
            self.wifiLabel.text = getWiFiAddress() ?? "Unknown"
            self.table.reloadData()
        })
    }

    override func viewDidAppear(_: Bool) {
        beacon.startBroadcasting()
    }

    override func viewDidDisappear(_: Bool) {
        manager.stopScan()
    }
}

extension ViewController: AltBeaconDelegate {
    func service(_ service: AltBeacon!, foundDevices devices: NSMutableDictionary!) {}
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
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            print("bluetooth on")
            central.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: true])
        }
    }

    func centralManager(_: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi: NSNumber) {
        if peripheral.name == nil { return }
        print(advertisementData)
        let firstIndex = rows.firstIndex(where: { per in per.name == peripheral.name! })

        if let idx = firstIndex {
            rows[idx].rssi = rssi.floatValue
            rows[idx].lastSeen = Date()
        } else {
            rows.append((name: peripheral.name!, rssi: rssi.floatValue, lastSeen: Date()))
        }

        table.reloadData()
    }
}
