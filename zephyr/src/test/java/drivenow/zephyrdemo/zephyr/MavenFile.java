package drivenow.zephyrdemo.zephyr;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.zephyr.kernel.core.ModuleCoordinate;
import lombok.SneakyThrows;

public class MavenFile {
  
  @SneakyThrows
  public static File pluginFile(String module, String name) {
    return moduleFile(module, "plugins", name);
  }

  @SneakyThrows
  public static File exporterFile(String module, String name) {
    return moduleFile(module, "exporter", name);
  }

  @SneakyThrows
  public static File moduleFile(String module, String folder, String name) {
    var file = new File(module + "/target/" + folder, name);
    if (!file.exists()) {
      file = new File("target/" + folder, name);
    }
    assert file.exists();
    return file;
  }

  /**
   * Provide a plugin api via the system classloaded instead of of a module isolated classloader so it can be shared between
   * other plugins and your test code.
   * 
   * It creates a zehpyr plugin jar with the given coordinates that contains no classes or libs
   * 
   * Why would you use this ?  In testing
   * 
   * You configure your project with the real plugin on your classpath (scope=test or provided) then you install 
   * an empty plugin created by this method into zephyr which will satisfy any dependencies on the specified coordinates.  
   * Then the plugin under test and your test code will use the classes from the app classloader instead of the isolated 
   * one that zephyr creates allowing you to use service references from your plugin under test in your test case.
   * 
   * @param coordinates
   * @return
   */
  @SneakyThrows
  public static File providedPluginJar(String coordinates) {
    var c = ModuleCoordinate.parse(coordinates);

    File file = File.createTempFile("plugin-", ".jar");
    file.delete();
    
    try(
      FileOutputStream fos = new FileOutputStream(file);
      ZipOutputStream zos = new ZipOutputStream(fos)
    ) {
    
      /* File is not on the disk, test.txt indicates
         only the file name to be put into the zip */
      ZipEntry entry = new ZipEntry("META-INF/MANIFEST.MF"); 
    
      zos.putNextEntry(entry);

      String type = "plugin";
      String manifest = "";
      manifest += "name: " + c.getName() + "\n";
      manifest += "group: " + c.getGroup() + "\n";
      manifest += "version: " + c.getVersion() + "\n";
      manifest += "type: " + type + "\n";

      zos.write(manifest.getBytes());
      zos.closeEntry();

      zos.finish();
      zos.flush();
      fos.flush();
      fos.close();
    
      } catch(IOException ioe) {
        ioe.printStackTrace();
        throw new RuntimeException(ioe);
      }
    
    file.deleteOnExit();
    return file;
  }

  @SneakyThrows
  public static String pomVersion(String moduleDirName) {
    var root = projectRoot();
    var pom = new File(new File(root, moduleDirName), "pom.xml");
    if (!pom.exists()) {
      throw new RuntimeException("module doesn't exist");
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(pom);
    Element project = (Element) doc.getElementsByTagName("project").item(0);
    Element version = (Element) project.getElementsByTagName("version").item(0);
    return version.getTextContent();
  }
  @SneakyThrows
  public static String pomVersion() {
    var pom = new File(projectRoot(), "pom.xml");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(pom);
    Element project = (Element) doc.getElementsByTagName("project").item(0);
    Element version = (Element) project.getElementsByTagName("version").item(0);
    return version.getTextContent();
  }
  
  @SneakyThrows
  public static String rootPomProperty(String name) {
    var pom = new File(projectRoot(), "pom.xml");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(pom);
    Element project = (Element) doc.getElementsByTagName("project").item(0);
    Element properties = (Element) project.getElementsByTagName("properties").item(0);
    Element property = (Element) properties.getElementsByTagName(name).item(0);
    return property.getTextContent();
  }

  static File projectRoot() {
    Path path = Path.of("").toAbsolutePath();
    while (path != null && !path.resolve("OWNERS").toFile().exists()) {
      path = path.getParent();
    }
    if (path != null) {
      return path.toFile();
    }
    throw new RuntimeException("cound not find project root");
  }
}
