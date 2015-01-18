import math


class HexGrid:
    def __init__(self, size=100.0, origx=0.0, origy=0.0):
        self.origx = origx
        self.origy = origy
        self.size = size
        self.h = self.size * math.cos(30.0 * math.pi / 180.0)
        self.v = self.size * 0.5
        self.skipx = 2.0 * self.h
        self.skipy = 3.0 * self.v

    def rc2xy(self, r, c):
        ofs = self.h if r % 2 != 0 else 0
        x = c * self.skipx + ofs + self.origx
        y = r * self.skipy + self.origy
        return x, y
