from common.errors_codes import DataNotValid



def validate_bluetooth_name(name):
    if len(name) > 64:
        raise DataNotValid('')

def validate_bssid(bssid):
    if len(bssid) > 20:
        raise DataNotValid('')

def validate_bt_mac(mac):
    if len(mac) > 20:
        raise DataNotValid('')
