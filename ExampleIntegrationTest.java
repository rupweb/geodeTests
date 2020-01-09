package myPack;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.tests.integration.ForkingClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ExampleIntegrationTest.GeodeClientConfiguration.class)
public class ExampleIntegrationTest extends ForkingClientServerIntegrationTestsSupport {
	private static final Logger log = LogManager.getLogger(ExampleIntegrationTest.class);

  @BeforeClass
  public static void startGemFireServer() throws IOException {
	log.traceEntry();
	new LogCheck("ExampleIntegrationTest.startGemFireServer");
	  
    startGemFireServer(GeodeServerConfiguration.class);
  }

  @Resource(name = "Example")
  private Region<Integer, String> mockRegion;
  
  @Test
  public void simpleGetAndPutRegionOpsWork() {
	  log.traceEntry();

	  log.info("Putting test data");
	  
      this.mockRegion.put(1, "test");
      
  	  log.info("Checking test data");

      assertThat(mockRegion).containsKey(1);
      assertThat(mockRegion.get(1)).isEqualTo("test");
      
  	  log.info("Checked test data");
      
	  log.traceExit();
  }

  @CacheServerApplication(name = "ExampleIntegrationTest", logLevel = GEMFIRE_LOG_LEVEL)
  @EnableEntityDefinedRegions
  public static class GeodeServerConfiguration {

    public static void main(String[] args) {
    	log.traceEntry();
    	new LogCheck("ExampleIntegrationTest.main");
    	
        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(GeodeServerConfiguration.class);

              applicationContext.registerShutdownHook();
	}
  }

  @ClientCacheApplication
  @EnableGemFireMockObjects
  @EnableEntityDefinedRegions 
  public static class GeodeClientConfiguration { 
    @Bean("Example")
    ClientRegionFactoryBean<Integer, String> mockRegion(GemFireCache cache) {
  	    log.traceEntry();
  	    
  	    System.setProperty("spring.data.gemfire.cache.client.region.shortcut", "LOCAL");

        ClientRegionFactoryBean<Integer, String> mockRegion = new ClientRegionFactoryBean<>();

        mockRegion.setCache(cache);
        mockRegion.setShortcut(ClientRegionShortcut.PROXY);
        
  	    log.traceExit();

        return mockRegion;   
    }
  }
  
  static class LogCheck {  
	  public LogCheck(String clazz) {
		  log.trace(clazz + ": Trace");
		  log.debug(clazz + ": Debug");
		  log.info(clazz + ": Info");
		  log.warn(clazz + ": Warn");
		  log.error(clazz + ": Error");
	  }
  }
}
