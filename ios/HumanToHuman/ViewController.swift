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
    @IBOutlet var toggleRunButton: UIButton!

    var beacon: Bluetooth!
    var queuedRows: Data?
    var running: Bool = false
    var rows: [(device: Device, lastSeen: Date)] = []

    override func viewDidLoad() {
        guard Database.initDatabase() else { print("Database failed to init"); exit(1) }
        toggleRunButton.isEnabled = false
        
        if let id = Database.getPropNumeric(prop: OWN_ID_KEY) {
            print("init with saved id \(id)")
            beacon = Bluetooth(delegate: self, id: id)
            toggleRunButton.isEnabled = true
        } else {
            Server.getUserId() { id in
                guard let id = id else { exit(1) }
                print("got id \(id)")
                Database.setPropNumeric(prop: OWN_ID_KEY, value: Int64(bitPattern: id))
                self.beacon = Bluetooth(delegate: self, id: id)
                DispatchQueue.main.async {
                   self.toggleRunButton.isEnabled = true
                }
            }
        }
        
        rows = []
        Timer.scheduledTimer(withTimeInterval: 5, repeats: true, block: { _ in
            if self.beacon == nil { return }
            if self.queuedRows == nil {
                self.queuedRows = Server.formatConnectionData(id: self.beacon.id, rows: Database.popRows())
                guard self.queuedRows != nil else { return }
            }
            
            Server.sendConnectionData(data: self.queuedRows!) {
                self.queuedRows = nil
            }
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

    override func viewDidAppear(_: Bool) {}

    override func viewDidDisappear(_: Bool) {}
    
    @IBAction func clearData() {
        print(Database.popRows())
        self.queuedRows = nil
    }
    
    @IBAction func toggleRun() {
        self.running = !self.running
        if self.running {
            self.beacon.start()
            self.toggleRunButton.setTitle("stop", for: .normal)
        } else {
            self.beacon.stop()
            self.toggleRunButton.setTitle("start", for: .normal)
        }
    }
}

extension ViewController: BTDelegate {
    func discoveredDevice(_ device: Device) {
        if !self.running { return }
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
