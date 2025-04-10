package com.udacity.catpoint.security.app;

import com.udacity.catpoint.security.data.AlarmStatus;

public interface StatusListener {
    void notify(AlarmStatus status);
    void catDetected(boolean catDetected);
    void sensorStatusChanged();
}
