var Gpio = require('onoff').Gpio;
var led = new Gpio(4, 'out');
var ledValue = 0;

function blink(){
	ledValue = ledValue ^ 1 // ^ is XOR operator
	led.write(ledValue, function (err) {
		console.log("Gpio pin 4 is " + ledValue);
      	if (err) {
        	 throw (err);
      	}
   	});
}

setInterval(blink, 1000); // execute blink each 1000 ms