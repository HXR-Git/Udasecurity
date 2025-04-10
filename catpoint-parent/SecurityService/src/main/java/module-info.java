module SecurityService {
    requires ImageService;
    requires transitive com.google.gson;
    requires transitive dev.mccue.guava.collect;
    requires transitive dev.mccue.guava.reflect;
    requires com.miglayout.swing;
    requires java.desktop;
    requires java.prefs;
    requires com.google.common;

    opens com.udacity.catpoint.security.data;
    opens com.udacity.catpoint.security.service;
}