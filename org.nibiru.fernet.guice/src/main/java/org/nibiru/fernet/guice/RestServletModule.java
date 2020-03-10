package org.nibiru.fernet.guice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.nibiru.fernet.core.*;
import org.nibiru.fernet.core.executor.BlockingMethodExecutor;
import org.nibiru.fernet.core.executor.CompletableFutureMethodExecutor;
import org.nibiru.fernet.core.gson.GsonSerializer;
import org.nibiru.fernet.core.jaxrs.JaxRsMethodResolver;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.servlet.ServletModule;

import java.util.List;

public class RestServletModule extends ServletModule {
	private final String defaultContentType;
	private Class<?> serviceClasses[];

	public RestServletModule(String defaultContentType,
			Class<?>... serviceClasses) {
		this.defaultContentType = requireNonNull(defaultContentType);
		this.serviceClasses = requireNonNull(serviceClasses);
	}

	@Override
	protected void configureServlets() {
		filter("*").through(RestFilter.class);
		bind(RestFilter.class).in(Singleton.class);
		bind(ServiceProvider.class).to(InjectorServiceProvider.class);
		bind(Serializer.class).to(GsonSerializer.class);
		MapBinder<String, Serializer> mapBinder = MapBinder.newMapBinder(
				binder(), String.class, Serializer.class);
		mapBinder.addBinding("application/json").to(GsonSerializer.class);
	}

	@Provides
	@Singleton
	public MethodResolver getMethodResolver() {
		return new JaxRsMethodResolver(defaultContentType, serviceClasses);
	}

	@Provides
	@Singleton
	public List<MethodExecutor> getMethodExecutors(CompletableFutureMethodExecutor futureMethodExecutor,
												   BlockingMethodExecutor blockingMethodExecutor) {
		return ImmutableList.of(futureMethodExecutor, blockingMethodExecutor);
	}
}