package com.uitgis.prototype.globe.world;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WorldModel {

	private WorldMode mode;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public WorldModel() {
		this.mode = WorldMode.VIEW;
	}

	public void setMode(WorldMode mode) {
		this.mode = mode;
		this.pcs.firePropertyChange("mode", null, this.mode);
	}

	public WorldMode getMode() {
		return this.mode;
	}

	public void addModeChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("mode", listener);
	}

}
