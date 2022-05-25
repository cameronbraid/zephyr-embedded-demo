package drivenow.zephyrdemo.plugin;

import drivenow.zephyrdemo.pluginapi.Name;
import drivenow.zephyrdemo.pluginapi.PluginApi;
import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistration;

public class DemoActivator implements ModuleActivator {
  
  ServiceRegistration<PluginApi> sr;

  @Override
  public void start(ModuleContext context) throws Exception {
    sr = context.register(PluginApi.class, new PluginApi() {
      @Override
      public String concat(String arg0, String arg1) {
        return arg0 + arg1;
      }
      @Override
      public String joinName(Name name) {
        return name.getFirstName() + " " +name.getLastName();
      }
    });

  }

  @Override
  public void stop(ModuleContext context) throws Exception {
    if (sr != null) sr.close();
  }
}
