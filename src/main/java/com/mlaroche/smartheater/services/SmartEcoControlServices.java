package com.mlaroche.smartheater.services;

import io.vertigo.core.node.component.Component;

public interface SmartEcoControlServices extends Component {

	/**
	 * Reprend les données jour par jour.
	 * @param fromDate Date au format YYYY-MM-DD
	 * @param toDate Date au format YYYY-MM-DD
	 * @param measureName
	 */
	void reprise(final String fromDate, final String toDate, String measureName);

	/**
	 * Charge les données de consomation electrique.
	 * @param measureName
	 */
	void updateElectricalConsumption(String measureName);

	/**
	 * Charge les status généraux.
	 * @param measureName
	 */
	void updateElectricalStatus(String measureName);

	/**
	 * Charge les status des pieces.
	 * @param measureName
	 */
	void updateRoomOrder(String measureName);
}
