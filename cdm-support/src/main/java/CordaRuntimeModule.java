import implementations.AllocateImpl;
import org.isda.cdm.CdmRuntimeModule;
import org.isda.cdm.functions.Allocate;


public class CordaRuntimeModule extends CdmRuntimeModule {

  @Override
  protected void configure() {
    super.configure();
    bind(Allocate.class).to(bindAllocate());
  }

  @Override
  protected Class<? extends Allocate> bindAllocate() {
    return AllocateImpl.class;
  }
}
