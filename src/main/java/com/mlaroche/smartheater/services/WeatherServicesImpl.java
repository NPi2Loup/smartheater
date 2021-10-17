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
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mlaroche.smartheater.domain.WeatherInfo;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.param.ParamManager;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TimeSeriesManager;

public class WeatherServicesImpl implements WeatherServices {

	//private final static Logger LOGGER = LogManager.getLogger(WeatherServicesImpl.class);

	
	@Inject
	private ParamManager paramManager;
	@Inject
	private TimeSeriesManager timeSeriesDataBaseManager;

	private static final String API_KEY_PARAM_NAME = "openweather_api_key";
	private static final String API_STATION_ID_PARAM_NAME = "openweather_stationid";
	private static final Gson GSON = new Gson();

	//last data
	private WeatherInfo weatherInfo;



	@Override
	public WeatherInfo getWeatherInfo() {
		return weatherInfo;
	}

	@Override
	public void updateWeather(final String measureName) {
		weatherInfo = retrieveData();
		putInfosToInflux(weatherInfo, measureName);
	}

	//ex:{"coord":{"lon":145.77,"lat":-16.92},
	//"weather":[{"id":802,"main":"Clouds","description":"scattered clouds","icon":"03n"}],
	//"base":"stations",
	//"main":{"temp":300.15,"pressure":1007,"humidity":74,"temp_min":300.15,"temp_max":300.15},"visibility":10000,"wind":{"speed":3.6,"deg":160},
	//"clouds":{"all":40},"dt":1485790200,
	//"sys":{"type":1,"id":8166,"message":0.2064,"country":"AU","sunrise":1485720272,"sunset":1485766550},"id":2172797,"name":"Cairns","cod":200}
	private WeatherInfo retrieveData() {
		final String apiKey = paramManager.getParam(API_KEY_PARAM_NAME).getValueAsString();
		final String stationId = paramManager.getParam(API_STATION_ID_PARAM_NAME).getValueAsString();

		final JsonObject result = callRestWS("http://api.openweathermap.org/data/2.5/weather?id=" + stationId + "&lang=fr&units=metric&APPID=" + apiKey, JsonObject.class);
		//LOGGER.info("Url :" + "http://api.openweathermap.org/data/2.5/weather?id=" + stationId + "&lang=fr&units=metric&APPID=" + apiKey);
		//LOGGER.info("Result :" + result);
		final WeatherInfo newWeatherInfo = new WeatherInfo();
		newWeatherInfo.setTimestamp(Instant.ofEpochSecond(result.get("dt").getAsLong()));
		newWeatherInfo.setTemperature(result.getAsJsonObject("main").get("temp").getAsDouble());
		newWeatherInfo.setPressure(result.getAsJsonObject("main").get("pressure").getAsInt());
		newWeatherInfo.setHumidity(result.getAsJsonObject("main").get("humidity").getAsDouble());
		newWeatherInfo.setClouds(result.getAsJsonObject("clouds").get("all").getAsInt());
		newWeatherInfo.setLocation(result.get("name").getAsString());
		newWeatherInfo.setDescription(result.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString());
		newWeatherInfo.setIcon(result.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString());
		newWeatherInfo.setSunrise(Instant.ofEpochSecond(result.getAsJsonObject("sys").get("sunrise").getAsLong()));
		newWeatherInfo.setSunset(Instant.ofEpochSecond(result.getAsJsonObject("sys").get("sunset").getAsLong()));
		return newWeatherInfo;
	}

	private void putInfosToInflux(final WeatherInfo weatherInfo, final String measureName) {
		final ZoneId zoneParis = ZoneId.of("Europe/Paris");
		final String dbName = paramManager.getParam("influxdb_dbname").getValueAsString();
		final int currentMinute = LocalDateTime.now(zoneParis).get(ChronoField.MINUTE_OF_DAY);
		final int sunriseMinute = LocalDateTime.ofInstant(weatherInfo.getSunrise(), zoneParis).get(ChronoField.MINUTE_OF_DAY);
		final int sunsetMinute = LocalDateTime.ofInstant(weatherInfo.getSunset(), zoneParis).get(ChronoField.MINUTE_OF_DAY);
		final Measure weatherInfoMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("nowTime", currentMinute)
				.addField("mesureTime", LocalDateTime.ofInstant(weatherInfo.getTimestamp(), zoneParis).get(ChronoField.MINUTE_OF_DAY))
				.addField("temperature", weatherInfo.getTemperature())
				.addField("humidity", weatherInfo.getHumidity())
				.addField("pressure", weatherInfo.getPressure())
				.addField("clouds", weatherInfo.getClouds())
				.addField("description", weatherInfo.getDescription())
				.addField("location", weatherInfo.getLocation())
				.addField("icon", weatherInfo.getIcon())
				.addField("daylenght", sunsetMinute - sunriseMinute)
				.addField("daylight", Math.min(currentMinute - sunriseMinute, sunsetMinute - currentMinute))
				.addField("sunrise", sunriseMinute)
				.addField("sunset", sunsetMinute)
				.tag("description", weatherInfo.getDescription())
				.tag("location", weatherInfo.getLocation())
				.tag("icon", weatherInfo.getIcon())
				.build();
		timeSeriesDataBaseManager.insertMeasure(dbName, weatherInfoMeasure);

	}

	private static <R> R callRestWS(final String wsUrl, final Type returnType) {
		Assertion.check().isNotBlank(wsUrl);
		// ---
		try {
			final URL url = new URL(wsUrl);
			final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(2000);
			httpURLConnection.setRequestProperty("Content-Type", "application/json");

			final ByteArrayOutputStream result = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			try (InputStream inputStream = httpURLConnection.getInputStream()) {
				int length;
				while ((length = inputStream.read(buffer)) != -1) {
					result.write(buffer, 0, length);
				}
			}
			return GSON.fromJson(result.toString("UTF-8"), returnType);
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}

	}

}
