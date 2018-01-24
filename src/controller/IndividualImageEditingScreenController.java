package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import model.ImageTaggingManager;
import view.Tag;

/**
 * A class that is responsible for allowing a user add and remove tags from an individual image,
 * rename the image, and move the image to a different directory.
 */
public class IndividualImageEditingScreenController {

  /** Store the path to the current image, as a String. */
  private String imagePath;

  /** Store the stage on which the scene that this class controls occurs. */
  private Stage stage;

  /**
   * Store all of the tags that the program is storing, and that the image does not currently have.
   */
  private List<String> allAvailableTags;

  /** Store an ImageTaggingManager object, which is responsible for all interactions the model. */
  private ImageTaggingManager imageTaggingManager;

  /** Store the GUI object that displays the image. */
  @FXML public ImageView imageView;

  /**
   * Store the GUI object that displays the name of the image, and that the user may click on to
   * view the image in their operating system's file viewer.
   */
  @FXML public Label imageNameLabel;

  /** Store the GUI object that displays the image's current tags. */
  @FXML public FlowPane thisImagesCurrentTagsFlowPane;

  /**
   * Store the GUI object that displays the tags that the program is storing, and that the image
   * does not presently have.
   */
  @FXML public FlowPane availableTagsFlowPane;

  /**
   * Store the GUI object that allows the user to enter the name of a new tag to add, and to search
   * the already-existing tags.
   */
  @FXML public TextField addTagTextField;

  /** Store the GUI object that allows that user to enter a new name for the image. */
  @FXML public TextField newImageNameTextField;

  /** Store the GUI object that allows to select an old name to revert back to. */
  @FXML public ChoiceBox<String> oldNamesChoiceBox;

  /**
   * Initialize the screen that allows the user to edit an individual image.
   *
   * @param stage a stage.
   * @param imageTaggingManager an image tagging manager object.
   * @param imagePath a path to an image, as a String.
   */
  void initialize(Stage stage, ImageTaggingManager imageTaggingManager, String imagePath) {
    this.imagePath = imagePath;
    this.stage = stage;
    this.imageTaggingManager = imageTaggingManager;

    try {
      File imageFile = new File(imagePath);
      URL imageURL = imageFile.toURI().toURL();
      Image newImage = new Image(imageURL.toString());
      imageView.setImage(newImage);

      imageNameLabel.setFont(Font.font(18));

      /*
       * Populate the old names choice box, the pane that display the image's current tags, and the pane that
       * displays the tags that the program is storing, and that the image does not already have.  In addition,
       * render the image name label, and set a tooltip so that when the user hovers the mouse over the
       * image name label, the full path will be displayed.
       */
      populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel();

      /*
       * Set the text field so that every time the user modifies the text, the available tags
       * pane will be updated to display only those available tags that contain the current value
       * of text field (this is not case-sensitive).
       */
      addTagTextField
          .textProperty()
          .addListener(event -> populateAvailableTagsFlowPaneBasedOnExistingListOfAvailableTags());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get an updated list of the image's current tags from the imageTaggingManager, and then populate
   * the pane that display's the images current tags based on the contents of this list.
   */
  private void populateImagesCurrentTagsFlowPane() {
    thisImagesCurrentTagsFlowPane.getChildren().clear();
    for (String tag : imageTaggingManager.getImagesTags(imagePath)) {
      /*
       * Add tags that display a red 'x' sign.
       */
      Tag newTag = new Tag(tag, false);
      thisImagesCurrentTagsFlowPane.getChildren().add(newTag);
      /*
       * Set the tag so that if the user clicks anywhere inside of it, the tag will be removed from the image.
       */
      newTag.setOnMouseClicked(event -> removeTag(newTag.getTag()));
    }
  }

  /**
   * Get an updated list of the tags that the program is storing, and that the image does not
   * currently have. Then, populate the available tags flow pane based on the contents of this list,
   * displaying only those tags that contain the current value of the addTagTextField (ignoring
   * case).
   */
  @FXML
  private void populateAvailableTagsFlowPane() {
    /*
     * Get all of the tags currently in use in the program, excluding those that the image already has.
     */
    allAvailableTags = imageTaggingManager.getAllAvailableTagsForImage(imagePath);
    populateAvailableTagsFlowPaneBasedOnExistingListOfAvailableTags();
  }

  /**
   * Update the available tags flow pane so that it only displays those tags that contain the
   * current value of the addTagTextField (ignoring case). Note: unlike populateAvailableTags, this
   * method does not get an updated list of the tags that the program is currently storing, and that
   * the image does not currently have.
   */
  @FXML
  private void populateAvailableTagsFlowPaneBasedOnExistingListOfAvailableTags() {
    availableTagsFlowPane.getChildren().clear();
    for (String tag : allAvailableTags) {
      /*
       * Only add tags to the flow pane that contain the current value of the addTagTextField, ignoring case.
       */
      if (tag.toLowerCase().contains(addTagTextField.getText().toLowerCase().trim())) {
        /*
         * Add tags that display a green '+' sign.
         */
        Tag newTag = new Tag(tag, true);
        availableTagsFlowPane.getChildren().add(newTag);
        /*
         * Set the tag so that when the user clicks anywhere inside of it, the tag will be added to the image.
         */
        newTag.setOnMouseClicked(event -> addTagFromExisting(newTag.getTag()));
      }
    }
  }

  /**
   * Populate the old names choice box with an updated list of the image's old names from the image
   * tagging manager.
   */
  private void populateOldNamesChoiceBox() {
    oldNamesChoiceBox.getItems().clear();
    oldNamesChoiceBox.getItems().addAll(imageTaggingManager.getImagesHistory(imagePath));
  }

  /** Go back to the screen where you can view multiple images. */
  @FXML
  private void returnToImageSelectionScreen() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/MainScreen.fxml"));
      Scene mainScreen = new Scene(loader.load());

      MainScreenController controller = loader.getController();
      stage.setScene(mainScreen);
      controller.initialize(stage, imageTaggingManager);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Add the current value of add tag text entry (minus trailing whitespace) to the image as a tag,
   * and then update the screen accordingly. If there is no text in the add tag text field, do
   * nothing.
   */
  @FXML
  private void addNewTag() {
    if (addTagTextField.getText().trim().length() != 0) {
      imagePath = imageTaggingManager.addTagToImage(imagePath, addTagTextField.getText().trim());
      addTagTextField.clear();
      /*
       * The available tags pane needs to be repopulated in case the user manually added a tag
       * that is already being stored by the program.
       */
      populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel();
    }
  }

  /**
   * Add the tag, tag, to the image, and then update the screen accordingly.
   *
   * @param tag a tag.
   */
  private void addTagFromExisting(String tag) {
    imagePath = imageTaggingManager.addTagToImage(imagePath, tag);
    /*
     * The name label needs to be rendered again in case the name of the image, with tag, is identical
     * to the name of another image in the same directory, in which case, 'copy', or 'copy 1', and so on, will
     * be appended to the image's name.
     */
    populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel();
  }

  /**
   * Remove the tag, tag, from the image, and then update the screen accordingly.
   *
   * @param tag a tag.
   */
  private void removeTag(String tag) {
    imagePath = imageTaggingManager.removeTagFromImage(imagePath, tag);
    /*
     * The name label needs to be rendered again in case the name of the image, without tag, is identical
     * to the name of another image in the same directory, in which case, 'copy', or 'copy 1', and so on, will
     * be appended to the image's name.
     */
    populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel();
  }

  /**
   * Rename the image to the current value of the new name text field (minus trailing white space),
   * and then update the screen accordingly. If the current value of the text field is the empty
   * string, do nothing.
   */
  @FXML
  private void renameImage() {
    if (newImageNameTextField.getText().trim().length() != 0) {
      imagePath =
          imageTaggingManager.renameImage(imagePath, newImageNameTextField.getText().trim());
      newImageNameTextField.clear();
      /*
       * The tag panes are repopulated in case the user renames the image with a name that includes a tag.
       */
      populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel();
    }
  }

  /**
   * Revert the image's name to the current value of the old names choice box, and then update the
   * screen accordingly. If there is nothing currently selected in the choice box, then do nothing.
   */
  @FXML
  private void revertToOldName() {
    if (oldNamesChoiceBox.getValue() != null) {
      imagePath = imageTaggingManager.revertToOldName(imagePath, oldNamesChoiceBox.getValue());
      populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel();
    }
  }

  /**
   * Open a directory chooser, move the image to the selected directory, and then update screen
   * accordingly. If no directory is selected, then do nothing.
   */
  @FXML
  private void moveImage() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setInitialDirectory(
        new File(imageTaggingManager.getImagesDirectory(imagePath)));
    File selectedDirectory = directoryChooser.showDialog(stage);
    if (selectedDirectory != null) {
      imagePath = imageTaggingManager.moveImage(imagePath, selectedDirectory.toPath().toString());
      /*
       * It is necessary to render the image name label again in case the file's old name already
       * exists in the directory that it is moved to, in which case, 'copy', or 'copy 1', and so on, will
       * be appended to the image's name.
       */
      installPathTooltipAndRenderImageNameLabel();
    }
  }

  /** Open the image's directory in the operating system's file manager. */
  @FXML
  private void handleOpenDirectory() {
    try {
      Desktop.getDesktop().open(new File(imageTaggingManager.getImagesDirectory(imagePath)));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Populate both the available tags flow pane, and the current tags flow pane, populate the old
   * names choice box, install a tool tip over the name label to display the image's full path, and
   * render the image name label.
   */
  private void populateBothTagPanesAndOldNamesChoiceBoxInstallPathTooltipAndRenderNameLabel() {
    populateAvailableTagsFlowPane();
    populateImagesCurrentTagsFlowPane();
    populateOldNamesChoiceBox();
    installPathTooltipAndRenderImageNameLabel();
  }

  /**
   * Install a tool tip over the name label to display the image's full path, and render the image
   * name label.
   */
  private void installPathTooltipAndRenderImageNameLabel() {
    Tooltip.install(imageNameLabel, new Tooltip(imagePath));
    imageNameLabel.setText(imageTaggingManager.getImagesName(imagePath));
  }
}
