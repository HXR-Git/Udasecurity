package com.udacity.catpoint.security.app;

import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.Color;
import java.io.Serial;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ControlPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    private final SecurityService securityService;
    private final Map<ArmingStatus, JButton> buttonMap;

    public ControlPanel(SecurityService securityService) {
        super();
        setLayout(new MigLayout());
        this.securityService = securityService;

        JLabel panelLabel = new JLabel("System Control");
        panelLabel.setFont(StyleService.HEADING_FONT);
        add(panelLabel, "span 3, wrap");

        buttonMap = Arrays.stream(ArmingStatus.values())
                .collect(Collectors.toMap(status -> status, status -> new JButton(status.getDescription())));

        buttonMap.forEach((status, button) -> button.addActionListener(e -> handleArmingStatus(status)));

        Arrays.stream(ArmingStatus.values()).forEach(status -> add(buttonMap.get(status)));

        ArmingStatus currentStatus = securityService.getArmingStatus();
        JButton currentButton = buttonMap.get(currentStatus);
        if (currentButton != null) {
            currentButton.setBackground(currentStatus.getColor());
        }
    }

    private void handleArmingStatus(ArmingStatus status) {
        securityService.setArmingStatus(status);
        updateButtonColors(status);
    }

    private void updateButtonColors(ArmingStatus selectedStatus) {
        buttonMap.forEach((s, b) -> b.setBackground(s == selectedStatus ? s.getColor() : null));
    }
}