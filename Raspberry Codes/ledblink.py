import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BOARD)
GPIO.setup(11,GPIO.OUT)

while 1:
	GPIO.output(11,1)
	print "pin 11 HIGH"
	time.sleep(1)
	GPIO.output(11,0)
	print "pin 11 LOW"
	time.sleep(1)


for i in range(10):
	print i


