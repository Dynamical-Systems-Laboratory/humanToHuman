import firebase_admin
from firebase_admin import auth
from firebase_admin.auth import AuthError
from global_values import firebase_app
from functools import wraps
from flask import jsonify
from flask_restful import abort, reqparse
from common.errors_codes import UnauthorizedAction, DataNotValid, DeviceDoesNotExist, TokenNotValid
from global_values import mongo
from hashlib import md5
from flask import request
from netaddr import IPNetwork, IPAddress
from flask import request
from global_values import app
from werkzeug.exceptions import BadRequest


def auth_app_login_required(func):
    @wraps(func)
    def wrapper():
        parser = reqparse.RequestParser()
        parser.add_argument('token',
                            type=str,
                            location='headers',
                            required=True)
        parser.add_argument('uid', type=str, location='headers', required=True)
        parser.add_argument('id', type=str, location='headers', required=True)
        args = parser.parse_args()

        token = args["token"]
        uid = args["uid"]
        id_device = args["id"]

        #Check if the user is valid
        checkUser(uid, token)
        #Now we have to check that the device already exists, and it is the user collection
        checkDevice(uid, id_device)
        return func()

    return wrapper


#Check by ip
def auth_ip(func):
    @wraps(func)
    def wrapper():
        # if not (IPAddress(request.remote_addr) in IPNetwork("130.192.0.0/16")):
        #     raise UnauthorizedAction
        return func()

    return wrapper


#When a user wants to register a new device
def auth_app_register_required(func):
    @wraps(func)
    def wrapper():
        parser = reqparse.RequestParser()
        parser.add_argument('token',
                            type=str,
                            location='headers',
                            required=True)
        parser.add_argument('uid', type=str, location='headers', required=True)
        args = parser.parse_args()
        token = args["token"]
        uid = args["uid"]
        checkUser(uid, token)
        return func()

    return wrapper


def checkUser(uid, token):
    #Firebase logic
    try:
        #Verify that the token is valid
        decode_token = auth.verify_id_token(token)
    except:
        app.logger.error(request.headers)
        raise TokenNotValid
    #We should check that the uid is valid, also it should be passed through the function, to know wich user is sending the info
    if uid != decode_token['uid']:
        raise UnauthorizedAction
    #Check if the user exist on the firebase database
    try:
        firebase_admin.auth.get_user(uid, firebase_app)
    except ValueError:
        raise DataNotValid
    except AuthError:
        raise UnauthorizedAction


def checkDevice(uid, id_device):
    users_collection = mongo.db.users
    n = users_collection.find_one({
        'uid':
        uid,
        'id_devices':
        md5(id_device.encode('utf-8')).hexdigest()
    })
    if (n == None):
        raise DeviceDoesNotExist