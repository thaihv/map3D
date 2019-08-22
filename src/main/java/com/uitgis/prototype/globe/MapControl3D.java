package com.uitgis.prototype.globe;

import javax.swing.SwingUtilities;

import com.airhacks.afterburner.injection.Injector;
import com.uitgis.prototype.globe.application.ApplicationView;
import com.uitgis.prototype.globe.util.Session;
import com.uitgis.prototype.globe.util.SessionManager;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader.StateChangeNotification;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MapControl3D extends Application {

	public static final String APPLICATION_TITLE = "Worldwind with Javafx";

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {

			SessionManager.getInstance().addSession(new Session(MapControl3D.APPLICATION_TITLE));
			ApplicationView applicationView = new ApplicationView();
			Scene scene = new Scene(applicationView.getView());//,800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			StackPane sp = new StackPane();
//			sp.getChildren().add(buildWW());
//			primaryStage.setScene(new Scene(sp, 800, 600));	
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

	private SwingNode buildWW() {
		SwingNode node = new SwingNode();

		SwingUtilities.invokeLater(() -> {
			WorldWindowGLJPanel wwj = new WorldWindowGLJPanel();

			Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);

			wwj.setModel(m);

//            wwj.setModel(new BasicModel());
			LayerList layers = m.getLayers();
			for (Layer l : layers) {

				if (l.isEnabled() == false)
					layers.remove(l);
				if (l instanceof NASAWFSPlaceNameLayer || l instanceof ScalebarLayer || l instanceof CompassLayer
						|| l instanceof WorldMapLayer // || l instanceof StarsLayer || l instanceof SkyGradientLayer
						|| l instanceof LandsatI3WMSLayer || l instanceof BMNGOneImage)
					layers.remove(l);
//                if (!(l instanceof LandsatI3WMSLayer || l instanceof BMNGOneImage || l instanceof BMNGWMSLayer))
//                	layers.remove(l);
			}
			ShapefileLayerFactory factory = new ShapefileLayerFactory();
			final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();
			factory.setAttributeDelegate(new ShapefileRenderable.AttributeDelegate() {
				@Override
				public void assignAttributes(ShapefileRecord shapefileRecord,
						ShapefileRenderable.Record renderableRecord) {
					renderableRecord.setAttributes(randomAttrs.nextAttributes().asShapeAttributes());
				}
			});
			// Load the shapefile. Define the completion callback.
			factory.createFromShapefileSource("C://data//3dModels//data//shp//TUSON_TIN.shp",
					new ShapefileLayerFactory.CompletionCallback() {
						@Override
						public void completion(Object result) {
							final Layer layer = (Layer) result; // the result is the layer the factory created
							layer.setName(WWIO.getFilename(layer.getName()));

							// Add the layer to the World Window's layer list on the Event Dispatch Thread.
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									wwj.getModel().getLayers().add(layer);
									for (Layer l : wwj.getModel().getLayers()) {
										System.out.println(l.getName() + " Enabled: " + l.isEnabled());
									}
								}
							});
						}

						@Override
						public void exception(Exception e) {
							Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
						}
					});
			factory.createFromShapefileSource("C://data//3dModels//data//shp//TUSON_BUILDINGS_Z.shp",
					new ShapefileLayerFactory.CompletionCallback() {
						@Override
						public void completion(Object result) {
							final Layer layer = (Layer) result; // the result is the layer the factory created
							layer.setName(WWIO.getFilename(layer.getName()));

							// Add the layer to the World Window's layer list on the Event Dispatch Thread.
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									wwj.getModel().getLayers().add(layer);
									for (Layer l : wwj.getModel().getLayers()) {
										System.out.println(l.getName() + " Enabled: " + l.isEnabled());
									}
								}
							});
						}

						@Override
						public void exception(Exception e) {
							Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
						}
					});

			node.setContent(wwj);

		});

		return node;
	}

	public static void main(String[] args) {
		launch(args);
	}
}