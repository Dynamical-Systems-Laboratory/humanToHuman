//
//  Server.swift
//  HumanToHuman
//
//  Created by Albert Liu on 6/1/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation

// Date formatter
let formatter = { () -> DateFormatter in
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
    formatter.timeZone = TimeZone(secondsFromGMT: 0)
    formatter.locale = Locale(identifier: "en_US_POSIX")
    return formatter
}()

struct Server {
    // Get a user id from the server asynchronously. The callback either gets a valid user id, or nil if the request failed.
    static func getUserId(baseurl: String, callback: @escaping (UInt64?) -> Void) {
        var request = URLRequest(url: URL(string: "\(baseurl)/addUser")!)
        request.httpMethod = "POST"

        URLSession.shared.dataTask(with: request) { data, _, error in
            guard let data = data, error == nil else {
                print("Server error: \(error?.localizedDescription ?? "No data")")
                callback(nil)
                return
            }

            guard let value = String(data: data, encoding: .utf8) else {
                print("Failed to decode data as utf8")
                callback(nil)
                return
            }

            guard let uintValue = UInt64(value.trimmingCharacters(in: .whitespacesAndNewlines))
            else {
                print("Failed to parse data from API")
                callback(nil)
                return
            }

            callback(uintValue)
        }.resume()
    }

    // Format connection data for use when sending a request. Returns nil if the list given is empty
    static func formatConnectionData(id: UInt64, rows: [Row]) -> Data? {
        if rows.count == 0 {
            return nil
        }

        let jsonRows = rows.map { row in
            [
                "time": formatter.string(from: row.time),
                "other": row.source,
                "power": row.power,
                "rssi": row.rssi,
            ]
        }

        return try! JSONSerialization.data(withJSONObject: [
            "id": id,
            "connections": jsonRows,
        ])
    }

    // Send connection data to the server asynchronously. Calls the given callback if the request succeeded.
    static func sendConnectionData(baseurl: String, data: Data, callback: @escaping () -> Void) {
        var request = URLRequest(url: URL(string: "\(baseurl)/addConnections")!)
        request.httpMethod = "POST"
        request.httpBody = data

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                return
            }

            let responseJSON = try? JSONSerialization.jsonObject(with: data, options: [])
            if let responseJSON = responseJSON as? [String: Any] {
                print("LOG `Util/Server.swift:\(#line)` got response: \(responseJSON)")
            }
            if let response = response as? HTTPURLResponse, response.statusCode == 200 {
                callback()
            }
        }.resume()
    }
}
