package drivenow.zephyrdemo.zephyr;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import drivenow.zephyrdemo.pluginapi.PluginApi;
import io.zephyr.api.ModuleEvents;
import io.zephyr.api.ServiceReference;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.core.Kernel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

      var kernel = zephyr.getKernel();

      AtomicBoolean failed = new AtomicBoolean(false);
      CountDownLatch l = new CountDownLatch(1);
      zephyr.getKernel().addEventListener((type,event) -> {
        failed.set(true);
        l.countDown();
      },  ModuleEvents.INSTALL_FAILED, ModuleEvents.RESOLUTION_FAILED, ModuleEvents.START_FAILED);

      zephyr.getKernel().addEventListener((type,event) -> {
        l.countDown();
      },  ModuleEvents.STARTED);

      var api = MavenFile.providedPluginJar("drivenow:zephyr-embedded-demo-api:0.0.0").toURI().toURL();
      var plugin = MavenFile.moduleFile("zerphyr", "plugins", "zephyr-embedded-demo-plugin.war").toURI().toURL();

      zephyr.install(api, plugin);

      var plugins = zephyr.getPluginCoordinates().stream().map(c->c.toCanonicalForm()).collect(Collectors.toList());
      
      zephyr.start(plugins);

      l.await();

      if (failed.get()) {
        failedCount++;
      }
      else {
        passedCount++;


        var sr = zephyrServiceRef(kernel, PluginApi.class, Optional.empty());

        Assertions.assertThat(sr.orElseThrow().getDefinition().get().concat("a","b")).isEqualTo("ab");
      }

    }

    log.info(("passedCount " + passedCount + " failedCount " + failedCount));
    if (passedCount == 0) {
      fail();
    }





  }



  private static <P> Optional<ServiceReference<P>> zephyrServiceRef(Kernel kernel, Class<P> serviceClass, Optional<String> pluginName) {

    return kernel.getModuleManager().getModules()
    .stream()
    .map(module->{

      var r = kernel.getServiceRegistry().getRegistrations(module);
      if (r == null) return null;
      return r.getRegistrations().stream()
      .filter(sr->{

        return sr.getReference().getDefinition().getType().getName().equals(serviceClass.getName());
        
      })
      .filter(sr->pluginName.isEmpty() ? true : sr.getReference().getModule().getCoordinate().getName().equals(pluginName.get()))
      .map(sr->(ServiceReference<P>)sr.getReference())
      .findFirst()
      .orElse(null);
    })
    .filter(Objects::nonNull)
    .findFirst();
  }
 
}
