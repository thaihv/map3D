package com.uitgis.prototype.globe;

import com.airhacks.afterburner.injection.Injector;
import com.uitgis.prototype.globe.application.ApplicationView;
import com.uitgis.prototype.globe.util.Session;
import com.uitgis.prototype.globe.util.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader.StateChangeNotification;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MapControl3D extends Application {

	public static final String APPLICATION_TITLE = "Worldwind with Javafx";

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {

			SessionManager.getInstance().addSession(new Session(MapControl3D.APPLICATION_TITLE));
			ApplicationView applicationView = new ApplicationView();
			Scene scene = new Scene(applicationView.getView(),800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Prototype Globe for 3D Visualization");
			primaryStage.show();
			this.notifyPreloader(new StateChangeNotification(null));
			primaryStage.setOnCloseRequest(e -> {
				Platform.exit();
				System.exit(0);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void stop() throws Exception {
		super.stop();
		Injector.forgetAll();
	}


	public static void main(String[] args) {
		launch(args);
	}
}