module top.alazeprt.plugifycraft.plugifycraft {
    requires javafx.controls;
    requires javafx.fxml;
    requires PlugifyCraftLib;
    requires com.jfoenix;
    requires javafx.web;
    requires java.desktop;
    requires java.security.jgss;
    requires com.google.gson;
    requires one.jpro.platform.mdfx;
    requires flexmark.html2md.converter;
    requires flexmark.util.data;
    requires flexmark;


    opens top.alazeprt.plugifycraft to javafx.fxml;
    exports top.alazeprt.plugifycraft;
}