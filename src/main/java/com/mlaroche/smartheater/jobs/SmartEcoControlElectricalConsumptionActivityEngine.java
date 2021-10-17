/**
 * Copyright 2018 Matthieu Laroche - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mlaroche.smartheater.jobs;

import javax.inject.Inject;

import com.mlaroche.smartheater.services.SmartEcoControlServices;

import io.vertigo.orchestra.services.execution.RunnableActivityEngine;

public class SmartEcoControlElectricalConsumptionActivityEngine extends RunnableActivityEngine {

	@Inject
	private SmartEcoControlServices electricalConsumptionServices;

	@Override
	public void run() {
		electricalConsumptionServices.updateElectricalConsumption("electrical.conso");
	}

}
