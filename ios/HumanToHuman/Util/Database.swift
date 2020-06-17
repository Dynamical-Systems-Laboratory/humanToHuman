// SQLite Database utilities.

import Foundation

// Property IDs. These are used in the database metadata table to store data.
let OWN_ID_KEY = 0
let CURRENT_CURSOR = 1
let ADVERTISING = 2
let SCANNING = 3

// Shared database
let shared: FMDatabase = {
    let fileURL = try! FileManager.default
        .url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        .appendingPathComponent("database.sqlite")
    let database = FMDatabase(url: fileURL)
    return database
}()

// A row in the sensor_data table.
struct Row {
    let id: UInt64
    let time: Date
    let source: UInt64
    let power: Int
    let rssi: Double
}

struct Database {
    // Initializes the local database.
    static func initDatabase() -> Bool {
        guard shared.open() else {
            print("failed to open database")
            return false
        }

        guard shared.executeStatements(
            """
            CREATE TABLE IF NOT EXISTS metadata (
                key_        INTEGER         PRIMARY KEY,
                tvalue      TEXT            NOT NULL DEFAULT '',
                nvalue      INTEGER         NOT NULL DEFAULT 0
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
    
    static func clearProp(prop: Int) {
        try? shared.executeUpdate("DELETE FROM metadata where key_ = ?", values: [prop])
    }

    // Gets a numeric property from the metadata table.
    static func getPropNumeric(prop: Int) -> UInt64? {
        let rs = shared.executeQuery("SELECT nvalue from metadata WHERE key_ = ?",
                                     withArgumentsIn: [prop])
        if let rs = rs, rs.next() {
            return UInt64(bitPattern: rs.longLongInt(forColumn: "nvalue"))
        } else {
            return nil
        }
    }

    // Sets a numeric property in the metadata table.
    static func setPropNumeric(prop: Int, value: Int64) {
        do {
            try shared.executeUpdate("INSERT INTO metadata (key_, nvalue) VALUES (?, ?)",
                                     values: [prop, value])
        } catch {
            try? shared.executeUpdate("UPDATE metadata SET nvalue = ? WHERE key_ = ?",
                                      values: [value, prop])
        }
    }

    // Pops all rows from the sensor_data table, reading then deleting them.
    static func popRows() -> [Row] {
        do {
            var rs = try shared.executeQuery("SELECT MAX(id) as max_id FROM sensor_data LIMIT 1", values: nil)
            rs.next()
            let rowMax = rs.longLongInt(forColumn: "max_id")
            rs.close()

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

    // Write a row to the database.
    static func writeRow(device: Device) -> Bool {
        return shared.executeUpdate(
            """
            insert into sensor_data (time, source, power, rssi)
            values (strftime('%s','now') || substr(strftime('%f','now'),4), ?, ?, ?)
            """,
            withArgumentsIn: [device.uuid, device.measuredPower, device.rssi]
        )
    }

    // Read all rows from the database.
    static func rowCount() -> Int {
        do {
            // Get a resultset from the database cooresponding to all rows in the sensor_data table
            let rs = try shared.executeQuery("SELECT COUNT(id) AS row_count FROM sensor_data", values: nil)
            guard rs.next() else {
                return 0
            }

            let rowCount = rs.long(forColumn: "row_count")
            rs.close()
            return rowCount
        } catch {
            print(shared.lastErrorMessage())
            return 0
        }
    }
}
