import CoreBluetooth
import Foundation
import UIKit

let API_URL = URL(string: "http://192.168.1.151")!

class BluetoothCell: UITableViewCell {
    @IBOutlet var name: UILabel!
    @IBOutlet var power: UILabel!
    @IBOutlet var rssi: UILabel!
}

class ViewController: UIViewController {
    @IBOutlet var table: UITableView!
    @IBOutlet var wifiLabel: UILabel!

    var beacon: Bluetooth!
    var queuedRows: [Row]?
    var rows: [(device: Device, lastSeen: Date)] = []

    override func viewDidLoad() {
        beacon = Bluetooth(delegate: self, id: 32)
        print(Database.initDatabase())
        rows = []
        Timer.scheduledTimer(withTimeInterval: 60, repeats: true, block: { _ in
            if self.queuedRows == nil {
                self.queuedRows = Database.popRows()
            }
            
            URLSession.shared.dataTask(with: API_URL) { (data, response, error) in
                if let data = data {
                    print(data)
                }
                
            }
            
            
            
            // try sending queuedRows
            
        })
        Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: { _ in
            let currentTime = Date()
            self.rows = self.rows.filter { row in
                row.lastSeen.addingTimeInterval(1.0).compare(currentTime) != .orderedAscending
            }
            self.wifiLabel.text = getWiFiAddress() ?? "Unknown"
            self.table.reloadData()
        })
    }

    override func viewDidAppear(_: Bool) {
        beacon.start()
    }

    override func viewDidDisappear(_: Bool) {}
    
    @IBAction func printData() {
        print(Database.readRows())
    }
}

extension ViewController: BTDelegate {
    func discoveredDevice(_ device: Device) {
        guard Database.writeRow(device: device) else {
            print("something went wrong with sql")
            return
        }
        let firstIndex = rows.firstIndex(where: { row in row.device.uuid == device.uuid })

        if let idx = firstIndex {
            rows[idx].device.rssi = device.rssi
            rows[idx].device.measuredPower = device.measuredPower
            rows[idx].lastSeen = Date()
        } else {
            rows.append((device: device, lastSeen: Date()))
        }
    }
}

extension ViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "BluetoothCell")! as! BluetoothCell
        let row = rows[indexPath.row]
        cell.name.text = "\(row.device.uuid)"
        cell.rssi.text = "\(row.device.rssi)"
        cell.power.text = "\(row.device.measuredPower)"
        return cell
    }

    func tableView(_: UITableView, numberOfRowsInSection _: Int) -> Int {
        return rows.count
    }
}
