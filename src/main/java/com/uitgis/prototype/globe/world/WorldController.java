package com.uitgis.prototype.globe.world;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.event.MessageListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ScreenSelector;
import gov.nasa.worldwindx.examples.util.ToolTipController;
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

	protected ToolTipController toolTipController;
	protected ScreenSelector screenSelector;
	protected SelectionHighlightController selectionHighlightController;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SwingUtilities.invokeLater(new WorldInitializer());
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
			worldPanel.add(wwd, BorderLayout.CENTER);
			worldNode.setContent(worldPanel);

			screenSelector = new ScreenSelector(wwd);
			toolTipController = new ToolTipController(wwd, AVKey.DISPLAY_NAME, null);
			selectionHighlightController = new SelectionHighlightController(wwd, screenSelector);


			wwd.addMouseListener(new WorldMouseListener());
			wwd.addSelectListener(new BasicDragger(wwd));
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

	public void setupWWLayers() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				LayerList layers = wwd.getModel().getLayers();
				for (Layer l : layers) {

//					if (l.isEnabled() == false)
//						layers.remove(l);
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

	public static class SelectionHighlightController extends HighlightController implements MessageListener {
		protected ScreenSelector screenSelector;
		protected List<Highlightable> lastBoxHighlightObjects = new ArrayList<Highlightable>();

		public SelectionHighlightController(WorldWindow wwd, ScreenSelector screenSelector) {
			super(wwd, SelectEvent.ROLLOVER);

			this.screenSelector = screenSelector;
			this.screenSelector.addMessageListener(this);
		}

		@Override
		public void dispose() {
			super.dispose();

			this.screenSelector.removeMessageListener(this);
		}

		public void onMessage(Message msg) {
			try {
				if (msg.getName().equals(ScreenSelector.SELECTION_STARTED)
						|| msg.getName().equals(ScreenSelector.SELECTION_CHANGED)) {
					this.highlightSelectedObjects(this.screenSelector.getSelectedObjects());
				}
			} catch (Exception e) {
				// Wrap the handler in a try/catch to keep exceptions from bubbling up
				Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
			}
		}

		protected void highlight(Object o) {
			if (this.lastHighlightObject != o && this.lastBoxHighlightObjects.contains(this.lastHighlightObject)) {
				this.lastHighlightObject = null;
				return;
			}

			super.highlight(o);
		}

		protected void highlightSelectedObjects(List<?> list) {
			if (this.lastBoxHighlightObjects.equals(list))
				return; // same thing selected
			for (Highlightable h : this.lastBoxHighlightObjects) {
				if (h != this.lastHighlightObject)
					h.setHighlighted(false);
			}
			this.lastBoxHighlightObjects.clear();

			if (list != null) {
				// Turn on highlight if object selected.
				for (Object o : list) {
					if (o instanceof Highlightable) {
						((Highlightable) o).setHighlighted(true);
						this.lastBoxHighlightObjects.add((Highlightable) o);
					}
				}
			}
			this.wwd.redraw();
		}
	}

	public WorldWindow getWwd() {
		return this.wwd;
	}

	public ScreenSelector getScreenSelector() {
		return this.screenSelector;
	}

	public SelectionHighlightController getHighlightController() {
		return this.selectionHighlightController;
	}
	public ToolTipController getToolTipController() {
		return this.toolTipController;
	}
	public void setHighlightController(SelectionHighlightController hlight) {
		selectionHighlightController = hlight;
	}
	public void setToolTipController(ToolTipController tooltip) {
		toolTipController = tooltip;
	}	
}
