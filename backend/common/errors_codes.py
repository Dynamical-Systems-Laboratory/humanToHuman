#Will manage the errors
from flask_restful import HTTPException

#Client errors
class DataNotValid(HTTPException):
    pass
class UnauthorizedAction(HTTPException):
    pass
class DeviceDoesNotExist(HTTPException):
    pass
class TokenNotValid(HTTPException):
    pass

#Server Errors
class DatabaseError(HTTPException):
    pass


errors = {
    #Client errors
    'DataNotValid' : {
        'message' : "The data you have sent is not valid",
        'status'  : 422
    },
    'UnauthorizedAction' : {
        'message' : 'Your credentials are not valid',
        'status' : 401
    },
    'TokenNotValid' : {
        'message' : 'Your credentials are not valid',
        'status' : 402
    },
    'DeviceDoesNotExist' : {
        'message' : 'The device does not exist',
        'status' : 410
    },
    #Server errors
    'DatabaseError' : {
        'message' : 'The data was not stored',
        'status' : 500
    }
}