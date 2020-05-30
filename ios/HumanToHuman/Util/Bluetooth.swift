//
//  Bluetooth.swift
//  HumanToHuman
//
//  Created by Albert Liu on 5/29/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import CoreBluetooth
import Foundation
import UIKit

struct Device {
    let uuid: UInt64
    var rssi: Float
    var measuredPower: Int
}

protocol BTDelegate {
    func discoveredDevice(_: Device)
}

class Bluetooth: NSObject {
    var delegate: BTDelegate
    let id: UInt64
    var peripheral: CBPeripheralManager!
    var central: CBCentralManager!
    var running: Bool = false

    init(delegate: BTDelegate, id: UInt64) {
        self.delegate = delegate
        self.id = id
    }

    private func scan() {
        central.scanForPeripherals(
            withServices: OverflowAreaUtils.allOverflowServiceUuids(),
            options: [CBCentralManagerScanOptionAllowDuplicatesKey: true]
        )
    }

    private func advertise() {
        peripheral.startAdvertising([
            CBAdvertisementDataServiceUUIDsKey: uint64ToOverflowServiceUuids(uint64: id),
        ])
    }

    func start() {
        if running { return }

        if central == nil {
            central = CBCentralManager(delegate: self, queue: nil)
        }

        if peripheral == nil {
            peripheral = CBPeripheralManager(delegate: self, queue: nil)
        }

        running = true
        if central.state == .poweredOn { scan() }
        if peripheral.state == .poweredOn { advertise() }
    }

    func stop() {
        if !running { return }
        running = false
        central.stopScan()
        peripheral.stopAdvertising()
    }
}

extension Bluetooth: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn, running { advertise() }
    }
}

extension Bluetooth: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn, running { scan() }
    }

    func centralManager(_: CBCentralManager, didDiscover _: CBPeripheral, advertisementData: [String: Any], rssi: NSNumber) {
        let serviceIds = advertisementData[CBAdvertisementDataServiceUUIDsKey] as? [CBUUID] ?? []
        let overflow = advertisementData[CBAdvertisementDataOverflowServiceUUIDsKey]
        if let overflowIds = overflow as? [CBUUID] {
            if let uuid = overflowServiceUuidsToUint64(cbUuids: serviceIds + overflowIds) {
                let measuredPower = advertisementData[CBAdvertisementDataTxPowerLevelKey] as? Int
                delegate.discoveredDevice(Device(
                    uuid: uuid,
                    rssi: rssi.floatValue,
                    measuredPower: measuredPower ?? -1
                ))
            } else {}
        }
    }
}

public func overflowServiceUuidsToUint64(cbUuids: [CBUUID]) -> UInt64? {
    var uint64: UInt64 = 0
    for cbUuid in cbUuids {
        let index = UInt64(OverflowAreaUtils.BitPostitionForOverflowServiceUuid[cbUuid]!)
        if index == 0 { continue }
        if index < 8, index > 0 { return nil }
        if index - 8 >= 64 { return nil }

        uint64 = uint64 | (1 << (index - 8))
    }
    return uint64
}

public func uint64ToOverflowServiceUuids(uint64: UInt64) -> [CBUUID] {
    var cbUuids: [CBUUID] = [OverflowAreaUtils.TableOfOverflowServiceUuidsByBitPosition[0]]
    for index in 0 ... 63 {
        if (1 << index) & uint64 != 0 {
            cbUuids.append(OverflowAreaUtils.TableOfOverflowServiceUuidsByBitPosition[index + 8])
        }
    }
    return cbUuids
}
