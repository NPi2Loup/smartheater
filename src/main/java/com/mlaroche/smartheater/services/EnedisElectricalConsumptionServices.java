package com.mlaroche.smartheater.services;

import java.io.File;

import io.vertigo.core.node.component.Component;

public interface EnedisElectricalConsumptionServices extends Component {

	/**
	 * Reprend les données jour par jour.
	 * @param fromDate Date au format YYYY-MM-DD
	 * @param toDate Date au format YYYY-MM-DD
	 * @param measureName
	 */
	void reprise(final String fromDate, final String toDate, File consoHeure, File consoPMax, String measureName);

	/**
	 * Charge les données de consomation electrique.
	 * @param measureName
	 */
	//void updateElectricalConsumption(String measureName);

}
