import CoreBluetooth
import Foundation
import UIKit

class BluetoothCell: UITableViewCell {
    @IBOutlet var name: UILabel!
    @IBOutlet var power: UILabel!
    @IBOutlet var rssi: UILabel!
}

class MainController: UIViewController {
    @IBOutlet var table: UITableView!
    @IBOutlet var idLabel: UILabel!
    
    var rows: [(device: Device, lastSeen: Date)] = []

    override func viewDidLoad() {
        print("main controller loading...")
        guard Database.initDatabase() else { print("Database failed to init"); exit(1) }
        Bluetooth.delegate = self
        Services.updateTable(self)
        Services.popToServer()
        
        if let id = Database.getPropNumeric(prop: OWN_ID_KEY) {
            print("init with saved id \(id)")
            Bluetooth.id = id
        } else {
            Server.getUserId { id in
                guard let id = id else { exit(1) }
                print("got id \(id)")
                Database.setPropNumeric(prop: OWN_ID_KEY, value: Int64(bitPattern: id))
                Bluetooth.id = id
            }
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        if let id = Database.getPropNumeric(prop: OWN_ID_KEY) {
            idLabel.text = "\(id)"
        }
    }

    
}

extension MainController: BTDelegate {
    func discoveredDevice(_ device: Device) {
        guard Database.writeRow(device: device) else {
            print("something went wrong with sql")
            return
        }
        
        if let idx = rows.firstIndex(where: { row in row.device.uuid == device.uuid }) {
            rows[idx].device.rssi = device.rssi
            rows[idx].device.measuredPower = device.measuredPower
            rows[idx].lastSeen = Date()
        } else {
            rows.append((device: device, lastSeen: Date()))
        }
    }
}

extension MainController: UITableViewDataSource, UITableViewDelegate {
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
