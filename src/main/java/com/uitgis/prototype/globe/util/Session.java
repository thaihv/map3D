package com.uitgis.prototype.globe.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.layers.Layer;

public class Session implements Identifiable {

	public static final String DEFAULT_SESSION_ID = "Default Session";

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private final Set<Layer> layers = new LinkedHashSet<Layer>();

	private final String id;

	public Session() {
		this(Session.DEFAULT_SESSION_ID);
	}

	public Session(String id) {
		this.id = id;
		this.init();
	}

	@Override
	public String getId() {
		return this.id;
	}

	public void init() {

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
		System.out.println("addPropertyChangeListener");
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
		System.out.println("removePropertyChangeListener");
	}
	public void addLayersChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("layers", listener);
	}
	public void removeLayersChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener("layers", listener);
	}	
	public void addLayer(Layer layer) {
		if (this.layers.add(layer)) {
			this.pcs.firePropertyChange("layers", null, (Iterable<Layer>) this.layers);
			System.out.println("addLayer");
		}
	}
	public void removeLayer(Layer layer) {

		if (this.layers.remove(layer)) {
			this.pcs.firePropertyChange("layers", null, (Iterable<Layer>) this.layers);
			System.out.println("removeLayer");
		}

	}
	public void clearAllLayers() {
		this.layers.clear();
		this.pcs.firePropertyChange("layers", null, (Iterable<Layer>) this.layers);
		System.out.println("clearAllLayers");
		
	}
	public void enableLayer(String layerName) {
	    for (Iterator<Layer> it = layers.iterator(); it.hasNext(); ) {
	    	Layer f = it.next();
	        if (f.getName().equals(layerName)) {
	        	f.setEnabled(true);
	        	break;
	        }
	    }
		this.pcs.firePropertyChange("layers", null, (Iterable<Layer>) this.layers);
		System.out.println("enableLayer");
	}
	public void disableLayer(String layerName) {
	    for (Iterator<Layer> it = layers.iterator(); it.hasNext(); ) {
	    	Layer f = it.next();
	        if (f.getName().equals(layerName)) {
	        	f.setEnabled(false);
	        	break;
	        }
	    }
		this.pcs.firePropertyChange("layers", null, (Iterable<Layer>) this.layers);
		System.out.println("disableLayer");	    
	}	
	public Set<Layer> getLayers() {
		return Collections.unmodifiableSet(this.layers);
	}

	@Override
	public boolean equals(Object o) {
		boolean equals = false;

		if (o instanceof Session) {
			equals = this.id.equals(((Session) o).id);
		}

		return equals;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

}