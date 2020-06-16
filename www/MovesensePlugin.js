//var exec = require('cordova/exec');

function MSPlugin() {}

MSPlugin.prototype.scan = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'MovesensePlugin', 'scan');
};

MSPlugin.prototype.stopscanning = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'MovesensePlugin', 'stopscan');
 };

MSPlugin.prototype.connect = function(successCallback, errorCallback, macAddress) {
  cordova.exec(successCallback, errorCallback, 'MovesensePlugin', 'connect', [macAddress]);
};

/*

MSPlugin.prototype.getData = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'MovesensePlugin', 'scan');
};

*/

MSPlugin.prototype.subscribe = function(successCallback, errorCallback, args) {
  cordova.exec(successCallback, errorCallback, 'MovesensePlugin', 'subscribe', args);
};

MSPlugin.prototype.unsubscribe = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'MovesensePlugin', 'unsubscribe');
};



/*
MSPlugin.prototype.stopscanning = function (successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "Insomnia", "keepAwake", []);

  cordova.exec(
	  (device) => { this.log(scan stopped); this.scanRunning(false)},
	  (message) => { this.log(Error during stopscan: ${message})},
	  'MovesensePlugin', 'stopscan'
);

};

MSPlugin.prototype.connect = function (successCallback, errorCallback) {
  //cordova.exec(successCallback, errorCallback, "Insomnia", "keepAwake", []);
  cordova.exec(
	  ({type, mac, serial}) => {
		  this.log(connection: ${type} ${mac} ${serial})
		  if (type === "connectComplete") {
  			this.connected = {mac, serial};
  		}
  	},
  	(message) => {
  		this.log(Error during connect: ${message})
  	},
  	'MovesensePlugin', 'connect',
  	[device.mac]
  );
};

MSPlugin.prototype.getData = function (successCallback, errorCallback) {
  //cordova.disconnect(successCallback, errorCallback, "Insomnia", "allowSleepAgain", []);
  cordova.exec(
  	(data) => {
  		this.log(data: ${data})
  	},
  	(message) => {
  		this.log(Error during get: ${message})
  	},
  	'MovesensePlugin', 'get',
  	[suunto://${this.connected.serial}/Info]
  );
};

MSPlugin.prototype.subscribe = function (successCallback, errorCallback) {
  //cordova.disconnect(successCallback, errorCallback, "Insomnia", "allowSleepAgain", []);
  cordova.exec(
  	(data) => {
  		this.log(data: ${data})
  	},
  	(message) => {
  		this.log(Error during subscribe: ${message})
  	},
  	'MovesensePlugin', 'subscribe',
  	["suunto://MDS/EventListener", JSON.stringify({Uri: ${this.connected.serial}/Meas/Acc/13}), this.subscriptionId]
 );
};

MSPlugin.prototype.unsubscribe = function (successCallback, errorCallback) {
  //cordova.disconnect(successCallback, errorCallback, "Insomnia", "allowSleepAgain", []);
  cordova.exec(
  	(data) => {
  		this.log(unsubscribed: ${data})
  	},
  	(message) => {
  		this.log(Error during unsubscribe: ${message})
  	},
  	'MovesensePlugin', 'unsubscribe',
  	[this.subscriptionId]
  );
};

*/

MSPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.msplugin = new MSPlugin();
  return window.plugins.msplugin;
};

cordova.addConstructor(MSPlugin.install);
