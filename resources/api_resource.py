from flask_restful import Resource, reqparse
from global_values import mongo
from datetime import datetime


class ApiResource(Resource):
    def __init__(self):
        self.parser = reqparse.RequestParser()
        self.db = mongo.db
        self.collections = mongo.db.collections
        self.conn_collection = mongo.db.conn
        self.users_collection = mongo.db.users
        super(ApiResource, self).__init__()

    def trunc_datetime(self, date):
        return date.replace(hour=0, minute=0, second=0, microsecond=0)

    def get_mongo_coll_name(self, date):
        return "conn_" + str(int(self.trunc_datetime(date).timestamp()))

    def get_conn_name_by_current_date(self):
        return self.get_mongo_coll_name(datetime.utcnow())

    def get_conn_doc_by_current_date(self):
        return self.get_conn_doc_by_date(datetime.utcnow())

    def get_conn_doc_by_date(self, date):
        return self.db[self.get_mongo_coll_name(date)]

# def get_dates_between_dates(self,date1,date2)
