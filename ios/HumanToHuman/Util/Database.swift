import Foundation

let OWN_ID_KEY = 0
let CURRENT_CURSOR = 1
let shared: FMDatabase = {
    let fileURL = try! FileManager.default
        .url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        .appendingPathComponent("database.sqlite")
    let database = FMDatabase(url: fileURL)
    return database
}()

struct Row {
    let id: UInt64
    let time: Date
    let source: UInt64
    let power: Int
    let rssi: Double
}

struct Database {
    static func initDatabase() -> Bool {
        guard shared.open() else {
            print("failed to open database")
            return false
        }
        
        guard shared.executeStatements(
            """
            CREATE TABLE IF NOT EXISTS metadata (
                key         INTEGER         PRIMARY KEY,
                tvalue      TEXT            NOT NULL DEFAULT '',
                nvalue      INTEGER         NOT NULL DEFAULT 0
            );

            CREATE TABLE IF NOT EXISTS experiment_member_ids (
                key         INTEGER         PRIMARY KEY
            );

            CREATE TABLE IF NOT EXISTS sensor_data (
                id          INTEGER         PRIMARY KEY AUTOINCREMENT,
                time        INTEGER         NOT NULL,
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

    static func startExperiment(id: UInt64, otherIds: [UInt64]) {
        shared.executeUpdate("INSERT INTO metadata (key, nvalue) VALUES (?, ?)", withArgumentsIn: [OWN_ID_KEY, id])
        shared.executeUpdate("INSERT INTO metadata (key, nvalue) VALUES (?, ?)", withArgumentsIn: [CURRENT_CURSOR, 0])

        for otherId in otherIds {
            shared.executeUpdate("INSERT INTO experiment_member_ids (key) VALUES (?)", withArgumentsIn: [otherId])
        }
    }

    static func popRows() -> [Row] {
        do {
            var rs = try shared.executeQuery("SELECT MAX(id) as max_id FROM sensor_data", values: nil)
            rs.next()
            let rowMax = rs.longLongInt(forColumn: "max_id")
            rs.close()
            
            print(rowMax)

            rs = try shared.executeQuery("SELECT * FROM sensor_data WHERE id <= ?", values: [rowMax])
            var list: [Row] = []
            while rs.next() {
                let timeNumber = Double(rs.longLongInt(forColumn: "time")) / 1000.0
                let row = Row(
                    id: UInt64(bitPattern: rs.longLongInt(forColumn: "id")),
                    time: Date(timeIntervalSince1970: timeNumber),
                    source: UInt64(bitPattern: rs.longLongInt(forColumn: "source")),
                    power: rs.long(forColumn: "power"),
                    rssi: rs.double(forColumn: "rssi")
                )
                list.append(row)
            }
            rs.close()
            shared.executeUpdate("DELETE FROM sensor_data WHERE id <= ?", withArgumentsIn: [rowMax])
            return list
        } catch {
            print(shared.lastErrorMessage())
            return []
        }
    }

    static func closeDatabase() {
        shared.close()
    }

    static func writeRow(device: Device) -> Bool {
        return shared.executeUpdate(
            """
            INSERT INTO sensor_data (time, source, power, rssi)
            VALUES (strftime('%s','now') || substr(strftime('%f','now'),4), ?, ?, ?)
            """,
            withArgumentsIn: [device.uuid, device.measuredPower, device.rssi]
        )
    }

    static func readRows() -> [Row] {
        do {
            let rs = try shared.executeQuery("SELECT * FROM sensor_data", values: nil)
            var list: [Row] = []
            while rs.next() {
                let timeNumber = Double(rs.longLongInt(forColumn: "time")) / 1000.0
                let row = Row(
                    id: UInt64(bitPattern: rs.longLongInt(forColumn: "id")),
                    time: Date(timeIntervalSince1970: timeNumber),
                    source: UInt64(bitPattern: rs.longLongInt(forColumn: "source")),
                    power: rs.long(forColumn: "power"),
                    rssi: rs.double(forColumn: "rssi")
                )
                list.append(row)
            }
            rs.close()
            return list
        } catch {
            print(shared.lastErrorMessage())
            return []
        }
    }
}
