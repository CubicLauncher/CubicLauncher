package com.cubiclauncher.launcher;

import com.cubiclauncher.launcher.ui.Main;
import javafx.application.Application;
import javafx.application.Platform;

public class Launcher {
    public static void main(String[] args) {
        // Lanzamos la nueva clase de UI que crearemos
        Application.launch(Main.class, args);
    }
}
