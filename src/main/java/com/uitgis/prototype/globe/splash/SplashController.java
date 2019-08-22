package com.uitgis.prototype.globe.splash;

import java.net.URL;
import java.util.ResourceBundle;
import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SplashController implements Initializable {

	@Inject
	private String uitgisLogo;

	@Inject
	private String productLogo;

	@FXML
	private ImageView uitgis;

	@FXML
	private ImageView brand;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		uitgis.setImage(new Image(classLoader.getResourceAsStream(uitgisLogo)));
		brand.setImage(new Image(classLoader.getResourceAsStream(productLogo)));
	}

}
