
package com.uitgis.prototype.globe.application;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import com.uitgis.prototype.globe.layer.LayerView;
import com.uitgis.prototype.globe.util.Common;
import com.uitgis.prototype.globe.world.WorldController;
import com.uitgis.prototype.globe.world.WorldMode;
import com.uitgis.prototype.globe.world.WorldModel;
import com.uitgis.prototype.globe.world.WorldView;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Position;
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
	
	private StringProperty     altitude   = new SimpleStringProperty();
	private StringProperty       latitude = new SimpleStringProperty();
	private StringProperty      longitude = new SimpleStringProperty();
	private StringProperty      elevation = new SimpleStringProperty();
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

	public void fullExtent() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WorldWindow ww = getCurrentWwd();
				Position oriPosition =  Position.fromDegrees(20.268460793018612, 103.52821602365239);
				System.out.println(ww.getView().getCenterPoint());
				System.out.println(ww.getView().getEyePosition());
				System.out.println(ww.getView().getGlobe().getExtent());
				double elv = ww.getView().getGlobe().getElevation(oriPosition.getLatitude(), oriPosition.getLongitude());
				ww.getView().goTo(oriPosition, elv);
			}
		});
	}

	public void openShpFile() {
		worldModel.setMode(WorldMode.LOADING);
		if (Common.selectShpFileLayer(layerPane) == null) {
			worldModel.setMode(WorldMode.VIEW);
		}
		;
	}
	private class WorldPositionListener implements PositionListener{

		@Override
		public void moved(PositionEvent event) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Position clickedPosition = getCurrentWwd().getCurrentPosition();
					if (null != clickedPosition) {
						DecimalFormat formatter = new DecimalFormat("#,###");
						
						String altVal = String.format("%.0f", getCurrentWwd().getView().getEyePosition().getAltitude() / 1000);  //?? Wrong
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
}
