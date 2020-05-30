import CoreBluetooth
import Foundation
import UIKit

class BluetoothCell: UITableViewCell {
    @IBOutlet var name: UILabel!
    @IBOutlet var power: UILabel!
    @IBOutlet var rssi: UILabel!
}

class ViewController: UIViewController {
    @IBOutlet var table: UITableView!
    @IBOutlet var wifiLabel: UILabel!

    var beacon: Bluetooth!
    var rows: [(device: Device, lastSeen: Date)] = []

    override func viewDidLoad() {
        beacon = Bluetooth(delegate: self, id: 32)
        rows = []
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
}

extension ViewController: BTDelegate {
    func discoveredDevice(_ device: Device) {
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
