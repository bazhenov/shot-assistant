import cv2
import sys, math
import numpy as np
from matplotlib import pyplot as plt

fnames = sys.argv[1:]
w = 1280
h = 1024
frameNo = 1;

def distance(p1, p2):
	x1, y1 = p1
	x2, y2 = p2
	return math.sqrt(math.pow(x1 - x2, 2) + math.pow(y1 - y2, 2))

def ang(p1, p2):
	x1, y1 = p1
	x2, y2 = p2
	a = x1 - x2
	b = y1 - y2
	return math.atan(a / b) * 180 / math.pi

def orderTopologically(p):
	ySorted = sorted(p, key=lambda i: i[1])
	upper = sorted(ySorted[0:2], key=lambda i: i[0])
	lower = sorted(ySorted[2:4], key=lambda i: -i[0])
	return upper + lower;

def midpoint(rect):
	((x, y), (w, h), a) = rect
	rad = a * math.pi / 180
	cosa = math.cos(rad)
	sina = math.sin(rad)
	wp = w/2
	hp = h/2
	return (int(x + wp * cosa - hp * sina),
		int(y - wp * sina + hp * cosa))

for fname in fnames:
	img = cv2.imread(fname)

	img = cv2.resize(img, (w, h)) 
	gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
	#gray = cv2.equalizeHist(gray)

	ret, thresh = cv2.threshold(gray, 140, 255, cv2.THRESH_BINARY)
	kernel = np.ones((3,3),'int')
	thresh = cv2.dilate(thresh, kernel)
	contours, i = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
	#contours = []
	#cv2.drawContours(img, contours, -1, (0,33,0), 1)

	ellipses = []
	for c in contours:
		if (len(c) >= 10 and len(c) < 80):
			el = cv2.fitEllipse(c)
			p, sz, angle = el

			areaPerc = sz[0] * sz[1] / (w * h) * 100
			ratio = sz[0] / sz[1] if sz[0] > sz[1] else sz[1] / sz[0]
			if ratio < 1.8 and areaPerc > 0.005 and areaPerc < 0.1:
				ellipses.append((el, distance(p,(w/2, h/2))))

	ellipses = sorted(ellipses, key=lambda i: i[1])
	ellipses = ellipses[0:4]

	midpoints = []
	for e, d in ellipses:
		p, sz, angle = e
		midpoints.append((int(p[0]), int(p[1])))
		#cv2.ellipse(img, e, (0, 255, 0), 2, 4)
		cv2.circle(img, (int(p[0]), int(p[1])), 3, (255, 255, 255), -1)

	if len(midpoints) == 4:
		midpoints = orderTopologically(midpoints)
		print ang(midpoints[0], midpoints[1])
		print ang(midpoints[2], midpoints[3])

		print ang(midpoints[1], midpoints[2])
		print ang(midpoints[3], midpoints[0])

		for i in range(0, len(midpoints) - 1):
			p1, p2 = midpoints[i:i + 2]
			cv2.line(img, p1, p2, (0, 0, 255), 1)
		#cv2.line(img, midpoints[0], midpoints[-1], (0, 0, 255), 1)

	cv2.imwrite("%05d.jpg" % (frameNo), img) 
	frameNo += 1


	#cv2.imshow('image', img)
	#cv2.waitKey(0)
	#cv2.destroyAllWindows()
