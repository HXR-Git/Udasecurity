package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.ImageService;
import com.udacity.catpoint.security.app.StatusListener;
import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.*;

public class SecurityService {
    private final ImageService imageService;
    private final SecurityRepository securityRepository;
    private  Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetected = false;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }
    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            getSensors().forEach(sensor -> {
                sensor.setActive(false);
                securityRepository.updateSensor(sensor);
            });

            if (armingStatus == ArmingStatus.ARMED_HOME && catDetected) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        securityRepository.setArmingStatus(armingStatus);
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }
    private void catDetected(Boolean cat) {
        catDetected = cat;
        boolean anySensorsActive = getSensors().stream().anyMatch(Sensor::getActive);

        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if(!anySensorsActive) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }
    private void handleSensorActivated() {
        AlarmStatus currentStatus = securityRepository.getAlarmStatus();

        if(currentStatus == AlarmStatus.NO_ALARM) {
            setAlarmStatus(AlarmStatus.PENDING_ALARM);
        } else if(currentStatus == AlarmStatus.PENDING_ALARM) {
            // Only trigger alarm if at least one sensor remains active
            if(getSensors().stream().anyMatch(Sensor::getActive)) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
    }
    private void handleSensorDeactivated() {
        AlarmStatus currentStatus = securityRepository.getAlarmStatus();
        boolean allInactive = getSensors().stream().noneMatch(Sensor::getActive);

        if(currentStatus == AlarmStatus.PENDING_ALARM && allInactive) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        if(sensor.getActive().equals(active)) return; // No change needed

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if(active) {
            handleSensorActivated();
        } else {
            handleSensorDeactivated();
        }
    }
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}

