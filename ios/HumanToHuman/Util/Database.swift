import Foundation

let shared: FMDatabase = {
    let fileURL = try! FileManager.default
        .url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        .appendingPathComponent("database.sqlite")
    let database = FMDatabase(url: fileURL)
    return database
}()

func initDatabase() -> Bool {
    guard shared.open() else {
        print("failed to open database")
        return false
    }
    guard shared.executeStatements(
        """
        CREATE TABLE IF NOT EXISTS metadata (
            key         INTEGER         PRIMARY KEY,
            value       TEXT            NOT NULL
        );

        CREATE TABLE IF NOT EXISTS experiment_member_ids (
            key         INTEGER         PRIMARY KEY,
        );

        CREATE TABLE IF NOT EXISTS sensor_data (
            id          INTEGER         PRIMARY KEY AUTOINCREMENT,
            time        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
            source      INTEGER         NOT NULL,
            power       INTEGER         NOT NULL,
            rssi        REAL            NOT NULL
        );
        """) else {
        print(shared.lastErrorMessage())
        return false
    }
    return true
}

func closeDatabase() {
    shared.close()
}

func writeRow(device: Device) -> Bool {
    return shared.executeUpdate(
        "INSERT INTO sensor_data (source, power, rssi) VALUES (?, ?, ?);",
        withArgumentsIn: [device.uuid, device.measuredPower, device.rssi]
    )
}

func readRows() -> [(device: Device, time: Date)] {
    do {
        let rs = try shared.executeQuery("SELECT time, source, power, rssi FROM sensor_data", values: nil)
        var list: [(device: Device, time: Date)] = []
        while rs.next() {
            let device = Device(
                uuid: UInt64(bitPattern: rs.longLongInt(forColumn: "source")),
                rssi: Float(rs.double(forColumn: "power")),
                measuredPower: rs.long(forColumn: "power")
            )
            let time = rs.date(forColumn: "time")!
            list.append((device: device, time: time))
        }
        return list
    } catch {
        print(shared.lastErrorMessage())
        return []
    }
}
