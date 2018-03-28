# What is Project Falx?
Falx analyzes battery usage by apps to uncover battery related problems.

It is not very straight forward to find the cause of excessive battery drain after it happened. It requires using a post-processing tool for the logs [Battery Historian](https://github.com/google/battery-historian). Though, these tools give insightful information, it is time consuming to analyze them, and logs need to be collected by acquiring the user's device, and logs often overflow losing vital information.

We wanted to create a tool that can be integrated with apps to collect data over a period of time that gives us information about battery usage. The tool monitors a set of events and metrices that correlate strongly with battery use.


# Getting Started
## Download
Get the library from [jitpack] (https://jitpack.io/#life360/android-falx/0.2.6)

[![](https://jitpack.io/v/life360/android-falx.svg)](https://jitpack.io/#life360/android-falx)


Android SDK requirement: Mininum Api level 15.

## Adding Monitors
Falx is used by adding one or more Monitors to your project.
You can add a monitor like this:

```java
FalxApi.getInstance(context).addMonitors(FalxApi.MONITOR_APP_STATE | FalxApi.MONITOR_NETWORK);
```

Monitors can be added any time, but events shall be recorded only if a monitor is added.

Each monitor have some extra requirements to make them work.

Foreground monitor:

```java
// Call in every Activity.onStart()
FalxApi.getInstance(activity).startSession(activity);

// Call in every Activity.onStop()
FalxApi.getInstance(activity).endSession(activity);
```

Network monitor:

```java
// Add this interceptor with a OkHttpClient:
// OkHttpClient.Builder okHttpClientBuilder
okHttpClientBuilder.addNetworkInterceptor(FalxApi.getInstance(context).getInterceptor());
```

Add a On/Off monitor:

```java
falxApi = FalxApi.getInstance(context1);

// Call this when a event starts: (e.g. Gps turned ON)
falxApi.addOnOffMonitor("YOUR_CUSTOM_MONITOR_ID, Constants.FALX_EVENT_ACTIVITY_DETECTION_ON);

// Call this when a event starts: (e.g. Gps turned OFF)
falxApi.addOnOffMonitor("YOUR_CUSTOM_MONITOR_ID, Constants.FALX_EVENT_ACTIVITY_DETECTION_OFF);
```

## Getting stats

```java
List<AggregatedFalxMonitorEvent> aggregatedFalxMonitorEvents = FalxApi.getInstance(this).allAggregatedEvents(true);

if (aggregatedFalxMonitorEvents != null) {
   for (AggregatedFalxMonitorEvent event : aggregatedFalxMonitorEvents) {
       // Event names are defined in: FalxConstants.java
       final String eventName = event.getName();
       
       // Get the key, value pairs for this event
       JSONObject paramsObj = event.getParamsAsJson();   
       
       // .. Use the event and parameters
   }
}
```

## Getting Help

Send a mail to this group for help or if you have questions: falx@life360.com

## Contributing to the project
Please see [contributing guidelines](https://github.com/life360/android-falx/blob/master/CONTRIBUTING.md)


## Project Maintainers
Primary maintainer: [skremon](https://github.com/skremon) remon@life360.com 

Additional maintainers: [vikassable](https://github.com/vikassable) vikas@life360.com 

Primary maintainer will be an employee of Life360 Inc. and there can be 1+ additional maintainers.
