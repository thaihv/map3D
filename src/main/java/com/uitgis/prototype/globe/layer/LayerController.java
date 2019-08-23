
package com.uitgis.prototype.globe.layer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;

import com.uitgis.prototype.globe.MapControl3D;
import com.uitgis.prototype.globe.util.Common;
import com.uitgis.prototype.globe.util.Session;
import com.uitgis.prototype.globe.util.SessionManager;
import com.uitgis.prototype.globe.world.WorldMode;
import com.uitgis.prototype.globe.world.WorldModel;

import gov.nasa.worldwind.layers.Layer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.util.StringConverter;

public class LayerController implements Initializable {

	@FXML
	private TreeView<Layer> treeView;

	private CheckBoxTreeItem<Layer> root = new CheckBoxTreeItem<Layer>();
	private LayersChangeListener lcl = new LayersChangeListener();

	@Inject
	private WorldModel worldModel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
		session.addLayersChangeListener(lcl);
		this.initLayers();
	}

	StringConverter<TreeItem<Layer>> converter = new StringConverter<TreeItem<Layer>>() {

		@Override
		public String toString(TreeItem<Layer> object) {
			return object.getValue() != null ? object.getValue().getName() : "";
		}

		@Override
		public TreeItem<Layer> fromString(String string) {
			return null;
		}

	};

	private void initLayers() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				root.getChildren().clear();
				root.setExpanded(true);
				Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
				for (Layer l : session.getLayers()) {
					CheckBoxTreeItem<Layer> addedLayer = new CheckBoxTreeItem<Layer>(l);
					root.getChildren().add(addedLayer);
					addedLayer.setSelected(addedLayer.getValue().isEnabled());

					addedLayer.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(),
							(CheckBoxTreeItem.TreeModificationEvent<Layer> evt) -> {
								CheckBoxTreeItem<Layer> item = evt.getTreeItem();
								Set<TreeItem<Layer>> selected = new HashSet<>();
								if (evt.wasIndeterminateChanged()) {
									if (item.isIndeterminate()) {
										selected.remove(item);
									} else if (item.isSelected()) {
										selected.add(item);
									}
								} else if (evt.wasSelectionChanged()) {
									if (item.isSelected()) {
										selected.add(item);
									} else {
										selected.remove(item);
									}
								}
								if (item.getValue() != null) {
									System.out.println(item.getValue().getName() + "checked->" + item.isSelected());
									if (item.isSelected()) {
										session.enableLayer(item.getValue().getName());
									} else {
										session.disableLayer(item.getValue().getName());
									}
								}
							});
				}
				treeView.setRoot(root);
				// set the cell factory
				// treeView.setCellFactory(CheckBoxTreeCell.<Layer>forTreeView());
				treeView.setCellFactory(tv -> {
					CheckBoxTreeCell<Layer> cell = new CheckBoxTreeCell<>();
					cell.setConverter(converter);
					return cell;
				});
				treeView.refresh();

			}
		});

	}

	public void addLayer() {
		worldModel.setMode(WorldMode.LOADING);
		if (Common.selectShpFileLayer(treeView) == null) {
			worldModel.setMode(WorldMode.VIEW);
		}
		;
	}

	public void removeLayer() {
		TreeItem<Layer> item = treeView.getSelectionModel().getSelectedItem();
		if (null != item) {
			root.getChildren().remove(item);
			Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
			session.removeLayer(item.getValue());
		}
	}

	public void clearAllLayers() {
		root.getChildren().remove(0, root.getChildren().size());
		Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
		session.clearAllLayers();
	}

	public void enableLayer() {
		TreeItem<Layer> item = treeView.getSelectionModel().getSelectedItem();
		setEnableLayer(item.getValue().getName());
	}

	public void setEnableLayer(String layerName) {
		Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
		session.enableLayer(layerName);
	}

	public void disableLayer() {
		TreeItem<Layer> item = treeView.getSelectionModel().getSelectedItem();
		setDisableLayer(item.getValue().getName());
	}

	public void setDisableLayer(String layerName) {
		Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
		session.disableLayer(layerName);
	}

	public void zoomToLayer() {

	}

	private class LayersChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initLayers();
		}
	}

}
