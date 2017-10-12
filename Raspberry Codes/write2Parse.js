var Parse = require('parse/node');
var onoff = require('onoff');
var Gpio = onoff.Gpio;

// Initializing output pin on Raspberry Pi 2B
// Please be careful since the integer argument on Gpio corresponds to a GPIO numbering.
// It does NOT correspond to the physical position of the pins!
// Input pins need a third argument correponding to the edge of the signal
var ledPower = new Gpio(4, 'out'), led1 = new Gpio(17, 'out'), input1 = new Gpio(21, 'in', 'rising');

// Turning LED on while application is running
ledPower.write(1,function(){
	console.log('Application is ON');
});

//client key: "jApSFpcbqVgXxWWNo9JAsUYx8VrM6vV8ouNGCjkD"
//js key: "mzZk2tvkxFcMfzl5WUk291efhitHFlmpBkgI8p6w"
//master key: "U0TfooANWPzL6iLK3VdlSzgsgFDwbKAVxq5dxGMq"
//rest key: "Se13kv8fmSZbI6ES0Jzy6lqPbXkxkd7ooOhtPIAt"

// Initializing with AppID and Javascript key
// Both are obtained from Back4App Dashboard -> Features -> Core Settings -> Server
// Use your Parse Server URL to connect. 
Parse.initialize('4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg','mzZk2tvkxFcMfzl5WUk291efhitHFlmpBkgI8p6w');
Parse.serverURL = "https://parseapi.back4app.com" ;

// Name of the class to be saved on Parse Server when event happens
var InputGPIO = Parse.Object.extend("InputGPIO");

// asynchronously reading from GPIO 21 (pin 40) on RPi 2B
// "value" can be either 0 (0 V) or 1 (3.3 V)

// Often multiple readings may come at once, even when the voltage on the pin is kept constant
// Therefore, the variable "valuePrev" is introduced to store the last voltage
// read from the pin and to trigger the desired actions only when it changes.
var valuePrev = -1;
input1.watch( function (err, value) {
	if(err){
		throw err;
	}
	
	if(value != valuePrev){
		valuePrev = value;	

		// Turning of/off a LED when another voltage is read in the input pin
		led1.write(value, function(){
			console.log('LED value changed to: ' + value);
		});
		
		// Creating new object
		var inputGPIO = new InputGPIO();
		
		if (value == 0){
			inputGPIO.set("content", "low");
			console.log("written low: " + value);
		}
		
		else{
			inputGPIO.set("content", "high");
			console.log("written high: " + value);			
		}
		
		// Saving object to Parse Server
		inputGPIO.save().then( function(m){
			console.log(m);
		}).catch( function(err){
			console.error(err);
		});		
	}
});

// Turning off all GPIO pins when process is interrupted.
process.on('SIGINT', function (){
	ledPower.writeSync(0);
	led1.writeSync(0);
	ledPower.unexport();
	led1.unexport();
	console.log('Bye, bye! Turning off LEDs');
	process.exit();
});

