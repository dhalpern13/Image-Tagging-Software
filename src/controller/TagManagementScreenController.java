package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import model.ImageTaggingManager;
import view.Tag;

import java.io.IOException;
import java.util.List;

/**
 * A class that is responsible for allowing the user to manage a collection of tags, whose existence
 * does not depend on their being in use on any images. From this screen, the user may add and
 * remove tags from this master collection. Upon removing a tag, the user will be prompted about
 * whether he or she wants to also remove that tag from all of the images that currently have it
 * (and have been previously viewed by the program, and have not had their paths modified outside of
 * the program in the interim).
 */
public class TagManagementScreenController {

  /** Store the stage upon which this scene occurs. */
  private Stage stage;

  /**
   * Store a list of all of the tags that are stored by the program, sorted in alphabetical order.
   */
  private List<String> allTagsStoredByProgram;

  /**
   * Store an ImageTaggingManager object, which is responsible for all interactions with the model.
   */
  private ImageTaggingManager imageTaggingManager;

  /**
   * Store the GUI object that allows the user to enter the name of a new tag, and to search the
   * collection of tags that the program is currently storing.
   */
  @FXML public TextField addTagTextField;

  /**
   * Store the GUI object that is responsible for displaying all of the tags that the program is
   * currently storing, and that contain the current value of the add tag text field (ignoring
   * case).
   */
  @FXML public FlowPane tagsFlowPane;

  /**
   * Initialize the tag management screen.
   *
   * @param stage a Stage.
   * @param imageTaggingManager an ImageManagerObject.
   */
  void initialize(Stage stage, ImageTaggingManager imageTaggingManager) {
    this.stage = stage;
    this.imageTaggingManager = imageTaggingManager;
    populateTagsFlowPane();
    /*
     * Set the tags flow pane so that it updates based on the current value of the add tag text field.
     */
    addTagTextField
        .textProperty()
        .addListener(event -> populateTagsFlowPaneBasedOnExistingListOfTags());
  }

  /**
   * Get an updated list of the tags that the program is storing, and then populate the tags pane
   * with those tags in the updated list that contain the current value of the add tag text entry
   * (ignoring case).
   */
  @FXML
  private void populateTagsFlowPane() {
    allTagsStoredByProgram = imageTaggingManager.getAllTagsInMasterCollection();
    populateTagsFlowPaneBasedOnExistingListOfTags();
  }

  /**
   * Populate the tags pane with those tags that contain the current value of the add tag text entry
   * (ignoring case). Note: unlike populateTagsFlowPane, this method does not first get an updated
   * list of tags from the image tagging manager.
   */
  @FXML
  private void populateTagsFlowPaneBasedOnExistingListOfTags() {
    tagsFlowPane.getChildren().clear();
    for (String tag : allTagsStoredByProgram) {
      if (tag.toLowerCase().contains(addTagTextField.getText().toLowerCase())) {
        /*
         * The tags will be displayed with a red 'x' sign.
         */
        Tag newTag = new Tag(tag, false);
        tagsFlowPane.getChildren().add(newTag);
        newTag.setOnMouseClicked(event -> removeTag(newTag.getTag()));
      }
    }
  }

  /** Render the start screen. */
  @FXML
  private void returnToStartScreen() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/StartScreen.fxml"));
      Scene startScene = new Scene(loader.load());
      StartScreenController controller = loader.getController();
      stage.setScene(startScene);
      controller.initialize(stage, imageTaggingManager);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Add, to the master collection of tags, the current value of the add tag text entry, excluding
   * trailing whitespace, and then update the display accordingly. If there is not text in the text
   * entry, then do nothing.
   */
  @FXML
  private void addNewTag() {
    if (addTagTextField.getText().trim().length() != 0) {
      imageTaggingManager.addTagToMasterCollection(addTagTextField.getText().trim());
      addTagTextField.clear();
      populateTagsFlowPane();
    }
  }

  /**
   * Render a dialog box to get confirmation from the user about whether he or she would like to
   * delete a tag. If the user clicks 'yes', then render a second dialog box asking the user whether
   * he or she would like to also delete the tag from all images. If he or she clicks yes, delete
   * the the tag from the master collection, and from all images that the program has interacted
   * with, and whose paths have not changed since the program last interacted with them. Otherwise,
   * only delete the tag from the master collection. If the user does not click 'yes' in the first
   * dialog box, then do nothing.
   *
   * @param tag a tag.
   */
  private void removeTag(String tag) {
    String removeAlertText =
        "Are you sure you want to remove tag '" + tag + "' from the program's master collection?";
    Alert confirmRemoveAlert =
        new Alert(Alert.AlertType.CONFIRMATION, removeAlertText, ButtonType.CANCEL, ButtonType.YES);
    /* Render a dialog box to confirm that the user would like to delete the tag. */
    confirmRemoveAlert.showAndWait();
    /* Handle the case where the user has confirmed, by clicking 'yes', that he or she would like to delete the tag. */
    if (confirmRemoveAlert.getResult() == ButtonType.YES) {
      String removeFromImagesAlertText =
          "Would you also like to remove tag '" + tag + "' from all images?";
      Alert imageRemovalAlert =
          new Alert(
              Alert.AlertType.CONFIRMATION,
              removeFromImagesAlertText,
              ButtonType.NO,
              ButtonType.YES);
      /* Render a dialog box to ask the user whether he or she would also like to delete the tag from all images. */
      imageRemovalAlert.showAndWait();
      /* Handle the case where the user clicked 'yes' to removing the tag from all images. */
      if (imageRemovalAlert.getResult() == ButtonType.YES) {
        imageTaggingManager.removeTagFromMasterCollectionAndDeleteFromAllImages(tag);
      }
      /* Handle the case where the user did not click 'yes' to removing the tag from all images, by only deleting the
       * tag from the master collection.
       */
      else {
        imageTaggingManager.removeTagFromMasterCollection(tag);
      }
      populateTagsFlowPane();
    }
  }
}
