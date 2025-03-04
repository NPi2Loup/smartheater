package com.mlaroche.smartheater.domain;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.core.lang.Generated;
import io.vertigo.datamodel.structure.definitions.DtFieldName;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class DtDefinitions implements Iterable<Class<?>> {

	/**
	 * Enumération des DtDefinitions.
	 */
	public enum Definitions {
		/** Objet de données ElectricalConsumption. */
		ElectricalConsumption(com.mlaroche.smartheater.domain.ElectricalConsumption.class),
		/** Objet de données ElectricalTarification. */
		ElectricalTarification(com.mlaroche.smartheater.domain.ElectricalTarification.class),
		/** Objet de données FreeboxInfo. */
		FreeboxInfo(com.mlaroche.smartheater.domain.FreeboxInfo.class),
		/** Objet de données FreeboxWifi. */
		FreeboxWifi(com.mlaroche.smartheater.domain.FreeboxWifi.class),
		/** Objet de données GeneralStatus. */
		GeneralStatus(com.mlaroche.smartheater.domain.GeneralStatus.class),
		/** Objet de données Heater. */
		Heater(com.mlaroche.smartheater.domain.Heater.class),
		/** Objet de données HeaterInfo. */
		HeaterInfo(com.mlaroche.smartheater.domain.HeaterInfo.class),
		/** Objet de données HeaterMode. */
		HeaterMode(com.mlaroche.smartheater.domain.HeaterMode.class),
		/** Objet de données HeatersByMode. */
		HeatersByMode(com.mlaroche.smartheater.domain.HeatersByMode.class),
		/** Objet de données Protocol. */
		Protocol(com.mlaroche.smartheater.domain.Protocol.class),
		/** Objet de données Room. */
		Room(com.mlaroche.smartheater.domain.Room.class),
		/** Objet de données Thermostat. */
		Thermostat(com.mlaroche.smartheater.domain.Thermostat.class),
		/** Objet de données WeatherInfo. */
		WeatherInfo(com.mlaroche.smartheater.domain.WeatherInfo.class),
		/** Objet de données WeeklyCalendar. */
		WeeklyCalendar(com.mlaroche.smartheater.domain.WeeklyCalendar.class)		;

		private final Class<?> clazz;

		private Definitions(final Class<?> clazz) {
			this.clazz = clazz;
		}

		/** 
		 * Classe associée.
		 * @return Class d'implémentation de l'objet 
		 */
		public Class<?> getDtClass() {
			return clazz;
		}
	}

	/**
	 * Enumération des champs de ElectricalConsumption.
	 */
	public enum ElectricalConsumptionFields implements DtFieldName<com.mlaroche.smartheater.domain.ElectricalConsumption> {
		/** Propriété 'Heure'. */
		timestamp,
		/** Propriété 'Puissance moyenne'. */
		meanPower,
		/** Propriété 'Conso HP'. */
		powerHP,
		/** Propriété 'Conso HC'. */
		powerHC,
		/** Propriété 'Cout HP'. */
		costHP,
		/** Propriété 'Cout HC'. */
		costHC	}

	/**
	 * Enumération des champs de ElectricalTarification.
	 */
	public enum ElectricalTarificationFields implements DtFieldName<com.mlaroche.smartheater.domain.ElectricalTarification> {
		/** Propriété 'Heure'. */
		timestamp,
		/** Propriété 'Conso HP'. */
		powerMax,
		/** Propriété 'Cout HP'. */
		costHP,
		/** Propriété 'Cout HC'. */
		costHC,
		/** Propriété 'Début HP'. */
		beginHP,
		/** Propriété 'Fin HP'. */
		endHP	}

	/**
	 * Enumération des champs de FreeboxInfo.
	 */
	public enum FreeboxInfoFields implements DtFieldName<com.mlaroche.smartheater.domain.FreeboxInfo> {
		/** Propriété 'success'. */
		success,
		/** Propriété 'UpTime en seconde'. */
		uptimeSecond,
		/** Propriété 'Dernier reboot'. */
		lastreboot,
		/** Propriété 'Température 1'. */
		tempT1,
		/** Propriété 'Température 2'. */
		tempT2,
		/** Propriété 'Température CPU B'. */
		tempCpuB,
		/** Propriété 'Ventilateur 1'. */
		fan0Speed,
		/** Propriété 'Débit déscendant'. */
		rateDown,
		/** Propriété 'Débit montant'. */
		rateUp,
		/** Propriété 'Total déscendant'. */
		bytesDown,
		/** Propriété 'Total montant'. */
		bytesUp,
		/** Propriété 'Max descendant (Kb/s)'. */
		bandwidthDown,
		/** Propriété 'Max montant (Kb/s)'. */
		bandwidthUp	}

	/**
	 * Enumération des champs de FreeboxWifi.
	 */
	public enum FreeboxWifiFields implements DtFieldName<com.mlaroche.smartheater.domain.FreeboxWifi> {
		/** Propriété 'success'. */
		success,
		/** Propriété 'UpTime en seconde'. */
		uptimeSecond,
		/** Propriété 'Dernier reboot'. */
		lastreboot,
		/** Propriété 'Température 1'. */
		tempT1,
		/** Propriété 'Température 2'. */
		tempT2,
		/** Propriété 'Température CPU B'. */
		tempCpuB,
		/** Propriété 'Ventilateur 1'. */
		fan0Speed,
		/** Propriété 'Débit déscendant'. */
		rateDown,
		/** Propriété 'Débit montant'. */
		rateUp,
		/** Propriété 'Total déscendant'. */
		bytesDown,
		/** Propriété 'Total montant'. */
		bytesUp,
		/** Propriété 'Max descendant (Kb/s)'. */
		bandwidthDown,
		/** Propriété 'Max montant (Kb/s)'. */
		bandwidthUp	}

	/**
	 * Enumération des champs de GeneralStatus.
	 */
	public enum GeneralStatusFields implements DtFieldName<com.mlaroche.smartheater.domain.GeneralStatus> {
		/** Propriété 'init_en_cours'. */
		initEnCours,
		/** Propriété 'rf_present'. */
		rfPresent,
		/** Propriété 'etat_wifi'. */
		etatWifi,
		/** Propriété 'etat_tic'. */
		etatTic,
		/** Propriété 'connect_internet'. */
		connectInternet,
		/** Propriété 'v_interface'. */
		versionInterface,
		/** Propriété 'v_app'. */
		versionApp	}

	/**
	 * Enumération des champs de Heater.
	 */
	public enum HeaterFields implements DtFieldName<com.mlaroche.smartheater.domain.Heater> {
		/** Propriété 'Id'. */
		heaId,
		/** Propriété 'Nom'. */
		name,
		/** Propriété 'Nom DNS/Adresse IP'. */
		dnsName,
		/** Propriété 'Actif'. */
		active,
		/** Propriété 'Mode Auto'. */
		auto,
		/** Propriété 'Retour au mode auto'. */
		autoSwitch,
		/** Propriété 'Calendrier'. */
		wcaId,
		/** Propriété 'Protocol'. */
		proCd,
		/** Propriété 'Mode'. */
		modCd	}

	/**
	 * Enumération des champs de HeaterInfo.
	 */
	public enum HeaterInfoFields implements DtFieldName<com.mlaroche.smartheater.domain.HeaterInfo> {
		/** Propriété 'Température'. */
		temperature,
		/** Propriété 'Humidité'. */
		humidity,
		/** Propriété 'Mode'. */
		mode	}

	/**
	 * Enumération des champs de HeaterMode.
	 */
	public enum HeaterModeFields implements DtFieldName<com.mlaroche.smartheater.domain.HeaterMode> {
		/** Propriété 'Id'. */
		modCd,
		/** Propriété 'Nom'. */
		label	}

	/**
	 * Enumération des champs de HeatersByMode.
	 */
	public enum HeatersByModeFields implements DtFieldName<com.mlaroche.smartheater.domain.HeatersByMode> {
		/** Propriété 'Id'. */
		mode,
		/** Propriété 'Nombre'. */
		count	}

	/**
	 * Enumération des champs de Protocol.
	 */
	public enum ProtocolFields implements DtFieldName<com.mlaroche.smartheater.domain.Protocol> {
		/** Propriété 'Id'. */
		proCd,
		/** Propriété 'Nom'. */
		label	}

	/**
	 * Enumération des champs de Room.
	 */
	public enum RoomFields implements DtFieldName<com.mlaroche.smartheater.domain.Room> {
		/** Propriété 'Id'. */
		rooId,
		/** Propriété 'Nom'. */
		name	}

	/**
	 * Enumération des champs de Thermostat.
	 */
	public enum ThermostatFields implements DtFieldName<com.mlaroche.smartheater.domain.Thermostat> {
		/** Propriété 'Code'. */
		theCd,
		/** Propriété 'Nom'. */
		name,
		/** Propriété 'Puissance'. */
		power,
		/** Propriété 'Observation'. */
		observation,
		/** Propriété 'Signal RF'. */
		signalRf,
		/** Propriété 'Signal RF'. */
		signalRfLabel,
		/** Propriété 'Clignote'. */
		blink,
		/** Propriété 'Piece'. */
		rooId	}

	/**
	 * Enumération des champs de WeatherInfo.
	 */
	public enum WeatherInfoFields implements DtFieldName<com.mlaroche.smartheater.domain.WeatherInfo> {
		/** Propriété 'Heure'. */
		timestamp,
		/** Propriété 'Temperature'. */
		temperature,
		/** Propriété 'Humidité'. */
		humidity,
		/** Propriété 'Pression'. */
		pressure,
		/** Propriété '% de Nuages'. */
		clouds,
		/** Propriété 'Mode'. */
		location,
		/** Propriété 'Mode'. */
		icon,
		/** Propriété 'Mode'. */
		description,
		/** Propriété 'Levé de soleil'. */
		sunrise,
		/** Propriété 'Couché de soleil'. */
		sunset	}

	/**
	 * Enumération des champs de WeeklyCalendar.
	 */
	public enum WeeklyCalendarFields implements DtFieldName<com.mlaroche.smartheater.domain.WeeklyCalendar> {
		/** Propriété 'Id'. */
		wcaId,
		/** Propriété 'Nom'. */
		name,
		/** Propriété 'Value as json'. */
		jsonValue	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Class<?>> iterator() {
		return new Iterator<>() {
			private Iterator<Definitions> it = Arrays.asList(Definitions.values()).iterator();

			/** {@inheritDoc} */
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			/** {@inheritDoc} */
			@Override
			public Class<?> next() {
				return it.next().getDtClass();
			}
		};
	}
}
