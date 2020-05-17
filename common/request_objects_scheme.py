from common.request_objects import *
from marshmallow import Schema, fields, post_load, post_dump

class ConnInfoRequestSchema(Schema):
    bt = fields.Integer(required=False)
    wifi = fields.Integer(required=False)
    date1 = fields.Integer(required = False)
    date2 = fields.Integer(required = False)
    loc = fields.Integer(required = False)
    #Todo, check
    id1 = fields.String(required=False)
    id2 = fields.String(required=False)
    csv = fields.Integer(required = False)

    @post_load
    def make_wifi_conn(self,args):
        return  ConnInfoRequest(**args)

    