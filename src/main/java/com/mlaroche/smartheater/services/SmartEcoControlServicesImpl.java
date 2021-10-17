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
package com.mlaroche.smartheater.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eaxy.Element;
import org.eaxy.Xml;

import com.mlaroche.smartheater.domain.ElectricalConsumption;
import com.mlaroche.smartheater.domain.ElectricalTarification;
import com.mlaroche.smartheater.domain.GeneralStatus;
import com.mlaroche.smartheater.domain.Room;
import com.mlaroche.smartheater.domain.Thermostat;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamManager;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TimeSeriesManager;

public class SmartEcoControlServicesImpl implements SmartEcoControlServices, Activeable {

	private final static Logger LOGGER = LogManager.getLogger(SmartEcoControlServicesImpl.class);

	@Inject
	private ParamManager paramManager;
	@Inject
	private TimeSeriesManager timeSeriesDataBaseManager;
	private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");

	private static final String API_SMARTECO_URL_PARAM_NAME = "smarteco_url";

	private Map<Long, Room> roomsIndex = Collections.synchronizedMap(new HashMap<>());

	//Modif :
	// http://192.168.1.60/cgi/observation_th.cgi?adresseTH=30&observationTH=Ch%20Lucas%201000W

	@Override
	public void start() {
		//updateElectricalStatus("electrical.status");
		//updateRoomOrder("room.order");
		//reprise("2016-12-11", "2017-01-01", "electrical.conso");
		//reprise("2017-01-01", "2017-03-01", "electrical.conso");
		//reprise("2017-03-01", "2017-06-01", "electrical.conso");
		//reprise("2017-06-01", "2017-09-01", "electrical.conso");
		// bug no data ?? >> reprise("2017-07-01", "2017-08-01", "electrical.conso");
		//reprise("2017-07-01", "2017-08-01", "electrical.conso");
		//last >> reprise("2017-09-01", "2018-01-01", "electrical.conso");
		//reprise("2019-12-01", "2020-01-06", "electrical.conso");
		//reprise("2019-09-16", "2019-09-20", "electrical.conso");
		//		final String apiUrl = paramManager.getParam(API_SMARTECO_URL_PARAM_NAME).getValueAsString();
		//		try {
		//			retrieveStatusRoom(apiUrl, 2, "heater.status");
		//		} catch (final FormatterException e) {
		//			throw WrappedException.wrap(e);
		//		}
	}

	@Override
	public void stop() {
		// nothing

	}

	/** {@inheritDoc } */
	@Override
	public void reprise(final String fromDate, final String toDate, final String measureName) {
		//final LocalDate fromDateTime = LocalDate.of(2016, 12, 11);
		final LocalDate fromDateTime = LocalDate.parse(fromDate);
		final LocalDate toDateTime = LocalDate.parse(toDate);
		//final LocalDate toDateTime = LocalDate.now(ZONE_PARIS);
		final String apiUrl = paramManager.getParam(API_SMARTECO_URL_PARAM_NAME).getValueAsString();
		callWS(apiUrl + "/cgi/utilisation_interface.cgi?utilisationInterface=1");

		final ElectricalTarification tarificationInfo = retrieveTarificationData(apiUrl);
		putInfosToInflux(tarificationInfo, measureName);

		for (LocalDate date = fromDateTime; date.isBefore(toDateTime); date = date.plusDays(1)) {

			final Collection<ElectricalConsumption> electricalConsumptions = retrieveConsumptionData(apiUrl, date);
			for (final ElectricalConsumption electricalConsumption : electricalConsumptions) {
				putInfosToInflux(electricalConsumption, tarificationInfo, measureName);
			}

			try {
				Thread.sleep(200);
			} catch (final InterruptedException e) {
				throw WrappedException.wrap(e);
			}
		}

	}

	/** {@inheritDoc }
	 * @throws FormatterException */
	@Override
	public void updateElectricalConsumption(final String measureName) {
		final String apiUrl = paramManager.getParam(API_SMARTECO_URL_PARAM_NAME).getValueAsString();
		if (retrieveStatusData(apiUrl).getInitEnCours()) {
			return;
		}
		callWS(apiUrl + "/cgi/utilisation_interface.cgi?utilisationInterface=1");

		final ElectricalTarification tarificationInfo = retrieveTarificationData(apiUrl);
		putInfosToInflux(tarificationInfo, measureName);

		final ElectricalConsumption consumptionInfo = retrieveConsumptionData(apiUrl, ZonedDateTime.now(ZONE_PARIS).minusMinutes(60));
		putInfosToInflux(consumptionInfo, tarificationInfo, measureName);

	}

	//@DaemonScheduled(name = "DmnElectricalConsumptionUpdate", periodInSeconds = 60 * 1) // every 30
	/** {@inheritDoc } */
	@Override
	public void updateElectricalStatus(final String measureName) {
		final String apiUrl = paramManager.getParam(API_SMARTECO_URL_PARAM_NAME).getValueAsString();
		putInfosToInflux(retrieveDateData(apiUrl), measureName);

		final GeneralStatus generalStatus = retrieveStatusData(apiUrl);
		putInfosToInflux(generalStatus, measureName);

		if (!generalStatus.getInitEnCours()) {
			callWS(apiUrl + "/cgi/utilisation_interface.cgi?utilisationInterface=1");

			putInfosToInflux(retrieveStatusWifi(apiUrl, measureName));

			putInfosToInflux(retrieveModeVacances(apiUrl, measureName));

			updateRoomsIndex(apiUrl);
			putInfosToInflux(retrieveListThermostat(apiUrl), measureName);
		}
	}

	/** {@inheritDoc } */
	@Override
	public void updateRoomOrder(final String measureName) {
		final String apiUrl = paramManager.getParam(API_SMARTECO_URL_PARAM_NAME).getValueAsString();
		if (retrieveStatusData(apiUrl).getInitEnCours()) {
			return;
		}
		if (roomsIndex.isEmpty()) {
			updateRoomsIndex(apiUrl);
		}

		for (final Room room : roomsIndex.values()) {
			putInfosToInflux(retrieveRoomOrder(apiUrl, room.getRooId(), measureName));
		}

	}

	///xml/tarification.xml
	/**
	 * Ex:
	 * <?xml version="1.0" encoding="ISO-8859-1" ?>
	<response>

	<option_tarifaire>0</option_tarifaire>
	<puissance_souscrite>18</puissance_souscrite>
	
	<heure_creuse_manu>1</heure_creuse_manu>
	<plage_heure_creuse_debut_1>90</plage_heure_creuse_debut_1>
	<plage_heure_creuse_fin_1>96</plage_heure_creuse_fin_1>
	<plage_heure_creuse_debut_2>0</plage_heure_creuse_debut_2>
	<plage_heure_creuse_fin_2>26</plage_heure_creuse_fin_2>

	<tarification>
	
		<tarif_TH>0,1211</tarif_TH>
	
		<tarif_HP>0,1564</tarif_HP>
		<tarif_HC>0,1277</tarif_HC>
	
		<tarif_HN>0,1078</tarif_HN>
		<tarif_PM>0,5431</tarif_PM>
	
		<tarif_HCJB>0,0799</tarif_HCJB>
		<tarif_HPJB>0,0961</tarif_HPJB>
		<tarif_HCJW>0,1133</tarif_HCJW>
		<tarif_HPJW>0,1358</tarif_HPJW>
		<tarif_HCJR>0,2097</tarif_HCJR>
		<tarif_HPJR>0,5537</tarif_HPJR>
	
		<tarif_HCHPM>0,3711</tarif_HCHPM>
	
	</tarification>

	</response>
	 * @param apiUrl
	 */
	private ElectricalTarification retrieveTarificationData(final String apiUrl) {
		final Element result = callXmlWS(apiUrl + "/xml/tarification.xml");
		final ElectricalTarification newTarificationInfo = new ElectricalTarification();
		newTarificationInfo.setTimestamp(Instant.now());
		newTarificationInfo.setPowerMax(asInt(result.find("puissance_souscrite").single().text()));
		newTarificationInfo.setCostHC(asDouble(result.find("...", "tarif_HC").single().text()));
		newTarificationInfo.setCostHP(asDouble(result.find("...", "tarif_HP").single().text()));
		newTarificationInfo.setBeginHP(asInt(result.find("plage_heure_creuse_fin_2").single().text()) * 15); //plage_heure*15 => minute of day
		newTarificationInfo.setEndHP(asInt(result.find("plage_heure_creuse_debut_1").single().text()) * 15);
		return newTarificationInfo;
	}

	private void putInfosToInflux(final ElectricalTarification electricalTarification, final String measureName) {
		final int currentMinute = electricalTarification.getTimestamp().atZone(ZONE_PARIS).getHour() * 60 + electricalTarification.getTimestamp().atZone(ZONE_PARIS).getMinute();
		final boolean currentTimeIsHP = currentMinute >= electricalTarification.getBeginHP() && currentMinute <= electricalTarification.getEndHP();
		final Measure electricalMeasure = Measure.builder(measureName)
				.time(electricalTarification.getTimestamp())
				.addField("tarif.powerMax", electricalTarification.getPowerMax())
				.addField("tarif.costHC", electricalTarification.getCostHC())
				.addField("tarif.costHP", electricalTarification.getCostHP())
				.addField("tarif.beginHP", electricalTarification.getBeginHP())
				.addField("tarif.endHP", electricalTarification.getEndHP())
				.addField("tarif.currentHP", currentTimeIsHP)
				.build();
		putInfosToInflux(electricalMeasure);
	}

	private Integer asInt(final String strValue) {
		try {
			return Integer.valueOf(strValue);
		} catch (final NumberFormatException e) {
			throw WrappedException.wrap(e);
		}
	}

	private Double asDouble(final String strValue) {
		try {
			return Double.valueOf(strValue);
		} catch (final NumberFormatException e) {
			throw WrappedException.wrap(e);
		}
	}

	///xml/status-date.xml
	/**
	<?xml version="1.0" encoding="ISO-8859-1"?>

	<Date>
	<Date_Annee>19</Date_Annee>
	<Date_Mois>9</Date_Mois>
	<Date_Jour>12</Date_Jour>
	<Date_Heure>9</Date_Heure>
	<Date_Minute>29</Date_Minute>
	<Date_Seconde>4</Date_Seconde>
	<Reglage_Heure_Auto>0</Reglage_Heure_Auto>
	</Date>
	 * @param apiUrl
	 */
	private ZonedDateTime retrieveDateData(final String apiUrl) {
		final Element result = callXmlWS(apiUrl + "/xml/status-date.xml");
		final ZonedDateTime now = ZonedDateTime.of(
				2000 + asInt(result.find("Date_Annee").single().text()),
				asInt(result.find("Date_Mois").single().text()),
				asInt(result.find("Date_Jour").single().text()),
				asInt(result.find("Date_Heure").single().text()),
				asInt(result.find("Date_Minute").single().text()),
				asInt(result.find("Date_Seconde").single().text()),
				0,
				ZONE_PARIS);

		return now;
	}

	private void putInfosToInflux(final ZonedDateTime heaterTime, final String measureName) {
		final Measure heatersMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("date.year", heaterTime.getYear())
				.addField("date.month", heaterTime.getMonthValue())
				.addField("date.day", heaterTime.getDayOfMonth())
				.addField("date.hour", heaterTime.getHour())
				.addField("date.minute", heaterTime.getMinute())
				.addField("date.second", heaterTime.getSecond())
				.addField("date.latence", Instant.now().getEpochSecond() - heaterTime.toEpochSecond())
				.build();
		putInfosToInflux(heatersMeasure);
	}

	///xml/status-general.xml
	/**
	<response>

	<premiere_connexion>0</premiere_connexion>
	<init_en_cours>0</init_en_cours>
	<rf_present>1</rf_present>
	<etat_wifi>2</etat_wifi>
	<etat_tic>0</etat_tic>
	<tarif_avec_pointe>0</tarif_avec_pointe>
	<autre_equipement>0</autre_equipement>
	<connect_internet>0</connect_internet>
	<maj_ok>1</maj_ok>
	<v_interface>106</v_interface>
	<v_app>216</v_app>

	</response>
	 * @param apiUrl
	 */
	private GeneralStatus retrieveStatusData(final String apiUrl) {
		final Element result = callXmlWS(apiUrl + "/xml/status-general.xml");
		final GeneralStatus statusMeasure = new GeneralStatus();
		statusMeasure.setInitEnCours(asInt(result.find("init_en_cours").single().text()) != 0);
		statusMeasure.setRfPresent(asInt(result.find("rf_present").single().text()) != 0);
		statusMeasure.setEtatWifi(asInt(result.find("etat_wifi").single().text()));
		statusMeasure.setEtatTic(asInt(result.find("etat_tic").single().text()));
		statusMeasure.setConnectInternet(asInt(result.find("connect_internet").single().text()) != 0);
		statusMeasure.setVersionInterface(result.find("v_interface").single().text());
		statusMeasure.setVersionApp(result.find("v_app").single().text());
		return statusMeasure;
	}

	private void putInfosToInflux(final GeneralStatus generalStatus, final String measureName) {
		final Measure statusMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("status.init_en_cours", generalStatus.getInitEnCours() ? 1 : 0)
				.addField("status.rf_present", generalStatus.getRfPresent() ? 1 : 0)
				.addField("status.etat_wifi", generalStatus.getEtatWifi())
				.addField("status.etat_tic", generalStatus.getEtatTic())
				.addField("status.connect_internet", generalStatus.getConnectInternet() ? 1 : 0)
				.addField("status.v_interface", generalStatus.getVersionInterface())
				.addField("status.v_app", generalStatus.getVersionApp())
				.build();
		putInfosToInflux(statusMeasure);
	}

	private void putInfosToInflux(final Measure measure) {
		final String dbName = paramManager.getParam("influxdb_dbname").getValueAsString();
		timeSeriesDataBaseManager.insertMeasure(dbName, measure);
	}

	// /mon-logement.html?n_piece={id}
	// /xml/status-piece.xml
	/**
	 *
	<?xml version="1.0" encoding="ISO-8859-1" ?>
	<reponse>
	
	<reponse_piece>

		<id_piece>11</id_piece>
		<prog_piece>3</prog_piece>
		<nom_piece><![CDATA[CH LENA]]></nom_piece>
		<allure_piece>3</allure_piece> <-- 5:HG; 3:conf; 0: ECO; 2:CONF-1; 1:CONF-2
		<mode_piece>7</mode_piece>  <-- par exemple arret=4; auto=7; confort=3; 5=HG; 0:ECO
		<consigne_piece>20.5</consigne_piece>
		<consigne_preferee>20.5</consigne_preferee>
		<encardrement_temp_plus>2</encardrement_temp_plus>
		<encardrement_temp_moins>2</encardrement_temp_moins>
		<fenetre_piece>1</fenetre_piece>
		<presence_piece>0</presence_piece>
		<verrou_piece>0</verrou_piece>

		<derog_piece>0</derog_piece> <-- 1 si derog
		<allure_derog>0</allure_derog>  <-- allure derog
		<heure_debut_derog>0</heure_debut_derog> <-- x15 en minute
		<heure_fin_derog>0</heure_fin_derog> <-- x15 en minute

	</reponse_piece>
	
	</reponse>
	 */

	private Measure retrieveRoomOrder(final String apiUrl, final long roomId, final String measureName) {
		callWS(apiUrl + "/mon-logement.html?n_piece=" + roomId);
		final Element result = callXmlWS(apiUrl + "/xml/status-piece.xml");
		final String roomName = result.find("...", "nom_piece").single().text();
		final int modePiece = asInt(result.find("...", "mode_piece").single().text());
		final int allurePiece = asInt(result.find("...", "allure_piece").single().text());
		final int allureDerog = asInt(result.find("...", "allure_derog").single().text());
		final boolean isDerog = asInt(result.find("...", "derog_piece").single().text()) == 1;
		final int computedAllure = isDerog ? 100 + modePiece * 10 + allureDerog : modePiece * 10 + allurePiece;
		final double consignePiece = asDouble(result.find("...", "consigne_piece").single().text());
		final int currentMinuteOfDay = ZonedDateTime.now(ZONE_PARIS).get(ChronoField.MINUTE_OF_DAY);
		final int heure_debut_derog = asInt(result.find("...", "heure_debut_derog").single().text()) * 15;
		final int heure_fin_derog = asInt(result.find("...", "heure_fin_derog").single().text()) * 15;

		final Measure statusMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("roomName", roomName)
				.addField("id_piece", asInt(result.find("...", "id_piece").single().text()))
				.addField("allure_piece", allurePiece)
				.addField("computed_allure", computedAllure)
				.addField("mode_piece", modePiece)
				.addField("consigne_piece", consignePiece)
				.addField("computed_consigne", computedAllure == 2 ? consignePiece - 1 : computedAllure == 1 ? consignePiece - 2 : consignePiece)
				.addField("consigne_preferee", asDouble(result.find("...", "consigne_preferee").single().text()))
				.addField("fenetre_piece", asInt(result.find("...", "fenetre_piece").single().text()))
				.addField("presence_piece", asInt(result.find("...", "presence_piece").single().text()))
				.addField("verrou_piece", asInt(result.find("...", "verrou_piece").single().text()))
				.addField("derog_piece", isDerog ? 1 : 0)
				.addField("allure_derog", allureDerog)
				.addField("heure_debut_derog", isDerog ? heure_debut_derog : 0) //min of day
				.addField("heure_fin_derog", isDerog ? heure_fin_derog : 0)//min of day
				.addField("remaining_derog", isDerog ? heure_fin_derog - currentMinuteOfDay : 0)//min of day
				.tag("roomName", roomName)
				.build();

		return statusMeasure;
	}

	//http://192.168.1.60/xml/vacances.xml
	/**
	<response>

	<hors_gel_permanent>0</hors_gel_permanent>

	<status_vacances>
	<vacancesActive>0</vacancesActive>
	<vacancesAnnee>2019</vacancesAnnee>
	<vacancesMois>3</vacancesMois>
	<vacancesJour>8</vacancesJour>
	</status_vacances>

	</response>
	 */

	private Measure retrieveModeVacances(final String apiUrl, final String measureName) {
		final Element result = callXmlWS(apiUrl + "/xml/vacances.xml");
		final boolean isHGPerm = asInt(result.find("...", "hors_gel_permanent").single().text()) != 0;
		final boolean isVacances = asInt(result.find("...", "vacancesActive").single().text()) != 0;
		final Measure statusMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("vacances.HGPerm", isHGPerm ? 1 : 0)
				.addField("vacances.isVacances", isVacances ? 1 : 0)
				.tag("horsGelManuel", isVacances || isHGPerm ? "TRUE" : "FALSE")
				.build();
		return statusMeasure;
	}

	//http://192.168.1.60/xml/status-wifi.xml?scan=1
	/**
	<wifi_response>
	<ssid>Beug</ssid>
	<scancount>1</scancount>
	<scanlist>
	<bss>
	<name>Beug</name>
	<privacy>9</privacy>
	<wlan>1</wlan>
	<strength>5</strength>
	</bss>
	
	</scanlist>
	</wifi_response>*/

	private Measure retrieveStatusWifi(final String apiUrl, final String measureName) {
		final Element result = callXmlWS(apiUrl + "/xml/status-wifi.xml?scan=1");
		final Measure statusMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("wifi.ssid", result.find("...", "ssid").single().text())
				.addField("wifi.scan.name", result.find("...", "name").first().text())
				.addField("wifi.scan.privacy", asInt(result.find("...", "privacy").first().text()))
				.addField("wifi.scan.wlan", asInt(result.find("...", "wlan").first().text()))
				.addField("wifi.scan.strength", asInt(result.find("...", "strength").first().text()))
				.build();
		return statusMeasure;
	}

	// /xml/liste-pieces.xml
	/**
	 *
	<?xml version="1.0" encoding="ISO-8859-1" ?>
	<response>
	
		<liste_pieces>

			<th_non_assignes>NULL</th_non_assignes>

			<piece id="2">
				<nom_piece><![CDATA[SALON]]></nom_piece>
				<thermostats><th>20</th>
	<th>21</th>
	</thermostats>
			</piece>

			<piece id="3">
				<nom_piece><![CDATA[CH LUCAS]]></nom_piece>
				<thermostats><th>30</th>
	</thermostats>
			</piece>

			<piece id="4">
				<nom_piece><![CDATA[PALIER ENTREE]]></nom_piece>
				<thermostats><th>40</th>
	<th>41</th>
	</thermostats>
			</piece>

			<piece id="5">
				<nom_piece><![CDATA[CH PARENTS]]></nom_piece>
				<thermostats><th>50</th>
	</thermostats>
			</piece>

			<piece id="6">
				<nom_piece><![CDATA[CUISINE]]></nom_piece>
				<thermostats><th>60</th>
	</thermostats>
			</piece>

			<piece id="7">
				<nom_piece>NULL</nom_piece>
				<thermostats>NULL</thermostats>
			</piece>

			<piece id="8">
				<nom_piece><![CDATA[BUREAU]]></nom_piece>
				<thermostats><th>80</th>
	</thermostats>
			</piece>

			<piece id="9">
				<nom_piece><![CDATA[SALLE JEUX]]></nom_piece>
				<thermostats><th>90</th>
	</thermostats>
			</piece>

			<piece id="10">
				<nom_piece>NULL</nom_piece>
				<thermostats>NULL</thermostats>
			</piece>

			<piece id="11">
				<nom_piece><![CDATA[CH LENA]]></nom_piece>
				<thermostats><th>B0</th>
	</thermostats>
			</piece>

			<piece id="12">
				<nom_piece>NULL</nom_piece>
				<thermostats>NULL</thermostats>
			</piece>

			<piece id="13">
				<nom_piece>NULL</nom_piece>
				<thermostats>NULL</thermostats>
			</piece>

			<piece id="14">
				<nom_piece>NULL</nom_piece>
				<thermostats>NULL</thermostats>
			</piece>

		</liste_pieces>

	</response>*/
	private Collection<Room> updateRoomsIndex(final String apiUrl) {
		final Element result = callXmlWS(apiUrl + "/xml/liste-pieces.xml");
		final Map<Long, Room> newRoomsIndex = new HashMap<>();
		for (final Element pieceElement : result.find("...", "piece").elements()) {
			if (pieceElement.find("thermostats", "th").isPresent()) {
				final Room room = new Room();
				room.setRooId(asInt(pieceElement.attr("id")).longValue());
				room.setName(pieceElement.find("...", "nom_piece").single().text());
				newRoomsIndex.put(room.getRooId(), room);
			}
		}
		roomsIndex = Collections.unmodifiableMap(newRoomsIndex);
		return roomsIndex.values();
	}

	//http://192.168.1.60/xml/status-thermostats.xml
	/**
	 * <?xml version="1.0" encoding="ISO-8859-1" ?>
	<response>
	
		<piece id="1"></piece>
		<piece id="2"><thermostat id="20">
	<observation>NULL</observation>
	<puissance>2000</puissance>
	<signal_RF>0</signal_RF> // Wifi /Server
	<blink>0</blink>
	</thermostat>
	<thermostat id="21">
	<observation>NULL</observation>
	<puissance>2000</puissance>
	<signal_RF>4</signal_RF> //Excellent
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="3"><thermostat id="30">
	<observation>NULL</observation>
	<puissance>1000</puissance>
	<signal_RF>3</signal_RF> //Bon
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="4"><thermostat id="40">
	<observation>NULL</observation>
	<puissance>750</puissance>
	<signal_RF>3</signal_RF> //bon
	<blink>0</blink>
	</thermostat>
	<thermostat id="41">
	<observation>NULL</observation>
	<puissance>750</puissance>
	<signal_RF>2</signal_RF> //faible
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="5"><thermostat id="50">
	<observation>NULL</observation>
	<puissance>1500</puissance>
	<signal_RF>4</signal_RF>
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="6"><thermostat id="60">
	<observation>NULL</observation>
	<puissance>1000</puissance>
	<signal_RF>4</signal_RF>
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="7"></piece>
		<piece id="8"><thermostat id="80">
	<observation>NULL</observation>
	<puissance>750</puissance>
	<signal_RF>2</signal_RF>
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="9"><thermostat id="90">
	<observation>NULL</observation>
	<puissance>1250</puissance>
	<signal_RF>2</signal_RF>
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="10"></piece>
		<piece id="11"><thermostat id="B0">
	<observation>NULL</observation>
	<puissance>750</puissance>
	<signal_RF>3</signal_RF>
	<blink>0</blink>
	</thermostat>
	</piece>
		<piece id="12"></piece>
		<piece id="13"></piece>
		<piece id="14"></piece>
	
	</response>
	 */
	private List<Thermostat> retrieveListThermostat(final String apiUrl) {
		final Element result = callXmlWS(apiUrl + "/xml/status-thermostats.xml");
		final List<Thermostat> thermostats = new ArrayList<>();
		for (final Element pieceElement : result.find("...", "piece").elements()) {
			if (pieceElement.find("thermostat").isPresent()) {
				int i = 0;
				for (final Element thermostatElement : pieceElement.find("thermostat").elements()) {
					final Long rooId = asInt(pieceElement.attr("id")).longValue();
					final Room room = roomsIndex.get(rooId);
					final Thermostat thermostat = new Thermostat();
					thermostat.setTheCd(thermostatElement.attr("id"));
					thermostat.setName((room != null ? room.getName() : "none") + "-" + i++);
					thermostat.setRooId(rooId);
					thermostat.setObservation(thermostatElement.find("observation").single().text());
					thermostat.setPower(asInt(thermostatElement.find("puissance").single().text()));
					thermostat.setSignalRf(asInt(thermostatElement.find("signal_RF").single().text()));
					thermostat.setSignalRfLabel(toSignalRfLabel(thermostat.getSignalRf()));
					thermostat.setBlink(asInt(thermostatElement.find("blink").single().text()) > 0);

					thermostats.add(thermostat);
				}
			}
		}
		return thermostats;
	}

	private void putInfosToInflux(final List<Thermostat> retrieveListThermostat, final String measureName) {
		final Measure statusMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("status.nbRoom", roomsIndex.size())
				.addField("status.nbThermostat", retrieveListThermostat.size())
				.build();
		putInfosToInflux(statusMeasure);

		for (final Thermostat thermostat : retrieveListThermostat) {
			final Room room = roomsIndex.get(thermostat.getRooId());
			final Measure thermostatMeasure = Measure.builder(measureName)
					.time(Instant.now())
					.addField("thermostat.code", thermostat.getTheCd())
					.addField("thermostat.name", thermostat.getName())
					.addField("roomId", thermostat.getRooId())
					.addField("roomName", room != null ? room.getName() : "none")
					.addField("thermostat.puissance", thermostat.getPower())
					.addField("thermostat.signalRF", thermostat.getSignalRf())
					.addField("thermostat.signalRFLabel", thermostat.getSignalRfLabel())
					.addField("thermostat.blink", thermostat.getBlink())
					.tag("thermostat.name", thermostat.getName())
					.tag("roomName", room != null ? room.getName() : "none")
					.build();
			putInfosToInflux(thermostatMeasure);
		}
	}

	//http://192.168.1.60/xml/diagnostic.xml
	/**
	 * <?xml version="1.0" encoding="ISO-8859-1" ?>
	<response>

	<pb_install>1</pb_install>

	<nb_th_non_assigne>0</nb_th_non_assigne>
	<pieces_non_prog><piece>
	<![CDATA[ PALIER ENTREE ]]>
	</piece>
	<piece>
	<![CDATA[ CUISINE ]]>
	</piece></pieces_non_prog>
	<pieces_fenetre_ouverte></pieces_fenetre_ouverte>
	<th_non_detectes></th_non_detectes>

	</response>
	
	</response>
	 */

	///xml/list-prog.xml
	/**
	 *
	<?xml version="1.0" encoding="ISO-8859-1" ?>
	<response>

	<liste_prog>
	
		<prog id="1">
			<nom_prog><![CDATA[Sans planning]]></nom_prog>
			<liste_pieces_prog><![CDATA[SALLE JEUX]]></liste_pieces_prog>
		</prog>
	
		<prog id="2">
			<nom_prog><![CDATA[SALON NORM]]></nom_prog>
			<liste_pieces_prog><![CDATA[SALON]]>, <![CDATA[PALIER ENTREE]]></liste_pieces_prog>
		</prog>
	
		<prog id="3">
			<nom_prog><![CDATA[CH ENF NORM]]></nom_prog>
			<liste_pieces_prog><![CDATA[CH LUCAS]]>, <![CDATA[CH LENA]]></liste_pieces_prog>
		</prog>
	
		<prog id="4">
			<nom_prog><![CDATA[CUISINE]]></nom_prog>
			<liste_pieces_prog><![CDATA[CUISINE]]></liste_pieces_prog>
		</prog>
	
		<prog id="5">
			<nom_prog><![CDATA[8H 0H]]></nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="6">
			<nom_prog><![CDATA[MATIN MIDI SOIR]]></nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="7">
			<nom_prog><![CDATA[CH PARENTS]]></nom_prog>
			<liste_pieces_prog><![CDATA[CH PARENTS]]></liste_pieces_prog>
		</prog>
	
		<prog id="8">
			<nom_prog><![CDATA[BUREAU]]></nom_prog>
			<liste_pieces_prog><![CDATA[BUREAU]]></liste_pieces_prog>
		</prog>
	
		<prog id="9">
			<nom_prog><![CDATA[OFF]]></nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="10">
			<nom_prog><![CDATA[7H 0H]]></nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="11">
			<nom_prog>NULL</nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="12">
			<nom_prog>NULL</nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="13">
			<nom_prog>NULL</nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
		<prog id="14">
			<nom_prog>NULL</nom_prog>
			<liste_pieces_prog>NULL</liste_pieces_prog>
		</prog>
	
	</liste_prog>

	</response>
	 */

	//http://192.168.1.60/xml/status-prog-piece.xml
	/**
	<response>

		<prog>
	
			<id_prog>3</id_prog>
	
			<jour_prog>
				<point_prog id="0">
					<heureDebut>0</heureDebut>
					<heureFin>32</heureFin>
				</point_prog>
				<point_prog id="1">
					<heureDebut>68</heureDebut>
					<heureFin>96</heureFin>
				</point_prog>
				<point_prog id="2">
					<heureDebut>NULL</heureDebut>
					<heureFin>NULL</heureFin>
				</point_prog>
				<point_prog id="3">
					<heureDebut>NULL</heureDebut>
					<heureFin>NULL</heureFin>
				</point_prog>
				<point_prog id="4">
					<heureDebut>NULL</heureDebut>
					<heureFin>NULL</heureFin>
				</point_prog>
			</jour_prog>
	
		</prog>
	
	</response>
	*/

	private String toSignalRfLabel(final int signalRf) {
		switch (signalRf) {
			case 0:
				return "Server / Wifi";
			case 1:
				return "Insuffisant";
			case 2:
				return "Faible";
			case 3:
				return "Bon";
			case 4:
				return "Excellent";
			default:
				return "Inconnu : " + signalRf;
		}
	}

	/**
	 * <?xml version="1.0" encoding="ISO-8859-1" ?>
	<response>
	
	<conso_journaliere>
	
		<conso id="0"><heure>0</heure>
	<tarif id="1">1</tarif>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="1"><heure>1</heure>
	<tarif id="1">2</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="2"><heure>2</heure>
	<tarif id="1">2</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="3"><heure>3</heure>
	<tarif id="1">2</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="4"><heure>4</heure>
	<tarif id="1">2</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="5"><heure>5</heure>
	<tarif id="1">2</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="6"><heure>6</heure>
	<tarif id="1">1</tarif>
	<tarif id="2">4</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="7"><heure>7</heure>
	<tarif id="2">16</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="8"><heure>8</heure>
	<tarif id="2">12</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="9"><heure>9</heure>
	<tarif id="2">8</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="10"><heure>10</heure>
	<tarif id="2">3</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="11"><heure>11</heure>
	<tarif id="2">3</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="12"><heure>12</heure>
	<tarif id="2">1</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="13"><heure>13</heure>
	<tarif id="2">1</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="14"><heure>14</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="15"><heure>15</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="16"><heure>16</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="17"><heure>17</heure>
	<tarif id="2">3</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="18"><heure>18</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="19"><heure>19</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="20"><heure>20</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="21"><heure>21</heure>
	<tarif id="2">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="22"><heure>22</heure>
	<tarif id="2">0</tarif>
	<tarif id="1">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
		<conso id="23"><heure>23</heure>
	<tarif id="1">0</tarif>
	<tarif id="127">0</tarif>
	</conso>
	
	</conso_journaliere>
	
	</response>
	 */
	private ElectricalConsumption retrieveConsumptionData(final String apiUrl, final ZonedDateTime zonedDateTime) {
		//http://192.168.1.60/cgi/unite_historique.cgi?uniteHisto=0
		callWS(apiUrl + "/cgi/date_historique.cgi?dateHistoJour=" + zonedDateTime.getDayOfMonth() + "&dateHistoMois=" + zonedDateTime.getMonthValue() + "&dateHistoAnnee=" + (zonedDateTime.getYear() - 2000) + "");

		final int hour = zonedDateTime.getHour();
		final ElectricalConsumption newElectricalConsumption = new ElectricalConsumption();
		newElectricalConsumption.setTimestamp(ZonedDateTime.of(zonedDateTime.toLocalDate(), LocalTime.of(hour, 0), ZONE_PARIS).toInstant());

		callWS(apiUrl + "/cgi/unite_historique.cgi?uniteHisto=0"); //0:Centime Euro, 1:Kwh
		final Element resultEuroCents = callXmlWS(apiUrl + "/xml/conso-journaliere.xml");
		final Element elementCost = resultEuroCents.find("...", "conso[id=" + String.valueOf(hour) + "]").single();
		final Element tarifHC = elementCost.find("...", "tarif[id=1]").singleOrDefault();
		final Element tarifHP = elementCost.find("...", "tarif[id=2]").singleOrDefault();
		final Element tarifH = elementCost.find("...", "tarif[id=0]").singleOrDefault();
		if (tarifHC != null || tarifHP != null || tarifH != null) {
			final double costHC = tarifHC != null ? asDouble(tarifHC.text()) : 0;
			final double costHP = tarifHP != null ? asDouble(tarifHP.text()) : tarifH != null ? asDouble(tarifH.text()) : 0;
			newElectricalConsumption.setCostHC(costHC / 100d);
			newElectricalConsumption.setCostHP(costHP / 100d);
		}

		callWS(apiUrl + "/cgi/unite_historique.cgi?uniteHisto=1");//0:Centime Euro, 1:Kwh
		final Element resultPower = callXmlWS(apiUrl + "/xml/conso-journaliere.xml");
		final Element elementPower = resultPower.find("...", "conso[id=" + String.valueOf(hour) + "]").single();
		final Element powerHCElement = elementPower.find("...", "tarif[id=1]").singleOrDefault();
		final Element powerHPElement = elementPower.find("...", "tarif[id=2]").singleOrDefault();
		final Element powerHElement = elementPower.find("...", "tarif[id=0]").singleOrDefault();
		if (powerHCElement != null || powerHPElement != null || powerHElement != null) {
			final int powerHC = powerHCElement != null ? asInt(powerHCElement.text()) : 0;
			final int powerHP = powerHPElement != null ? asInt(powerHPElement.text()) : powerHElement != null ? asInt(powerHElement.text()) : 0;
			newElectricalConsumption.setPowerHC(powerHC);
			newElectricalConsumption.setPowerHP(powerHP);
		}
		return newElectricalConsumption;
	}

	private Collection<ElectricalConsumption> retrieveConsumptionData(final String apiUrl, final LocalDate localDate) {
		//http://192.168.1.60/cgi/unite_historique.cgi?uniteHisto=0
		callWS(apiUrl + "/cgi/date_historique.cgi?dateHistoJour=" + localDate.getDayOfMonth() + "&dateHistoMois=" + localDate.getMonthValue() + "&dateHistoAnnee=" + (localDate.getYear() - 2000) + "");

		//final int hour = zonedDateTime.getHour();
		final Map<Integer, ElectricalConsumption> newElectricalConsumptionMap = new HashMap<>();

		callWS(apiUrl + "/cgi/unite_historique.cgi?uniteHisto=0"); //0:Centime Euro, 1:Kwh
		final Element resultEuroCents = callXmlWS(apiUrl + "/xml/conso-journaliere.xml");
		for (int hour = 0; hour <= 23; hour++) {
			final Element elementCost = resultEuroCents.find("...", "conso[id=" + String.valueOf(hour) + "]").single();
			final Element tarifHC = elementCost.find("...", "tarif[id=1]").singleOrDefault();
			final Element tarifHP = elementCost.find("...", "tarif[id=2]").singleOrDefault();
			final Element tarifH = elementCost.find("...", "tarif[id=0]").singleOrDefault();
			if (tarifHC != null || tarifHP != null || tarifH != null) {
				final ElectricalConsumption newElectricalConsumption = newElectricalConsumptionMap.computeIfAbsent(hour, k -> new ElectricalConsumption());
				newElectricalConsumption.setTimestamp(ZonedDateTime.of(localDate, LocalTime.of(hour, 0), ZONE_PARIS).toInstant());
				final double costHC = tarifHC != null ? asDouble(tarifHC.text()) : 0;
				final double costHP = tarifHP != null ? asDouble(tarifHP.text()) : tarifH != null ? asDouble(tarifH.text()) : 0;
				newElectricalConsumption.setCostHC(costHC / 100d);
				newElectricalConsumption.setCostHP(costHP / 100d);
			}
		}
		callWS(apiUrl + "/cgi/unite_historique.cgi?uniteHisto=1");//0:Centime Euro, 1:Kwh
		final Element resultPower = callXmlWS(apiUrl + "/xml/conso-journaliere.xml");
		for (int hour = 0; hour <= 23; hour++) {
			final Element elementPower = resultPower.find("...", "conso[id=" + String.valueOf(hour) + "]").single();
			final Element powerHCElement = elementPower.find("...", "tarif[id=1]").singleOrDefault();
			final Element powerHPElement = elementPower.find("...", "tarif[id=2]").singleOrDefault();
			final Element powerHElement = elementPower.find("...", "tarif[id=0]").singleOrDefault();
			if (powerHCElement != null || powerHPElement != null || powerHElement != null) {
				final ElectricalConsumption newElectricalConsumption = newElectricalConsumptionMap.computeIfAbsent(hour, k -> new ElectricalConsumption());
				newElectricalConsumption.setTimestamp(ZonedDateTime.of(localDate, LocalTime.of(hour, 0), ZONE_PARIS).toInstant());
				final int powerHC = powerHCElement != null ? asInt(powerHCElement.text()) : 0;
				final int powerHP = powerHPElement != null ? asInt(powerHPElement.text()) : powerHElement != null ? asInt(powerHElement.text()) : 0;
				newElectricalConsumption.setPowerHC(powerHC);
				newElectricalConsumption.setPowerHP(powerHP);
			}
		}
		return newElectricalConsumptionMap.values();
	}

	private void putInfosToInflux(final ElectricalConsumption electricalConsumption, final ElectricalTarification electricalTarification, final String measureName) {
		final int dayOfYear = electricalConsumption.getTimestamp().atZone(ZONE_PARIS).getDayOfYear();
		final String dayOfWeek = electricalConsumption.getTimestamp().atZone(ZONE_PARIS).getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH);
		final Measure heatersMeasure = Measure.builder(measureName)
				.time(electricalConsumption.getTimestamp())
				.addField("costHC", electricalConsumption.getCostHC())
				.addField("costHP", electricalConsumption.getCostHP())
				.addField("cost", electricalConsumption.getCostHP() + electricalConsumption.getCostHC())
				.addField("powerHC", electricalConsumption.getPowerHC())
				.addField("powerHP", electricalConsumption.getPowerHP())
				.addField("power", electricalConsumption.getPowerHC() + electricalConsumption.getPowerHP())
				.addField("computedCostHC", electricalConsumption.getPowerHC() * electricalTarification.getCostHC() / 1000d)
				.addField("computedCostHP", electricalConsumption.getPowerHP() * electricalTarification.getCostHP() / 1000d)
				.addField("computedCost", (electricalConsumption.getPowerHC() * electricalTarification.getCostHC() + electricalConsumption.getPowerHP() * electricalTarification.getCostHP()) / 1000d)
				.addField("dayOfWeek", dayOfWeek)
				.addField("dayOfYear", dayOfYear)
				.tag("dayOfWeek", dayOfWeek)
				.tag("dayOfYear", String.valueOf(dayOfYear))
				.build();
		putInfosToInflux(heatersMeasure);
	}

	private static Element callXmlWS(final String wsUrl) {
		return Xml.xml(callWS(wsUrl)).getRootElement();
	}

	private static String callWS(final String wsUrl) {
		Assertion.check().isNotBlank(wsUrl);
		// ---
		try {
			final URL url = new URL(wsUrl);
			final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(5000);
			httpURLConnection.setRequestProperty("Content-Type", "application/xml");

			final ByteArrayOutputStream result = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			try (InputStream inputStream = httpURLConnection.getInputStream()) {
				int length;
				while ((length = inputStream.read(buffer)) != -1) {
					result.write(buffer, 0, length);
				}
			}
			LOGGER.info("Call : " + wsUrl);
			if (wsUrl.contains(".xml")) {
				LOGGER.debug(result.toString("UTF-8"));
			}
			return result.toString("UTF-8");
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

	}

}
