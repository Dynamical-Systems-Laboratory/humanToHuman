import CoreBluetooth
import Foundation
import UIKit

let API_USER_URL = URL(string: "http://192.168.1.151:8080/addUser")!
let API_CONNECTIONS_URL = URL(string: "http://192.168.1.151:8080/addConnections")!
let formatter = { () -> DateFormatter in
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
    formatter.timeZone = TimeZone(secondsFromGMT: 0)
    formatter.locale = Locale(identifier: "en_US_POSIX")
    return formatter
}()

class BluetoothCell: UITableViewCell {
    @IBOutlet var name: UILabel!
    @IBOutlet var power: UILabel!
    @IBOutlet var rssi: UILabel!
}

class ViewController: UIViewController {
    @IBOutlet var table: UITableView!
    @IBOutlet var wifiLabel: UILabel!

    var beacon: Bluetooth!
    var queuedRows: Data?
    var rows: [(device: Device, lastSeen: Date)] = []

    override func viewDidLoad() {
        beacon = Bluetooth(delegate: self, id: 32)
        print(Database.initDatabase())
        rows = []
        Timer.scheduledTimer(withTimeInterval: 5, repeats: true, block: { _ in
            print("trying to send data")
            if self.queuedRows == nil {
                let rows = Database.popRows().map() { row in
                    [
                        "time": formatter.string(from: row.time),
                        "other": row.source,
                        "power": row.power,
                        "rssi": row.rssi
                    ]
                }
                if rows.count == 0 {
                    print("nothing to send")
                    return
                }
                self.queuedRows = try? JSONSerialization.data(withJSONObject: [
                    "id": self.beacon.id,
                    "connections":rows
                ])
            }
            var request = URLRequest(url: API_CONNECTIONS_URL)
            request.httpMethod = "POST"
            request.httpBody = self.queuedRows

            let task = URLSession.shared.dataTask(with: request) { data, response, error in
                guard let data = data, error == nil else {
                    print(error?.localizedDescription ?? "No data")
                    return
                }
                
                self.queuedRows = nil
                
                let responseJSON = try? JSONSerialization.jsonObject(with: data, options: [])
                if let responseJSON = responseJSON as? [String: Any] {
                    print(responseJSON)
                }
            }
            task.resume()
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
