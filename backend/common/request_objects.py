import math


class ConnInfoRequest(object):
    def __init__(self,**kwargs):
        self.bt = bool(kwargs.get('bt',0))
        self.wifi = bool(kwargs.get('wifi',0))
        self.loc = bool(kwargs.get('loc',0))
        self.id1 = kwargs.get('id1',None)
        self.id2 = kwargs.get('id2',None)
        self.date1 = kwargs.get('date1',None)
        self.date2 = kwargs.get('date2',None)
        self.csv = bool(kwargs.get('csv',None))

