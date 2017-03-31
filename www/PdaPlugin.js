var exec = require('cordova/exec');

var pda = {
    m1:function(success, error) {
    	exec(success, error, "Pda", "m1", []);
	},
   	cpu:function(success, error) {
    	exec(success, error, "Pda", "cpu", []);
	},
	uhf:function(success, error) {
    	exec(success, error, "Pda", "uhf", []);
	},
	stopuhf:function(success, error) {
    	exec(success, error, "Pda", "stopuhf", []);
	} 
};

module.exports = pda;
