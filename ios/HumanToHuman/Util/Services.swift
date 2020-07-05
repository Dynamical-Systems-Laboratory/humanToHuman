//
//  Service.swift
//  HumanToHuman
//
//  Created by Albert Liu on 6/16/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation

class Services {
    private static var popToServerTimer : Timer?
    private static var queuedRows : Data?
    
    static func popToServer() {
        guard popToServerTimer == nil else { return }
        popToServerTimer = Timer.scheduledTimer(withTimeInterval: 5, repeats: true, block: { _ in
            if queuedRows == nil {
                queuedRows = Server.formatConnectionData(id: Bluetooth.id, rows: Database.popRows())
                guard queuedRows != nil else { return }
            }
            
            Server.sendConnectionData(data: queuedRows!) {
                queuedRows = nil
            }
        })
    }
    
    static func popDestroy() {
        guard popToServerTimer != nil else { return }
        let _ = Database.popRows()
        queuedRows = nil
    }
    
    static func stopPopToServer() {
        guard popToServerTimer != nil else { return }
        popToServerTimer?.invalidate()
        popToServerTimer = nil
    }
}
