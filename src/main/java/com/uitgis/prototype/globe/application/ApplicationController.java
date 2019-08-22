
package com.uitgis.prototype.globe.application;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
				System.out.println(ww.getView().getCenterPoint());
				System.out.println(ww.getView().getEyePosition());
				System.out.println(ww.getView().getViewport());
				ww.redraw();
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
}
