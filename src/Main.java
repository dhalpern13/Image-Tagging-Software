import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ImageTaggingManager;
import model.ImageTaggingManagerFactory;
import controller.StartScreenController;

import java.io.IOException;

public class Main extends Application {

  private ImageTaggingManager imageTaggingManager;

  /**
   * Starts the program.
   *
   * @param primaryStage Stage for the controller to run scenes
   * @throws IOException If the fxml file cannot load
   */
  public void start(Stage primaryStage) throws IOException {
    // loads the fxml file
    FXMLLoader loader = new FXMLLoader(getClass().getResource("view/StartScreen.fxml"));
    Parent root = loader.load();
    StartScreenController controller = loader.getController();
    imageTaggingManager = ImageTaggingManagerFactory.getImageTaggingManager();
    controller.initialize(primaryStage, imageTaggingManager);

    // setup the stage
    primaryStage.setTitle("Image Tagger");
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setX(250);
    primaryStage.setY(25);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  /** Closes the program. */
  public void stop() {
    imageTaggingManager.saveData();
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
