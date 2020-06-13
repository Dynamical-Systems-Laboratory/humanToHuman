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

// The service UUID we search with; this cooresponds to index zero of TableOfOverflowServiceUuidStringsByBitPosition
let GLOBAL_SERVICE_UUID = CBUUID(string: "00000000-0000-0000-0000-00000000007C")

// A single device that we've scanned.
struct Device {
    let uuid: UInt64
    var rssi: Float
    var measuredPower: Int
}

// The delegate for the scanning service.
protocol BTDelegate {
    func discoveredDevice(_: Device)
}

// The class that handles the bluetooth communication. Its two main methods are start() and stop()
class Bluetooth: NSObject {
    let delegate: BTDelegate
    let id: UInt64
    var peripheral: CBPeripheralManager!
    var central: CBCentralManager!
    var scanning: Bool = false
    var advertising: Bool = false

    init(delegate: BTDelegate, id: UInt64) {
        self.delegate = delegate
        self.id = id
    }

    // Start scanning for peripherals, using the global service UUID
    private func scan() {
        central.scanForPeripherals(
            withServices: OverflowAreaUtils.allOverflowServiceUuids(),
            options: [CBCentralManagerScanOptionAllowDuplicatesKey: true]
        )
    }

    // start advertising, using the service uuid combination that cooresponds to our id.
    private func advertise() {
        peripheral.startAdvertising([
            CBAdvertisementDataServiceUUIDsKey: uint64ToOverflowServiceUuids(uint64: id),
        ])
    }

    func startAdvertising() {
        if advertising { return }
        advertising = true
        if peripheral == nil {
            peripheral = CBPeripheralManager(delegate: self, queue: nil)
            return
        }

        advertise()
    }

    func startScanning() {
        if scanning { return }
        scanning = true
        if central == nil {
            central = CBCentralManager(delegate: self, queue: nil)
            return
        }

        scan()
    }

    func stopScanning() {
        if !scanning { return }
        scanning = false
        central.stopScan()
    }

    // stop advertising and scanning.
    func stopAdvertising() {
        if !advertising { return }
        advertising = false
        peripheral.removeAllServices()
        peripheral.stopAdvertising()
    }
}

extension Bluetooth: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        if peripheral.state == .poweredOn, advertising { advertise() }
    }
}

extension Bluetooth: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn, scanning { scan() }
    }

    // Called whenever a new device is detected; service uuids are stored as CBAdvertisementDataServiceUUIDsKey, and then as
    // CBAdvertisementDataOverflowServiceUUIDsKey when more space is needed. We depend on this behavior to store a UUID for
    // each device as a collection of service UUIDs.
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
            }
        }
    }
}

// We convert service uuids into a bitmap, and also ensure we find our sentinel value, the 0th bit in the overflow area.
// The (index - 8) is because we skip the first byte of overflow space to store a sentinel value.
public func overflowServiceUuidsToUint64(cbUuids: [CBUUID]) -> UInt64? {
    var uint64: UInt64 = 0
    var foundSentinel = false
    for cbUuid in cbUuids {
        let index = UInt64(OverflowAreaUtils.BitPostitionForOverflowServiceUuid[cbUuid]!)
        if index == 0 { foundSentinel = true; continue }
        if index < 8, index > 0 { return nil }
        if index - 8 >= 64 { return nil }

        uint64 = uint64 | (1 << (index - 8))
    }

    if foundSentinel { return uint64 }
    else { return nil }
}

// We convert our bitmap into a list of service uuids, and ensure the sentinel value is included.
// The [index + 8] is because we skip the first byte of overflow space to store a sentinel value.
public func uint64ToOverflowServiceUuids(uint64: UInt64) -> [CBUUID] {
    var cbUuids = [OverflowAreaUtils.TableOfOverflowServiceUuidsByBitPosition[0]]
    for index in 0 ... 63 {
        if (1 << index) & uint64 != 0 {
            cbUuids.append(OverflowAreaUtils.TableOfOverflowServiceUuidsByBitPosition[index + 8])
        }
    }
    return cbUuids
}
