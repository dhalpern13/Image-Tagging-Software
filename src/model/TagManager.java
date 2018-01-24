package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A class responsible for managing a collection of tags. */
public class TagManager {

  /** Store a list, sorted alphabetically, of all tags being managed by the manager. */
  private List<String> tags;

  /**
   * Create a new TagManager object given a list of tags.
   *
   * @param tagsList a list of tags.
   */
  public TagManager(List<String> tagsList) {
    tags = tagsList;
    /* Ensure tags is sorted. */
    tags.sort(String::compareToIgnoreCase);
  }

  /**
   * Store tag, tag, if this TagManager is not already storing it.
   *
   * @param tag a tag.
   */
  public void addTags(String tag) {
    if (!tags.contains(tag)) {
      tags.add(tag);
      /* Ensure tags list is sorted. */
      tags.sort(String::compareToIgnoreCase);
    }
  }

  /**
   * Store all of the tags in tagsToAdd.
   *
   * @param tagsToAdd a Collection of tags.
   */
  public void addTags(Collection<String> tagsToAdd) {
    for (String tag : tagsToAdd) {
      addTags(tag);
    }
  }

  /**
   * Stop storing tag, tag, if this TagManager is currently storing it.
   *
   * @param tag a tag to remove.
   */
  public void removeTag(String tag) {
    tags.remove(tag);
  }

  /**
   * Return a list, sorted alphabetically, of all tags under management.
   *
   * @return a sorted list of tags.
   */
  public List<String> getTags() {
    /* Return a shallow copy of tags. */
    return new ArrayList<>(tags);
  }

  /**
   * Return a list, sorted alphabetically, of tags of all tags under management that are not in the
   * List tagsToExclude, and that contain String mustContain.
   *
   * @param tagsToExclude a list of tags to exclude from the list that is returned.
   * @param mustContain a String that all tags in the list that is returned must contain.
   * @return a sorted list of tags.
   */
  public List<String> getTags(Collection<String> tagsToExclude, String mustContain) {
    List<String> filteredTags = new ArrayList<>();
    for (String tag : tags) {
      if (tag.toLowerCase().contains(mustContain.toLowerCase()) && !tagsToExclude.contains(tag)) {
        filteredTags.add(tag);
      }
    }
    return filteredTags;
  }

  /**
   * Return a list, sorted alphabetically, of all tags under management that are not in the List
   * tagsToExclude.
   *
   * @param tagsToExclude a list of tags to exclude from the list that is returned.
   * @return a sorted list of tags.
   */
  public List<String> getTags(Collection<String> tagsToExclude) {
    List<String> filteredTags = new ArrayList<>();
    for (String tag : tags) {
      if (!tagsToExclude.contains(tag)) {
        filteredTags.add(tag);
      }
    }
    return filteredTags;
  }
}
