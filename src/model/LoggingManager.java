package model;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * A class that is responsible for updating a user-facing log of all changes made to images through
 * the program, and creating temporary logs that record changes made to the master collection of
 * tags, and to image files. If the program crashes, these later two logs can be used to ensure that
 * the master collection of tags and the image path to old image names map are updated to reflect
 * the changes made prior to the program crashing (this includes if the program crashed for multiple
 * consecutive sessions). In addition, this class is responsible for reading and writing the files
 * containing the serialized master list of tags stored by the program, and the serialized image
 * path to list of old names map, which allow for this data to persist between the closing and
 * re-opening of the program.
 */
class LoggingManager {

  /** Store the location of the user-facing log file, as a String. */
  private String logFileLocation;

  /**
   * Store the location, as a String, of the temporary log file that is responsible for recording
   * changes made to images through the program.
   */
  private String imagesTempFileLocation;

  /**
   * Store the location, as a String, of the temporary log file that is responsible for recording
   * changes made to the program's master collection of tags.
   */
  private String tagTempFileLocation;

  /**
   * Store the location, as a String, of the serialized image path to list of old names map file.
   */
  private String serializedImageHistoryLocation;

  /** Store the location, as a String, of serialized tags list file. */
  private String serializedTagsLocation;

  /** Store the image history temporary log file. */
  private File imagesTempFile;

  /** Store the master tag list history temporary log file. */
  private File tagTempFile;

  /** Store the serialize image path to list of old names map file. */
  private File serializedImagesHistoryFile;

  /** Store the serialized master list of tags file. */
  private File serializedTagsFile;

  /** Store the BufferedWriter that writes to the user-facing log file. */
  private BufferedWriter logFileWriter;

  /** Store the BufferedWriter that writes to the image history temporary log file. */
  private BufferedWriter imagesTempWriter;

  /** Store the BufferedWriter that writes to the master tag collection temporary log file. */
  private BufferedWriter tagTempWriter;

  /**
   * Create a new LoggingManager object, given locations to store and read the necessary files
   *
   * @param logFileLocation location of user-facing log file, as a String.
   * @param imagesTempFileLocation location of temporary image history log file, as a String.
   * @param tagTempFileLocation location of temporary tags log file, as a String.
   * @param serializedImageHistoryLocation location of serialized image history file, as a String.
   * @param serializedTagsLocation location of serialized tags list file, as a String.
   */
  LoggingManager(
      String logFileLocation,
      String imagesTempFileLocation,
      String tagTempFileLocation,
      String serializedImageHistoryLocation,
      String serializedTagsLocation) {
    /* Initialize all locations and files. */
    this.logFileLocation = logFileLocation;
    this.imagesTempFileLocation = imagesTempFileLocation;
    this.tagTempFileLocation = tagTempFileLocation;
    this.serializedImageHistoryLocation = serializedImageHistoryLocation;
    this.serializedTagsLocation = serializedTagsLocation;
    File logFile = new File(logFileLocation);
    imagesTempFile = new File(imagesTempFileLocation);
    tagTempFile = new File(tagTempFileLocation);
    serializedImagesHistoryFile = new File(serializedImageHistoryLocation);
    serializedTagsFile = new File(serializedTagsLocation);

    /* If log files do not exist, attempt to create them.  Note: the log files are only deleted when the
     * program is successfully closed.  Therefore, if the program crashes multiple times in a row, the data from
     * the sessions prior to the most recent one will also be retained.  */
    if (!logFile.exists()) {
      try {
        logFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (!imagesTempFile.exists()) {
      try {
        imagesTempFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (!tagTempFile.exists()) {
      try {
        tagTempFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    /* Create writers for all log files. */
    try {
      logFileWriter = new BufferedWriter(new FileWriter(logFile, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      imagesTempWriter = new BufferedWriter(new FileWriter(imagesTempFile, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      tagTempWriter = new BufferedWriter(new FileWriter(tagTempFile, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Log a change to an image, in both the user-facing log and the temporary image history log. If
   * no change occurred, write nothing.
   *
   * @param oldPath a path to an image, as a String.
   * @param newPath a new path to that image, as a String.
   */
  void writeImageUpdateToLog(String oldPath, String newPath) {
    if (!oldPath.equals(newPath)) {
      /* Write changes to temporary image history log. */
      writeToLog(imagesTempWriter, oldPath + "," + newPath);
      /* Write changes to the user-facing log file. */
      String toWrite;
      if (!PathFormatter.getDirectory(oldPath).equals(PathFormatter.getDirectory(newPath))) {
        /* Image was moved */
        toWrite = "Moved image at location:\"" + oldPath + "\" to location:\"" + newPath + "\"";
      } else {
        /* Image was not moved*/
        toWrite =
            "Image:\""
                + PathFormatter.getFullName(oldPath)
                + "\" renamed to:\""
                + PathFormatter.getFullName(newPath)
                + "\"";
      }
      writeToLog(logFileWriter, (new Timestamp((new Date()).getTime())).toString() + " " + toWrite);
    }
  }

  /**
   * Write to the temporary master tag collection history log that a new tag, tag, has been added to
   * the master collection of tags.
   *
   * @param tag a tag.
   */
  void writeTagAddedToLog(String tag) {
    writeToLog(tagTempWriter, "a:" + tag);
  }

  /**
   * Write to the temporary master tag collection history log that a tag, tag, was removed from the
   * master collection of tags.
   *
   * @param tag a tag.
   */
  void writeTagRemovedToLog(String tag) {
    writeToLog(tagTempWriter, "r:" + tag);
  }

  /**
   * Write to a log, given a writer and a string line of what to write.
   *
   * @param writer a writer used to write to the log.
   * @param line a string to write to the log as a new line.
   */
  private void writeToLog(BufferedWriter writer, String line) {
    try {
      writer.write(line);
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Return the Map that was serialized in the serialized image histories file, or an empty map if
   * this object does not exist.
   *
   * @return a map from image paths, as Strings, to lists of old names.
   */
  @SuppressWarnings("unchecked")
  Map<String, List<String>> getSerializedImageHistories() {
    Object serializedImages =
        readSerializedFile(serializedImageHistoryLocation, serializedImagesHistoryFile);
    if (serializedImages != null) {
      return (Map<String, List<String>>) serializedImages;
    } else {
      return new HashMap<>();
    }
  }

  /**
   * Return the list of tags that is serialized in the serialized tags list file, or an empty list
   * if this object does not exist.
   *
   * @return a list of tags.
   */
  @SuppressWarnings("unchecked")
  List<String> getSerializedOldTags() {
    Object serializedTags = readSerializedFile(serializedTagsLocation, serializedTagsFile);
    if (serializedTags != null) {
      return (List<String>) serializedTags;
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Return the object that is serialized in the file serializedFile, at location, location.
   *
   * @param location a location of a file with a serialized object, as a String.
   * @param serializedFile the File.
   * @return the Object serialized at the file.
   */
  private Object readSerializedFile(String location, File serializedFile) {
    Object serializedObject = null;
    if (serializedFile.exists()) {
      try {
        InputStream file = new FileInputStream(location);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);
        serializedObject = input.readObject();
        input.close();
      } catch (ClassNotFoundException | IOException e) {
        System.err.println("Error reading serialized File");
        e.printStackTrace();
      }
    }
    return serializedObject;
  }

  /**
   * Return a list of all lines in the temporary master tag collection history log file.
   *
   * @return a list of lines, corresponding to changes to the master tag collection.
   */
  List<String> readTagTempFile() {
    return readFileAsList(tagTempFileLocation);
  }

  /**
   * Return a list of all lines in temporary image history log file.
   *
   * @return a list of lines, corresponding to changes to images.
   */
  List<String> readImagesTempFile() {
    return readFileAsList(imagesTempFileLocation);
  }

  String readLogFile() {
    return readFileAsString(logFileLocation);
  }

  /**
   * Return a list of lines in a file at a given location.
   *
   * @param location a location of a text file, as a String.
   * @return a list of the lines that the file contains.
   */
  private List<String> readFileAsList(String location) {
    List<String> lines = new ArrayList<>();
    try {
      String line;
      FileReader fileReader = new FileReader(location);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      while ((line = bufferedReader.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  /**
   * Return the contents of a text file as a string, with new line characters at the location of
   * every new line in the file.
   *
   * @param location a location of a text file, as a String.
   * @return a String with the contents of this file.
   */
  private String readFileAsString(String location) {
    StringBuilder content = new StringBuilder();
    try {
      String line;
      FileReader fileReader = new FileReader(location);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      while ((line = bufferedReader.readLine()) != null) {
        content.append(line);
        content.append(System.getProperty("line.separator"));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return content.toString();
  }

  /**
   * End all logging. Serialize the map from images paths, as Strings, to list of old names, and the
   * master list of tags being stored by the program. Delete all temporary logs. Note: this method
   * is called when the program is being closed successfully.
   *
   * @param imageHistories Map of image histories to be serialized
   * @param tags List of tags to be serialized
   */
  void endLogging(Map<String, List<String>> imageHistories, List<String> tags) {
    /* Serialize objects. */
    writeSerializedFile(
        imageHistories, serializedImagesHistoryFile, serializedImageHistoryLocation);
    writeSerializedFile(tags, serializedTagsFile, serializedTagsLocation);
    /* Close all writers. */
    try {
      logFileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      imagesTempWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      tagTempWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    /* Delete temporary log files. */
    imagesTempFile.delete();
    tagTempFile.delete();
  }

  /**
   * Replace serialized file at a location, location, with a new file containing o, serialized.
   *
   * @param o an object to serialize.
   * @param serializedFile a file to serialize to.
   * @param location the location of the file, as a String.
   */
  private void writeSerializedFile(Object o, File serializedFile, String location) {
    if (serializedFile.exists()) {
      serializedFile.delete();
    }
    try {
      OutputStream file = new FileOutputStream(location);
      OutputStream buffer = new BufferedOutputStream(file);
      ObjectOutput output = new ObjectOutputStream(buffer);
      output.writeObject(o);
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
