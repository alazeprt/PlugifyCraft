module top.alazeprt.plugifycraft.plugifycraft {
    requires javafx.controls;
    requires javafx.fxml;
    requires PlugifyCraftLib;
    requires com.jfoenix;
    requires javafx.web;
    requires java.desktop;


    opens top.alazeprt.plugifycraft to javafx.fxml;
    exports top.alazeprt.plugifycraft;
}