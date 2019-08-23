
package com.uitgis.prototype.globe.application;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import com.uitgis.prototype.globe.MapControl3D;
import com.uitgis.prototype.globe.layer.LayerView;
import com.uitgis.prototype.globe.util.Common;
import com.uitgis.prototype.globe.util.Session;
import com.uitgis.prototype.globe.util.SessionManager;
import com.uitgis.prototype.globe.world.WorldController;
import com.uitgis.prototype.globe.world.WorldMode;
import com.uitgis.prototype.globe.world.WorldModel;
import com.uitgis.prototype.globe.world.WorldView;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.render.airspaces.Cake;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Orbit;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.PolyArc;
import gov.nasa.worldwind.render.airspaces.Route;
import gov.nasa.worldwind.render.airspaces.TrackAirspace;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class ApplicationController implements Initializable {

	@FXML
	private AnchorPane layerPane;

	@FXML
	private AnchorPane worldPane;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	TextField tfAltitude, tfLat, tfLon, tfElev;

	@FXML
	Label lblStatus;

	@Inject
	private WorldModel worldModel;

	@Inject
	private String aboutTitle;

	@Inject
	private String aboutHeader;

	@Inject
	private String aboutContent;

	private WorldView worldView = new WorldView();

	private LayerView layerView = new LayerView();

	protected RandomShapeAttributes randomAttrs = new RandomShapeAttributes();
	
	private StringProperty altitude = new SimpleStringProperty();
	private StringProperty latitude = new SimpleStringProperty();
	private StringProperty longitude = new SimpleStringProperty();
	private StringProperty elevation = new SimpleStringProperty();
	private StringProperty downloadStatus = new SimpleStringProperty();

	public StringProperty altitudeProperty() {
		return this.altitude;
	}

	public String getAltitude() {
		return this.altitudeProperty().get();
	}

	public void setAltitude(final String altitude) {
		this.altitudeProperty().set(altitude);
	}

	public StringProperty latitudeProperty() {
		return this.latitude;
	}

	public String getLatitude() {
		return this.latitudeProperty().get();
	}

	public void setLatitude(final String latitude) {
		this.latitudeProperty().set(latitude);
	}

	public StringProperty longitudeProperty() {
		return this.longitude;
	}

	public String getLongitude() {
		return this.longitudeProperty().get();
	}

	public void setLongitude(final String longitude) {
		this.longitudeProperty().set(longitude);
	}

	public StringProperty elevationProperty() {
		return this.elevation;
	}

	public String getElevation() {
		return this.elevationProperty().get();
	}

	public void setElevation(final String elevation) {
		this.elevationProperty().set(elevation);
	}

	public StringProperty downloadStatusProperty() {
		return this.downloadStatus;
	}

	public String getDownloadStatus() {
		return this.downloadStatusProperty().get();
	}

	public void setDownloadStatus(final String downloadStatus) {
		this.downloadStatusProperty().set(downloadStatus);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.worldPane.getChildren().add(worldView.getView());
		AnchorPane.setTopAnchor(worldView.getView(), 0d);
		AnchorPane.setLeftAnchor(worldView.getView(), 0d);
		AnchorPane.setRightAnchor(worldView.getView(), 0d);
		AnchorPane.setBottomAnchor(worldView.getView(), 0d);

		this.layerPane.getChildren().add(layerView.getView());
		AnchorPane.setTopAnchor(layerView.getView(), 0d);
		AnchorPane.setLeftAnchor(layerView.getView(), 0d);
		AnchorPane.setRightAnchor(layerView.getView(), 0d);
		AnchorPane.setBottomAnchor(layerView.getView(), 0d);

		this.progressIndicator.setVisible(false);
		this.worldModel.addModeChangeListener(new ModeChangeListener());
		this.getCurrentWwd().addPositionListener(new WorldPositionListener());

		tfAltitude.textProperty().bindBidirectional(altitudeProperty());
		tfLat.textProperty().bindBidirectional(latitudeProperty());
		tfLon.textProperty().bindBidirectional(longitudeProperty());
		tfElev.textProperty().bindBidirectional(elevationProperty());
		lblStatus.textProperty().bindBidirectional(downloadStatusProperty());

	}

	public void exit() {
		Platform.exit();
		System.exit(0);
	}

	public void about() {
		ApplicationAlert about = new ApplicationAlert(AlertType.INFORMATION);
		about.setTitle(this.aboutTitle);
		about.setHeaderText(this.aboutHeader);
		about.setContentText(this.aboutContent);
		about.showAndWait();
	}

	private class ModeChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					switch (worldModel.getMode()) {
					case EDIT:
					case LOADING:
						progressBar.setProgress(-1d);
						progressIndicator.toFront();
						progressIndicator.setVisible(true);
						progressIndicator.setProgress(-1d);
						break;
					default:
						progressBar.setProgress(0d);
						progressIndicator.toBack();
						progressIndicator.setVisible(false);
						progressIndicator.setProgress(0d);
					}
				}
			});
		}
	}

	private WorldWindow getCurrentWwd() {
		WorldController wwcontroller = (WorldController) worldView.getPresenter();
		return wwcontroller.getWwd();
	};

	private ScreenSelector getScreenSelector() {
		WorldController wwcontroller = (WorldController) worldView.getPresenter();
		return wwcontroller.getScreenSelector();
	};

	public void fullExtent() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldWindow ww = getCurrentWwd();
				Position oriPosition = Position.fromDegrees(20.268460793018612, 103.52821602365239);
				System.out.println(ww.getView().getCenterPoint());
				System.out.println(ww.getView().getEyePosition());
				System.out.println(ww.getView().getGlobe().getExtent());
				double elv = ww.getView().getGlobe().getElevation(oriPosition.getLatitude(),
						oriPosition.getLongitude());
				ww.getView().goTo(oriPosition, elv);
			}
		});
	}

	public void zoomIn() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldWindow ww = getCurrentWwd();
				View view = ww.getView();
				if (view.isAnimating()) {
					view.stopAnimations();
				}
				Position pos = view.getEyePosition();
				System.out.println(pos);
				Position tmpPos = Position.fromDegrees(pos.getLatitude().degrees, pos.getLongitude().degrees,
						pos.getElevation() - 100);
				System.out.println(tmpPos);
				view.setEyePosition(tmpPos);

			}
		});
	}

	public void zoomOut() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldWindow ww = getCurrentWwd();
				View view = ww.getView();
				if (view.isAnimating()) {
					view.stopAnimations();
				}
				Position pos = view.getEyePosition();
				System.out.println(pos);
				Position tmpPos = Position.fromRadians(pos.getLatitude().radians, pos.getLongitude().radians,
						pos.getElevation() + 100);
				view.setEyePosition(tmpPos);

			}
		});
	}

	public void selectElement() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				if (worldModel.getMode().equals(WorldMode.SELECT)) {
					worldModel.setMode(WorldMode.VIEW);
					getScreenSelector().disable();
					
				} else {
					worldModel.setMode(WorldMode.SELECT);
					getScreenSelector().enable();
				}
			}
		});

	}

	public void editElement() {
		insertBeforePlaceNameLayer(getCurrentWwd(), makeCustomElements());
	}

	public void openShpFile() {
		worldModel.setMode(WorldMode.LOADING);
		if (Common.selectShpFileLayer(layerPane) == null) {
			worldModel.setMode(WorldMode.VIEW);
		};
	}

	private class WorldPositionListener implements PositionListener {

		@Override
		public void moved(PositionEvent event) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Position clickedPosition = getCurrentWwd().getCurrentPosition();
					if (null != clickedPosition) {
						DecimalFormat formatter = new DecimalFormat("#,###");

						String altVal = String.format("%.0f",
								getCurrentWwd().getView().getEyePosition().getAltitude() / 1000); // ?? Wrong
						String latVal = String.format("%.4f", clickedPosition.getLatitude().degrees);
						String lonVal = String.format("%.4f", clickedPosition.getLongitude().degrees);
						String eleVal = String.format("%.0f", clickedPosition.getElevation());

						altitude.setValue(formatter.format(Double.parseDouble(altVal)) + " km");
						latitude.setValue(latVal + "\u00B0");
						longitude.setValue(lonVal + "\u00B0");
						elevation.setValue(formatter.format(Double.parseDouble(eleVal)) + " meters");
					}
				}
			});

		}
	}
	public static void insertBeforePlaceNameLayer(WorldWindow wwd, Layer layer) {
		int compassPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers) {
			if (l instanceof PlaceNameLayer)
				compassPosition = layers.indexOf(l);
		}
		layers.add(compassPosition, layer);
	}
	protected Layer makeCustomElements() {
		AirspaceAttributes attrs = randomAttrs.nextAttributes().asAirspaceAttributes();
		RenderableLayer layer = new RenderableLayer();
		layer.setName("Editable");
		
		Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
		long suffName =session.getLayers().stream().filter(l -> l.getName().contains(layer.getName())).count();
		String layerName = suffName == 0 ? layer.getName() : layer.getName() + "_" + String.valueOf(suffName);
		
		layer.setName(layerName);
		session.addLayer(layer);

		// Continent-sized cylinder.
		CappedCylinder cyl = new CappedCylinder(attrs);
		cyl.setCenter(LatLon.fromDegrees(0.0, 0.0));
		cyl.setRadii(1000000.0, 3000000.0);
		cyl.setAltitudes(100000.0, 500000.0);
		cyl.setTerrainConforming(false, false);
		cyl.setValue(AVKey.DISPLAY_NAME, "3,000km Cylinder");
		layer.addRenderable(cyl);

		PartialCappedCylinder partCyl = new PartialCappedCylinder(attrs);
		partCyl.setCenter(LatLon.fromDegrees(46.7477, -123.6372));
		partCyl.setRadii(15000.0, 30000.0);
		partCyl.setAltitudes(5000.0, 10000.0);
		partCyl.setAzimuths(Angle.fromDegrees(0.0), Angle.fromDegrees(90.0));
		partCyl.setTerrainConforming(false, false);
		partCyl.setValue(AVKey.DISPLAY_NAME, "Partial Cylinder from 0 to 90 degrees");
		layer.addRenderable(partCyl);

		// Cake
		Cake cake = new Cake(attrs);
		cake.setLayers(Arrays.asList(
				new Cake.Layer(LatLon.fromDegrees(46.7477, -121.6372), 10000.0, Angle.fromDegrees(190.0),
						Angle.fromDegrees(170.0), 10000.0, 15000.0),
				new Cake.Layer(LatLon.fromDegrees(46.7477, -121.6372), 15000.0, Angle.fromDegrees(190.0),
						Angle.fromDegrees(90.0), 16000.0, 21000.0),
				new Cake.Layer(LatLon.fromDegrees(46.7477, -121.6372), 12500.0, Angle.fromDegrees(270.0),
						Angle.fromDegrees(60.0), 22000.0, 27000.0)));
		cake.getLayers().get(0).setTerrainConforming(false, false);
		cake.getLayers().get(1).setTerrainConforming(false, false);
		cake.getLayers().get(2).setTerrainConforming(false, false);
		cake.setValue(AVKey.DISPLAY_NAME, "3 layer Cake");
		layer.addRenderable(cake);

		// Left Orbit
		Orbit orbit = new Orbit(attrs);
		orbit.setLocations(LatLon.fromDegrees(45.7477, -123.6372), LatLon.fromDegrees(45.7477, -122.6372));
		orbit.setAltitudes(10000.0, 20000.0);
		orbit.setWidth(30000.0);
		orbit.setOrbitType(Orbit.OrbitType.LEFT);
		orbit.setTerrainConforming(false, false);
		orbit.setValue(AVKey.DISPLAY_NAME, "Left Orbit");
		layer.addRenderable(orbit);

		// Right Orbit
		orbit = new Orbit(attrs);
		orbit.setLocations(LatLon.fromDegrees(45.7477, -123.6372), LatLon.fromDegrees(45.7477, -122.6372));
		orbit.setAltitudes(10000.0, 20000.0);
		orbit.setWidth(30000.0);
		orbit.setOrbitType(Orbit.OrbitType.RIGHT);
		orbit.setTerrainConforming(false, false);
		orbit.setValue(AVKey.DISPLAY_NAME, "Right Orbit");
		layer.addRenderable(orbit);

		// PolyArc
		PolyArc polyArc = new PolyArc(attrs);
		polyArc.setLocations(Arrays.asList(LatLon.fromDegrees(45.5, -122.0), LatLon.fromDegrees(46.0, -122.0),
				LatLon.fromDegrees(46.0, -121.0), LatLon.fromDegrees(45.5, -121.0)));
		polyArc.setAltitudes(5000.0, 10000.0);
		polyArc.setRadius(30000.0);
		polyArc.setAzimuths(Angle.fromDegrees(-45.0), Angle.fromDegrees(135.0));
		polyArc.setTerrainConforming(false, false);
		polyArc.setValue(AVKey.DISPLAY_NAME, "PolyArc with 30km radius from -45 to 135 degrees");
		layer.addRenderable(polyArc);

		// Route
		Route route = new Route(attrs);
		route.setAltitudes(5000.0, 20000.0);
		route.setWidth(20000.0);
		route.setLocations(Arrays.asList(LatLon.fromDegrees(43.0, -121.0), LatLon.fromDegrees(44.0, -121.0),
				LatLon.fromDegrees(44.0, -120.0), LatLon.fromDegrees(43.0, -120.0)));
		route.setTerrainConforming(false, false);
		route.setValue(AVKey.DISPLAY_NAME, "Route");
		layer.addRenderable(route);

		// Track
		TrackAirspace track = new TrackAirspace(attrs);
		track.setEnableInnerCaps(false);
		track.setEnableCenterLine(true);
		track.setValue(AVKey.DISPLAY_NAME, "Semi-connected Track");
		double leftWidth = 100000d;
		double rightWidth = 100000d;
		double minAlt = 150000d;
		double maxAlt = 250000d;
		Box leg;
		track.addLeg(LatLon.fromDegrees(40.4705, -117.9242), LatLon.fromDegrees(42.6139, -108.3518), minAlt, maxAlt,
				leftWidth, rightWidth);
		leg = track.addLeg(LatLon.fromDegrees(42.6139, -108.3518), LatLon.fromDegrees(44.9305, -97.6665), minAlt / 2,
				maxAlt / 2, leftWidth, rightWidth);
		leg.setTerrainConforming(false, false);
		leg = track.addLeg(LatLon.fromDegrees(44.9305, -97.6665), LatLon.fromDegrees(47.0121, -94.9218), minAlt / 2,
				maxAlt / 2, leftWidth, rightWidth);
		leg.setTerrainConforming(false, false);
		leg = track.addLeg(LatLon.fromDegrees(47.0121, -94.9218), LatLon.fromDegrees(44.7964, -68.4230), minAlt / 4,
				maxAlt / 4, leftWidth, rightWidth);
		leg.setTerrainConforming(false, false);
		layer.addRenderable(track);
		
		return layer;
	}	
	
}
