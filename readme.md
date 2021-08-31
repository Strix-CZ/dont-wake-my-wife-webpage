# Don't wake my wife

This repo contains a webserver and client that will be
used to control a WiFi alarm clock. The alarm clock
will have a very minimalistic user interface. The
webpage will be the place to set time of alarm. In the
future, there might be an app talking to the server
as well.

The actual physical device doing the waking-up will
check the server to download the time of the alarm.
Also, the device will send its battery level, so the
user can get notified when the battery is too low.

### Usage
```
cd server/target

# Start server.
java -jar server-dont-wake-my-wife.jar server localhost 9087

# Add new user.
java -jar server-dont-wake-my-wife.jar addUser john@example.com

# Add new device that is registered to an existing user.
java -jar server-dont-wake-my-wife.jar addDevice john@example.com

# Run the client on a development server locally.
cd client
elm reactor
elm make src/Main.elm
```

### Current assumptions

- 1 user = 1 device* = 1 time of alarm
- Recurrence of alarm = every day
  
* A user can have multiple devices, but which will be
  used for Alarm handlers is non-deterministic.

### TODOs

- Method to estimate battery percentage from check-ins
  https://lygte-info.dk/info/BatteryChargePercent%20UK.html
- Handle to get battery level
- Allow users to change and recover passwords.
- Extract recurrence of alarms (EveryDayRecurrence, SingleOcurrence, WeeklyRecurrence)

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
