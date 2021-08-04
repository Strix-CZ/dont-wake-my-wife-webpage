# Don't wake my wife

This repo contains a server for a gentle alarm device.
Users will set the time they would like to be woken
up using a webpage or possibly in the future in
an app that would talk to the server.

The actual physical device doing the waking-up will
check the server to download the time of the alarm.
Also, the device will send its battery level, so the
user can get notified when the battery is too low.

### Usage
```
cd server/target
java -jar server-dont-wake-my-wife.jar server localhost 9087
java -jar server-dont-wake-my-wife.jar addUser john@example.com
java -jar server-dont-wake-my-wife.jar addDevice john@example.com

cd client
elm reactor
elm make src/Main.elm
```

### Current assumptions

- 1 user = 1 device* = 1 time of alarm
  
* A user can have multiple devices, but which will be
  used for Alarm handlers is non-deterministic.

### TODOs

- Method to estimate battery percentage from check-ins
- Handle to get battery level
- Allow users to change and recover passwords.

### Ideas

- 3rd party authorization
- E-mail notification when battery goes low
- Multiple alarm times
- Scenario configuration. Example:
    - If I clear the alarm, I need to confirm it in my
      phone within 10 minutes. If I fail to do that,
      the alarm will start again.
    - If I don't clear the alarm, call my on my phone.  
    - If I don't respond, call my wife on my phone.
    - If she doesn't respond, give up.
- Planned gradual adjustment to a different time-zone
  or when switching to/from daylight savings time.
- Offer the gradual adjustment in e-mail to users that
  are going to experience the switch soon.
