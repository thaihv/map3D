package com.uitgis.prototype.globe.world;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.uitgis.prototype.globe.MapControl3D;
import com.uitgis.prototype.globe.util.Session;
import com.uitgis.prototype.globe.util.SessionManager;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class WorldController implements Initializable {

	@FXML
	private AnchorPane worldNodePane;

	@FXML
	private SwingNode worldNode;

	@Inject
	private WorldModel worldModel;

	private final WorldWindowGLJPanel wwd = new WorldWindowGLJPanel();

	private LayersChangeListener lcl = new LayersChangeListener();

	private final SectorSelector sectorSelector = new SectorSelector(wwd);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SwingUtilities.invokeLater(new WorldInitializer());

		this.sectorSelector.setInteriorColor(Color.MAGENTA);
		this.sectorSelector.setInteriorOpacity(0.5d);
		this.sectorSelector.setBorderColor(Color.MAGENTA);
		this.sectorSelector.setBorderWidth(1d);
		this.sectorSelector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new SectorChangeListener());

		setupWWLayers();

	}

	private WorldMode getMode() {
		return this.worldModel.getMode();
	}

	private void setMode(WorldMode mode) {
		this.worldModel.setMode(mode);
	}

	private class WorldInitializer implements Runnable {
		@Override
		public void run() {
			JPanel worldPanel = new JPanel(new BorderLayout());

			Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
			wwd.setModel(m);

			// set world panel / node layout
			worldPanel.add(wwd, BorderLayout.CENTER);
			worldNode.setContent(worldPanel);
			wwd.addMouseListener(new WorldMouseListener());
		}
	}

	private class WorldMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (getMode().equals(WorldMode.VIEW)) {
				SwingUtilities.invokeLater(new selectedPositionHandler());
			} else if (getMode().equals(WorldMode.EDIT)) {
			}
		}
	}

	private class SectorChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getMode().equals(WorldMode.EDIT)) {
				if (null == evt.getNewValue()) {
					Sector envSector = sectorSelector.getSector();
					if (null != envSector) {
					}
					sectorSelector.disable();
				}
			}
		}
	}

	public void setupWWLayers() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				LayerList layers = wwd.getModel().getLayers();
				for (Layer l : layers) {

					if (l.isEnabled() == false)
						layers.remove(l);
					if (l instanceof NASAWFSPlaceNameLayer || l instanceof ScalebarLayer || l instanceof CompassLayer
							|| l instanceof WorldMapLayer || l instanceof LandsatI3WMSLayer
							|| l instanceof BMNGOneImage)
						layers.remove(l);
				}
				Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
				for (Layer l : wwd.getModel().getLayers()) {
					session.addLayer(l);
				}
				session.addLayersChangeListener(lcl);
			}
		});
	}

	private class LayersChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Session session = SessionManager.getInstance().getSession(MapControl3D.APPLICATION_TITLE);
			System.out.println("CHANGED TO: " + session.getLayers());
			wwd.getModel().getLayers().clear();
			wwd.getModel().getLayers().addAll(session.getLayers());
			wwd.redraw();
			setMode(WorldMode.VIEW);
		}
	}

	private class selectedPositionHandler implements Runnable {
		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				System.out.println("Clicked");
			}
		}
	}

	public WorldWindow getWwd() {
		return this.wwd;
	}
}
