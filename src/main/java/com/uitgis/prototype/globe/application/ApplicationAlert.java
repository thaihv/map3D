package com.uitgis.prototype.globe.application;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

public class ApplicationAlert extends Alert {

	public ApplicationAlert(AlertType alertType) {
		super(alertType);
		this.initStyle(StageStyle.DECORATED);
		this.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
	}

	public ApplicationAlert(AlertType alertType, String contentText, ButtonType... buttons) {
		super(alertType, contentText, buttons);
		this.initStyle(StageStyle.UNDECORATED);
		this.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
	}

}
