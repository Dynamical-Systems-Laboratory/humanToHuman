from resources.api_resource import ApiResource
from common.auth import auth_app_register_required as auth
from common.errors_codes import DatabaseError
from hashlib import md5
from flask import jsonify

#Resource that will register the device id in to the database
class IdDevice(ApiResource):
    method_decorators = {
        'post' : [auth]
    }

    def __init__(self):
        super(IdDevice, self).__init__()
    
    def post(self):
        #Getting the arguments (located in the HTTO header) from the post request 
        self.parser.add_argument('id',type = str, required = True, location = 'headers')
        self.parser.add_argument('uid', type = str, required = True, location = "headers")
        args = self.parser.parse_args()
        uid = args['uid']
        id_device = md5(args['id'].encode('utf-8')).hexdigest()
        
        #In the case where the data is valid the device will be registered
        try:
            self.users_collection.update(
                {'uid' : uid },
                {
                    '$addToSet' : {
                        'id_devices' : id_device
                    }
                },
                True 
            )
        except:
            raise DatabaseError
        return jsonify({"mss" : "ok", "id" : id_device})

    def get(self):
        self.parser.add_argument('id_device',type = str, required = True)
        args = self.parser.parse_args()
        id_device = md5(args['id_device'].encode('utf-8')).hexdigest()
        return jsonify({"mss" : "ok", "id" : id_device})