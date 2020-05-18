
from common.databaseobjects.db_objects_scheme import ConnSchema, WifiConnSchema, BtConnSchema
from marshmallow import ValidationError
from app import app, mongo
from resources.api_resource import ApiResource
from flask import jsonify, request
from common.errors_codes import DataNotValid, DatabaseError, DeviceDoesNotExist

class ServerTest(ApiResource):
    def __init__(self):
        super(ServerTest, self).__init__()

    def post(self):
        from netaddr import IPNetwork, IPAddress    
        if not(IPAddress(request.remote_addr) in IPNetwork("130.192.0.0/16")):
            return 403
        self.parser.add_argument('conn_info',type=dict, location="json", required = True)
        args = self.parser.parse_args()
        schema = ConnSchema()
        try:
            conn = schema.load(args['conn_info'])
        except ValidationError:
            raise DataNotValid
        try:
            mongo.db.test.update(
                { '_id' : conn.id_device }, #Search that _id
                { '$push' : 
                    { 
                        'bt_conns' : {'$each' : BtConnSchema(many=True).dump(conn.bt_conns)}, 
                        'wifi_conns' : {'$each' : WifiConnSchema(many=True).dump(conn.wifi_conns)}
                    }
                }, #$Push to add the info, and $each is for every item in the list bt_conns 
                True) #Upsert must be true, because we want to add this information if the devices doesn't exist
        except:
           raise DatabaseError

        return jsonify({"message" : "ok"})