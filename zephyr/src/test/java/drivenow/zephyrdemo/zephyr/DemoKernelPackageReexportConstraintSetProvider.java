package drivenow.zephyrdemo.zephyr;

import java.util.Set;

import io.zephyr.kernel.core.KernelPackageReexportConstraintSetProvider;

public class DemoKernelPackageReexportConstraintSetProvider implements KernelPackageReexportConstraintSetProvider {

  @Override
  public Mode getMode() {
    return Mode.Include;
  }

  @Override
  public Set<String> getPackages() {
    // return Set.of("drivenow.zephyrdemo.test", "drivenow.zephyrdemo.pluginapi.*");
    // return Set.of( "drivenow.zephyrdemo.pluginapi.*");
    return Set.of();
  }

  @Override
  public int compareTo(KernelPackageReexportConstraintSetProvider arg0) {
    return Integer.compare(this.getPrecedence(), arg0.getPrecedence());
  }

  
}
