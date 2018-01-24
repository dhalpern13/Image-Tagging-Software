package model;

import java.util.*;

/** A class responsible for storing old names of images. */
public class ImageHistoryManager {

  /**
   * Store a map from a path, as a String, to a list of old names, where the lists do not contain
   * duplicates.
   */
  private Map<String, List<String>> imagePathToOldNamesList;

  /**
   * Create a new ImageHistoryManager object.
   *
   * @param imagePathToOldNamesList a map from paths, as Strings, to lists of old names.
   */
  public ImageHistoryManager(Map<String, List<String>> imagePathToOldNamesList) {
    this.imagePathToOldNamesList = imagePathToOldNamesList;
  }

  /**
   * If the image history manager has an old names list for this image, then update the key for the
   * image's map entry to its new path. Furthermore, if the image has a new name, and this image
   * history manager is not already storing this name, then store the old name.
   *
   * @param oldPath a path, as a String.
   * @param newPath a path, as a String.
   */
  public void updateImage(String oldPath, String newPath) {
    String oldName = PathFormatter.getFullName(oldPath);
    String newName = PathFormatter.getFullName(newPath);
    /*
     * Handle the case where an image's path has changed, and this image history manager has a List
     * of old names for this image.
     */
    if (imagePathToOldNamesList.containsKey(oldPath)) {
      /*
       * Change the key that points to this image's list of old names to the image's new path.
       */
      imagePathToOldNamesList.put(newPath, imagePathToOldNamesList.remove(oldPath));
      /*
       * Handle the case where the image's name has changed, and this image history manager's list of old names
       * for the image does not contain this name.
       */
      if (!oldName.equals(newName) && !imagePathToOldNamesList.get(newPath).contains(oldName)) {
        imagePathToOldNamesList.get(newPath).add(oldName);
      }
      /*
       * Handle the case where the program does not already have a list of old names for the image,
       * and the image's name has changed.  If the image's path has changed, but its name has not changed (i.e.
       * it was moved), then do nothing.
       */
    } else if (!oldName.equals(newName)) {
      List<String> thisImagesOldNames = new ArrayList<>();
      thisImagesOldNames.add(oldName);
      imagePathToOldNamesList.put(newPath, thisImagesOldNames);
    }
  }

  /**
   * Add an entry in the path to old names map that with key, path, and value, an empty list.
   *
   * @param path a path, as a String
   */
  public void addImage(String path) {
    if (!imagePathToOldNamesList.containsKey(path)) {
      imagePathToOldNamesList.put(path, new ArrayList<>());
    }
  }

  /**
   * Remove the entry in the path to old names list map at path, path. If the path to old names list
   * map does not contain path as a key, then do nothing.
   *
   * @param path a path, as a String.
   */
  public void removeImage(String path) {
    imagePathToOldNamesList.remove(path);
  }

  /**
   * Return a list of this image's old names. If this image history manager does not have a list of
   * old names for this image, then return an empty list.
   *
   * @param path a path, as a String.
   * @return a list of names.
   */
  public List<String> getImagesOldNames(String path) {
    if (imagePathToOldNamesList.containsKey(path)) {
      /*
       * Return a shallow copy of the list of old names stored by this ImageHistoryManager.
       */
      return new ArrayList<>(imagePathToOldNamesList.get(path));
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Return a set of paths, as Strings, that this image history manager has old names lists for
   * (including the images that it has only empty lists for).
   *
   * @return a Set of paths, as Strings.
   */
  public Set<String> getAllImages() {
    /*
     * Return a shallow copy of the key set for imagePathToOldNames map.
     */
    return new HashSet<>(imagePathToOldNamesList.keySet());
  }

  /**
   * Return the map that maps paths of images, as Strings, to lists of old names.
   *
   * @return a map from paths, as Strings, to lists of old names.
   */
  public Map<String, List<String>> getMap() {
    return imagePathToOldNamesList;
  }
}
