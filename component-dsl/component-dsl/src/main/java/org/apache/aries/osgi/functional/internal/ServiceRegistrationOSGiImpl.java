/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.aries.osgi.functional.internal;

import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Carlos Sierra Andrés
 */
public class ServiceRegistrationOSGiImpl<T>
	extends OSGiImpl<ServiceRegistration<T>> {

	public ServiceRegistrationOSGiImpl(
		Class<T> clazz, T service, Map<String, Object> properties) {

		super(bundleContext -> {
			ServiceRegistration<T> serviceRegistration =
				bundleContext.registerService(
					clazz, service, getProperties(properties));

			return getServiceRegistrationOSGiResult(serviceRegistration);
		});
	}

	public ServiceRegistrationOSGiImpl(
		Class<T> clazz, ServiceFactory<T> serviceFactory,
		Map<String, Object> properties) {

		super(bundleContext -> {
			ServiceRegistration<T> serviceRegistration =
				bundleContext.registerService(
					clazz, serviceFactory, getProperties(properties));

			return getServiceRegistrationOSGiResult(serviceRegistration);
		});
	}

	public ServiceRegistrationOSGiImpl(
		String[] clazz, Object service, Map<String, ?> properties) {

		super(bundleContext -> {
			ServiceRegistration<?> serviceRegistration =
				bundleContext.registerService(
					clazz, service, new Hashtable<>(properties));

			return getServiceRegistrationOSGiResult(
				(ServiceRegistration)serviceRegistration);
		});
	}

	private static Hashtable<String, Object> getProperties(
		Map<String, Object> properties) {

		if (properties == null) {
			return new Hashtable<>();
		}

		return new Hashtable<>(properties);
	}

	private static <T> OSGiResultImpl<ServiceRegistration<T>>
		getServiceRegistrationOSGiResult(
			ServiceRegistration<T> serviceRegistration) {

		Pipe<Tuple<ServiceRegistration<T>>, Tuple<ServiceRegistration<T>>>
            added = Pipe.create();

		Consumer<Tuple<ServiceRegistration<T>>> addedSource =
            added.getSource();

		Tuple<ServiceRegistration<T>> tuple = Tuple.create(
            serviceRegistration);

		return new OSGiResultImpl<>(
            added,
            () -> addedSource.accept(tuple),
            () -> {
                try {
                    serviceRegistration.unregister();
                }
                catch (Exception e) {
                }
                finally {
                    tuple.terminate();
                }
            });
	}

}
