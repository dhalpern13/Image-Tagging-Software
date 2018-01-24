package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * A class that enables the user to add tags to image files, store a collection of tags whose
 * existence does not depend on their being used by any images, and view the old names of image
 * files that have been edited via this class.
 */
public class ImageTaggingManager {

  /**
   * Store an instance of a class responsible for storing the old names for a collection of images.
   */
  private ImageHistoryManager imageHistoryManager;

  /** Store the an instance of a class responsible for managing a collection of tags. */
  private TagManager tagManager;

  /**
   * Store an instance of a class that is responsible for logging and ensuring that data persists
   * between the closing and re-opening the program.
   */
  private LoggingManager loggingManager;

  /**
   * Store a sorted list of the paths, as Strings, of all of the image files at and below current
   * directory.
   */
  private List<String> allImagePaths = new ArrayList<>();

  /**
   * Store a sorted list of the paths, as Strings, of all of the image files at and below the
   * current directory that contain all tags in tagsToFilterBy.
   */
  private List<String> filteredImagePaths = new ArrayList<>();

  /**
   * Store a Collection of tags, such that each of the images whose paths are stored in
   * filteredImagePaths have all of these tags.
   */
  private List<String> tagsToFilterBy = new ArrayList<>();

  /**
   * Store the path to the directory, as a String, such that the images that the image tagging
   * manager is presently interacting with are the images that are at or below this directory.
   */
  private String currentDirectoryPath;

  /**
   * Create a new ImageTagging Manager object that interacts with the images at and below the
   * directory at path directoryPath.
   *
   * @param imageHistoryManager an ImageHistoryManager object.
   * @param tagManager a TagManager object.
   * @param loggingManager a LoggingManager object.
   */
  ImageTaggingManager(
      ImageHistoryManager imageHistoryManager,
      TagManager tagManager,
      LoggingManager loggingManager) {
    this.imageHistoryManager = imageHistoryManager;
    this.tagManager = tagManager;
    this.loggingManager = loggingManager;
  }

  /**
   * Change the directory, such that the images that this image tagging manager is currently
   * interacting with are at or below this directory. Note: when this method is called, any tag
   * filters that were previously in effect are removed.
   *
   * @param directoryPath a path to a directory, as a String.
   */
  public void changeDirectory(String directoryPath) {
    allImagePaths.clear();
    filteredImagePaths.clear();
    tagsToFilterBy.clear();
    currentDirectoryPath = directoryPath;
    if (directoryPath != null) {
      try {
        /* Add all of the paths of the image files at and below the current directory to allImagePaths. */
        generateImagesHelper(Paths.get(currentDirectoryPath));
      } catch (IOException e) {
        System.err.println(
            "ImageTaggingManager was unable to get the images at and below the supplied directory.");
      }

      /* Sort allImagePaths alphabetically, based on the name of the image. */
      sortByName(allImagePaths);

      /*
       * Make filteredImagePaths equal to a shallow copy of allImagePaths, in accordance with their being
       * no filters in effect after the directory has been changed.
       */
      filteredImagePaths = new ArrayList<>(allImagePaths);
    }
  }

  /**
   * Sort a list of image paths, as Strings, in alphabetical order based on their file names,
   * ignoring case.
   *
   * @param images a set of image paths, as Strings.
   */
  private void sortByName(List<String> images) {
    images.sort(
        (a, b) -> PathFormatter.getFullName(a).compareToIgnoreCase(PathFormatter.getFullName(b)));
  }

  /**
   * Add the paths of all of the images at and below dirPath, as Strings, to allImagePaths.
   *
   * @param dirPath a Path of a directory.
   * @throws IOException if the a Path does not correspond to a location in the computer's file
   *     system.
   */
  private void generateImagesHelper(Path dirPath) throws IOException {
    DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath);
    for (Path entryPath : directoryStream) {
      if (Files.isDirectory(entryPath)) {
        generateImagesHelper(entryPath);
      } else if (entryPath.toString().toLowerCase().matches("(.)+\\.(png|jpg|jpeg)$")) {
        String imagePath = entryPath.toString();
        allImagePaths.add(imagePath);
        /* If the image contains a tag, and the image history manager is not already storing data on it,
         * add its path to the image path to list of old names map that the image history manager is storing,
         * with an empty list.  If the user chooses to delete a tag from all images that the program
         * has interacted with, the program will use the key set for this map to find all of the images to
         * delete the tag from.  Therefore, the path to any image that contains a tag must be a key in the
         * map, even if the program does not have a list of old names for that image.
         */
        if (PathFormatter.containsATag((imagePath))) {
          imageHistoryManager.addImage(imagePath);
          /* Ensure that any tags added outside of the program are added to the master collection. */
          for (String tag : getImagesTags(imagePath)) {
            addTagToMasterCollection(tag);
          }
        }
      }
    }
  }

  /* Managed Image Methods */

  /**
   * Return a sorted list (alphabetically based on the file name, ignoring case) of paths to images,
   * as Strings, that are under the management of this ImageTaggingManager and contain of all of the
   * tags that are being filtered for.
   *
   * @return a list of paths, as Strings.
   */
  public List<String> getImagePaths() {
    return new ArrayList<>(filteredImagePaths);
  }

  /**
   * Return the path to the root directory of all of the images that this ImageTaggingManager is
   * currently interacting with, as a String.
   *
   * @return a path to a directory, as a String.
   */
  public String getCurrentDirectoryPath() {
    return currentDirectoryPath;
  }

  /**
   * Add a tag filter. Now, when the user calls the method getImagePaths, all of the paths in the
   * list that is returned will correspond to images that have this tag.
   *
   * @param tag a tag.
   */
  public void addTagFilter(String tag) {
    if (!tagsToFilterBy.contains(tag)) tagsToFilterBy.add(tag);
    tagsToFilterBy.sort(String::compareToIgnoreCase);
    HashSet<String> pathsToRemove = new HashSet<>();
    for (String path : filteredImagePaths) {
      if (!PathFormatter.hasTags(path, tag)) {
        pathsToRemove.add(path);
      }
    }
    filteredImagePaths.removeAll(pathsToRemove);
  }

  /**
   * Remove a tag filter. Now, when the user calls the method getImagePaths, the paths in the list
   * that is returned will no longer necessarily correspond to images that have this tag.
   *
   * @param tag a tag.
   */
  public void removeTagFilter(String tag) {
    tagsToFilterBy.remove(tag);
    /* Handle the case where there are no longer any tags remaining to filter by. */
    if (tagsToFilterBy.size() == 0) {
      /* Set filteredImagePaths equal to a shallow copy of allImagePaths. */
      filteredImagePaths = new ArrayList<>(allImagePaths);
    }
    /* Handle the case where there are still tags to filter by. */
    else {
      /* Clear filteredImagePaths, and then re-filter based on the remaining tags. */
      filteredImagePaths.clear();
      for (String path : allImagePaths) {
        if (PathFormatter.hasTags(path, tagsToFilterBy)) {
          filteredImagePaths.add(path);
        }
      }
    }
  }

  /* Image Editing Methods */

  /**
   * Add tag, tag to the image at path imagePath, add this tag to the program's master collection of
   * tags, if it is not already in this collection, and return the image's updated path, as a
   * String.
   *
   * @param imagePath a path to an image, as a String.
   * @param tag a tag.
   * @return an updated path to the image, as a String.
   */
  public String addTagToImage(String imagePath, String tag) {
    addTagToMasterCollection(tag);
    return updateImage(imagePath, PathFormatter.addTag(imagePath, tag));
  }

  /**
   * Remove the tag, tag, from the image at path imagePath, and return the image's updated path, as
   * a String.
   *
   * @param imagePath a path to an image, as a String.
   * @param tag a tag.
   * @return an updated path to the image, as a String.
   */
  public String removeTagFromImage(String imagePath, String tag) {
    return updateImage(imagePath, PathFormatter.removeTag(imagePath, tag));
  }

  /**
   * Rename the image at path imagePath to newName, and return the image's updated path, as a
   * String. Note: this will not modify the image's tags.
   *
   * @param imagePath a path to an image, as a String.
   * @param newName a new name.
   * @return an updated path for the image, as a String.
   */
  public String renameImage(String imagePath, String newName) {
    /*
     * If the user tries to rename an image to a name that contains tags, the program will store these tags
     * in the master collection, if it is not already doing so.
     */
    String newPath = updateImage(imagePath, PathFormatter.rename(imagePath, newName));
    tagManager.addTags(PathFormatter.getTags(newPath));
    return newPath;
  }

  /**
   * Move the image at path imagePath to the directory at directoryPath, and return the image's new
   * path, as a String.
   *
   * @param imagePath a path to an image, as a String.
   * @param directoryPath a path to a directory, as a String.
   * @return an updated path to the image, as a String.
   */
  public String moveImage(String imagePath, String directoryPath) {
    return updateImage(imagePath, PathFormatter.move(imagePath, directoryPath));
  }

  /**
   * Change the full name of the image at path imagePath to the name fullName. Note: the image's
   * tags will be changed to those tags, if any, that are in fullName.
   *
   * @param imagePath a path to an image, as a String.
   * @param fullName a full name.
   * @return an updated path to the image, as a String.
   */
  public String revertToOldName(String imagePath, String fullName) {
    String newPath = PathFormatter.renameFullName(imagePath, fullName);
    /*
     * If the new name contains tags that this ImageTaggingManager is not currently storing, store them.
     */
    tagManager.addTags(PathFormatter.getTags(newPath));
    return updateImage(imagePath, newPath);
  }

  /**
   * Change the image's path in the computer's file system from oldPath to newPath, log these
   * changes, update the image's history, and update the lists containing the all image paths at and
   * below the current directory.
   *
   * @param oldPath an old path to an image, as a String.
   * @param newPath a new path to an image, as a String.
   * @return an updated path to the image, as a String.
   */
  private String updateImage(String oldPath, String newPath) {
    if (!oldPath.equals(newPath)) {
      File destination = new File(newPath);
      if (destination.exists()) {
        String imageName = PathFormatter.getName(newPath);
        int index = 1;
        while (destination.exists()) {
          newPath = PathFormatter.rename(newPath, imageName + " copy " + index);
          destination = new File(newPath);
          index++;
        }
      }
      try {
        Files.move(Paths.get(oldPath), Paths.get(newPath));
        loggingManager.writeImageUpdateToLog(oldPath, newPath);
        imageHistoryManager.updateImage(oldPath, newPath);
        /*
         * Remove the old path from the lists of image paths.
         */
        if (currentDirectoryPath != null) {
          allImagePaths.remove(oldPath);
          filteredImagePaths.remove(oldPath);
          /*
           * Only add new path if the image is located at or below current directory.
           */
          if (PathFormatter.getDirectory(newPath).startsWith(currentDirectoryPath)) {
            allImagePaths.add(newPath);
            sortByName(allImagePaths);
            /*
             * Only add new path to list of paths of filtered images if the image has all of the
             * tags that are being filtered for.
             */
            if (PathFormatter.hasTags(newPath, tagsToFilterBy)) {
              filteredImagePaths.add(newPath);
              sortByName(filteredImagePaths);
            }
          }
        }
      } catch (IOException e) {
        System.err.print("Image could not be moved");
        e.printStackTrace();
      }
    }
    return newPath;
  }

  /**
   * Return the path to the directory of the image at imagePath, as a String.
   *
   * @param imagePath a path to an image, as a String.
   * @return a path to a directory, as a String.
   */
  public String getImagesDirectory(String imagePath) {
    return PathFormatter.getDirectory(imagePath);
  }

  /**
   * Return an alphabetically sorted list of the tags that the image at imagePath has.
   *
   * @param imagePath a path to an image, as a String.
   * @return a sorted list of tags.
   */
  public List<String> getImagesTags(String imagePath) {
    return PathFormatter.getTags(imagePath);
  }

  /**
   * Return an alphabetically sorted list of all tags, such that at least one image that is located
   * at one of the paths in the collection has each of these tags.
   *
   * @param images a Collection of image paths, as Strings.
   * @return a sorted list of tags.
   */
  public List<String> getImagesTags(Collection<String> images) {
    Set<String> tags = new HashSet<>();
    for (String image : images) {
      tags.addAll(getImagesTags(image));
    }
    List<String> tagsAsList = new ArrayList<>(tags);
    tagsAsList.sort(String::compareToIgnoreCase);
    return tagsAsList;
  }

  /**
   * Return the name of the image at imagePath. Note: the name does not contain the tags or the file
   * extension.
   *
   * @param imagePath a path to an image, as a String.
   * @return a name.
   */
  public String getImagesName(String imagePath) {
    return PathFormatter.getName(imagePath);
  }

  /**
   * Return a list of this image's old names, without any duplicates. If the program is not storing
   * any old names for the image, then return an empty list.
   *
   * @param imagePath a path to an image, as a String.
   * @return a list of old names.
   */
  public List<String> getImagesHistory(String imagePath) {
    return imageHistoryManager.getImagesOldNames(imagePath);
  }

  /* Tag Methods */

  /**
   * Store tag, tag, such that it will persist even if it is not being used by any images.
   *
   * @param tag a tag.
   */
  public void addTagToMasterCollection(String tag) {
    tagManager.addTags(tag);
    loggingManager.writeTagAddedToLog(tag);
  }

  /**
   * Stop storing tag, tag, and remove it from all images that the program has ever interacted with,
   * and whose paths have not been changed outside of the program in the interim.
   *
   * @param tag a tag.
   */
  public void removeTagFromMasterCollectionAndDeleteFromAllImages(String tag) {
    for (String image : imageHistoryManager.getAllImages()) {
      removeTagFromImage(image, tag);
    }
    removeTagFromMasterCollection(tag);
  }

  /**
   * Stop storing tag, tag, in the master collection of tags. Note: If the user selects a directory
   * to view and edit images using the program, and there is an image with this tag at or below that
   * directory, the tag will be added back into the master collection.
   *
   * @param tag a tag.
   */
  public void removeTagFromMasterCollection(String tag) {
    tagManager.removeTag(tag);
    loggingManager.writeTagRemovedToLog(tag);
  }

  /**
   * Return a list, sorted alphabetically, of all of the tags that the program is storing.
   *
   * @return a sorted list of tags.
   */
  public List<String> getAllTagsInMasterCollection() {
    return tagManager.getTags();
  }

  /**
   * Return a list, sorted alphabetically, of the tags the program is storing that contain a given
   * string, excluding tags that are in the collection of tags, tagsToExclude.
   *
   * @param tagsToExclude Collection of tags to exclude
   * @param mustContain String all tags must contain
   * @return Sorted list of all tags the program is storing that contain mustContain and excluding
   *     tags in tagsToExclude
   */
  public List<String> getAllTagsInMasterCollection(
      Collection<String> tagsToExclude, String mustContain) {
    return tagManager.getTags(tagsToExclude, mustContain);
  }

  /**
   * Return the list of the current tag filters.
   *
   * @return a list of tags.
   */
  public List<String> getFilteredTags() {
    return this.tagsToFilterBy;
  }

  /**
   * Return a list, sorted alphabetically, of all of the tags that the program is storing, and that
   * the image at path pathImage does not currently have.
   *
   * @param imagePath a path to an image.
   * @return a sorted list of tags.
   */
  public List<String> getAllAvailableTagsForImage(String imagePath) {
    return tagManager.getTags(PathFormatter.getTags(imagePath));
  }

  /**
   * Return the subset of the list of tags, tags, such that every tag in the subset is on all of the
   * images at the paths in the collection of paths, images.
   *
   * @param tags a list of tags.
   * @param images collection of paths, as Strings.
   * @return a list of tags.
   */
  public List<String> getTagsAllImagesContain(List<String> tags, Collection<String> images) {
    List<String> tagsAllContain = new ArrayList<>();
    for (String tag : tags) {
      boolean inAll = true;
      for (String image : images) {
        if (!PathFormatter.hasTags(image, tag)) {
          inAll = false;
        }
      }
      if (inAll) {
        tagsAllContain.add(tag);
      }
    }
    return tagsAllContain;
  }

  /* File System Methods */

  /**
   * Return a list of lines in the log file that records all of the changes made to images files
   * through the program.
   *
   * @return list of lines corresponding to changes made to images through the program.
   */
  public String readLogFile() {
    return loggingManager.readLogFile();
  }

  /**
   * Close the log files, and serialize the list of tags that the program is storing, and the map
   * from image path to list of old names, so that they will be preserved for when the program is
   * run again.
   */
  public void saveData() {
    loggingManager.endLogging(imageHistoryManager.getMap(), tagManager.getTags());
  }
}
