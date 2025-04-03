module top.alazeprt.plugifycraft.plugifycraft {
    requires javafx.controls;
    requires javafx.fxml;
    requires PlugifyCraftLib;
    requires com.jfoenix;
    requires javafx.web;
    requires java.desktop;
    requires java.security.jgss;
    requires com.google.gson;


    opens top.alazeprt.plugifycraft to javafx.fxml;
    exports top.alazeprt.plugifycraft;
}