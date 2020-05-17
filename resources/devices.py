from resources.api_resource import ApiResource
from flask import jsonify, request
import json
from bson import json_util
from common.auth import auth_ip


class Devices(ApiResource):
    method_decorators = {'get': [auth_ip]}

    def __init__(self):
        super(Devices, self).__init__()

    def get(self):

        dict_finds = []

        # can be converted to not use the dumps/loads
        collections = json_util.dumps(self.collections.find({}, {'_id': 0}))
        collections = json.loads(collections)
        print(collections)
        for coll in collections:
            mongo_finds = self.db[coll['name']].find({}, {'_id': 1})
            dict_finds.extend(json.loads(json_util.dumps(mongo_finds)))
        ids = set([d['_id'] for d in dict_finds])
        id_list = []
        for _id in ids:
            uid = self.users_collection.find({'id_devices': _id}, {
                'uid': 1,
                '_id': 0
            })
            db_uids = json.loads(json_util.dumps(uid))
            uids = []
            try:
                for uid in db_uids:
                    uids.append(uid['uid'])
            except Exception:
                pass
            id_list.append({'uid': uids, '_id': _id})
        return {'id_devices': id_list}