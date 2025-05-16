# 동작원리

### 1. 이벤트 발행 시 리스너로 전달되는 과정
---
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext {
			@Override 
	public void publishEvent(Object event) { 
			publishEvent(event, null); 
	} 
		
	protected void publishEvent(Object event, @Nullable ResolvableType typeHint) {
		...
		
		// Multicast right now if possible - or lazily once the multicaster is initialized
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.multicastEvent(applicationEvent, eventType);
			}
	
		...
		
		// Publish event via parent context as well...
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext abstractApplicationContext) {
				abstractApplicationContext.publishEvent(event, typeHint);
			}
			else {
				this.parent.publishEvent(event);
			}
		}
	}
}
```
- ApplicationEventPublisher 주입받은 곳에서 이벤트 발행 -> call publishEvent()
- 멀티캐스터를 통해 리스너들에게 전파 (ApplicationEventMulticaster에게 이벤트 처리를 위임)
- 부모 컨텍스트가 있다면 해당 컨텍스트도 발행(전파)
-  해당 이벤트를 맵핑된 모든 리스너에게 멀티 캐스트로 발행
-  이때 사용되는 ApplicationEventMulticaster는 SimpleApplicationEventMulticaster
- AbstractApplicationContext의 빈 생성시 초기화 별도의 다른 설정이 없다면 SimpleApplicationEventMulticaster 가 기본으로 세팅

```java
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

    @Override
    public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
        Executor executor = getTaskExecutor();

        for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            } else {
                invokeListener(listener, event);
            }
        }
    }
}
``` 
- 리스너를 순회하면서 메서드를 실행 시도 invokeListener();
- executor를 사용하려면 SimpleApplicationEventMulticaster에 executor를 주입해야함. 이벤트를 개별적으로 비동기로 처리하는 것이 아니라 이벤트 발행 전체 로직을 별도의 스레드로 처리
- invokeListener() -> doInvokeListener()
```java
private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
		try {
			listener.onApplicationEvent(event);
		}
		catch (ClassCastException ex) {
			// ..캐스팅 예외 처리
			if("예외 메시지가 없거나 또는 이벤트 타입과 메시지가 일치"){
				// 로그만 남기고 예외 삼킴
			}
			else {
				throw ex;
			}
		}
	}
```
- doInvokeListener() -> listener.onApplicationEvent(event)
```java
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	/**
	 * Handle an application event.
	 * @param event the event to respond to
	 */
	void onApplicationEvent(E event);
	...
}

// 구현체
public class ApplicationListenerMethodAdapter implements GenericApplicationListener {
	...
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (isDefaultExecution()) {
			processEvent(event);
		}
	}
	
	// onApplication() -> processEvent()
	
	public void processEvent(ApplicationEvent event) {
		@Nullable Object[] args = resolveArguments(event);
		if (shouldHandle(event, args)) {
			Object result = doInvoke(args);
			if (result != null) {
				handleResult(result);
			}
			else {
				logger.trace("No result object given - no result to handle");
			}
		}
	}
	
	// processEvenet() -> resolveArguments()
	
	protected @Nullable Object @Nullable [] resolveArguments(ApplicationEvent event) {
		ResolvableType declaredEventType = getResolvableType(event);
		if (declaredEventType == null) {
			return null;
		}
		if (this.method.getParameterCount() == 0) {
			return new Object[0];
		}
		Class<?> declaredEventClass = declaredEventType.toClass();
		if (!ApplicationEvent.class.isAssignableFrom(declaredEventClass) &&
				event instanceof PayloadApplicationEvent<?> payloadEvent) {
			Object payload = payloadEvent.getPayload();
			if (declaredEventClass.isInstance(payload)) {
				return new Object[] {payload};
			}
		}
		return new Object[] {event};
	}
	
	// resolveArguments() -> getResolvableType
	
	private @Nullable ResolvableType getResolvableType(ApplicationEvent event) {
		ResolvableType payloadType = null;
		if (event instanceof PayloadApplicationEvent<?> payloadEvent) {
			ResolvableType eventType = payloadEvent.getResolvableType();
			if (eventType != null) {
				payloadType = eventType.as(PayloadApplicationEvent.class).getGeneric();
			}
		}
		for (ResolvableType declaredEventType : this.declaredEventTypes) {
			Class<?> eventClass = declaredEventType.toClass();
			if (!ApplicationEvent.class.isAssignableFrom(eventClass) &&
					payloadType != null && declaredEventType.isAssignableFrom(payloadType)) {
				return declaredEventType;
			}
			if (eventClass.isInstance(event)) {
				return declaredEventType;
			}
		}
		return null;
	}

}


```

-  onApplication -> processEvent 호출
- resolveArguments: 호출된 이벤트를 consume할 수 있는 메서드(리스너)를 확인한다.
	- Object[] {payload}를 반환하는 상황은 우리가 정의한 이벤트 클래스를 리턴
	- Object[] {event}는 ApplicationEvent 타입 (상속)한 이벤트를 리턴
- getResolvableType: 
	- declaredEventTypes를 순회하면서 리스너의 파라미터 타입과 일치하는 타입을 반환

> 요약

┌──────────────────────────┐
│ ApplicationEventPublisher     │
│ publishEvent(event)               │
└────────────┬─────────────┘
	        │
            ▼
┌───────────────────────────────────┐
│ SimpleApplicationEventMulticaster       │
│ multicastEvent(event)                            │
│  └─ forEach listener                                │
│      └─ (async or sync) invokeListener     │
└────────────┬──────────────────────┘
             │
             ▼
┌──────────────────────────────────┐
│ ApplicationListenerMethodAdapter     │
│ onApplicationEvent(event)                   │
│  └─ processEvent(event)                        │
│       ├─ resolveArguments()                    │
│       ├─ shouldHandle()                           │
│       └─ doInvoke(args)                            │
│            ├─ getTargetBean()                     │
│            └─ method.invoke(targetBean)   │
└────────────────────────────────── ┘

### TransactionEventListener는?

```java
public class TransactionalApplicationListenerMethodAdapter extends ApplicationListenerMethodAdapter {
	public void onApplicationEvent(ApplicationEvent event) {
		if (TransactionalApplicationListenerSynchronization.register(event, this, this.callbacks)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Registered transaction synchronization for " + event);
			}
		}
		else if (isDefaultExecution()) {
			if (getTransactionPhase() == TransactionPhase.AFTER_ROLLBACK && logger.isWarnEnabled()) {
				logger.warn("Processing " + event + " as a fallback execution on AFTER_ROLLBACK phase");
			}
			processEvent(event);
		}
		else {
			// No transactional event execution at all
			if (logger.isDebugEnabled()) {
				logger.debug("No transaction is active - skipping " + event);
			}
		}
	}
}

abstract class TransactionalApplicationListenerSynchronization<E extends ApplicationEvent> implements Ordered {
	...
		public static <E extends ApplicationEvent> boolean register(
			E event, TransactionalApplicationListener<E> listener,
			List<TransactionalApplicationListener.SynchronizationCallback> callbacks) {

		if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive() &&
				org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
			org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
					new PlatformSynchronization<>(event, listener, callbacks));
			return true;
		}
		else if (event.getSource() instanceof TransactionContext txContext) {
			org.springframework.transaction.reactive.TransactionSynchronizationManager rtsm =
					new org.springframework.transaction.reactive.TransactionSynchronizationManager(txContext);
			if (rtsm.isSynchronizationActive() && rtsm.isActualTransactionActive()) {
				rtsm.registerSynchronization(new ReactiveSynchronization<>(event, listener, callbacks));
				return true;
			}
		}
		return false;
	}
}
```

-  TransactionalApplicationListenerMethodAdapter.onApplicationEvent -> register()로 등록

```java
public abstract class TransactionSynchronizationUtils {
	public static void triggerBeforeCommit(boolean readOnly) {
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			synchronization.beforeCommit(readOnly);
		}
	}
	
	public static void triggerBeforeCompletion() {
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			try {
				synchronization.beforeCompletion();
			}
			catch (Throwable ex) {
				logger.error("TransactionSynchronization.beforeCompletion threw exception", ex);
			}
		}
	}
	
	public static void triggerAfterCommit() {
		invokeAfterCommit(TransactionSynchronizationManager.getSynchronizations());
	}

	public static void invokeAfterCommit(@Nullable List<TransactionSynchronization> synchronizations) {
		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				synchronization.afterCommit();
			}
		}
	}

	public static void triggerAfterCompletion(int completionStatus) {
			List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			invokeAfterCompletion(synchronizations, completionStatus);
	}
	
	public static void invokeAfterCompletion(@Nullable List<TransactionSynchronization> synchronizations,
			int completionStatus) {
			
		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				try {
					synchronization.afterCompletion(completionStatus);
				}
				catch (Throwable ex) {
					logger.error("TransactionSynchronization.afterCompletion threw exception", ex);
				}
			}
		}
	}
}
```
- 트랜잭션 상태에 따라 TransactionSynchronization 메서드를 실행 하는데

```java
abstract class TransactionalApplicationListenerSynchronization<E extends ApplicationEvent> implements Ordered {
	public void processEventWithCallbacks() {
		this.callbacks.forEach(callback -> callback.preProcessEvent(this.event));
		try {
			this.listener.processEvent(this.event);
		}
		catch (RuntimeException | Error ex) {
			this.callbacks.forEach(callback -> callback.postProcessEvent(this.event, ex));
			throw ex;
		}
		this.callbacks.forEach(callback -> callback.postProcessEvent(this.event, null));
	}
	...

	private static class PlatformSynchronization<AE extends ApplicationEvent>
			extends TransactionalApplicationListenerSynchronization<AE>
			implements org.springframework.transaction.support.TransactionSynchronization {

		public PlatformSynchronization(AE event, TransactionalApplicationListener<AE> listener,
				List<TransactionalApplicationListener.SynchronizationCallback> callbacks) {

			super(event, listener, callbacks);
		}

		@Override
		public void beforeCommit(boolean readOnly) {
			if (getTransactionPhase() == TransactionPhase.BEFORE_COMMIT) {
				processEventWithCallbacks();
			}
		}

		@Override
		public void afterCompletion(int status) {
			TransactionPhase phase = getTransactionPhase();
			if (phase == TransactionPhase.AFTER_COMMIT && status == STATUS_COMMITTED) {
				processEventWithCallbacks();
			}
			else if (phase == TransactionPhase.AFTER_ROLLBACK && status == STATUS_ROLLED_BACK) {
				processEventWithCallbacks();
			}
			else if (phase == TransactionPhase.AFTER_COMPLETION) {
				processEventWithCallbacks();
			}
		}
	}
	
}
```
-  TransactionalApplicationListenerSynchronization 내부의 TransactionSynchronization 상속받은 정적 중첩 클래스에 의해 이벤트가 실행됨
- 이후 내용은 일반 이벤트 리스너와 동일일



## 2. 이벤트 리스너 등록과정 
---

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext {
		...
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		...
		
		try {
			// Allows post-processing of the bean factory in context subclasses.
			postProcessBeanFactory(beanFactory);

			StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
			// Invoke factory processors registered as beans in the context.
			invokeBeanFactoryPostProcessors(beanFactory);
			...
			// Check for listener beans and register them.
			registerListeners();
			
			// Instantiate all remaining (non-lazy-init) singletons.
			finishBeanFactoryInitialization(beanFactory);
		}
	}
	
	...
	
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
	
		// Detect a LoadTimeWeaver and prepare for weaving, if found in the meantime
		// (for example, through an @Bean method registered by ConfigurationClassPostProcessor)
		if (!NativeDetector.inNativeImage() && beanFactory.getTempClassLoader() == null &&
				beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}
	}

	...
	
	protected void registerListeners() {
		// Register statically specified listeners first.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// Publish early application events now that we finally have a multicaster...
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (!CollectionUtils.isEmpty(earlyEventsToProcess)) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}
}

final class PostProcessorRegistrationDelegate {
	...
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			StartupStep postProcessBeanFactory = beanFactory.getApplicationStartup().start("spring.context.bean-factory.post-process")
					.tag("postProcessor", postProcessor::toString);
			postProcessor.postProcessBeanFactory(beanFactory);
			postProcessBeanFactory.end();
		}
	}
	...
}
public class EventListenerMethodProcessor
		implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.originalEvaluationContext.setBeanResolver(new BeanFactoryResolver(this.beanFactory));

		Map<String, EventListenerFactory> beans = beanFactory.getBeansOfType(EventListenerFactory.class, false, false);
		List<EventListenerFactory> factories = new ArrayList<>(beans.values());
		AnnotationAwareOrderComparator.sort(factories);
		this.eventListenerFactories = factories;
	}
	
	@Override
	public void afterSingletonsInstantiated() {
		ConfigurableListableBeanFactory beanFactory = this.beanFactory;
		Assert.state(beanFactory != null, "No ConfigurableListableBeanFactory set");
		String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
		for (String beanName : beanNames) {
			if (!ScopedProxyUtils.isScopedTarget(beanName)) {
				Class<?> type = null;
				// 생략..
				if (type != null) {
					// 생략..
					try {
						processBean(beanName, type);
					}
					catch (Throwable ex) {
						throw new BeanInitializationException("Failed to process @EventListener " +
								"annotation on bean with name '" + beanName + "': " + ex.getMessage(), ex);
					}
				}
			}
		}
	}

	private void processBean(final String beanName, final Class<?> targetType) {
		//@EventListener 빈타입인지 검증
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, EventListener.class) &&
				!isSpringContainerClass(targetType)) {
				// .. 생략
				
				if (CollectionUtils.isEmpty(annotatedMethods)) {
				this.nonAnnotatedClasses.add(targetType);
					if (logger.isTraceEnabled()) {
						logger.trace("No @EventListener annotations found on bean class: " + targetType.getName());
					}
			}
			else {
				for (Method method : annotatedMethods.keySet()) {
					for (EventListenerFactory factory : factories) {
						if (factory.supportsMethod(method)) {
							Method methodToUse = AopUtils.selectInvocableMethod(method, context.getType(beanName));
							ApplicationListener<?> applicationListener =
									factory.createApplicationListener(beanName, targetType, methodToUse);
							if (applicationListener instanceof ApplicationListenerMethodAdapter alma) {
								alma.init(context, this.evaluator);
							}
							context.addApplicationListener(applicationListener);
							break;
						}
					}
				}
			//.. 디버그 로그 남김
			}
		}
	}
}

```

- SpringApplication.run()으로 인해 스프링 애플리케이션이 실행될 때
	- refresh()
		- invokeBeanFactoryPostProcessors()
			- PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors()
				- postProcessBeanFactory() 에서 빈 팩토리의 참조만 등록(리스너 등록 아님)
				- BeanFactoryPostProcessor postProcessor : postProcessor 구현체 중EventListenerMethodProcessor 클래스가 있다 
		- registerListeners() 에서 ApplicationContext에 등록된 Listener들을 AbstractApplicationEventMulticaster에 등록 
		- finishBeanFactoryInitialization()
			- beanFactory.preInstantiateSingletons() 에서 모든 싱글톤 빈 초기화
				- EventListenerMethodProcessor.afterSingletonsInstantiated()
					- EventListenerMethodProcessor.processBean() 에서 @EventListener 타입의 Bean인 경우에 이벤트 리스너로 등록
						- addApplicationLister() 
