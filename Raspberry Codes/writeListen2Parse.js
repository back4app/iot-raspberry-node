/* Part 0: Importing required modules, turning ON a LED for indicating
 * the application is running
 */
  
var Parse = require('parse/node');
var onoff = require('onoff');
var Gpio = onoff.Gpio;

// Initializing output pin on Raspberry Pi 2B
// Please be careful since the integer argument on Gpio corresponds to a GPIO numbering.
// It does NOT correspond to the physical position of the pins!
var ledPower = new Gpio(4, 'out');

// Turning LED on while application is running
ledPower.write(1,function(){
	console.log('Application is ON');
});

/* Part 1: listening to Parse Server events and performing corresponding
 * action
 */

// Setting LiveQuery up
// App ID is obtained from Back4App Dashboard -> Features -> Core Settings -> Server
// serverURL is obtained from Back4App Dashboard -> Features -> Web Hosting and Live Query ->
// -> Server -> Subdomain name (must start with "wss:")
var LiveQueryClient = Parse.LiveQueryClient;
var client = new LiveQueryClient({
	applicationId: '4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg',
	serverURL: 'wss:hellonewworld.back4app.io/',
	javascriptKey: '',
	masterKey: ''
});

// Check if client is set up successfully
console.log("pre client.open");
client.open();
console.log("post client.open");

// Start query for class "CommandGPIO1":
// Perform an action whenever an object is created on Parse Server
var query1 = new Parse.Query("CommandGPIO1");
var sub1 = client.subscribe(query1);
console.log("post sub1");

// Starting GPIO pin
output1 = new Gpio(17, 'out'); 
var count1 = 0;
sub1.on('create', (object) => {
	
	// Change logic value on output according to "content" field on object
	console.log(object.get("content"));
	if(object.get("content") == "off") count1 = 0;	
	if(object.get("content") == "on") count1 = 1;
	
	
	// Writing logic on corresponding GPIO pin
	output1.write(count1, function(){ 
		console.log('Output 1 status changed to ' + count1); 
	});	
});


/* Part 2: waiting for events on the board and creating an object in 
 * Parse Server. Also, toggling a LED connected to the board
 */

// Initializing with AppID and Javascript key
// Both are obtained from Back4App Dashboard -> Features -> Core Settings -> Server
// Use your Parse Server URL to connect. 
Parse.initialize('4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg','mzZk2tvkxFcMfzl5WUk291efhitHFlmpBkgI8p6w');
Parse.serverURL = "https://parseapi.back4app.com" ;

// Name of the class to be saved on Parse Server when event happens
var InputGPIO = Parse.Object.extend("InputGPIO");

// Asynchronously reading from GPIO 21 (pin 40) on RPi 2B
// "value" can be either 0 (0 V) or 1 (3.3 V)
// DO NOT PUT A VOLTAGE GREATER THAN 3.3 V, OR YOU MAY DAMAGE YOUR PI!
// The same value that is read is written to GPIO 23 (pin 16)

// Often multiple readings may come at once, even when the voltage on the pin is kept constant
// Therefore, the variable "valuePrev" is introduced to store the last voltage
// read from the pin and to trigger the desired actions only when it changes.

// Input pins need a third argument correponding to the edge of the signal
var input1 = new Gpio(21, 'in', 'rising'), output2 = new Gpio(23, 'out');
var valuePrev = -1;
input1.watch( function (err, value) {
	if(err){
		throw err;
	}
	
	if(value != valuePrev){
		valuePrev = value;	

		// Turning of/off a LED when another voltage is read in the input pin
		output2.write(value, function(){
			console.log('Output2 changed to: ' + value);
		});
		
		// Creating new object
		var inputGPIO = new InputGPIO();
		
		inputGPIO.set("type", "interrupt");
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
	output1.writeSync(0);
	output2.writeSync(0);
	
	ledPower.unexport();
	output1.unexport();
	output2.unexport();
	
	console.log('Bye, bye! Turning off LEDs');
	process.exit();
});



