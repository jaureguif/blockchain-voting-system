package com.epam.asset.tracking.repository.blockchain.fabric;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FabricAspect {

  private static final Logger log = LoggerFactory.getLogger(FabricAspect.class);

  private @Autowired FabricInitializerHelper initializerHelper;

  @Before("@annotation(InitFabric)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    BaseFabricRepository<?, ?> repository = (BaseFabricRepository<?, ?>)joinPoint.getThis();
    init(repository);
    return joinPoint.proceed();
  }

  public void init(BaseFabricRepository<?, ?> repository) {
    try {
      initializerHelper.init();
    } catch (Exception ex) {
      log.error("Error while initializing HFClient/Channel", ex);
    }
    repository.setClient(initializerHelper.getClient());
    repository.setChannel(initializerHelper.getChannel());
  }
}
