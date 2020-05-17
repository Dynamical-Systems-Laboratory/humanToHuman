from flask import Flask
from flask_pymongo import PyMongo
from flask_cors.extension import CORS
from flask_restful import Api, Resource, reqparse
from common.errors_codes import errors
import firebase_admin
from firebase_admin import credentials

app = Flask(__name__)

CORS(app)
#Inserting config file to flask app
app.config.from_object('config.Config')

#Create parser
parser = reqparse.RequestParser()

# Create database
app.config[
    "MONGO_URI"] = "mongodb://mongo_connect:gmaEQ6MpgyErKp7S@localhost/api"
mongo = PyMongo(app)

#Creatin API object
api = Api(app, errors=errors)

#Initialize app
if (not len(firebase_admin._apps)):
    cred = credentials.Certificate('python_project_key.json')
    firebase_app = firebase_admin.initialize_app(cred)
