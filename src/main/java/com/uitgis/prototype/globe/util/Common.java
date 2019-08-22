package com.uitgis.prototype.globe.util;

import java.io.File;

import javax.swing.SwingUtilities;

import com.uitgis.prototype.globe.MapControl3D;
import com.uitgis.prototype.globe.RandomShapeAttributes;

import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import javafx.scene.Node;
import javafx.stage.FileChooser;

public final class Common {
	public static File selectShpFileLayer(Node source) {
		File recordsDir = new File(System.getProperty("user.home"), ".myglobe3D/shp");
		if (! recordsDir.exists()) {
		    recordsDir.mkdirs();
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open SHP file");
		fileChooser.setInitialDirectory(recordsDir);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Shape file", "*.shp"));
		File file = fileChooser.showOpenDialog(source.getScene().getWindow());

		if (file != null) {
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
			factory.createFromShapefileSource(file, new ShapefileLayerFactory.CompletionCallback() {
				@Override
				public void completion(Object result) {
					final Layer layer = (Layer) result; // the result is the layer the factory created
					layer.setName(WWIO.getFilename(layer.getName()));
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
							String layerName = session.getLayers().stream().anyMatch(l -> l.getName().equals(layer.getName())) == false ? layer.getName() : layer.getName() + "_1";
							layer.setName(layerName);
							session.addLayer(layer);
							System.out.println("SHP added: " + layer.getName());
						}
					});
				}

				@Override
				public void exception(Exception e) {
					Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
				}
			});
		}
		return file;
	}

}