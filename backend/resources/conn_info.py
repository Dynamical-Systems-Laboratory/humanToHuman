import pprint
from flask_restful import Resource
from global_values import app, mongo
from flask import jsonify, request
from common.databaseobjects.db_objects_scheme import ConnSchema, WifiConnSchema, BtConnSchema, ConnSchemaRequest, LocalizationConnSchema
from common.request_objects_scheme import ConnInfoRequestSchema
from marshmallow import ValidationError
from common.databaseobjects.db_objects import Conn
from bson import json_util
from common.auth import auth_app_login_required as auth
from common.auth import auth_ip
from common.errors_codes import DataNotValid, DatabaseError, DeviceDoesNotExist
from resources.api_resource import ApiResource
from datetime import datetime
from werkzeug.exceptions import BadRequest

import json


class ConnInfo(ApiResource):
    method_decorators = {'post': [auth], 'get': [auth_ip]}

    def __init__(self):
        super(ConnInfo, self).__init__()

    def post(self):
        #raise DeviceDoesNotExist
        #Getting args
        try:
            self.parser.add_argument('conn_info',
                                     type=dict,
                                     location="json",
                                     required=True)
            args = self.parser.parse_args()
        except Exception:
            app.logger.error(request.get_json())
            args = request.get_json()
        #Saving requested json
        #Preparing document to add it to the db

        #1 Validate that the json structure is valid by constructing the objects
        #Try catch should handle the exception, and it must tell that the infromation is not valid
        #In case it is valid, now we can added to the database
        schema = ConnSchema()
        try:
            conn = schema.load(args['conn_info'])
        except ValidationError:
            raise DataNotValid

        #Add object to the collection
        #Using update from the api collection, we search if the id exists, in that case it will add the values
        #In case the _id doesnt exist, it will create one automatically and will add the values
        # try:
        query = {}
        query['bt_conns'] = {
            '$each': BtConnSchema(many=True).dump(conn.bt_conns)
        }
        query['wifi_conns'] = {
            '$each': WifiConnSchema(many=True).dump(conn.wifi_conns)
        }
        if conn.loc_conn != None:
            query['loc_conns'] = LocalizationConnSchema().dump(conn.loc_conn)

        current_name = self.get_conn_name_by_current_date()
        self.collections.update({'name': current_name}, {'name': current_name},
                                True)
        self.get_conn_doc_by_current_date().update(
            {'_id': conn.id_device},  #Search that _id
            {
                '$push': query
            },  #$Push to add the info, and $each is for every item in the list bt_conns 
            True
        )  #Upsert must be true, because we want to add this information if the devices doesn't exist
        # except:
        #    raise DatabaseError
        return jsonify({"message": "ok"})

    def get(self):

        args = request.args
        schema = ConnInfoRequestSchema()
        try:
            params = schema.load(args)
        except ValidationError:
            raise DataNotValid

        #We request to the database the existing collections that are storing information
        # of the differents scans
        collections = json_util.dumps(self.collections.find({}, {'_id': 0}))
        collections = json.loads(collections)
        #With the parameters passed by the user it is created the respective mongoquery
        query = get_query(params)

        dict_finds = []
        #For each of the existing collections the query is applyied
        for coll in collections:
            mongo_finds = self.db[coll['name']].aggregate(query)
            dict_finds.extend(json.loads(json_util.dumps(mongo_finds)))

        #Then all the data is going to be aggregate by device _id
        # and also it is going to removed those objects that are empty
        # by checking if the field of the object exist or not

        final_dict = []
        for conn_info in dict_finds:
            bt = False
            wifi = False
            loc = False
            try:
                bt = len(conn_info['bt_conns']) > 0
            except:
                bt = False
            try:
                wifi = len(conn_info['wifi_conns']) > 0
            except:
                wifi = False
            try:
                loc = len(conn_info['loc_conns']) > 0
            except:
                loc = False

            if (bt or wifi or loc): final_dict.append(conn_info)

        #Aggregate data by by id
        ids = set([d['_id'] for d in dict_finds])
        final_conns = []
        for _id in ids:
            conns = [x for x in final_dict if x['_id'] == _id]
            for i in range(len(conns)):
                if i == 0: conn = conns[i]
                else:
                    if loc: conn['loc_conns'].extend(conns[i]['loc_conns'])
                    if wifi: conn['wifi_conns'].extend(conns[i]['wifi_conns'])
                    if bt: conn['bt_conns'].extend(conns[i]['bt_conns'])
            final_conns.append(conn)
        final_dict = final_conns
        #Which is the format of the data requested?
        if not params.csv: return final_dict
        #If it is not JSON, it will be requested a CSV, by default it will return
        #the data from the BluetoothConn object
        if params.wifi: return wifi_csv(final_dict)
        if params.loc: return loc_csv(final_dict)
        return bt_csv(final_dict)


################################################################
#Function that will prepare the query if something is requested#
# ##############################################################
def get_query(params):
    query = []
    if params.id1 != None: query.append(query_match(params))
    if params.bt or params.wifi or params.loc:
        query.append(query_project(params))
    return query


#####################################################################
#When it is request the scanning device by ID, here it can be passed#
#more than one device                                               #
#####################################################################
def query_match(params):
    return {'$match': {'_id': params.id1}}


def query_project(params):
    project = {}
    if params.bt: project['bt_conns'] = bt_conns_filter(params)
    if params.wifi: project['wifi_conns'] = wifi_conns_filter(params)
    if params.loc: project['loc_conns'] = loc_conns_filter(params)
    return {'$project': project}


######################################################
#Which the object we want to filter from the database#
######################################################
#For the BluetoothConn object
def bt_conns_filter(params):
    and_filter = timestamp_filter(params)
    #Here, it is specified in the case we are looking for an specific device which
    #has been scanned
    if params.id2 != None:
        and_filter.append({'$eq': ['$$conn.device_name', params.id2]})
    return {
        '$filter': {
            'input': '$bt_conns',
            'as': 'conn',
            'cond': {
                '$and': and_filter
            }
        }
    }


#For the WifiConn object
def wifi_conns_filter(params):
    return {
        '$filter': {
            'input': '$wifi_conns',
            'as': 'conn',
            'cond': {
                '$and': timestamp_filter(params)
            }
        }
    }


#For the LocationConn Object
def loc_conns_filter(params):
    return {
        '$filter': {
            'input': '$loc_conns',
            'as': 'conn',
            'cond': {
                '$and': timestamp_filter(params)
            }
        }
    }


#############################################################
#This will add the filter by timestamps in case it is needed#
#############################################################
def timestamp_filter(params):
    and_filter = []
    if params.date1 != None:
        and_filter.append({'$gt': ['$$conn.time', params.date1]})
    if params.date2 != None:
        and_filter.append({'$lt': ['$$conn.time', params.date2]})
    return and_filter


####################################################
#Part for creating a CSV file that will be returned#
####################################################
def create_csv(conns_object, fields):
    import csv
    import io
    from common.response import csv_response
    #Initialize
    si = io.StringIO()
    csv = csv.writer(si,
                     delimiter=',',
                     quotechar='"',
                     quoting=csv.QUOTE_MINIMAL)
    csv.writerow(fields)
    for conn in conns_object:
        for data in conn:
            csv.writerow(data)
    return csv_response(si)


#########################################################################
#Here is defined how it should be created the CSV file obtained by query#
#########################################################################
#For bluetooth
def bt_csv(final_dict):
    ##CSV PART
    schema = ConnSchemaRequest(many=True)
    try:
        conns = schema.load(final_dict)
    except ValidationError:
        raise DataNotValid

    bt_all_conns = []
    for conn in conns:
        bt_all_conns.append(conn.convert_to_csv_bt())
    fields = [
        "id", "device_name_found", "mac_address_found", "mobile", "rssi",
        "time", "timestamp", "scree_on"
    ]
    return create_csv(bt_all_conns, fields)


#For WiFI
def wifi_csv(final_dict):
    ##CSV PART
    schema = ConnSchemaRequest(many=True)
    try:
        conns = schema.load(final_dict)
    except ValidationError:
        raise DataNotValid
    wifi_all_conns = []
    for conn in conns:
        wifi_all_conns.append(conn.convert_to_csv_wifi())
    fields = ["id", "bssid", "rssi", "time", "timestamp"]
    return create_csv(wifi_all_conns, fields)


#For location
def loc_csv(final_dict):
    schema = ConnSchemaRequest(many=True)
    try:
        conns = schema.load(final_dict)
    except ValidationError:
        raise DataNotValid
    loc_all_conns = []
    for conn in conns:
        loc_all_conns.append(conn.convert_to_csv_loc())
    fields = [
        "id", "longitude", "latitude", "altitude", "accuracy", "time",
        "timestamp"
    ]
    return create_csv(loc_all_conns, fields)