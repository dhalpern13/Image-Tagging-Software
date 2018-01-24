package view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/** A class that represents a Thumbnail on-screen. */
public class Thumbnail extends BorderPane {

  /** Store the path of the image being displayed in the thumbnail, as a String. */
  private String path;

  /**
   * Create a new Thumbnail to represent the image location path, and with label name.
   *
   * @param path path of the image to display
   * @param name name of image to display as a label
   */
  public Thumbnail(
      String path, String name, int imageWidth, int imageHeight, int imageWithLabelHeight) {
    super();
    this.path = path;

    File newFile = new File(path);
    URL newURL = null;
    try {
      newURL = newFile.toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    if (newURL != null) {
      Image newImage = new Image(newURL.toString(), imageWidth, imageHeight, true, true, true);
      ImageView thisImageView = new ImageView();
      thisImageView.setImage(newImage);

      Label newLabel = new Label(name);
      this.setCenter(thisImageView);
      this.setBottom(newLabel);
      this.setMaxHeight(imageWithLabelHeight);
      this.setMinHeight(imageWithLabelHeight);
      this.setMaxWidth(imageWidth);
      this.setMinWidth(imageHeight);
      BorderPane.setAlignment(newLabel, Pos.CENTER);

      Tooltip.install(this, new Tooltip(path));
    }
  }

  /**
   * Return the path of the image that this thumbnail displays
   *
   * @return path of the image this thumbnail displays
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Update the path of the image that this thumbnail displays without changing the image it is
   * displaying, and update the tooltip to display the new path.
   *
   * @param newPath the new path of the image that the thumbnail is displaying.
   */
  public void setPath(String newPath) {
    this.path = newPath;
    Tooltip.install(this, new Tooltip(newPath));
  }

  /** "Select" the thumbnail. Sets the background to blue. */
  public void select() {
    this.setStyle("-fx-background-color: #0099FF;");
  }

  /** "Deselect" the thumbnail. Sets the background to grey. */
  public void deselect() {
    this.setStyle("-fx-background-color: #F4F4F4;");
  }
}
