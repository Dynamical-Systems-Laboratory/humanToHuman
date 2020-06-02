//
//  Server.swift
//  HumanToHuman
//
//  Created by Albert Liu on 6/1/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation

let API_USER_URL = URL(string: "http://192.168.1.151:8080/addUser")!
let API_CONNECTIONS_URL = URL(string: "http://192.168.1.151:8080/addConnections")!
let formatter = { () -> DateFormatter in
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
    formatter.timeZone = TimeZone(secondsFromGMT: 0)
    formatter.locale = Locale(identifier: "en_US_POSIX")
    return formatter
}()

struct Server {
    
    static func getUserId(callback: @escaping (UInt64?) -> Void) {
        var request = URLRequest(url: API_USER_URL)
        request.httpMethod = "POST"

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                callback(nil)
                return
            }
            
            guard let value = String(data: data, encoding: .utf8) else {
                print("Failed to decode data as utf8")
                callback(nil)
                return
            }
            guard let uintValue = UInt64(value.trimmingCharacters(in: .whitespacesAndNewlines)) else {
                print(value)
                print("Failed to parse data from API")
                callback(nil)
                return
            }
            callback(uintValue)
        }.resume()
    }
    
    static func formatConnectionData(id: UInt64, rows: [Row]) -> Data? {
        if rows.count == 0 {
            return nil
        }
        
        let jsonRows = rows.map() { row in
            [
                "time": formatter.string(from: row.time),
                "other": row.source,
                "power": row.power,
                "rssi": row.rssi
            ]
        }
        
        return try? JSONSerialization.data(withJSONObject: [
            "id": id,
            "connections": jsonRows
        ])
    }
    
    static func sendConnectionData(data: Data, callback: @escaping () -> Void) {
        var request = URLRequest(url: API_CONNECTIONS_URL)
        request.httpMethod = "POST"
        request.httpBody = data

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                return
            }
                        
            let responseJSON = try? JSONSerialization.jsonObject(with: data, options: [])
            if let responseJSON = responseJSON as? [String: Any] {
                print(responseJSON)
            }
            
            callback()
        }.resume()
    }
    
}


