
from flask import make_response

def csv_response(stIO, name ="export.csv"):
    output = make_response(stIO.getvalue())
    output.headers["Content-Disposition"] = "attachment; filename=" +name
    output.headers["Content-type"] = "text/csv"
    return output