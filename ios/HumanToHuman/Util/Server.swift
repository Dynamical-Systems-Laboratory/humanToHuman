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

struct IDInformation : Codable {
    var token: String
    var id: UInt64
}

struct Server {
    // Get a user id from the server asynchronously. The callback either gets a valid user id, or nil if the request failed.
    static func getUserId(callback: @escaping (UInt64?, String) -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/addUser")!)
        request.httpMethod = "POST"

        URLSession.shared.dataTask(with: request) { data, _, error in
            guard let data = data, error == nil else {
                callback(nil, "Server error: \(error?.localizedDescription ?? "No data")")
                return
            }

            let decoder = JSONDecoder()
            guard let idInformation = try? decoder.decode(IDInformation.self, from: data) else {
                callback(nil, "Failed to parse data from API")
                return
            }

            callback(idInformation.id, idInformation.token)
        }.resume()
    }

    // Format connection data for use when sending a request. Returns nil if the list given is empty
    static func formatConnectionData(id: UInt64, token: String, rows: [Row]) -> Data? {
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
            "key": token,
            "connections": jsonRows,
        ])
    }

    // Send connection data to the server asynchronously. Calls the given callback if the request succeeded.
    static func sendConnectionData(data: Data, callback: @escaping () -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/addConnections")!)
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
    
    static func getDescription(callback: @escaping (String?) -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/description")!)
        request.httpMethod = "GET"
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                callback(nil)
                return
            }
            
            callback(String(decoding: data, as: UTF8.self))
        }.resume()
    }
    
    static func getPrivacyPolicy(callback: @escaping (String?) -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/policy")!)
        request.httpMethod = "GET"
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                callback(nil)
                return
            }
            
            callback(String(decoding: data, as: UTF8.self))
        }.resume()
    }
}
