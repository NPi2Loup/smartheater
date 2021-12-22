
/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.mlaroche.smartheater.services.EnedisElectricalConsumptionServices;
import com.mlaroche.smartheater.services.EnedisElectricalConsumptionServicesImpl;

import io.vertigo.commons.CommonsFeatures;
import io.vertigo.connectors.influxdb.InfluxDbFeatures;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.NodeConfigBuilder;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.param.properties.PropertiesParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.core.plugins.resource.url.URLResourceResolverPlugin;
import io.vertigo.database.DatabaseFeatures;

public final class smartheaterTestConfig {

	public static NodeConfigBuilder createNodeConfigBuilder() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.addPlugin(URLResourceResolverPlugin.class)
						.addPlugin(PropertiesParamPlugin.class,
								Param.of("url", "smartheaterNPI-dev.properties"))
						.build())
				.addModule(new InfluxDbFeatures()
						.withInfluxDb(Param.of("host", "http://192.168.1.53:8086"),
								Param.of("user", "user"),
								Param.of("password", "password"))
						.build())
				.addModule(new CommonsFeatures()
						.build())
				.addModule(new DatabaseFeatures()
						.withTimeSeriesDataBase()
						.withInfluxDb(
								Param.of("dbNames", "iotdb-dev"))
						.build())
				.addModule(ModuleConfig.builder("smartheater")
						.addComponent(EnedisElectricalConsumptionServices.class, EnedisElectricalConsumptionServicesImpl.class)
						.build());
	}

	public static NodeConfig config() {
		return createNodeConfigBuilder().build();
	}

}
