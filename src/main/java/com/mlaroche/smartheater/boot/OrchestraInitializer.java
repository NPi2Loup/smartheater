package com.mlaroche.smartheater.boot;

import javax.inject.Inject;

import com.mlaroche.smartheater.jobs.FreeboxActivityEngine;
import com.mlaroche.smartheater.jobs.WeatherActivityEngine;

import io.vertigo.core.node.component.ComponentInitializer;
import io.vertigo.orchestra.definitions.OrchestraDefinitionManager;
import io.vertigo.orchestra.definitions.ProcessDefinition;
import io.vertigo.orchestra.definitions.ProcessType;

/**
 * Initialisation des processus gérés par Orchestra
 *
 * @author mlaroche.
 * @version $Id$
 */
public class OrchestraInitializer implements ComponentInitializer {

	@Inject
	private OrchestraDefinitionManager orchestraDefinitionManager;

	/** {@inheritDoc} */
	@Override
	public void init() {

		final ProcessDefinition weatherRetrieval = ProcessDefinition.builder("ProWeather", "Recupération des informations météos")
				.withProcessType(ProcessType.UNSUPERVISED)
				.withCronExpression("0 0/5 * ? * * *")
				.addActivity("retriveData", "retriveData", WeatherActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(weatherRetrieval);
		

		final ProcessDefinition freeboxRetrieval = ProcessDefinition.builder("ProFreebox", "Recupération des informations freebox")
				.withProcessType(ProcessType.UNSUPERVISED)
				.withCronExpression("0 0/1 * ? * * *")
				.addActivity("retriveData", "retriveData", FreeboxActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(freeboxRetrieval);

		/*final ProcessDefinition enedisRetrieval = ProcessDefinition.builder("ProEnedisElectricalConsumption", "Recupération de information de consommation electrique")
				.withProcessType(ProcessType.UNSUPERVISED)
				.withCronExpression("0 0 6 ? * * *")
				.addActivity("retrieveData", "retriveData", EnedisElectricalConsumptionActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(enedisRetrieval);*/
		
		/*final ProcessDefinition statusRetrieval = ProcessDefinition.builder("RoomActivity", "Recupération des informations de status des pieces")
				.withProcessType(ProcessType.UNSUPERVISED)
				.withCronExpression("0 0/5 * ? * * *")//5min
				.addActivity("retriveStatus", "Recupération des informations de status", SmartEcoControlStatusActivityEngine.class)
				.addActivity("retriveRoomOrder", "Recupération des informations de consigne des piece", SmartEcoControlRoomOrderActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(statusRetrieval);

		final ProcessDefinition consumptionRetrieval = ProcessDefinition.builder("Consumption", "Recupération des informations de consomation électrique")
				.withProcessType(ProcessType.UNSUPERVISED)
				.withCronExpression("0 0/30 * ? * * *") //30min
				.addActivity("retriveData", "retriveData", SmartEcoControlElectricalConsumptionActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(consumptionRetrieval);

		final ProcessDefinition repriseRetrieval = ProcessDefinition.builder("Reprise", "Permet de lancer une reprise manuelle")
				.withProcessType(ProcessType.UNSUPERVISED)
				.addActivity("retriveData", "retriveData", SmartEcoControlRepriseActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(repriseRetrieval);
		 */
		/*final ProcessDefinition enedisRetrieval = ProcessDefinition.builder("ProEnedisElectricalConsumption", "Recupération de information de consommation electrique")
				.withProcessType(ProcessType.UNSUPERVISED)
				.withCronExpression("0 0 6 ? * * *")
				.addActivity("retrieveData", "retriveData", EnedisElectricalConsumptionActivityEngine.class)
				.build();
		orchestraDefinitionManager.createOrUpdateDefinition(enedisRetrieval);*/

	}

}
