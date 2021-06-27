# Don't wake my wife

This repo contains a server for a gentle alarm device.
Users will set the time they would like to be woken
up using a webpage or possibly in the future in
an app that would talk to the server.

The actual physical device doing the waking-up will
check the server to download the time of the alarm.
Also, the device will send its battery level, so the
user can get notified when the battery is too low.

### Current assumptions

- 1 user = 1 device = 1 time of alarm
- 1 hard-coded user

### TODOs

- Handler to get the time of alarm
- Handler to set the time of alarm
- Authentication method for 1 hard-coded user  
- Method to estimate battery percentage from check-ins
- Handle to get battery level
- Add support for multiple users, 3rd-party based authentication?
- Admin interface to add a user and device (command line?)
- Way how to execute the server (main method)
- Deployment

### Ideas

- E-mail notification when battery goes low
- Multiple alarm times
- Scenario configuration. Example:
    - If I clear the alarm, I need to confirm it in my
      phone within 10 minutes. If I fail to do that,
      the alarm will start again.
    - If I don't clear the alarm, call my on my phone.  
    - If I don't respond, call my wife on my phone.
    - If she doesn't respond, give up.
    