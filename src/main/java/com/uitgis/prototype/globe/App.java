package com.uitgis.prototype.globe;

import com.sun.javafx.application.LauncherImpl;

@SuppressWarnings("restriction")
public class App {
	public static void main(String[] args) {
		LauncherImpl.launchApplication(MapControl3D.class, SplashScreen.class, args);
	}
}
