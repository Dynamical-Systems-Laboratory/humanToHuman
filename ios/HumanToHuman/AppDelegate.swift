//
//  AppDelegate.swift
//  HumanToHuman
//
//  Created by Albert Liu on 5/17/20.
//  Copyright © 2020 Albert Liu. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var rows: [(device: Device, lastSeen: Date)] = []


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        print("application launch")
        guard Database.initDatabase() else { print("Database failed to init"); exit(1) }
        Bluetooth.delegate = self
        
        if let baseurl = Database.getPropText(prop: KEY_SERVER_BASE_URL) { // we have a server to connect to
            Services.popToServer(baseurl: baseurl)
            
            if let id = Database.getPropNumeric(prop: KEY_OWN_ID) {
                print("init with saved id \(id)")
                Bluetooth.id = id
            } else {
                Server.getUserId(baseurl: baseurl) { id in
                    guard let id = id else { exit(1) }
                    print("got id \(id)")
                    Database.setPropNumeric(prop: KEY_OWN_ID, value: Int64(bitPattern: id))
                    Bluetooth.id = id
                }
            }
        }
        
        
        // Override point for customization after application launch.
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        print("applicationDidEnterBackground")
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        print("applicationWillTerminate")
    }


}

extension AppDelegate: BTDelegate {
    func discoveredDevice(_ device: Device) {
        guard Database.writeRow(device: device) else {
            print("something went wrong with sql")
            return
        }
        
        if let idx = rows.firstIndex(where: { row in row.device.uuid == device.uuid }) {
            rows[idx].device.rssi = device.rssi
            rows[idx].device.measuredPower = device.measuredPower
            rows[idx].lastSeen = Date()
        } else {
            rows.append((device: device, lastSeen: Date()))
        }
    }
}
