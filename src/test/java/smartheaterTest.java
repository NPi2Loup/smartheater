
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

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import javax.inject.Inject;

//import lib to use mqtt paho
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mlaroche.smartheater.services.EnedisElectricalConsumptionServices;

import io.vertigo.core.node.AutoCloseableNode;
import io.vertigo.core.node.component.di.DIInjector;
import io.vertigo.core.node.config.NodeConfig;

/**
 * Test of smartheater.
 *
 * @author npi2loup
 */
public final class smartheaterTest {

	private AutoCloseableNode node;

	@BeforeEach
	public void setUp() {
		node = new AutoCloseableNode(buildNodeConfig());
		DIInjector.injectMembers(this, node.getComponentSpace());
	}

	@AfterEach
	public void tearDown() {
		if (node != null) {
			node.close();
		}
	}

	protected static NodeConfig buildNodeConfig() {
		return smartheaterTestConfig.config();
	}

	@Inject
	private EnedisElectricalConsumptionServices enedisElectricalConsumptionServices;

	public static final DateTimeFormatter PAYPAL_DATE_TIME_FORMAT = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.append(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			.parseLenient()
			.parseStrict()
			.toFormatter();

	@Test
	public void testDate() {
		OffsetDateTime odt = OffsetDateTime.parse("2021-04-29T08:00:00+02:00");
		System.out.println(odt.toInstant());
		odt = OffsetDateTime.parse("2021-02-17T15:00:00+01:00");
		System.out.println(odt);
	}

	@Test
	public void reprise() {
		enedisElectricalConsumptionServices.reprise("2018-01-01", "2021-12-21",
				new File("d:\\@GitHub.NPi2Loup\\smartheater\\Enedis_Conso_Heure_20200102-20211221_21102894297007.csv"),
				new File("d:\\@GitHub.NPi2Loup\\smartheater\\Enedis_Conso_Pmax_20190107-20211221_21102894297007.csv"),
				"enedis");
	}

}
