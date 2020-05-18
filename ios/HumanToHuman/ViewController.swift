import UIKit
import CoreBluetooth
import MultipeerConnectivity
import SystemConfiguration.CaptiveNetwork

func getConnectedWifiMacAdrees() -> [String:String] {
    var informationDictionary = [String:String]()
    let informationArray: NSArray? = CNCopySupportedInterfaces()
    if let information = informationArray {
        let dict: NSDictionary? = CNCopyCurrentNetworkInfo(information[0] as! CFString)
        if let temp = dict {
            informationDictionary["SSID"] = String(temp["SSID"]as!String)
            informationDictionary["BSSID"] = String(temp["BSSID"]as!String)
            return informationDictionary
        }
    }

    return informationDictionary
}

// https://developer.apple.com/documentation/multipeerconnectivity
class ViewController: UIViewController, MCNearbyServiceBrowserDelegate {
    
    // PROTOCOL: MCNearbyServiceBrowserDelegate
    func browser(_ browser: MCNearbyServiceBrowser, foundPeer peerID: MCPeerID, withDiscoveryInfo info: [String : String]?) {
        print("Discovered Peer ID:", peerID)
        print("    Discovery Info:", info ?? [String:String]())
        print()
    }
    
    // PROTOCOL: MCNearbyServiceBrowserDelegate
    func browser(_ browser: MCNearbyServiceBrowser, lostPeer peerID: MCPeerID) {
        print("Lost Peer ID:", peerID)
        print()
    }
    

    // Outlet for sliders
    @IBOutlet weak var redSlider: UISlider!
    @IBOutlet weak var connectButton: UIButton!
    
    var peerId : MCPeerID!
    var serviceBrowser : MCNearbyServiceBrowser!
    
    // Properties
//    private var centralManager: CBCentralManager!
//    private var peripheral: CBPeripheral!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        print("View loaded")
        self.peerId = MCPeerID(displayName: UIDevice.current.name)
        self.serviceBrowser = MCNearbyServiceBrowser(peer: self.peerId, serviceType: "humantohuman")
        self.redSlider.isEnabled = true
    }
    
    @IBAction func buttonPressed(_ sender: Any) {
        print("button pressed")
    }
    
    @IBAction func buttonReleased(_ sender: Any) {
        print("button released outsidee")
    }
    
    @IBAction func sliderChanged(_ sender: Any) {
        print("red:",redSlider.value);
    }
}

