# MS_Plugin
Plugin for MS for Phoinegap Build

This repository is exclusively the preoperty of Toby Hart T/S TaM Solutions

### To call a function in javascript you use
window.plugins.msplugin.

### followed by one of the following funcitons:

  - scan(successCallback, errorCallback)

  - stopscanning(successCallback, errorCallback)

  - connect(successCallback, errorCallback, macAddress)

  - subscribe(successCallback, errorCallback, args)
  - WHERE ARGS IS AN ARRAY LIKE THIS ["suunto://MDS/EventListener", "{\"Uri\": \"" + VAR1 + "/" + VAR2 + "\"}"] where var1 is the serial of the device and var2 is the special subscription code (i.e. Meas/Acc/13)

  - unsubscribe(successCallback, errorCallback)

### Subscription Codes (to be put where VAR2 is in the example subscribe function above):
  - Meas/Acc/13 = Accelerometer

  - Meas/Gyro/13 = Gyroscope
  
  - Meas/Magn/13 = Magnometer
  
  - Meas/Temperature = what you think it is
  
  - Meas/HR = heart rate
  
  - Meas/ECG = ECG which i think is something to do with heartrate
  
  *ALL OF THE ABOVE NEED A SAMPLE RATE ADDED AT THE END SUCH AS /13*
  

