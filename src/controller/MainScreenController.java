package controller;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.ImageTaggingManager;

import javafx.scene.text.Font;
import view.Tag;
import view.Thumbnail;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * A class that is responsible for allowing a user to view and interact with the images at and below
 * a certain directory. In view mode, the user is able to view images based on whether they contain
 * selected tags, launch a slideshow, and go to a screen where they can edit an individual image. In
 * edit mode, the user is able to add and remove tags from multiple images at once.
 */
public class MainScreenController {

  /** Store the stage upon which this scene occurs. */
  private Stage stage;

  /**
   * Store the ImageTaggingManager object, which is responsible for all interactions with the model.
   */
  private ImageTaggingManager imageTaggingManager;

  /** Store the number of columns of pictures to display. */
  private static final int COLUMNS_OF_PICTURES = 3;

  /** Store the GUI object that displays the images. */
  @FXML public GridPane grid;

  /** Store the back button. */
  @FXML public Button backButton;

  /**
   * Store the GUI object that displays the path to the directory, such that all images being
   * displayed are at or below this directory. Clicking on this label open's this directory in the
   * operating system's file manager.
   */
  @FXML public Label directory;

  /**
   * Store the GUI object that displays 'Current tag filters:' in view mode, when the top flow pane
   * stores the current tag filters, and 'Tags to remove:' in edit mode, when the top pane stores
   * all tags, such that at least one of the selected images has that tag.
   */
  @FXML public Label topLabel;

  /**
   * Store the GUI object that displays the current tag filters in view mode, and, in edit mode, all
   * tags, such that at least one of the selected images has that tag.
   */
  @FXML public FlowPane topFlowPane;

  /**
   * Store the GUI object that displays 'Available tag filters:' in view mode, when the bottom flow
   * pane is displaying all of the tags that are stored by the program that are not currently
   * filters, and 'Tags to add:' in edit mode, when the bottom pane stores all the tags that the
   * program is storing, such that at least one of the selected images does not have that tag.
   */
  @FXML public Label bottomLabel;

  /**
   * Store the GUI object that, in view mode, displays the tags that the program is storing that are
   * not currently being used as filters, and, in edit mode, displays all tags that the program is
   * storing, such that at least one of the selected images does not have that tag.
   */
  @FXML public FlowPane bottomFlowPane;

  /**
   * Store the GUI object that, in view mode, allows the user to search the available tag filters,
   * and, in edit mode, allows the user to search the available tags to add, and to enter the name
   * of a new tag to add that the program is not currently storing.
   */
  @FXML public TextField textField;

  /**
   * Store the button that, in edit mode, allows the user to add a new tag to the currently selected
   * images (not visible in view mode).
   */
  @FXML public Button addButton;

  /** Store the button that allows the user to toggle between view mode and edit mode. */
  @FXML public Button enterModeButton;

  /**
   * Store the button that, in view mode, allows the user to launch a slideshow. In edit mode, this
   * button allows the user to select all images to add or remove tags from.
   */
  @FXML public Button selectSlideshow;

  /**
   * Store a button that, in edit mode, allows the user to deselect all images. In view mode, this
   * button is not visible.
   */
  @FXML public Button deselect;

  /** Store a set of all thumbnails that have been selected in edit mode. */
  private Set<Thumbnail> selectedThumbnails = new HashSet<>();

  /**
   * Store a list of tags, such that at least one of the currently selected thumbnails has each of
   * these tags.
   */
  private List<String> tagsToRemove = new ArrayList<>();

  /**
   * Store a list of tags, such that all of the currently selected images have each of these tags.
   */
  private List<String> tagsAllSelectedHave = new ArrayList<>();

  /** Store true if and only if the user is currently in edit mode. */
  private boolean editMode;

  /**
   * Initialize the main screen with a given imageTaggingManager. Note: the screen will always start
   * off in view mode. Furthermore, if there are any tag filters in place, those filters will remain
   * in place (this can occur when the user goes back from the individual image editing screen.
   *
   * @param stage a stage.
   * @param imageTaggingManager imageTaggingManager to use.
   */
  void initialize(Stage stage, ImageTaggingManager imageTaggingManager) {
    this.stage = stage;
    this.imageTaggingManager = imageTaggingManager;

    /*
     * Get the path to the current directory, as a String. This will have been set by the start screen controller
     * prior to this screen being initialized.
     */
    String curDirPath = imageTaggingManager.getCurrentDirectoryPath();
    directory.setText(curDirPath);
    directory.setFont(Font.font(20));

    /*
     * If the user clicks on the directory path label, he or she will be able to view this full path, which is
     * helpful in case the full path does not fit on the screen.
     */
    Tooltip.install(directory, new Tooltip(curDirPath));
    topLabel.setFont(Font.font(18));
    bottomLabel.setFont(Font.font(18));

    /*
     * Load all of the images at and below the current directory that have the current tag filters, or all
     * of the images if there are no tag filers.
     */
    loadImages();

    /*
     * Start the screen in view mode.
     */
    setToViewMode();

    /*
     * Set the text field below the bottom tag pane so that it will only display the tags that contain the current
     * value of this text field (this is not case sensitive).
     */
    textField.textProperty().addListener(event -> updateBasedOnTextField());
  }

  /**
   * Get a list of the paths to all of the images at and below the current directory that contain
   * the tag filters. If there are no filters, then this list will contain the paths to all of the
   * images. Then, populate the images grid pane based on this list.
   */
  private void loadImages() {
    grid.getChildren().clear();
    int imagesPlaced = 0;
    for (String path : imageTaggingManager.getImagePaths()) {
      int row = imagesPlaced / COLUMNS_OF_PICTURES;
      int col = imagesPlaced % COLUMNS_OF_PICTURES;

      Thumbnail thisThumbnail =
          new Thumbnail(path, imageTaggingManager.getImagesName(path), 200, 200, 220);

      handleThumbnailPress(thisThumbnail);
      grid.add(thisThumbnail, col, row);
      imagesPlaced += 1;
    }
  }

  /**
   * Given a thumbnail, set the action that occurs upon clicking that thumbnail. If the user is in
   * view mode, view the image in the individual image editing screen. If the user is in edit mode,
   * then select the thumbnail if it is not currently selected, and deselect if it is currently
   * selected.
   *
   * @param thumb a Thumbnail.
   */
  private void handleThumbnailPress(Thumbnail thumb) {
    thumb.setOnMouseClicked(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            if (editMode) {
              if (selectedThumbnails.contains(thumb)) {
                selectedThumbnails.remove(thumb);
                thumb.deselect();
                updateTagsBasedOnSelection();

              } else {
                selectedThumbnails.add(thumb);
                thumb.select();
                updateTagsBasedOnSelection();
              }
              /* Handle the case where the user is in view mode. */
            } else {
              try {
                FXMLLoader loader =
                    new FXMLLoader(
                        getClass().getResource("../view/IndividualImageEditingScreen.fxml"));
                Scene imageScreen = new Scene(loader.load());

                IndividualImageEditingScreenController controller = loader.getController();
                stage.setScene(imageScreen);
                controller.initialize(stage, imageTaggingManager, thumb.getPath());

              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        });
  }

  /** Return to the start screen. Set the image tagging manager's directory to null. */
  @FXML
  private void handleBackToStart() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/StartScreen.fxml"));
      Scene startScreen = new Scene(loader.load());
      StartScreenController controller = loader.getController();
      stage.setScene(startScreen);
      imageTaggingManager.changeDirectory(null);
      controller.initialize(stage, imageTaggingManager);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Set main screen to view mode. Update GUI objects as described above. */
  private void setToViewMode() {
    editMode = false;
    topLabel.setText("Current tag filters:");
    bottomLabel.setText("Available tag filters:");
    selectSlideshow.setText("Start SlideShow");
    textField.setPromptText("Search tags");
    textField.clear();
    addButton.setVisible(false);
    deselect.setVisible(false);
    enterModeButton.setText("Enter Edit Mode");
    selectedThumbnails.clear();
    tagsToRemove.clear();
    tagsAllSelectedHave.clear();
    populateFilteredTags();
    populateUnfilteredTags();
    loadImages();
  }

  /**
   * Switch the screen to edit mode if currently in view mode, and switch to view mode if currently
   * in editing mode.
   */
  @FXML
  private void handleSwitchMode() {
    if (editMode) {
      setToViewMode();
    } else {
      setToEditMode();
    }
  }

  /** Set main screen to edit mode. Update the GUI objects as described above. */
  private void setToEditMode() {
    editMode = true;
    topLabel.setText("Tags to delete:");
    bottomLabel.setText("Tags to add:");
    selectSlideshow.setText("Select All");
    textField.setPromptText("Search/enter new tag");
    textField.clear();
    addButton.setVisible(true);
    deselect.setVisible(true);
    enterModeButton.setText("Enter View Mode");
    populateAddTags();
  }

  /**
   * Handle a select/slideshow button press event. If in view mode, begin the slideshow. If in edit
   * mode, select all thumbnails.
   */
  @FXML
  private void handleSelectSlideshow() {
    if (editMode) {
      selectAll();
    } else {
      startSlideShow();
    }
  }

  /**
   * Select all thumbnails in the image pane, and update the panes that display the tags the are
   * available to be added and removed accordingly. Note: this is for use in edit mode only.
   */
  private void selectAll() {
    for (Node thumb : grid.getChildren()) {
      Thumbnail castedThumb = (Thumbnail) thumb;
      castedThumb.select();
      selectedThumbnails.add(castedThumb);
    }
    updateTagsBasedOnSelection();
  }

  /**
   * Deselect all thumbnails in the image pane, and update the panes that display the tags that are
   * available to be added and removed accordingly. Note: this is for use in edit mode only.
   */
  @FXML
  private void handleDeselectAll() {
    for (Thumbnail thumb : selectedThumbnails) {
      thumb.deselect();
    }
    selectedThumbnails.clear();
    updateTagsBasedOnSelection();
  }

  /**
   * Update the panes displaying the tags available to be added and removed from the currently
   * selected images. Note: this is for use in edit mode only.
   */
  private void updateTagsBasedOnSelection() {
    Collection<String> selectedPaths = new HashSet<>();
    for (Thumbnail thumb : selectedThumbnails) {
      selectedPaths.add(thumb.getPath());
    }
    tagsToRemove = imageTaggingManager.getImagesTags(selectedPaths);
    tagsAllSelectedHave = imageTaggingManager.getTagsAllImagesContain(tagsToRemove, selectedPaths);
    populateAddTags();
    populateRemoveTags();
  }

  /**
   * Update the current tag filters pane, if in view mode, and the
   * tags-that-are-available-to-be-added pane, if in edit mode, so that all tags being displayed
   * contain the current value of the add tag text field (this is not case sensitive).
   */
  private void updateBasedOnTextField() {
    if (editMode) {
      populateAddTags();
    } else {
      populateUnfilteredTags();
    }
  }

  /**
   * Create a new stage and display a slideshow of all images at or below the current directory that
   * contain all of the tags being filtered for. Or, if no tag filters are selected, then display a
   * slide show with all images. Note: this is for use in view mode only.
   */
  private void startSlideShow() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/SlideShowScreen.fxml"));

      Stage slideShowStage = new Stage();
      slideShowStage.setTitle("SlideShow");
      slideShowStage.setFullScreen(true);
      slideShowStage.setResizable(false);

      Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
      Scene slideShowScene =
          new Scene(loader.load(), primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight());
      slideShowStage.setScene(slideShowScene);

      slideShowStage.show();
      SlideShowController controller = loader.getController();
      controller.initialize(slideShowStage, imageTaggingManager.getImagePaths());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Update the tags-that-are-available-to-be-added flow pane so that it display the tags that are
   * stored by the program, such that at least one of the currently selected images does not have
   * each of these tags. Note: this is for use in edit mode only.
   */
  private void populateAddTags() {
    List<Node> flowPaneChildren = bottomFlowPane.getChildren();
    flowPaneChildren.clear();
    for (String tag :
        imageTaggingManager.getAllTagsInMasterCollection(
            tagsAllSelectedHave, textField.getText())) {
      Tag newTag = new Tag(tag, true);
      flowPaneChildren.add(newTag);
      newTag.setOnMouseClicked(
          event -> {
            for (Thumbnail thumb : selectedThumbnails) {
              thumb.setPath((imageTaggingManager.addTagToImage(thumb.getPath(), tag)));
            }
            updateTagsBasedOnSelection();
          });
    }
  }

  /**
   * Update the tags-that-are-available-to-be-removed flow pane so that it displays the tags that
   * are stored by the program, such that at least one of the images has each of these tags. Note:
   * this is for use in edit mode only.
   */
  private void populateRemoveTags() {
    List<Node> flowPaneChildren = topFlowPane.getChildren();
    flowPaneChildren.clear();
    for (String tag : tagsToRemove) {
      Tag newTag = new Tag(tag, false);
      flowPaneChildren.add(newTag);
      newTag.setOnMouseClicked(
          event -> {
            for (Thumbnail thumb : selectedThumbnails) {
              thumb.setPath((imageTaggingManager.removeTagFromImage(thumb.getPath(), tag)));
            }
            updateTagsBasedOnSelection();
          });
    }
  }

  /**
   * If the user has entered text in the add tag text field, add the text as a tag (excluding
   * trailing whitespace) to all selected thumbnails. Note: this is for use in edit mode only.
   */
  @FXML
  private void handleAddNewTag() {
    if (textField.getText().trim().length() > 0) {
      String tag = textField.getText().trim();
      for (Thumbnail thumb : selectedThumbnails) {
        thumb.setPath((imageTaggingManager.addTagToImage(thumb.getPath(), tag)));
      }
      textField.clear();
      updateTagsBasedOnSelection();
    }
  }

  /**
   * Populate the pane containing the tags that are currently being used as filters. Note: this is
   * for use in view mode only.
   */
  private void populateFilteredTags() {
    List<Node> flowPaneChildren = topFlowPane.getChildren();
    flowPaneChildren.clear();
    for (String tag : imageTaggingManager.getFilteredTags()) {
      Tag newTag = new Tag(tag, false);
      flowPaneChildren.add(newTag);
      newTag.setOnMouseClicked(
          event -> {
            imageTaggingManager.removeTagFilter(tag);
            loadImages();
            populateFilteredTags();
            populateUnfilteredTags();
          });
    }
  }

  /**
   * Populate the pane that contains the tags that can be added as filters with all tags being
   * stored by the program that are not currently being used as filters, and that contain the
   * current value of the add tag text field (ignoring case). Note: this is for use in view mode
   * only.
   */
  private void populateUnfilteredTags() {
    List<Node> flowPaneChildren = bottomFlowPane.getChildren();
    flowPaneChildren.clear();
    for (String tag :
        imageTaggingManager.getAllTagsInMasterCollection(
            imageTaggingManager.getFilteredTags(), textField.getText().trim())) {
      Tag newTag = new Tag(tag, true);
      flowPaneChildren.add(newTag);
      newTag.setOnMouseClicked(
          event -> {
            imageTaggingManager.addTagFilter(tag);
            loadImages();
            populateFilteredTags();
            populateUnfilteredTags();
          });
    }
  }

  /** Open the current directory in the operating system's file viewer. */
  @FXML
  private void handleOpenDirectory() {
    try {
      Desktop.getDesktop().open(new File(imageTaggingManager.getCurrentDirectoryPath()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
