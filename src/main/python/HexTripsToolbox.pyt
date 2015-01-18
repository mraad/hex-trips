from hex import Hex
from hexgrid import HexGrid

from cassandra.cluster import Cluster
import arcpy
import traceback


class Toolbox(object):
    def __init__(self):
        self.label = "Toolbox"
        self.alias = ""
        self.tools = [HexTripsTool]


class HexTripsTool(object):
    def __init__(self):
        self.label = "Display Hex Cells"
        self.description = "Display Hex Cells from Cassandra"
        self.canRunInBackground = False

    def getParameterInfo(self):
        paramFC = arcpy.Parameter(
            name="out_cells",
            displayName="Cells",
            direction="Output",
            datatype="DEFeatureClass",
            parameterType="Derived")
        return [paramFC]

    def isLicensed(self):
        return True

    def updateParameters(self, parameters):
        return

    def updateMessages(self, parameters):
        return

    def execute(self, parameters, messages):

        hex = Hex(size=100)
        hexgrid = HexGrid(size=100, origx=-8300000.0, origy=4800000.0)

        fc = "in_memory/cells"
        if arcpy.Exists(fc):
            arcpy.management.Delete(fc)

        spref = arcpy.SpatialReference(102100)
        arcpy.management.CreateFeatureclass("in_memory", "cells", "POLYGON", spatial_reference=spref)
        arcpy.management.AddField(fc, "POPULATION", "LONG")

        cursor = arcpy.da.InsertCursor(fc, ['SHAPE@', 'POPULATION'])
        try:
            cluster = Cluster(['192.168.172.1'], port=9042)
            session = cluster.connect('test')
            crows = session.execute('select * from hexgrid')
            for crow in crows:
                xy = hexgrid.rc2xy(crow.row, crow.col)
                cursor.insertRow([hex.toPolygon(xy[0], xy[1]), crow.population])
            del crows
        except:
            arcpy.AddError(traceback.format_exc())

        del cursor
        parameters[0].value = fc
        return
