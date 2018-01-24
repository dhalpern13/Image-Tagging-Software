package tests;

import model.PathFormatter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathFormatterTest {
  @Test
  void addExistingTagTest() throws Exception {
    String imagePath = "sports" + File.separator + "playoffs @jays.jpg";
    assertEquals(imagePath, PathFormatter.addTag(imagePath, "jays"));
  }

  @Test
  void addNonExistingTagTest() throws Exception {
    String imagePath = "sports" + File.separator + "playoffs @jays.jpg";
    String expectedPath = "sports" + File.separator + "playoffs @jays @baseball.jpg";
    assertEquals(expectedPath, PathFormatter.addTag(imagePath, "baseball"));
  }

  @Test
  void removeNonExistingTagTest() throws Exception {
    String imagePath = "seasons" + File.separator + "winter @ice @cold.jpg";
    assertEquals(imagePath, PathFormatter.removeTag(imagePath, "icecap"));
  }

  @Test
  void removeExistingTagTest() throws Exception {
    String imagePath = "seasons" + File.separator + "winter @ice @cold.jpg";
    String expectedPath = "seasons" + File.separator + "winter @cold.jpg";
    assertEquals(expectedPath, PathFormatter.removeTag(imagePath, "ice"));
  }

  @Test
  void renameTest() throws Exception {
    String imagePath = "photos" + File.separator + "vacation @Mexico.jpg";
    String expectedPath = "photos" + File.separator + "summer break @Mexico.jpg";
    assertEquals(expectedPath, PathFormatter.rename(imagePath, "summer break"));
  }

  @Test
  void moveTest() throws Exception {
    String curPath = "UofT" + File.separator + "student.jpg";
    String directory = "Schools" + File.separator + "Ryerson";
    String expectedPath = "Schools" + File.separator + "Ryerson" + File.separator + "student.jpg";
    assertEquals(expectedPath, PathFormatter.move(curPath, directory));
  }

  @Test
  void renameFullSameNameTest() throws Exception {
    String curPath = "Food" + File.separator + "pizza @party.jpg";
    assertEquals(curPath, PathFormatter.renameFullName(curPath, "pizza @party.jpg"));
  }

  @Test
  void renameFullNameWithTagsTest() throws Exception {
    String curPath = "Food" + File.separator + "pizza @party.jpg";
    String expectedPathWithNewTags = "Food" + File.separator + "cake @birthday.jpg";
    assertEquals(
        expectedPathWithNewTags, PathFormatter.renameFullName(curPath, "cake @birthday.jpg"));
  }

  @Test
  void renameFullNameWithoutTagsTest() throws Exception {
    String curPath = "Food" + File.separator + "pizza @party.jpg";
    String expectedPathWithoutTags = "Food" + File.separator + "sushi.jpg";
    assertEquals(expectedPathWithoutTags, PathFormatter.renameFullName(curPath, "sushi.jpg"));
  }

  @Test
  void getFullNameTest() throws Exception {
    String imagePath =
        "CSC207" + File.separator + "Final Project" + File.separator + "round earth.png";
    String expectedName = "round earth.png";
    assertEquals(expectedName, PathFormatter.getFullName(imagePath));
  }

  @Test
  void getTagsNoTagsTest() throws Exception {
    String noTagsImage = "cooking.png";
    assertEquals(Collections.emptyList(), PathFormatter.getTags(noTagsImage));
  }

  @Test
  void getTagsTest() throws Exception {
    String withTagsImage = "Holidays" + File.separator + "Christmas @joy @gifts @family.jpg";
    List<String> expectedTags = Arrays.asList("joy", "gifts", "family");
    Collections.sort(expectedTags);
    assertEquals(expectedTags, PathFormatter.getTags(withTagsImage));
  }

  @Test
  void getNoDirectoryTest() throws Exception {
    String noDirectoryImage = "cat.gif";
    assertEquals("", PathFormatter.getDirectory(noDirectoryImage));
  }

  @Test
  void getDirectoryTest() throws Exception {
    String withDirectoryImage = "Animals" + File.separator + "cat.gif";
    assertEquals("Animals", PathFormatter.getDirectory(withDirectoryImage));
  }

  @Test
  void getNameTest() throws Exception {
    String imagesPath = "Views" + File.separator + "sunset @toronto @glory.png";
    assertEquals("sunset", PathFormatter.getName(imagesPath));
  }

  @Test
  void hasTagTest() throws Exception {
    String imagesPath = "Pictures" + File.separator + "Wedding @vows @special.png";
    assertTrue(PathFormatter.hasTags(imagesPath, "vows"));
  }

  @Test
  void hasFalseTagTest() throws Exception {
    String imagesPath = "Pictures" + File.separator + "Wedding @vows @special.png";
    assertFalse(PathFormatter.hasTags(imagesPath, "specialDay"));
  }

  @Test
  void hasEmptyTagsTest() throws Exception {
    String imagesPath = "School Work" + File.separator + "Exams @UofTears @happyTimes.png";
    assertTrue(PathFormatter.hasTags(imagesPath, Collections.emptyList()));
  }

  @Test
  void hasAllTagsTest() throws Exception {
    String imagesPath = "School Work" + File.separator + "Exams @UofTears @happyTimes.png";
    List<String> tags = Arrays.asList("UofTears", "happyTimes");
    assertTrue(PathFormatter.hasTags(imagesPath, tags));
  }

  @Test
  void hasAllTagsFalseTest() throws Exception {
    String imagesPath = "School Work" + File.separator + "Exams @UofTears @happyTimes.png";
    List<String> nonTag = Collections.singletonList("UofTeary");
    assertFalse(PathFormatter.hasTags(imagesPath, nonTag));
  }

  @Test
  void containsNoTagTest() throws Exception {
    String noTag = "no tag here.png";
    assertFalse(PathFormatter.containsATag(noTag));
  }

  @Test
  void containsATagTest() throws Exception {
    String withTag = "Tags" + File.separator + "friends @tag";
    assertTrue(PathFormatter.containsATag((withTag)));
  }
}
