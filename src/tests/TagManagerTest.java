package tests;

import model.TagManager;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TagManagerTest {
  private TagManager tagManager;
  private List<String> tagList;

  @BeforeEach
  void setUp() {
    tagList = new ArrayList<>();
    tagList.add("storm");
    tagList.add("rain");
    tagList.add("winter");
    tagManager = new TagManager(tagList);
  }

  @AfterEach
  void tearDown() {
    tagManager = null;
  }

  @Test
  void addExistingTagTest() {
    tagManager.addTags("storm");
    assertEquals(tagList, tagManager.getTags());
  }

  @Test
  void addNewTagTest() {
    tagList.add("winter");
    tagManager.addTags("winter");
    assertEquals(tagList, tagManager.getTags());
  }

  @Test
  void addMultipleTagsTest() {
    Collection<String> tags = Arrays.asList("olympics", "chills");
    tagList.addAll(tags);
    tagManager.addTags(tags);
    assertEquals(tagList, tagManager.getTags());
  }

  @Test
  void addTagsWithExistingTagsTest() {
    Collection<String> tags = Arrays.asList("olympics", "storm");
    tagList.add("olympics");
    tagManager.addTags(tags);
    assertEquals(tagList, tagManager.getTags());
  }

  @Test
  void removeTagTest() {
    tagManager.removeTag("storm");
    assertFalse(tagManager.getTags().contains("storm"));
  }

  @Test
  void getTagsTest() {
    assertEquals(tagList, tagManager.getTags());
  }

  @Test
  void getFilteredTagsEmptyFilterTest() {
    assertEquals(tagList, tagManager.getTags(Collections.emptyList(), ""));
  }

  @Test
  void getFilteredTagsTest() {
    Collection<String> excludedTags = Arrays.asList("win", "storm");
    String mustContain = "in";
    List<String> expectedFilteredTags = Arrays.asList("rain", "winter");
    assertEquals(expectedFilteredTags, tagManager.getTags(excludedTags, mustContain));
  }

  @Test
  void getNoFilteredTagsTest() {
    Collection<String> excludedTags = Arrays.asList("win", "storm");
    assertEquals(Collections.emptyList(), tagManager.getTags(excludedTags, "none"));
  }

  @Test
  void getTagsExcludeAllNonExistingTagsTest() {
    Collection<String> tagsToExclude = Arrays.asList("snow", "freeze");
    assertEquals(tagList, tagManager.getTags(tagsToExclude));
  }

  @Test
  void getTagsExcludeTest() {
    Collection<String> tagsToExclude = Arrays.asList("winter", "snow");
    List<String> expectedFilteredTags = Arrays.asList("rain", "storm");
    assertEquals(expectedFilteredTags, tagManager.getTags(tagsToExclude));
  }
}
