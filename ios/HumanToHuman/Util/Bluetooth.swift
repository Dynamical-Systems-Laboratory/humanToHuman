//
//  Bluetooth.swift
//  HumanToHuman
//
//  Created by Albert Liu on 5/29/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import Foundation
import CoreBluetooth

struct Device {
    let name: String
    var rssi: Float
    var measuredPower: Int?
}

protocol BTDelegate {
    func discoveredDevice(_: Device)
}

class Bluetooth: NSObject {
    var delegate: BTDelegate
    var beacon: AltBeacon!
    var central: CBCentralManager!
    var running: Bool = false
    
    init(delegate: BTDelegate, ident: String) {
        self.delegate = delegate
        self.beacon = AltBeacon(identifier:  ident)
    }
    
    private func scan() {
        self.central.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: true])
    }

    func start() {
        if self.running { return }
        
        if self.central == nil {
            self.central = CBCentralManager(delegate: self, queue: nil)
        }
        
        self.running = true
        if self.central.state == .poweredOn { self.scan() }
        self.beacon.startBroadcasting()
    }
    
    func stop() {
        if !self.running { return }
        self.running = false
        self.central.stopScan()
        self.beacon.stopBroadcasting()
    }
}

extension Bluetooth: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn && self.running { self.scan() }
    }

    func centralManager(_: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi: NSNumber) {
        if peripheral.name == nil { return }
        self.delegate.discoveredDevice(Device(
            name: peripheral.name!,
            rssi: rssi.floatValue,
            measuredPower: advertisementData[CBAdvertisementDataTxPowerLevelKey] as? Int
        ))
    }
}

