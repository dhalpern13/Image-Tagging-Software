package view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/*
 * Code taken nearly-verbatim from: https://stackoverflow.com/questions/37378973/implement-tags-bar-in-javafx
 */

/** A class that represents a tag on-screen. */
public class Tag extends HBox {

  /** Store the tag that this Tag object represents on-screen. */
  private String tag;

  /**
   * Create a new Tag object to represent tag, tag.
   *
   * @param tag a tag.
   */
  public Tag(String tag, boolean displayAddSign) {
    super();

    this.maxHeight(30);
    this.minHeight(30);
    this.setSpacing(5);
    this.setPadding(new Insets(5));
    this.setStyle("-fx-background-color: white;");

    this.tag = tag;

    Label tagLabel = new Label(tag);
    tagLabel.setFont(Font.font(14));

    ImageView tagIcon;

    /*
     * If displayAddSign is true, then the tag will have a red 'x' to the right of the label.  If not,
     * it will have a green '+' sign.
     */
    if (displayAddSign) {
      Image addSignIcon = new Image("view/add.png");
      tagIcon = new ImageView(addSignIcon);
    } else {
      Image deleteSignIcon = new Image("view/delete.png");
      tagIcon = new ImageView(deleteSignIcon);
    }

    tagIcon.setFitHeight(20);
    tagIcon.setFitWidth(20);
    tagIcon.setPreserveRatio(true);

    this.getChildren().add(tagLabel);
    this.getChildren().add(tagIcon);
  }

  /**
   * Return the string on the tag.
   *
   * @return a tag.
   */
  public String getTag() {
    return tag;
  }
}
