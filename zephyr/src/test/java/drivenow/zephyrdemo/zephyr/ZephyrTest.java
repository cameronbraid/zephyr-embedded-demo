package drivenow.zephyrdemo.zephyr;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.Test;

import io.zephyr.api.ModuleEvents;
import io.zephyr.cli.Zephyr;

public class ZephyrTest {

  @Test
  public void testEmbddedZephyr() throws Exception {

    var passedCount = 0;
    var failedCount = 0;

    for (int i = 0; i < 1; i++) {
      File home = Files.createTempDirectory("zephyr").toFile();

      var zephyr = Zephyr.builder()
      .homeDirectory(home)
      .create();

      zephyr.startup();

      AtomicBoolean failed = new AtomicBoolean(false);
      CountDownLatch l = new CountDownLatch(1);
      zephyr.getKernel().addEventListener((type,event) -> {
        failed.set(true);
        // System.out.println(type);
        // System.out.println(        event.getClass()
        // System.out.println(event.getTarget());
        l.countDown();
      },  ModuleEvents.INSTALL_FAILED, ModuleEvents.RESOLUTION_FAILED, ModuleEvents.START_FAILED);

      zephyr.getKernel().addEventListener((type,event) -> {
        l.countDown();
      },  ModuleEvents.STARTED);

      // cater for differing cwd : vsode in '/zephyr' and mvn in '/'
      var path = new File("zephyr/target/plugins/").exists() ? "zephyr/target/plugins/" : "target/plugins/"; 

      zephyr.install(new File(path + "zephyr-embedded-demo-api.war").toURI().toURL(), new File(path + "zephyr-embedded-demo-plugin.war").toURI().toURL());

      var plugins = zephyr.getPluginCoordinates().stream().map(c->c.toCanonicalForm()).collect(Collectors.toList());
      
      zephyr.start(plugins);

      l.await();

      if (failed.get()) {
        failedCount++;
      }
      else {
        passedCount++;
      }

    }

    System.out.println(("passedCount " + passedCount + " failedCount " + failedCount));
    if (passedCount == 0) {
      fail();
    }
  }

}
