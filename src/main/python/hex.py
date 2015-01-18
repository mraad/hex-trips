import math


class Hex:
    def __init__(self, size):
        self.xy = []
        for i in range(7):
            angle = math.pi * ((i % 6) + 0.5) / 3.0
            x = size * math.cos(angle)
            y = size * math.sin(angle)
            self.xy.append((x, y))

    def toPolygon(self, cx, cy):
        return [(x + cx, y + cy) for x, y in self.xy]
