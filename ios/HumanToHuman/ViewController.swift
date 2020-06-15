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
    @IBOutlet var toggleAdvertiseButton: UIButton!
    @IBOutlet var toggleScanButton: UIButton!
    @IBOutlet var clearDataButton: UIButton!

    var beacon: Bluetooth!
    var queuedRows: Data?
    var rows: [(device: Device, lastSeen: Date)] = []

    override func viewDidLoad() {
        guard Database.initDatabase() else { print("Database failed to init"); exit(1) }
        wifiLabel.text = getWiFiAddress() ?? "Unknown"
        toggleScanButton.isEnabled = true
        toggleAdvertiseButton.isEnabled = false
        clearDataButton.isEnabled = Database.rowCount() != 0

        if let id = Database.getPropNumeric(prop: OWN_ID_KEY) {
            print("init with saved id \(id)")
            beacon = Bluetooth(delegate: self, id: id)
            toggleAdvertiseButton.isEnabled = true
        } else {
            Server.getUserId { id in
                guard let id = id else { exit(1) }
                print("got id \(id)")
                Database.setPropNumeric(prop: OWN_ID_KEY, value: Int64(bitPattern: id))
                self.beacon = Bluetooth(delegate: self, id: id)
                DispatchQueue.main.async {
                    self.toggleAdvertiseButton.isEnabled = true
                }
            }
        }

        Timer.scheduledTimer(withTimeInterval: 5, repeats: true, block: { _ in
            if self.beacon == nil { return }
            if self.queuedRows == nil {
                self.queuedRows = Server.formatConnectionData(id: self.beacon.id, rows: Database.popRows())
                guard self.queuedRows != nil else { return }
            }
            self.clearDataButton.isEnabled = true

            Server.sendConnectionData(data: self.queuedRows!) {
                self.queuedRows = nil
            }
        })

        Timer.scheduledTimer(withTimeInterval: 0.2, repeats: true, block: { _ in
            let currentTime = Date()
            self.rows = self.rows.filter { row in
                row.lastSeen.addingTimeInterval(1.0).compare(currentTime) != .orderedAscending
            }
            self.table.reloadData()
        })
    }

    @IBAction func clearData() {
        Database.popRows()
        queuedRows = nil
        clearDataButton.isEnabled = false
    }
    
    @IBAction func toggleAdvertising() {
        if beacon.advertising {
            beacon.stopAdvertising()
            toggleAdvertiseButton.setTitle("advertise", for: .normal)
        } else {
            beacon.startAdvertising()
            toggleAdvertiseButton.setTitle("stop advertising", for: .normal)
        }
    }
    
    @IBAction func toggleScanning() {
        if beacon.scanning {
            beacon.stopScanning()
            toggleScanButton.setTitle("scan", for: .normal)
        } else {
            beacon.startScanning()
            toggleScanButton.setTitle("stop scanning", for: .normal)
        }
    }
}

extension ViewController: BTDelegate {
    func discoveredDevice(_ device: Device) {
        guard Database.writeRow(device: device) else {
            print("something went wrong with sql")
            return
        }
        
        clearDataButton.isEnabled = true
        if let idx = rows.firstIndex(where: { row in row.device.uuid == device.uuid }) {
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
