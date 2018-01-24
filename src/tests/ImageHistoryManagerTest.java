package tests;

import model.ImageHistoryManager;

import java.io.File;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageHistoryManagerTest {
  private ImageHistoryManager manager;
  private Map<String, List<String>> mapToCompare;

  @BeforeEach
  void setUp() throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    ArrayList<String> myList = new ArrayList<>();
    myList.add("Documents" + File.separator + "myPic @tag.jpg");
    map.put("Documents" + File.separator + "myPic.jpg", myList);
    manager = new ImageHistoryManager(map);
    mapToCompare = new HashMap<>(map);
  }

  @AfterEach
  void tearDown() throws Exception {
    manager = null;
  }

  @Test
  void updateImageNoKey() throws Exception {
    String oldPath = "Documents" + File.separator + "pic.jpg";
    String newPath = "Documents" + File.separator + "pic @tag.jpg";
    manager.updateImage(oldPath, newPath);
    assertTrue(manager.getMap().containsKey(newPath));
    assertTrue(manager.getMap().get(newPath).contains("pic.jpg"));
  }

  @Test
  void updateImageNoKeySameName() throws Exception {
    String oldPath = "Documents" + File.separator + "pic.jpg";
    String newPath = "Documents" + File.separator + "pic.jpg";
    manager.updateImage(oldPath, newPath);
    assertTrue(!manager.getMap().containsKey(newPath));
  }

  @Test
  void updateImageContainsKey() throws Exception {
    String oldPath = "Documents" + File.separator + "myPic.jpg";
    String newPath = "Documents" + File.separator + "pic.jpg";
    List<String> tempList = manager.getMap().get(oldPath);

    manager.updateImage(oldPath, newPath);
    assertTrue(manager.getMap().containsKey(newPath));
    assertTrue(!manager.getMap().containsKey(oldPath));
    assertTrue(manager.getMap().get(newPath).contains(tempList.get(0)));
    assertTrue(manager.getMap().get(newPath).contains("myPic.jpg"));
  }

  @Test
  void addImageNoKey() throws Exception {
    String path = "Documents" + File.separator + "newPath";
    manager.addImage(path);
    assertTrue(manager.getMap().containsKey(path));
  }

  @Test
  void addImageHasKey() throws Exception {
    String path = "Documents" + File.separator + "myPic.jpg";
    manager.addImage(path);
    assertEquals(manager.getMap(), mapToCompare);
  }

  @Test
  void removeImage() throws Exception {
    String toRemove = "Documents" + File.separator + "myPic.jpg";
    manager.removeImage(toRemove);
    assertTrue(!manager.getMap().containsKey(toRemove));
  }

  @Test
  void getImagesOldNames() throws Exception {
    String key = "Documents" + File.separator + "myPic.jpg";
    List<String> tempList = manager.getImagesOldNames(key);
    ArrayList<String> myList = new ArrayList<>();
    myList.add("Documents" + File.separator + "myPic @tag.jpg");
    assertEquals(tempList, myList);
  }

  @Test
  void getImagesOldNamesNoKey() throws Exception {
    String key = "Documents" + File.separator + "noPic.jpg";
    List<String> temp = manager.getImagesOldNames(key);
    assertEquals(temp, new ArrayList<String>());
  }

  @Test
  void getAllImages() throws Exception {
    Set<String> keys = manager.getAllImages();
    Set<String> expected = new HashSet<>();
    expected.add("Documents" + File.separator + "myPic.jpg");
    assertEquals(keys, expected);
  }

  @Test
  void getMap() throws Exception {
    assertEquals(manager.getMap(), mapToCompare);
  }
}
