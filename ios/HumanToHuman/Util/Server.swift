//
//  Server.swift
//  HumanToHuman
//
//  Created by Albert Liu on 6/1/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation

// Date formatter


struct IDInformation : Codable {
    var token: String
    var id: UInt64
}

struct EMessage : Error {
    var message: String
}

struct Server {
    // Get a user id from the server asynchronously. The callback either gets a valid user id, or nil if the request failed.
    static func getUserId(callback: @escaping (UInt64?, String) -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/addUser")!)
        request.httpMethod = "POST"

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                callback(nil, "Server error: \(error?.localizedDescription ?? "No data")")
                return
            }
            
            guard let resp = response as? HTTPURLResponse, resp.statusCode == 200 else {
                callback(nil, "Server error: \(response)")
                return
            }

            let decoder = JSONDecoder()
            guard let idInformation = try? decoder.decode(IDInformation.self, from: data) else {
                callback(nil, "Failed to parse data from API: '\(String(data: data, encoding: .utf8) ?? "")'")
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
        
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
        formatter.timeZone = TimeZone.current
        formatter.locale = Locale(identifier: "en_US_POSIX")
        
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
    static func sendConnectionData(data: Data, callback: @escaping (Error?) -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/addConnections")!)
        request.httpMethod = "POST"
        request.httpBody = data

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                callback(error)
                return
            }

            var responseJSON : Any? = nil
            do {
                responseJSON = try JSONSerialization.jsonObject(with: data, options: [])
            } catch {
                callback(error)
                return
            }
        
            if let responseJSON = responseJSON as? [String: Any] {
                print("LOG `Util/Server.swift:\(#line)` got response: \(responseJSON)")
            }
            if let response = response as? HTTPURLResponse, response.statusCode == 200 {
                callback(nil)
            } else {
                callback(EMessage(message: "hello"))
            }
        }.resume()
    }
    
    static func removeUser(callback: @escaping (String?) -> Void) {
        var request = URLRequest(url: URL(string: "\(AppLogic.getServerURL())/removeUser")!)
        request.httpMethod = "POST"
        request.httpBody = "token=\(AppLogic.getToken())".data(using: .utf8)

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                callback(error?.localizedDescription ?? "No data")
                return
            }

            let responseJSON = try? JSONSerialization.jsonObject(with: data, options: [])
            if let responseJSON = responseJSON as? [String: Any] {
                print("LOG `Util/Server.swift:\(#line)` got response: \(responseJSON)")
            }
            if let response = response as? HTTPURLResponse, response.statusCode == 200 {
                callback(nil)
            } else {
                callback("got non-success response code \((response as? HTTPURLResponse)?.statusCode ?? -1)")
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
            
            if let response = response as? HTTPURLResponse, response.statusCode == 200 {
                callback(String(decoding: data, as: UTF8.self))
            } else {
                callback(nil)
            }
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
            
            if let response = response as? HTTPURLResponse, response.statusCode == 200 {
                callback(String(decoding: data, as: UTF8.self))
            } else {
                callback(nil)
            }
        }.resume()
    }
}
