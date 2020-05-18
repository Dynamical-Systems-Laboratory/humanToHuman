from common.databaseobjects.db_objects import *
from marshmallow import Schema, fields, post_load, post_dump
from common.validation import validate_bluetooth_name, validate_bssid, validate_bt_mac

#The following classes will define the requirements of the objects structure
#It also validates the information passed throught the json data

class WifiConnSchema(Schema):
    bssid = fields.String(required=True,validate=validate_bssid)
    rssi = fields.Float(required=True)
    time = fields.Integer(required = True)
    @post_load
    def make_wifi_conn(self,data):
        return WifiConn(**data)

class BtConnSchema(Schema):
    device_name = fields.String(required = True,validate=validate_bluetooth_name)
    rssi = fields.Float(required = True)
    mac_address= fields.String(required = True, validate=validate_bt_mac)
    time = fields.Integer(required = True)
    mobile = fields.Bool(required = False)
    screen = fields.Bool(required = False)

    @post_load
    def make_bt_conn(self,data):
        return BtConn(**data)

class LocalizationConnSchema(Schema):
    longitude = fields.Float()
    latitude = fields.Float()
    altitude = fields.Float()
    accuracy = fields.Float()
    time = fields.Int()
    @post_load
    def make_loc(self,data): 
        return LocalizationConn(**data)

class ConnSchema(Schema):
    id_device = fields.Str(required = True)
    bt_conns = fields.List(fields.Nested(BtConnSchema))
    wifi_conns = fields.List(fields.Nested(WifiConnSchema))
    loc_conn = fields.Nested(LocalizationConnSchema)
    @post_load
    def make_conn(self,data): 
        return Conn(**data)


    
#Schemes for when we are getting the values from the database

class BtConnSchemaRequest(BtConnSchema):
    @post_load
    def make_bt_conn(self,data):
        return BtConn(**data, hash_name= False)

class ConnSchemaRequest(Schema):
    _id = fields.Str(required = True, attribute= "id_device")
    bt_conns = fields.List(fields.Nested(BtConnSchemaRequest,required = False))
    wifi_conns = fields.List(fields.Nested(WifiConnSchema,required = False))
    loc_conns = fields.List(fields.Nested(LocalizationConnSchema,required= False),attribute = "loc_conn")
    @post_load
    def make_conn(self,data): 
        return Conn(**data, hash_name = False)

