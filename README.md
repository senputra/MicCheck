Mic-Test

An application that forwards voice captured by the microphone of an Android phone and forwards it to a computer running Java application.

Requirements:
  Java JRE (On Computer)
  Android (On Phone)
 
Usage:
  1. Run the Java Application on the laptop.
        Go in to the "server File" folder and
        By typing "java micServer" in console
  2. Check the Ip address of the computer.
        By typing "ip address" on Linux 
        and "ifconfig -all" on Windows 
  3. On the Android, enable the permission to install from unknown source
  4. Put in the value of the IP Address of your computer. [E.g. 192.168.0.12 or 10.42.0.10]
  5. Then have fun!
  
  
The purpose of this application:
  It is to replace physical microphones that need tidious setups and prone to failure due to running out of battery in schools and universities. By using this app, teachers, lecturers and speakers only need to connect their laptop to the projector which usually comes with the sound system and they are on the go. 
  
Limitations:
  1. The time lag that is quite observable between the input and output
     UDP packets may need some adjustments
     And I may also need to use Native Code (AAudio or OpenCL) instead of relying on the premade Java APIs from android.
     
  2. UI is currently not as pleasant.
