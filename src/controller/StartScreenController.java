package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.ImageTaggingManager;

import java.io.File;
import java.io.IOException;

/**
 * A class that is responsible for allowing a user to go to a screen that allows him or her to
 * manage a collection tags, view a log of all image editing done through the program, or select a
 * directory and then view and edit the images at that directory.
 */
public class StartScreenController {

  /** Store the path to the directory where the directory chooser starts. */
  private static final String DEFAULT_PATH = System.getProperty("user.home");

  /** Store the stage upon which this scene occurs. */
  private Stage stage;

  /**
   * Store an ImageTaggingManager object, which is responsible for all interactions with the model.
   */
  private ImageTaggingManager imageTaggingManager;

  /**
   * Initialize the start screen.
   *
   * @param stage a Stage.
   * @param imageTaggingManager an ImageTaggingManager object.
   */
  public void initialize(Stage stage, ImageTaggingManager imageTaggingManager) {
    this.stage = stage;
    this.imageTaggingManager = imageTaggingManager;
  }

  /**
   * Render, on a new stage, a log that displays all changes made to images through the program,
   * including a timestamp for each change.
   */
  @FXML
  private void handleViewLog() throws IOException {
    ScrollPane root = new ScrollPane();
    root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    Text text = new Text();
    text.setText(imageTaggingManager.readLogFile());
    text.setWrappingWidth(800);
    root.setContent(text);
    Scene scene = new Scene(root);
    Stage stage = new Stage();
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Prompt the user to select a directory, and then render the main screen based on this selection.
   * If the user does not select a directory, then remain on the start screen.
   */
  @FXML
  private void handleChooseDirectory() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Folders");
    File defaultDirectory = new File(DEFAULT_PATH);
    chooser.setInitialDirectory(defaultDirectory);
    File selectedDirectory = chooser.showDialog(stage);
    if (selectedDirectory != null) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/MainScreen.fxml"));
        Scene mainScreen = new Scene(loader.load());

        MainScreenController controller = loader.getController();
        stage.setScene(mainScreen);
        imageTaggingManager.changeDirectory(selectedDirectory.toString());
        controller.initialize(stage, imageTaggingManager);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Render the tag management screen. */
  @FXML
  private void handleTagManager() {
    try {
      FXMLLoader loader =
          new FXMLLoader(getClass().getResource("../view/TagManagementScreen.fxml"));
      Scene tagManagementScene = new Scene(loader.load());

      TagManagementScreenController controller = loader.getController();
      stage.setScene(tagManagementScene);
      controller.initialize(stage, imageTaggingManager);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
