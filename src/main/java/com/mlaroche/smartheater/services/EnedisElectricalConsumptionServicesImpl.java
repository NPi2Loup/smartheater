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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import javax.inject.Inject;

import io.vertigo.basics.formatter.FormatterDefault;
import io.vertigo.core.lang.BasicType;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.param.ParamManager;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.MeasureBuilder;
import io.vertigo.database.timeseries.TimeSeriesManager;
import io.vertigo.datamodel.structure.definitions.FormatterException;

public class EnedisElectricalConsumptionServicesImpl implements EnedisElectricalConsumptionServices {

	//private final static Logger LOGGER = LogManager.getLogger(ElectricalConsumptionServicesImpl.class);

	private final FormatterDefault formatterDefault = new FormatterDefault(null);
	private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");

	@Inject
	private ParamManager paramManager;
	@Inject
	private TimeSeriesManager timeSeriesManager;

	private static final int HC_END_MIN = 6 * 60 + 30; //6h30
	private static final int HC_START_MIN = 22 * 60 + 30; //22h30
	private static final double HC_COST = 0.1360;
	private static final double HP_COST = 0.1821;

	/** {@inheritDoc } */
	@Override
	public void reprise(final String fromDate, final String toDate, final File consoHeure, final File consoPMax, final String measureName) {
		final Instant start30minDateTime = LocalDate.of(2021, 04, 30).atStartOfDay(ZONE_PARIS).toInstant();
		//final LocalDate fromDateTime = LocalDate.of(2016, 12, 11);
		final LocalDate fromDateTime = LocalDate.parse(fromDate);
		final LocalDate toDateTime = LocalDate.parse(toDate);
		//final LocalDate toDateTime = LocalDate.now(ZONE_PARIS);

		try (BufferedReader br = new BufferedReader(new FileReader(consoPMax))) {
			String line;
			while ((line = br.readLine()) != null) {
				final String[] values = line.split(";");
				final Instant timestamp = asInstant(values[0]);
				if (timestamp.isBefore(toDateTime.atStartOfDay(ZONE_PARIS).toInstant())
						&& !timestamp.isBefore(fromDateTime.atStartOfDay(ZONE_PARIS).toInstant())) {
					final int powerMax = asInt(values[1]);
					final Measure electricalMeasure = Measure.builder(measureName)
							.time(timestamp)
							.addField("powerMax", powerMax)
							.build();
					putInfosToInflux(electricalMeasure);
				}
			}
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final DateTimeFormatter toDayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		final DateTimeFormatter toMonthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
		final DateTimeFormatter toYearFormatter = DateTimeFormatter.ofPattern("yyyy");

		try (BufferedReader br = new BufferedReader(new FileReader(consoHeure))) {
			String line;
			while ((line = br.readLine()) != null) {
				final String[] values = line.split(";");
				Instant timestamp = asInstant(values[0]); //c'est l'heure de fin, mais pour les aggrégations il faut l'heure de début
				final int durationMinutes = !timestamp.isBefore(start30minDateTime) ? 30 : 60;
				timestamp = timestamp.minus(durationMinutes, ChronoUnit.MINUTES);
				if (timestamp.isBefore(toDateTime.atStartOfDay(ZONE_PARIS).toInstant())
						&& !timestamp.isBefore(fromDateTime.atStartOfDay(ZONE_PARIS).toInstant())
						&& values.length > 1) {
					final int power = asInt(values[1]);
					final int powerWh = (int) (asInt(values[1]) * (durationMinutes / 60d));
					final int startMinuteOfDay = timestamp.atZone(ZONE_PARIS).getHour() * 60 + timestamp.atZone(ZONE_PARIS).getMinute();
					final int endMinuteOfDay = startMinuteOfDay + durationMinutes;
					final boolean startHC = startMinuteOfDay >= HC_START_MIN || startMinuteOfDay < HC_END_MIN;
					final boolean endHC = endMinuteOfDay > HC_START_MIN || endMinuteOfDay <= HC_END_MIN;
					final int powerHC = (int) (startHC && endHC ? power : startHC && !endHC ? power / 2d : 0);
					final int powerHP = (int) (startHC && endHC ? 0 : startHC && !endHC ? power / 2d : power);
					final int powerWhHC = (int) (powerHC * (durationMinutes / 60d));//on a besoin de kWh
					final int powerWhHP = (int) (powerHP * (durationMinutes / 60d));
					final double costHC = powerWhHC / 1000d * HC_COST; //le cout est en kWh
					final double costHP = powerWhHP / 1000d * HP_COST;
					final int dayOfYear = timestamp.atZone(ZONE_PARIS).getDayOfYear();
					final String month = timestamp.atZone(ZONE_PARIS).getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH);
					final String dayOfWeek = timestamp.atZone(ZONE_PARIS).getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH);
					final MeasureBuilder electricalMeasureBuilder = Measure.builder(measureName)
							.time(timestamp)
							.addField("cost", costHC + costHP)
							.addField("power", power)
							.addField("powerWh", powerWh)
							.addField("durationMin", durationMinutes)
							.addField("dayOfWeek", dayOfWeek)
							.addField("dayOfYear", dayOfYear)
							.addField("month", month)
							.tag("dayOfWeek", dayOfWeek)
							.tag("month", month)
							.tag("dayOfYear", String.valueOf(dayOfYear))
							.tag("timeAsDay", timestamp.atZone(ZONE_PARIS).format(toDayFormatter))
							.tag("timeAsMonth", timestamp.atZone(ZONE_PARIS).format(toMonthFormatter))
							.tag("year", timestamp.atZone(ZONE_PARIS).format(toYearFormatter));
					if (powerHC > 0) {
						electricalMeasureBuilder
								.addField("costHC", costHC)
								.addField("powerHC", powerHC)
								.addField("powerWhHC", powerWhHC);
					}
					if (powerHP > 0) {
						electricalMeasureBuilder
								.addField("costHP", costHP)
								.addField("powerHP", powerHP)
								.addField("powerWhHP", powerWhHP);
					}
					putInfosToInflux(electricalMeasureBuilder.build());
				}
			}
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//https://apps.lincs.enedis.fr/mes-mesures/api/private/v1/personnes/DDR717QSA/prms/21102894297007/donnees-pmax?dateDebut=6-1-2019&dateFin=21-12-2021&mesuretypecode=CONS
	
	//https://apps.lincs.enedis.fr/mes-mesures/api/private/v1/personnes/DDR717QSA/prms/21102894297007/courbe-de-charge?dateDebut=14-12-2021&dateFin=21-12-2021&mesuretypecode=CONS
	
	private Instant asInstant(final String strValue) {
		if (!Character.isDigit(strValue.charAt(0))) {
			return OffsetDateTime.parse(strValue.substring(1)).toInstant();
		}
		return OffsetDateTime.parse(strValue).toInstant();
	}

	private Integer asInt(final String strValue) {
		try {
			return (Integer) formatterDefault.stringToValue(strValue, BasicType.Integer);
		} catch (final FormatterException e) {
			throw WrappedException.wrap(e);
		}
	}

	/*private Double asDouble(final String strValue) {
		try {
			return (Double) formatterDefault.stringToValue(strValue, BasicType.Double);
		} catch (final FormatterException e) {
			throw WrappedException.wrap(e);
		}
	}*/

	private void putInfosToInflux(final Measure measure) {
		final String dbName = paramManager.getParam("influxdb_dbname").getValueAsString();
		timeSeriesManager.insertMeasure(dbName, measure);
	}

	/*
	@Inject
	private ParamManager paramManager;
	@Inject
	private CodecManager codecManager;
	@Inject
	private TimeSeriesManager timeSeriesManager;
	
	private static final String ENEDIS_LOGIN_PARAM = "enedis_login";
	private static final String ENEDIS_PASSWORD_PARAM = "enedis_password";
	private static final DateTimeFormatter ENEDIS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Gson GSON = new Gson();
	
	@Override
	public void run() {
	
		final String dbName = paramManager.getParam("influxdb_dbname").getValueAsString();
		final TimedDatas lastConsumption = timeSeriesManager.getTabularTimedData(dbName, Arrays.asList("meanPower:last"), DataFilter.builder("electricalConsumption").build(), TimeFilter.builder("now() - 30w", "now()").build());
		final LocalDate from;
		if (!lastConsumption.getTimedDataSeries().isEmpty()) {
			from = lastConsumption.getTimedDataSeries().get(0).getTime().atZone(ZoneId.of("Europe/Paris")).toLocalDate();
		} else {
			from = LocalDate.now().minusMonths(6); // go back 6 months just to have some data
		}
	
		if (from.until(LocalDate.now(), ChronoUnit.DAYS) > 1L) {
			//there is data to be retrieve
			final List<ElectricalConsumption> electricalConsumptions = retrieveHourData(from, LocalDate.now());
			final List<Measure> measures = electricalConsumptions.stream().map(electricalConsumption -> Measure.builder("electricalConsumption")
					.time(electricalConsumption.getTimestamp())
					.addField("meanPower", electricalConsumption.getMeanPower())
					.build()).collect(Collectors.toList());
			timeSeriesManager.insertMeasures(dbName, measures);
		}
	}
	
	private List<ElectricalConsumption> retrieveHourData(final LocalDate from, final LocalDate to) {
	
		final CookieJar cookieJar = new CookieJar() {
			private final List<Cookie> cookies = new ArrayList<>();
	
			@Override
			public void saveFromResponse(final HttpUrl url, final List<Cookie> cookies) {
				this.cookies.addAll(cookies);
			}
	
			@Override
			public List<Cookie> loadForRequest(final HttpUrl url) {
				return cookies;
	
			}
		};
	
		final OkHttpClient client = new OkHttpClient.Builder()
				.followRedirects(false)
				.cookieJar(cookieJar)
				.build();
	
		final String login = paramManager.getParam(ENEDIS_LOGIN_PARAM).getValueAsString();
		final String password = paramManager.getParam(ENEDIS_PASSWORD_PARAM).getValueAsString();
	
		final RequestBody formBodyLogin = new FormBody.Builder()
				.add("IDToken1", login)
				.add("IDToken2", password)
				.add("goto", codecManager.getBase64Codec().encode("https://espace-client-particuliers.enedis.fr/group/espace-particuliers/accueil".getBytes(StandardCharsets.UTF_8)))
				.add("SunQueryParamsString", codecManager.getBase64Codec().encode("realm=particuliers".getBytes(StandardCharsets.UTF_8)))
				.add("encoded", "true")
				.add("gx_charset", "UTF-8")
				.build();
	
		final Request requestLogin = new Request.Builder()
				.url("https://espace-client-connexion.enedis.fr/auth/UI/Login")
				.post(formBodyLogin)
				.build();
	
		final RequestBody formBodyData = new FormBody.Builder()
				.add("p_p_id", "lincspartdisplaycdc_WAR_lincspartcdcportlet")
				.add("p_p_lifecycle", "2")
				.add("p_p_resource_id", "urlCdcHeure")
				.add("_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateDebut", from.format(ENEDIS_DATE_FORMAT))
				.add("_lincspartdisplaycdc_WAR_lincspartcdcportlet_dateFin", to.format(ENEDIS_DATE_FORMAT))
				.build();
	
		final Request requestData = new Request.Builder()
				.url("https://espace-client-particuliers.enedis.fr/group/espace-particuliers/suivi-de-consommation")
				.post(formBodyData)
				.build();
	
		final EnedisInfo enedisInfo;
		try {
			client.newCall(requestLogin).execute().close();//login
			client.newCall(requestData).execute().close(); //first call needed don't know why maybe a cookie...
	
			final Response response = client.newCall(requestData).execute();
			enedisInfo = GSON.fromJson(response.body().string(), EnedisInfo.class);
			response.close();
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	
		Assertion.check().isTrue("termine".equals(enedisInfo.etat.valeur), "Error retrieving infos from Enedis");
		final Instant debut = LocalDate.parse(enedisInfo.graphe.periode.dateDebut, ENEDIS_DATE_FORMAT).atStartOfDay(ZoneId.of("Europe/Paris")).toInstant();
		final Instant fin = LocalDate.parse(enedisInfo.graphe.periode.dateFin, ENEDIS_DATE_FORMAT).atStartOfDay(ZoneId.of("Europe/Paris")).toInstant();
		return enedisInfo.graphe.data
				.stream()
				.filter(enedisData -> enedisData.valeur != -2.0)
				.map(enedisData -> {
					final ElectricalConsumption electricalConsumption = new ElectricalConsumption();
					electricalConsumption.setTimestamp(debut.plus(30 * (enedisData.ordre - 1), ChronoUnit.MINUTES));
					electricalConsumption.setMeanPower(enedisData.valeur);
					return electricalConsumption;
				}).collect(Collectors.toList());
	
	}
	
	private static class EnedisInfo {
		public EtatEnedis etat;
		public GrapheEnedis graphe;
	
		private static class EtatEnedis {
			public String valeur;
		}
	
		private static class GrapheEnedis {
			//public Integer puissanceSouscrite;
			//public Integer decalage;
			public PeriodeEnedis periode;
			public List<DataEnedis> data = new ArrayList<>();
		}
	
		private static class PeriodeEnedis {
			public String dateDebut;
			public String dateFin;
		}
	
		private static class DataEnedis {
			public Double valeur;
			public Integer ordre;
		}
	
	}*/

}
