from flask import Flask, url_for
from flask_restful import Resource
from config import Config
import firebase_admin
from firebase_admin import credentials
from flask import current_app, request
from flask_cors.extension import CORS
from common.errors_codes import errors
from global_values import api, app
import os

#Routes imports
from resources.conn_info import ConnInfo
from resources.id_device import IdDevice
from resources.devices import Devices
#from resources.experiment1 import Experiment1
#from resources.server_test import ServerTest


@app.route("/")
def index():
    return "Api Project"


#Adding resources to api object
api.add_resource(ConnInfo,
                 '/conn_info',
                 '/conn_info/',
                 '/conn_info/',
                 endpoint='conn_info')
api.add_resource(IdDevice, '/id_device', '/id_device/', endpoint='id_device')
api.add_resource(Devices, '/devices', '/devices/', endpoint='devices')
#api.add_resource(ServerTest,'/test','/test/',endpoint = 'test')
#api.add_resource(Experiment1,'/experiment1','/experiment1/',endpoint = 'experiment1')

#Starting the app
if __name__ == "__main__":
    import logging
    app.run(debug=True, host='0.0.0.0')
    LOG_FILENAME = os.path.join(os.path.dirname(__name__), 'errors.log')
    logging.basicConfig(filename=LOG_FILENAME, level=logging.INFO)
