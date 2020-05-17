from flask import jsonify
from hashlib import md5
import datetime
from datetime import datetime

time_form = '%Y-%m-%d %H:%M:%S'
#This class will store the different devices that has been identified by the app
#and also it will have the id of the device
class Conn (object): 
    def __init__(self, id_device, bt_conns = [], wifi_conns = [], loc_conn = None, hash_name = True):
        #Since we want the _id of the device be the _id of the collection
        #We should change the field name once we added to the database
        self.id_device = md5(id_device.encode('utf-8')).hexdigest() if hash_name else id_device
        self.bt_conns = bt_conns
        self.wifi_conns = wifi_conns
        self.loc_conn = loc_conn
    def convert_to_csv_bt(self):
        list_bt_conns = []
        for bt_conn in self.bt_conns:
            bt_conn = bt_conn.convert_to_csv(self.id_device)
            list_bt_conns.append(bt_conn)
        return list_bt_conns
    def convert_to_csv_wifi(self):
        list_wifi_conns = []
        for wifi_conn in self.wifi_conns:
            wifi_conn = wifi_conn.convert_to_csv(self.id_device)
            list_wifi_conns.append(wifi_conn)
        return list_wifi_conns
    def convert_to_csv_loc(self):
        list_loc_conns = []
        for loc_conn in self.loc_conn:
            loc_conn = loc_conn.convert_to_csv(self.id_device)
            list_loc_conns.append(loc_conn)
        return list_loc_conns

#It will store the information about the bt devices found by the device
class BtConn(object):
    def __init__(self, device_name, rssi, time,mac_address, mobile = True, screen = False, hash_name = True):
        self.device_name = md5(device_name.encode('utf-8')).hexdigest() if hash_name else device_name
        #print(md5(device_name.encode('utf-8')).hexdigest())
        self.rssi = rssi
        self.time =time
        self.mac_address = mac_address
        self.mobile = mobile
        self.screen = screen
        
    def convert_to_csv(self,id_device):
        timestamp_day = datetime.fromtimestamp(self.time/1000)
        return [id_device,self.device_name,self.mac_address,self.rssi,self.mobile,timestamp_day.strftime(time_form),self.time,self.screen]

class WifiConn(object):
    def __init__(self,bssid,rssi,time):
        self.bssid = bssid
        self.rssi = rssi
        self.time = time
    
    def convert_to_csv(self,id_device):
        timestamp_day = datetime.fromtimestamp(self.time/1000)
        return [id_device,self.bssid,self.rssi,timestamp_day.strftime(time_form),self.time]

class LocalizationConn(object):
    def __init__(self,longitude,latitude,altitude,accuracy,time):
        self.longitude = longitude
        self.latitude =  latitude
        self.altitude =  altitude
        self.accuracy =  accuracy
        self.time = time
    def convert_to_csv(self,id_device):
        timestamp_day = datetime.fromtimestamp(self.time/1000)
        return [self.longitude, self.latitude,self.altitude,self.accuracy,timestamp_day.strftime(time_form),self.time]


#For when we request some info from the database, we want to handle sometimes the data differently

class ConnRequest (Conn): 
    def __init__(self,**kwargs):
        super(**kwargs,hash_name=False).__init__()

class BtConnRequest(BtConn):
    def __init__(self, **kwargs):
        super(**kwargs, hash_name = False).__init__()
   