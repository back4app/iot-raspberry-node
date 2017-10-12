var onoff = require('onoff');
var Gpio = onoff.Gpio, ledPower = new Gpio(4, 'out'), led1 = new Gpio(17, 'out');

ledPower.write(1,function(){
	console.log('Application is ON');
});


let Parse = require('parse/node');

//Parse.initialize("4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg");
//Parse.serverURL = 'https://parseapi.back4app.com/' ;

let LiveQueryClient = Parse.LiveQueryClient;
let client = new LiveQueryClient({
	applicationId: '4OU4UVDyP0nZj7CJOB6AlAaZNbpMYLB4rbFm2LOg',
	serverURL: 'wss:hellonewworld.back4app.io/',
	javascriptKey: '',
	masterKey: ''
});

//There is an error: "You need to set a proper Parse LiveQuery server url
//before using LiveQueryClient"
//What to put on serverURL? https://parseapi.back4app.com
// wss:hellonewworld.back4app.io (how I set up subdomain on LiveQuery dashboard,
// it is also this way on my .java app.

console.log("pre client.open");
client.open();
console.log("post client.open");

let query = new Parse.Query("Message"); // I put here the object created by poke
let subscription = client.subscribe(query);

console.log("post subscription");
var count = 0;
subscription.on('create', (object) => {
	count = (count + 1) % 2;
	
	if(object.get("content") == 'off') count = 0;	
	if(object.get("content") == 'on') count = 1;
	
	
	led1.write(count, function(){ 
		console.log('LED status changed to ' + count); 
	});
	console.log('Message object created.');
	console.log(object.id);
	console.log(object.get("content"));
	
});

process.on('SIGINT', function (){
	ledPower.writeSync(0);
	led1.writeSync(0);
	ledPower.unexport();
	led1.unexport();
	console.log('Bye, bye! Turning off LEDs');
	process.exit();
});

