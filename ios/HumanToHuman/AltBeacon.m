//
//  INBlueToothService.m
//  AltBeacon (Renamed from Vicinity)
//
//  Created by Ben Ford on 10/28/13 and modified by Martin Palatnik on 02/03/2014
//  
//  The MIT License (MIT)
// 
//  Copyright (c) 2013 Instrument Marketing Inc
//  Copyright (c) 2014 CharruaLabs
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.


#import "AltBeacon.h"
#import <CoreBluetooth/CoreBluetooth.h>
#import "CBPeripheralManager+Ext.h"
#import "CBUUID+Ext.h"


#define DEBUG_CENTRAL NO
#define DEBUG_PERIPHERAL NO
#define DEBUG_PROXIMITY NO

#define UPDATE_INTERVAL 1.0f
#define PROCESS_PERIPHERAL_INTERVAL 2.0f
#define RESTART_SCAN_INTERVAL 3.0f

@interface AltBeacon () <CBPeripheralManagerDelegate, CBPeripheralDelegate>
@end

@implementation AltBeacon {
    NSString *identifier;
    CBPeripheralManager *peripheralManager;
}


- (id)initWithIdentifier:(NSString *)identifier {
    if ((self = [super init])) {
        self->identifier = identifier;
    }
    return self;
}

- (void)startBroadcasting {
    if (![self canBroadcast])
        return;
    [self startBluetoothBroadcast];

}

- (void)stopBroadcasting {
    _isBroadcasting = NO;
    [peripheralManager stopAdvertising];
    peripheralManager = nil;
}

- (void)startBluetoothBroadcast {
    if (!peripheralManager) {
        peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:nil];

    }
}

- (void)startAdvertising {
    NSDictionary *advertisingData = @{CBAdvertisementDataLocalNameKey : identifier,
            CBAdvertisementDataServiceUUIDsKey : @[[CBUUID UUIDWithString:ALT_BEACON_SERVICE]]};
    [peripheralManager startAdvertising:advertisingData];

    _isBroadcasting = YES;
}

- (BOOL)canBroadcast {
    return YES;
}

- (BOOL)canMonitorBeacons {
    return YES;
}

#pragma mark - CBPeripheralManagerDelegate

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral {
    if (peripheral.state == CBManagerStatePoweredOn) {
        [self startAdvertising];
    }
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral error:(NSError *)error {
    if (DEBUG_PERIPHERAL) {
        if (error)
            NSLog(@"error starting advertising: %@", [error localizedDescription]);
        else
            NSLog(@"started advertising");
    }
}

- (BOOL)hasBluetooth {
    return self->peripheralManager.state == CBManagerStatePoweredOn;
}

@end
