package model;

import java.io.File;

/**
 * A class that initializes an ImageTaggingManager and all of its sub-managers, LogManager,
 * TagManager, and ImageHistoryManager.
 */
public class ImageTaggingManagerFactory {

  /**
   * Store the location, as a String, where the temporary log file that records changes to images is
   * to be stored.
   */
  private static final String IMAGES_TEMP_FILE_LOCATION = "TempImagesLog.txt";

  /**
   * Store the location, as a String, where the temporary log file that records adding and removing
   * tags from the master collection is to be stored.
   */
  private static final String TAGS_TEMP_FILE_LOCATION = "TempTagsLog.txt";

  /** Store the location where the user-facing log file is to be stored, as a String. */
  private static final String LOG_FILE_LOCATION = "Log.txt";

  /**
   * Store the location where the Serialized map from image paths, as Strings, to lists of old names
   * is to be stored, as a String.
   */
  private static final String IMAGES_SERIALIZED_FILE_LOCATION = "ImageHistory.ser";

  /** Store the location where the Serialized tags list is to be stored, as a String. */
  private static final String TAGS_SERIALIZED_FILE_LOCATION = "TagHistory.ser";

  /**
   * Returns a new ImageTaggingManager object with all sub manager's initialized.
   *
   * @return a new ImageTaggingManager object.
   */
  public static ImageTaggingManager getImageTaggingManager() {
    LoggingManager loggingManager =
        new LoggingManager(
            LOG_FILE_LOCATION,
            IMAGES_TEMP_FILE_LOCATION,
            TAGS_TEMP_FILE_LOCATION,
            IMAGES_SERIALIZED_FILE_LOCATION,
            TAGS_SERIALIZED_FILE_LOCATION);
    ImageHistoryManager imageHistoryManager =
        new ImageHistoryManager(loggingManager.getSerializedImageHistories());
    TagManager tagManager = new TagManager(loggingManager.getSerializedOldTags());
    /*
     * If the temporary image history log file is not an empty file (in which case the program crashed the last
     * time(s) that it was run), update the programs image history data so that it reflects the changes that were made
     * during this session(s).
     */
    for (String line : loggingManager.readImagesTempFile()) {
      if (line != null) {
        /* The format of this temporary log file is an old path, followed by a comma, followed by the new path. */
        String[] split = line.split(",");
        imageHistoryManager.updateImage(split[0], split[1]);
      }
    }
    /*
     * If the temporary tag history file is not an empty file, update the programs master list of tags to reflect
     * the changes that were made during the last session(s), where the program did not close properly.
     */
    for (String line : loggingManager.readTagTempFile()) {
      if (line != null) {
        /*
         * The format of this temporary log is an 'a' (corresponding to add) or an 'r' (corresponding to remove)
         * followed by a colon, followed by the tag.
         */
        if (line.startsWith("a")) {
          tagManager.addTags(line.substring(2));
        } else if (line.startsWith("r")) {
          tagManager.removeTag(line.substring(2));
        }
      }
    }

    for (String image : imageHistoryManager.getAllImages()) {
      /*
       * Loop through all images being tracked and delete those that no longer exist in the computer's file
       * system at the path that the program is storing.
       */
      if (!(new File(image)).exists()) {
        imageHistoryManager.removeImage(image);
      }
    }
    return new ImageTaggingManager(imageHistoryManager, tagManager, loggingManager);
  }
}
