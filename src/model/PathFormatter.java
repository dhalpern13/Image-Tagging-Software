package model;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A class responsible for generating properly formatted paths, and extracting information from
 * paths (e.g. what tags an image has, etc.).
 */
public class PathFormatter {

  /** Store the symbol to be used preceding a tag in an image path. */
  private static final String TAG_SYMBOL = " @";

  /** Store the symbol used before the extension of an image. */
  private static final String EXTENSION_SYMBOL = ".";

  /** Store the extension symbol as a regular expression. */
  private static final String EXTENSION_SYMBOL_REGEX = "\\.";

  /**
   * Return an updated path, as a String, with the tag, tag, added. If the image already has the
   * tag, do nothing and return the original path, as a String.
   *
   * @param imagePath a path to an image, as a String.
   * @param tag a tag.
   * @return the image's updated path, as a String.
   */
  public static String addTag(String imagePath, String tag) {
    /* Handle the case where the image already has tag. */
    if (hasTags(imagePath, tag)) {
      return imagePath;
      /* Handle the case where the image does not already have the tag. */
    } else {
      int extensionIndex = imagePath.lastIndexOf(EXTENSION_SYMBOL);
      /* Add tag before extension of image. */
      return imagePath.substring(0, extensionIndex)
          + TAG_SYMBOL
          + tag
          + imagePath.substring(extensionIndex);
    }
  }

  /**
   * Return an updated path, as a String, with tag, tag, removed. If the image does not have the
   * tag, do nothing and return the original path, as a String.
   *
   * @param imagePath a path to an image, as a String.
   * @param tag a tag.
   * @return the image's updated path, as a String.
   */
  public static String removeTag(String imagePath, String tag) {
    /* Handle the case where the image has the tag. */
    if (hasTags(imagePath, tag)) {
      int locationOfTag = imagePath.lastIndexOf(tag);
      /* Return path without the tag and TAG_SYMBOL before. */
      return imagePath.substring(0, locationOfTag - TAG_SYMBOL.length())
          + imagePath.substring(locationOfTag + tag.length());
      /* Handle the case where the image does not have the tag. */
    } else {
      return imagePath;
    }
  }

  /**
   * Return an updated path, as a String, with the new name. Note: this does not modify the image's
   * tags.
   *
   * @param imagePath a path to an image to update, as a String.
   * @param newName a name to rename image to.
   * @return the image's updated path, as a String.
   */
  public static String rename(String imagePath, String newName) {
    String fullName = getFullName(imagePath);
    String newFullName;
    /* Handle the case where the image has tags. */
    if (containsATag(imagePath)) {
      newFullName = newName + fullName.substring(fullName.indexOf(TAG_SYMBOL));
      /* Handle the case where the image does not have tags. */
    } else {
      newFullName = newName + fullName.substring(fullName.lastIndexOf(EXTENSION_SYMBOL));
    }
    return renameFullName(imagePath, newFullName);
  }

  /**
   * Return an updated path, as a String, with the new directory.
   *
   * @param imagePath path of an image to update, as a String.
   * @param directory a directory path, as a String.
   * @return the image's updated path, as a String.
   */
  public static String move(String imagePath, String directory) {
    return directory + File.separator + getFullName(imagePath);
  }

  /**
   * Return an updated path, as a string, where the full name has been changed to the new name.
   * Note: All previous tags have been removed and replaced with ones in the new name. newName must
   * include the image extension.
   *
   * @param imagePath a path to an image, as a String
   * @param newName a new full name.
   * @return the image's updated path, as a String.
   */
  public static String renameFullName(String imagePath, String newName) {
    return getDirectory(imagePath) + File.separator + newName;
  }

  /**
   * Return the full name of the image at imagePath, including any tags and its file extension.
   *
   * @param imagePath a path to an image, as a String.
   * @return the full name of the image, including all tags and its file extension.
   */
  public static String getFullName(String imagePath) {
    if (imagePath.contains(File.separator)) {
      /* Return part after the last file separator in path*/
      return imagePath.substring(imagePath.lastIndexOf(File.separator) + File.separator.length());
    } else {
      /* Image does not have a directory */
      return imagePath;
    }
  }

  /**
   * Return a list, sorted alphabetically, of all tags that the image at imagePath contains.
   *
   * @param imagePath a path to an image, as a String.
   * @return the full name of the image including any tags and its file extension.
   */
  public static List<String> getTags(String imagePath) {
    String fullName = getFullName(imagePath);
    if (containsATag(imagePath)) {
      /* Image has tags, split around tag symbol excluding the file extension. */
      String[] split =
          fullName.substring(0, fullName.lastIndexOf(EXTENSION_SYMBOL)).split(TAG_SYMBOL);
      /* Tags as a list. First element is name so begin at second.*/
      List<String> tags = Arrays.asList(split).subList(1, split.length);
      /* Sort the tags */
      tags.sort(String::compareToIgnoreCase);
      return tags;
    } else {
      /* Image has no tags */
      return Collections.emptyList();
    }
  }

  /**
   * Return this image's directory, as a String. If the imagePath does not have a directory, return
   * the empty string.
   *
   * @param imagePath a path to an image, as a String.
   * @return the path to the image's directory, as a String.
   */
  public static String getDirectory(String imagePath) {
    if (imagePath.contains(File.separator)) {
      /* Image has a directory, return substring of path without name */
      return imagePath.substring(0, imagePath.lastIndexOf(File.separator));
    } else {
      /* Image does not have a directory */
      return "";
    }
  }

  /**
   * Return the image's name, excluding any tags and the file extension.
   *
   * @param imagePath a path to an image, as a String.
   * @return the image's name without any tags or a file extension.
   */
  public static String getName(String imagePath) {
    String fullName = getFullName(imagePath);
    if (containsATag(imagePath)) {
      /* Image has tags */
      return fullName.substring(0, fullName.indexOf(TAG_SYMBOL));
    } else {
      /* Image does not have tags */
      return fullName.substring(0, fullName.lastIndexOf(EXTENSION_SYMBOL));
    }
  }

  /**
   * Return true if and only if the image at path has tag, tag.
   *
   * @param path a path of an image, as a String.
   * @param tag a tag to check.
   * @return a boolean value corresponding to whether image has tag, tag.
   */
  public static Boolean hasTags(String path, String tag) {
    return getFullName(path)
        .matches(
            "(.)+"
                + TAG_SYMBOL
                + tag
                + "("
                + TAG_SYMBOL
                + "|"
                + EXTENSION_SYMBOL_REGEX
                + ")"
                + "(.)+");
  }

  /**
   * Return true if and only if the image at path has all tags in the list of tags, tags.
   *
   * @param path a path to an image, as a String.
   * @param tags a Collection of tags.
   * @return a boolean value corresponding to whether image has all tags in tags.
   */
  public static Boolean hasTags(String path, Collection<String> tags) {
    for (String tag : tags) {
      if (!hasTags(path, tag)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return true if and only if the image contains a tag, where a tag is defined as a string with a
   * space, followed by '@', followed by at least one alphanumeric character.
   *
   * @param path a path to an image, as a String.
   * @return a boolean value.
   */
  public static Boolean containsATag(String path) {
    return getFullName(path).matches("(.)+" + TAG_SYMBOL + "[a-zA-Z0-9]" + "(.)+");
  }
}
