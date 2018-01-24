package controller;

import com.sun.glass.ui.Cursor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static javafx.scene.layout.StackPane.setAlignment;

/**
 * A class that allows the user to view a slideshow of images. The slideshow begins in automatic
 * mode, and the user may bring it under his or her control by pressing the left or right key. Once
 * in manual mode, the user may return to automatic mode by pressing the up or down key. While in
 * automatic mode, the up or down key reduces the time that each picture is displayed for by a set
 * increment. Pressing any key other will exit the slideshow.
 */
public class SlideShowController {

  /**
   * Store the amount of time that each picture is displayed for in automatic mode, in milliseconds.
   */
  private int timePerPicture = 2000;

  /**
   * Store the increment by which the amount of time each picture is displayed for is increased or
   * decreased, in milliseconds.
   */
  private final int CHANGE_TIME_STEP = 200;

  /** Store a list of the paths to the images in the slide show, as Strings. */
  private List<String> images;

  /** Store the index in the images list corresponding to the image currently being viewed. */
  private int index = 0;

  /** Store the stage on which the slideshow is displayed. */
  private Stage stage;

  /**
   * Store true if and only if the slide show is proceeding automatically (versus under the control
   * of the user via the right and left arrow keys).
   */
  private boolean automatic = true;

  /** Store the pane that holds all of the elements in the slideshow scene. */
  @FXML public StackPane mainPane;

  /** Store the GUI object that displays the current image. */
  @FXML public ImageView imageDisplay;

  /** Store image that displays instructions. */
  @FXML private ImageView instructions;

  /** Timer to keep track of when to hide instructions. */
  private Timer timer;

  /** Hide the cursor and instructions when timer expires. */
  private TimerTask task;

  /**
   * Initialize a slideshow. The slideshow will begin in automatic mode.
   *
   * @param imagesToDisplay a List of image paths, as Strings.
   */
  void initialize(Stage stage, List<String> imagesToDisplay) {
    this.stage = stage;

    setAlignment(instructions, Pos.BOTTOM_CENTER);

    Image instLabel = new Image("view/automaticLabel.png", 700, 500, true, true);
    instructions.setImage(instLabel);
    instructions.setVisible(false);

    stage
        .getScene()
        .setOnMouseMoved(
            e -> {
              /*
               * Hide the cursor and instructions if mouse does not move for a set amount of time.
               */
              Cursor.setVisible(true);
              instructions.setVisible(true);

              task =
                  new TimerTask() {
                    @Override
                    public void run() {
                      instructions.setVisible(false);

                      Platform.runLater(() -> Cursor.setVisible(false));
                    }
                  };

              if (!(timer == null)) {
                timer.cancel();
              }
              timer = new Timer(true);

              timer.schedule(task, 2000);
            });

    if (imagesToDisplay.size() == 0) {
      /*
       * If there are no images to display, close the slideshow.
       */
      closeStage();
    }

    stage
        .getScene()
        .setOnKeyPressed(
            e -> {
              /*
               * Handle a right or left key press event.
               */
              if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                if (e.getCode() == KeyCode.LEFT) {
                  /*
                   * Set the index to the index of the previous photo, or the last photo, if the first photo is
                   * currently being displayed.
                   */
                  goToPrevIndex();
                } else {
                  /*
                   * Set the index to the index of the next photo, or the first photo, if the last photo is currently
                   * being displayed.
                   */
                  goToNextIndex();
                }

                /*
                 * Turn off automatic mode, and display the photo corresponding to the now-modified index.
                 */
                automatic = false;
                Image manualLabel = new Image("view/manualLabel.png", 700, 500, true, true);
                instructions.setImage(manualLabel);

                File newFile = new File(images.get(index));
                URL newURL = null;

                try {
                  newURL = newFile.toURI().toURL();
                } catch (MalformedURLException te) {
                  te.printStackTrace();
                }

                if (newURL != null) {
                  Image toDisplay =
                      new Image(newURL.toString(), stage.getWidth(), stage.getHeight(), true, true);
                  setAlignment(imageDisplay, Pos.CENTER);
                  imageDisplay.setImage(toDisplay);
                  imageDisplay.setFitHeight(toDisplay.getHeight());
                  imageDisplay.setFitWidth(toDisplay.getWidth());
                }
                /* Handle an up or down key press event. */
              } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                /*
                 * If the slideshow is in automatic mode, adjust the time that each photo is displayed for by a set
                 * increment.
                 */
                if (automatic) {
                  if (e.getCode() == KeyCode.UP) {
                    timePerPicture += CHANGE_TIME_STEP;
                  } else if (timePerPicture > CHANGE_TIME_STEP) {
                    timePerPicture -= CHANGE_TIME_STEP;
                  }
                  /*
                   * If the slideshow is not currently in automatic mode, then switch to automatic mode.
                   */
                } else {
                  automatic = true;
                  Image autoLabel = new Image("view/automaticLabel.png", 700, 500, true, true);
                  instructions.setImage(autoLabel);
                  playSlideshow();
                }
                /*
                 * Handle the pressing of any other key by terminating the slideshow.
                 */
              } else {
                closeStage();
              }
            });

    images = imagesToDisplay;
    playSlideshow();
  }

  /** Display the next picture in automatic mode. */
  private void playSlideshow() {
    File newFile = new File(images.get(index));

    URL newURL = null;

    try {
      newURL = newFile.toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    if (newURL != null) {
      Image toDisplay =
          new Image(newURL.toString(), stage.getWidth(), stage.getHeight(), true, true);
      imageDisplay.setImage(toDisplay);
      imageDisplay.setFitHeight(toDisplay.getHeight());
      imageDisplay.setFitWidth(toDisplay.getWidth());
    }

    Timer timer = new Timer(true);

    TimerTask task =
        new TimerTask() {
          @Override
          public void run() {
            Platform.runLater(
                () -> {
                  if (automatic) {
                    goToNextIndex();
                    playSlideshow();
                  }
                });
          }
        };

    timer.schedule(task, timePerPicture);
  }

  /** Terminate the slideshow. */
  private void closeStage() {
    if (!(timer == null)) {
      timer.cancel();
    }
    Cursor.setVisible(true);
    stage.close();
  }

  /** Increment the index. Set to beginning if it is now out of range of the images list. */
  private void goToNextIndex() {
    index++;
    if (index >= images.size()) {
      index = 0;
    }
  }

  /** Decrement the index. Set to the end if it is now out of range of the images list. */
  private void goToPrevIndex() {
    index--;
    if (index < 0) {
      index = images.size() - 1;
    }
  }
}
