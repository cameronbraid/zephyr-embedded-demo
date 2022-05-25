package drivenow.zephyrdemo.plugin;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import drivenow.zephyrdemo.pluginapi.Name;
import drivenow.zephyrdemo.pluginapi.PluginApi;
import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistration;

public class DemoActivator implements ModuleActivator {
  
  ServiceRegistration<PluginApi> sr;
  ConfigurableApplicationContext ctx;
  @Override
  public void start(ModuleContext context) throws Exception {
    sr = context.register(PluginApi.class, new PluginApi() {
      @Override
      public String concat(String arg0, String arg1) {
        return arg0 + arg1;
      }
      @Override
      public String joinName(Name name) {
        return name.getFirstName() + " " + name.getLastName();
      }
    });


    ctx = SpringApplication.run(App.class);
  }

  @Override
  public void stop(ModuleContext context) throws Exception {
    if (ctx != null) ctx.close();
    if (sr != null) sr.close();
  }
}
