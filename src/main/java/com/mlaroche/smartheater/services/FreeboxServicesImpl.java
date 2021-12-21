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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mlaroche.smartheater.domain.FreeboxInfo;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.ParamManager;
import io.vertigo.database.timeseries.Measure;
import io.vertigo.database.timeseries.TimeSeriesManager;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FreeboxServicesImpl implements FreeboxServices, Activeable {

	//private final static Logger LOGGER = LogManager.getLogger(FreeboxServicesImpl.class);

	@Inject
	private ParamManager paramManager;
	@Inject
	private TimeSeriesManager timeSeriesManager;

	private static final String FREEBOX_APP_ID = "freebox_app_id";
	private static final String FREEBOX_APP_TOKEN = "freebox_app_token";
	private static final String FREEBOX_SESSION_HEADER = "X-Fbx-App-Auth";

	private static final Gson GSON = new Gson();

	private final CookieJar cookieJar = new CookieJar() {
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

	@Override
	public void start() {

	}

	@Override
	public void stop() {
		// nothing

	}

	@Override
	public void updateFreebox(final String measureName) {
		final String sessionId = callLoginPost();
		try {
			final FreeboxInfo freeboxInfo = retrieveData(sessionId);
			
			putInfosToInflux(freeboxInfo, measureName);
			
			retrieveEthernet(measureName, sessionId);
			
			retrieveWifi(measureName, sessionId);
		} finally {
			closeSession(sessionId);
		}
	}

	private void retrieveEthernet(final String measureName, final String sessionId) {
		//{"success":true,"result":[{"duplex":"half","name":"Ethernet 3","link":"down","id":3,"mode":"10BaseT-HD","speed":"10","rrd_id":"3"},{"duplex":"half","name":"Ethernet 1","link":"down","id":1,"mode":"10BaseT-HD","speed":"10","rrd_id":"1"},{"duplex":"full","mac_list":[{"mac":"14:BB:6E:40:E8:5B","hostname":"[BD]H8900"},{"mac":"1C:5A:6B:D2:A1:9A","hostname":"TV Salon"},{"mac":"00:24:C6:11:DB:C1","hostname":"Alarme"},{"mac":"00:11:32:65:9D:F3","hostname":"NAS-Beug"},{"mac":"8C:97:EA:56:64:AE","hostname":"Repeteur Wifi Freebox"}],"name":"Ethernet 2","link":"up","id":2,"mode":"1000BaseT-FD","speed":"1000","rrd_id":"2"}]}
		String ethernetMesureName = measureName+".ethernet";
		final JsonObject ethernetResponse = callRestWS("http://mafreebox.freebox.fr/api/v8/switch/status/", sessionId, JsonObject.class);
		if(ethernetResponse.get("success").getAsBoolean() && ethernetResponse.getAsJsonArray("result")!=null) {
			for (final JsonElement ethernetElt : ethernetResponse.getAsJsonArray("result")) {
				final JsonObject ethernet = ethernetElt.getAsJsonObject();
				int id = ethernet.get("id").getAsInt();
				String name = ethernet.get("name").getAsString();
				String mode = ethernet.get("mode").getAsString();
				String link = ethernet.get("link").getAsString();
				if("up".equals(link)) {
					List<String> macs = new ArrayList<>();
					List<String> hostnames = new ArrayList<>();
					for (final JsonElement macElt : ethernet.get("mac_list").getAsJsonArray()) {
						final JsonObject mac = macElt.getAsJsonObject();
						macs.add(mac.get("mac").getAsString());
						hostnames.add(mac.get("hostname").getAsString());
					}
					updateEthernet(id, name, mode, ethernetMesureName, hostnames, macs, sessionId);
				}
			}
		}
	}
	


	private void retrieveWifi(final String measureName, final String sessionId) {
		//{"success":true,"result":[{"capabilities":{"2d4g":{"shortgi20":true,"vht_rx_ldpc":false,"ldpc":true,"ht_40":true,"smps_dynamic":true,"vht_shortgi80":false,"vht_rx_stbc_4":false,"vht_txops_ps":false,"vht_max_ampdu_len_exp4":false,"smps_static":false,"vht_shortgi160":false,"vht_tx_stbc":false,"delayed_ba":false,"ht_20":true,"vht_max_ampdu_len_exp3":false,"vht_su_beamformer":false,"vht_tx_antenna_consistency":false,"vht_rx_stbc_3":false,"vht_max_ampdu_len_exp1":false,"vht_rx_antenna_consistency":false,"vht_max_ampdu_len_exp7":false,"vht_mpdu_len_11454":false,"vht_max_ampdu_len_exp2":false,"vht_rx_stbc_1":false,"vht_max_ampdu_len_exp6":false,"vht_max_ampdu_len_exp5":false,"max_amsdu_7935":false,"vht_su_beamformee":false,"dsss_cck_40":true,"vht_80_80":false,"vht_160":false,"vht_htc":false,"psmp":false,"vht_80":false,"rx_stbc_3":false,"rx_stbc_2":false,"supported":true,"greenfield":false,"vht_rx_stbc_2":false,"rx_stbc_1":true,"vht_mu_beamformee":false,"vht_mpdu_len_7991":false,"tx_stbc":true,"shortgi40":true,"vht_mu_beamformer":false,"lsig_txop_prot":false},"60g":{"shortgi20":false,"vht_rx_ldpc":false,"ldpc":false,"ht_40":false,"smps_dynamic":false,"vht_shortgi80":false,"vht_rx_stbc_4":false,"vht_txops_ps":false,"vht_max_ampdu_len_exp4":false,"smps_static":false,"vht_shortgi160":false,"vht_tx_stbc":false,"delayed_ba":false,"ht_20":false,"vht_max_ampdu_len_exp3":false,"vht_su_beamformer":false,"vht_tx_antenna_consistency":false,"vht_rx_stbc_3":false,"vht_max_ampdu_len_exp1":false,"vht_rx_antenna_consistency":false,"vht_max_ampdu_len_exp7":false,"vht_mpdu_len_11454":false,"vht_max_ampdu_len_exp2":false,"vht_rx_stbc_1":false,"vht_max_ampdu_len_exp6":false,"vht_max_ampdu_len_exp5":false,"max_amsdu_7935":false,"vht_su_beamformee":false,"dsss_cck_40":false,"vht_80_80":false,"vht_160":false,"vht_htc":false,"psmp":false,"vht_80":false,"rx_stbc_3":false,"rx_stbc_2":false,"supported":false,"greenfield":false,"vht_rx_stbc_2":false,"rx_stbc_1":false,"vht_mu_beamformee":false,"vht_mpdu_len_7991":false,"tx_stbc":false,"shortgi40":false,"vht_mu_beamformer":false,"lsig_txop_prot":false},"5g":{"shortgi20":false,"vht_rx_ldpc":false,"ldpc":false,"ht_40":false,"smps_dynamic":false,"vht_shortgi80":false,"vht_rx_stbc_4":false,"vht_txops_ps":false,"vht_max_ampdu_len_exp4":false,"smps_static":false,"vht_shortgi160":false,"vht_tx_stbc":false,"delayed_ba":false,"ht_20":false,"vht_max_ampdu_len_exp3":false,"vht_su_beamformer":false,"vht_tx_antenna_consistency":false,"vht_rx_stbc_3":false,"vht_max_ampdu_len_exp1":false,"vht_rx_antenna_consistency":false,"vht_max_ampdu_len_exp7":false,"vht_mpdu_len_11454":false,"vht_max_ampdu_len_exp2":false,"vht_rx_stbc_1":false,"vht_max_ampdu_len_exp6":false,"vht_max_ampdu_len_exp5":false,"max_amsdu_7935":false,"vht_su_beamformee":false,"dsss_cck_40":false,"vht_80_80":false,"vht_160":false,"vht_htc":false,"psmp":false,"vht_80":false,"rx_stbc_3":false,"rx_stbc_2":false,"supported":false,"greenfield":false,"vht_rx_stbc_2":false,"rx_stbc_1":false,"vht_mu_beamformee":false,"vht_mpdu_len_7991":false,"tx_stbc":false,"shortgi40":false,"vht_mu_beamformer":false,"lsig_txop_prot":false}},"name":"2.4G","id":0,"config":{"channel_width":"20","ht":{"greenfield":false,"shortgi20":true,"vht_rx_ldpc":false,"ldpc":false,"vht_rx_stbc":"disabled","vht_shortgi80":false,"ht_enabled":true,"rx_stbc":"1","dsss_cck_40":false,"tx_stbc":true,"ac_enabled":false,"smps":"disabled","vht_shortgi160":false,"vht_mu_beamformer":false,"vht_tx_stbc":false,"vht_su_beamformee":false,"vht_su_beamformer":false,"delayed_ba":false,"vht_tx_antenna_consistency":false,"max_amsdu_7935":false,"vht_max_ampdu_len_exp":0,"vht_max_mpdu_len":"default","psmp":false,"shortgi40":true,"vht_rx_antenna_consistency":false,"lsig_txop_prot":false},"dfs_enabled":false,"band":"2d4g","secondary_channel":0,"primary_channel":0},"status":{"channel_width":"20","primary_channel":6,"dfs_disabled":false,"dfs_cac_remaining_time":0,"secondary_channel":0,"state":"active"}},{"capabilities":{"2d4g":{"shortgi20":false,"vht_rx_ldpc":false,"ldpc":false,"ht_40":false,"smps_dynamic":false,"vht_shortgi80":false,"vht_rx_stbc_4":false,"vht_txops_ps":false,"vht_max_ampdu_len_exp4":false,"smps_static":false,"vht_shortgi160":false,"vht_tx_stbc":false,"delayed_ba":false,"ht_20":false,"vht_max_ampdu_len_exp3":false,"vht_su_beamformer":false,"vht_tx_antenna_consistency":false,"vht_rx_stbc_3":false,"vht_max_ampdu_len_exp1":false,"vht_rx_antenna_consistency":false,"vht_max_ampdu_len_exp7":false,"vht_mpdu_len_11454":false,"vht_max_ampdu_len_exp2":false,"vht_rx_stbc_1":false,"vht_max_ampdu_len_exp6":false,"vht_max_ampdu_len_exp5":false,"max_amsdu_7935":false,"vht_su_beamformee":false,"dsss_cck_40":false,"vht_80_80":false,"vht_160":false,"vht_htc":false,"psmp":false,"vht_80":false,"rx_stbc_3":false,"rx_stbc_2":false,"supported":false,"greenfield":false,"vht_rx_stbc_2":false,"rx_stbc_1":false,"vht_mu_beamformee":false,"vht_mpdu_len_7991":false,"tx_stbc":false,"shortgi40":false,"vht_mu_beamformer":false,"lsig_txop_prot":false},"60g":{"shortgi20":false,"vht_rx_ldpc":false,"ldpc":false,"ht_40":false,"smps_dynamic":false,"vht_shortgi80":false,"vht_rx_stbc_4":false,"vht_txops_ps":false,"vht_max_ampdu_len_exp4":false,"smps_static":false,"vht_shortgi160":false,"vht_tx_stbc":false,"delayed_ba":false,"ht_20":false,"vht_max_ampdu_len_exp3":false,"vht_su_beamformer":false,"vht_tx_antenna_consistency":false,"vht_rx_stbc_3":false,"vht_max_ampdu_len_exp1":false,"vht_rx_antenna_consistency":false,"vht_max_ampdu_len_exp7":false,"vht_mpdu_len_11454":false,"vht_max_ampdu_len_exp2":false,"vht_rx_stbc_1":false,"vht_max_ampdu_len_exp6":false,"vht_max_ampdu_len_exp5":false,"max_amsdu_7935":false,"vht_su_beamformee":false,"dsss_cck_40":false,"vht_80_80":false,"vht_160":false,"vht_htc":false,"psmp":false,"vht_80":false,"rx_stbc_3":false,"rx_stbc_2":false,"supported":false,"greenfield":false,"vht_rx_stbc_2":false,"rx_stbc_1":false,"vht_mu_beamformee":false,"vht_mpdu_len_7991":false,"tx_stbc":false,"shortgi40":false,"vht_mu_beamformer":false,"lsig_txop_prot":false},"5g":{"shortgi20":true,"vht_rx_ldpc":true,"ldpc":true,"ht_40":true,"smps_dynamic":true,"vht_shortgi80":true,"vht_rx_stbc_4":false,"vht_txops_ps":false,"vht_max_ampdu_len_exp4":true,"smps_static":false,"vht_shortgi160":true,"vht_tx_stbc":true,"delayed_ba":false,"ht_20":true,"vht_max_ampdu_len_exp3":true,"vht_su_beamformer":true,"vht_tx_antenna_consistency":true,"vht_rx_stbc_3":false,"vht_max_ampdu_len_exp1":true,"vht_rx_antenna_consistency":true,"vht_max_ampdu_len_exp7":true,"vht_mpdu_len_11454":true,"vht_max_ampdu_len_exp2":true,"vht_rx_stbc_1":true,"vht_max_ampdu_len_exp6":true,"vht_max_ampdu_len_exp5":true,"max_amsdu_7935":true,"vht_su_beamformee":true,"dsss_cck_40":true,"vht_80_80":true,"vht_160":true,"vht_htc":false,"psmp":false,"vht_80":true,"rx_stbc_3":false,"rx_stbc_2":false,"supported":true,"greenfield":false,"vht_rx_stbc_2":false,"rx_stbc_1":true,"vht_mu_beamformee":true,"vht_mpdu_len_7991":true,"tx_stbc":true,"shortgi40":true,"vht_mu_beamformer":true,"lsig_txop_prot":false}},"name":"5G","id":1,"config":{"channel_width":"80","ht":{"greenfield":false,"shortgi20":true,"vht_rx_ldpc":true,"ldpc":true,"vht_rx_stbc":"1","vht_shortgi80":true,"ht_enabled":true,"rx_stbc":"1","dsss_cck_40":true,"tx_stbc":true,"ac_enabled":true,"smps":"disabled","vht_shortgi160":true,"vht_mu_beamformer":true,"vht_tx_stbc":true,"vht_su_beamformee":true,"vht_su_beamformer":true,"delayed_ba":false,"vht_tx_antenna_consistency":true,"max_amsdu_7935":false,"vht_max_ampdu_len_exp":0,"vht_max_mpdu_len":"11454","psmp":false,"shortgi40":true,"vht_rx_antenna_consistency":true,"lsig_txop_prot":false},"dfs_enabled":false,"band":"5g","secondary_channel":0,"primary_channel":0},"status":{"channel_width":"80","primary_channel":44,"dfs_disabled":true,"dfs_cac_remaining_time":0,"secondary_channel":48,"state":"active"}}]}
		String wifiMesureName = measureName+".wifi";
		final JsonObject wifiStationResponse = callRestWS("http://mafreebox.freebox.fr/api/v8/wifi/ap/", sessionId, JsonObject.class);
		if(wifiStationResponse.get("success").getAsBoolean() && wifiStationResponse.getAsJsonArray("result")!=null) {
			for (final JsonElement wifiStationElt : wifiStationResponse.getAsJsonArray("result")) {
				final JsonObject wifiStation = wifiStationElt.getAsJsonObject();
				int id = wifiStation.get("id").getAsInt();
				String name = wifiStation.get("name").getAsString();
				updateWifi(id, name, wifiMesureName, sessionId);
			}
		}
	}
	
	private void updateEthernet(final long ethId, final String ethName, final String ethMode, String measureName, List<String> hostnames, List<String> macs, String sessionId) {
		////{"success":true,"result":{"rx_discard_packets":74,"rx_bad_bytes":0,"tx_bytes":118496023,"rx_bytes_rate":4989,"tx_packets":208169,"tx_collisions":0,"rx_jabber_packets":0,"rx_multicast_packets":4891,"tx_multicast_packets":6270,"tx_late":0,"rx_fragments_packets":0,"rx_packets_rate":26,"rx_err_packets":0,"tx_packets_rate":25,"rx_filtered_packets":0,"rx_unicast_packets":139355,"tx_filtered_packets":0,"tx_multiple":0,"tx_fcs":0,"tx_single":0,"rx_pause":226,"tx_pause":0,"rx_good_packets":148408,"rx_broadcast_packets":4162,"tx_bytes_rate":4099,"rx_good_bytes":22055714,"rx_oversize_packets":0,"tx_unicast_packets":201475,"tx_broadcast_packets":424,"rx_fcs_packets":0,"tx_excessive":0,"tx_deferred":0,"rx_undersize_packets":0}}
		final JsonObject ethResponse = callRestWS("http://mafreebox.freebox.fr/api/v8/switch/port/"+ethId+"/stats", sessionId, JsonObject.class);
		if(ethResponse.get("success").getAsBoolean() && ethResponse.getAsJsonObject("result")!=null) {
				final JsonObject eth = ethResponse.getAsJsonObject("result");
				final int rx_good_bytes = eth.get("rx_good_bytes").getAsInt(); 
				final long rx_bytes_rate = eth.get("rx_bytes_rate").getAsLong();
				final long tx_bytes = eth.get("tx_bytes").getAsLong();
				final int tx_bytes_rate = eth.get("tx_bytes_rate").getAsInt();
				
				final Measure statusMeasure = Measure.builder(measureName)
						.time(Instant.now())
						.addField("ethName", ethName)
						.addField("ethId", ethId)
						.addField("ethMode", ethMode)
						.addField("hostnames", String.join(";", hostnames))
						.addField("macs", String.join(";", macs))
						.addField("rx_good_bytes", rx_good_bytes)
						.addField("rx_bytes_rate", rx_bytes_rate)
						.addField("tx_bytes", tx_bytes)
						.addField("tx_bytes_rate", tx_bytes_rate)
						.tag("ethName", ethName)
						.tag("ethMode", ethMode)
						.tag("hostnames", String.join(";", hostnames))
						.build();
		
				putInfosToInflux(statusMeasure);
		}
	}
	
	private void updateWifi(final long stationId, final String stationName, String measureName, String sessionId) {
		//{"success":true,"result":[{"rx_bytes":3040000,"tx_bytes":3240000,"bssid":"8C:97:EA:DF:64:70","host":{"l2ident":{"id":"D4:F5:47:32:B3:36","type":"mac_address"},"active":true,"persistent":true,"names":[{"name":"Google-Nest-Mini","source":"dhcp"},{"name":"0dc9c0f0-c90d-d3a8-701c-b9bbe1f6f37d","source":"mdns"}],"vendor_name":"Google Inc.","host_type":"multimedia_device","interface":"pub","id":"ether-d4:f5:47:32:b3:36","last_time_reachable":1605212925,"primary_name_manual":false,"network_control":{"current_mode":"allowed","profile_id":3,"name":"Google"},"default_name":"Google-Nest-Mini","l3connectivities":[{"addr":"192.168.1.62","active":true,"reachable":true,"last_activity":1605212929,"af":"ipv4","last_time_reachable":1605212901},{"addr":"fe80::d6f5:47ff:fe32:b336","active":true,"reachable":true,"last_activity":1605212842,"af":"ipv6","last_time_reachable":1605212842},{"addr":"2a01:e0a:2ed:360:8c39:32b6:49c:7f0e","active":false,"reachable":false,"last_activity":1605209116,"af":"ipv6","last_time_reachable":1605209116},{"addr":"2a01:e0a:2ed:360:acbc:aa0a:4b8:f5ce","active":false,"reachable":false,"last_activity":1605209121,"af":"ipv6","last_time_reachable":1605209121},{"addr":"2a01:e0a:2ed:360:acbc:aa0a:4b8:f5ce","active":true,"reachable":true,"last_activity":1605212848,"af":"ipv6","last_time_reachable":1605212848},{"addr":"2a01:e0a:2ed:360:d81a:13ef:6a9c:b0a0","active":true,"reachable":true,"last_activity":1605212925,"af":"ipv6","last_time_reachable":1605212925}],"reachable":true,"last_activity":1605212929,"access_point":{"mac":"8C:97:EA:DF:64:70","type":"gateway","connectivity_type":"wifi","uid":"5345eeb2c6077277d4dc51ee07893cae","wifi_information":{"band":"2d4g","ssid":"Beug","signal":-6},"rx_rate":709,"tx_rate":371},"primary_name":"Google-Nest-Mini"},"last_tx":{"bitrate":650,"mcs":6,"shortgi":true,"vht_mcs":-1,"width":"20"},"hostname":"Google-Nest-Mini","mac":"D4:F5:47:32:B3:36","access_type":"full","custom_key_id":0,"id":"8C:97:EA:DF:64:70-D4:F5:47:32:B3:36","pairwise_cipher":"ccmp","wpa_alg":"wpa2","state":"authenticated","inactive":0,"last_rx":{"bitrate":10,"mcs":-1,"shortgi":false,"vht_mcs":-1,"width":"20"},"flags":{"vht":false,"legacy":false,"authorized":true,"ht":true},"tx_rate":371,"conn_duration":3644,"rx_rate":709,"signal":-7}]}
		final JsonObject wifiResponse = callRestWS("http://mafreebox.freebox.fr/api/v8/wifi/ap/"+stationId+"/stations/", sessionId, JsonObject.class);
		if(wifiResponse.get("success").getAsBoolean() && wifiResponse.getAsJsonArray("result")!=null) {
			for (final JsonElement wifiElt : wifiResponse.getAsJsonArray("result")) {
				final JsonObject wifi = wifiElt.getAsJsonObject();
				final String hostname = wifi.get("hostname").getAsString();
				final String mac = wifi.get("mac").getAsString();
				final int connDuration = wifi.get("conn_duration").getAsInt(); //second
				final long rx_bytes = wifi.get("rx_bytes").getAsLong();
				final int rx_rate = wifi.get("rx_rate").getAsInt(); 
				final long tx_bytes = wifi.get("tx_bytes").getAsLong();
				final int tx_rate = wifi.get("tx_rate").getAsInt();
				final int inactive = wifi.get("inactive").getAsInt();
				
				final Measure statusMeasure = Measure.builder(measureName)
						.time(Instant.now())
						.addField("hostname", hostname)
						.addField("mac", mac)
						.addField("stationId", stationId)
						.addField("stationName", stationName)
						.addField("connDuration", connDuration)
						.addField("rx_bytes", rx_bytes)
						.addField("rx_rate", rx_rate)
						.addField("tx_bytes", tx_bytes)
						.addField("tx_rate", tx_rate)
						.addField("inactive", inactive)
						.tag("stationName", stationName)
						.tag("hostname", hostname)
						.tag("actif", inactive==0?"ACTIF":"INACTIF")
						.build();
		
				putInfosToInflux(statusMeasure);
			}
		}
	}

	private FreeboxInfo retrieveData(String sessionId) {
		//callAuthorizePost();
		final JsonObject result = callRestWS("http://mafreebox.freebox.fr/api/v8/system/", sessionId, JsonObject.class);
		//LOGGER.info("Url :" + "http://mafreebox.freebox.fr/api/v8/system/");
		//LOGGER.info("Result :" + result);
		final FreeboxInfo newFreeboxInfo = new FreeboxInfo();
		newFreeboxInfo.setSuccess(true);

		if (result.get("success").getAsBoolean() && result.getAsJsonObject("result")!=null) {
			final int upTimeSecond = result.getAsJsonObject("result").get("uptime_val").getAsInt();
			newFreeboxInfo.setUptimeSecond(upTimeSecond);
			newFreeboxInfo.setLastreboot(Instant.ofEpochMilli(System.currentTimeMillis() - upTimeSecond * 1000));
			for (final JsonElement sensor : result.getAsJsonObject("result").getAsJsonArray("sensors")) {
				final String sensorId = sensor.getAsJsonObject().get("id").getAsString();
				switch (sensorId) {
					case "temp_t1":
						newFreeboxInfo.setTempT1(sensor.getAsJsonObject().get("value").getAsInt());
						break;
					case "temp_t2":
						newFreeboxInfo.setTempT2(sensor.getAsJsonObject().get("value").getAsInt());
						break;
					case "temp_cpub":
						newFreeboxInfo.setTempCpuB(sensor.getAsJsonObject().get("value").getAsInt());
						break;
				}
			}
			for (final JsonElement fan : result.getAsJsonObject("result").getAsJsonArray("fans")) {
				final String fanId = fan.getAsJsonObject().get("id").getAsString();
				switch (fanId) {
					case "fan0_speed":
						newFreeboxInfo.setFan0Speed(fan.getAsJsonObject().get("value").getAsInt());
						break;
				}
			}
		} else {
			newFreeboxInfo.setSuccess(false);
		}
		final JsonObject resultBw = callRestWS("http://mafreebox.freebox.fr/api/v8/connection/full/", sessionId, JsonObject.class);
		//LOGGER.info("Url :" + "http://mafreebox.freebox.fr/api/v8/connection/full/");
		//LOGGER.info("ResultBw :" + resultBw);
		if (resultBw.get("success").getAsBoolean()) {
			newFreeboxInfo.setBandwidthDown(resultBw.getAsJsonObject("result").get("bandwidth_down").getAsLong());
			newFreeboxInfo.setBytesDown(resultBw.getAsJsonObject("result").get("bytes_down").getAsLong());
			newFreeboxInfo.setRateDown(resultBw.getAsJsonObject("result").get("rate_down").getAsInt());
			newFreeboxInfo.setBandwidthUp(resultBw.getAsJsonObject("result").get("bandwidth_up").getAsLong());
			newFreeboxInfo.setBytesUp(resultBw.getAsJsonObject("result").get("bytes_up").getAsLong());
			newFreeboxInfo.setRateUp(resultBw.getAsJsonObject("result").get("rate_up").getAsInt());
		} else {
			newFreeboxInfo.setSuccess(false);
		}
		return newFreeboxInfo;
	}

	private void putInfosToInflux(final FreeboxInfo freeboxInfo, final String measureName) {
		final ZoneId zoneParis = ZoneId.of("Europe/Paris");
		final int currentMinute = LocalDateTime.now(zoneParis).get(ChronoField.MINUTE_OF_DAY);
		final int lastRebootMinute = freeboxInfo.getLastreboot() == null ? currentMinute : LocalDateTime.ofInstant(freeboxInfo.getLastreboot(), zoneParis).get(ChronoField.MINUTE_OF_DAY);

		final Measure freeboxInfoMeasure = Measure.builder(measureName)
				.time(Instant.now())
				.addField("nowTime", currentMinute)
				.addField("uptimeVal", freeboxInfo.getUptimeSecond())
				.addField("lastReboot", lastRebootMinute)
				.addField("tempT1", freeboxInfo.getTempT1())
				.addField("tempT2", freeboxInfo.getTempT2())
				.addField("tempCpuB", freeboxInfo.getTempCpuB())
				.addField("fan0Speed", freeboxInfo.getFan0Speed())
				.addField("bandwidthDown", freeboxInfo.getBandwidthDown())
				.addField("bytesDown", freeboxInfo.getBytesDown())
				.addField("rateDown", freeboxInfo.getRateDown())
				.addField("bandwidthUp", freeboxInfo.getBandwidthUp())
				.addField("bytesUp", freeboxInfo.getBytesUp())
				.addField("rateUp", freeboxInfo.getRateUp())
				.addField("success", freeboxInfo.getSuccess())
				.tag("success", freeboxInfo.getSuccess() ? "OK" : "KO")
				.tag("reboot", freeboxInfo.getUptimeSecond() < 60 ? "REBOOT" : "RUN")
				.build();
		putInfosToInflux(freeboxInfoMeasure);
	}
	
	private void putInfosToInflux(final Measure measure) {
		final String dbName = paramManager.getParam("influxdb_dbname").getValueAsString();
		timeSeriesManager.insertMeasure(dbName, measure);
	}

	private <R> R callRestWS(final String wsUrl, final String sessionId, final Type returnType) {
		Assertion.check().isNotBlank(wsUrl);
		// ---
		final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieJar).build();
		final Request requestWs = new Request.Builder()
				.url(wsUrl)
				.header("Content-Type", "application/json; charset=utf-8")
				.header(FREEBOX_SESSION_HEADER, sessionId)
				.get()
				.build();
		return callToJson(client, requestWs, returnType);
	}

	private <R> R callToJson(final OkHttpClient client, final Request request, final Type returnType) {
		try (Response response = client.newCall(request).execute()) {
			final ByteArrayOutputStream result = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			try (InputStream inputStream = response.body().byteStream()) {
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

	private boolean closeSession(final String sessionId) {
		final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieJar).build();
		final RequestBody formBodyLogout = new FormBody.Builder().build();
		final Request requestLogout = new Request.Builder()
				.url("http://mafreebox.freebox.fr/api/v8/login/logout/")
				.header(FREEBOX_SESSION_HEADER, sessionId)
				.post(formBodyLogout)
				.build();
		final JsonObject logoutResponse = callToJson(client, requestLogout, JsonObject.class);
		return logoutResponse.get("success").getAsBoolean();
	}

	/*private String callAuthorizePost() {
		final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieJar).build();
		final String app_id = paramManager.getParam(FREEBOX_APP_ID).getValueAsString();
		String paramString = "{\"app_id\":\"" + app_id +
				"\",\"app_name\":\"" + app_id +
				"\",\"app_version\":\"0.0.4\"," +
				"\"device_name\":\""+ "PC_BEUG" + "\"}";

		final RequestBody formBodySession = RequestBody.create(MediaType.parse("application/json"), paramString);

		final Request requestSession = new Request.Builder()
				.url("http://mafreebox.freebox.fr/api/v3/login/authorize/")
				.post(formBodySession)
				.build();
		JsonObject sessionResponse = callToJson(client, requestSession, JsonObject.class);
		System.out.println(sessionResponse);
		return sessionResponse.getAsJsonObject("result").get("app_token").getAsString();
	}*/

	private String callLoginPost() {

		//removeAll
		cookieJar.loadForRequest(null).clear();

		final OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).cookieJar(cookieJar).build();

		final Request requestLogin = new Request.Builder()
				.url("http://mafreebox.freebox.fr/api/v8/login/")
				.get()
				.build();

		final JsonObject loginChallenge = callToJson(client, requestLogin, JsonObject.class);
		final String challenge = loginChallenge.getAsJsonObject("result").get("challenge").getAsString();
		final String password_salt = loginChallenge.getAsJsonObject("result").get("password_salt").getAsString();

		final String app_id = paramManager.getParam(FREEBOX_APP_ID).getValueAsString();
		final String app_token = paramManager.getParam(FREEBOX_APP_TOKEN).getValueAsString();
		String password;
		try {
			password = calculateRFC2104HMAC(challenge, app_token); //hmac-sha1(app_token, challenge)
		} catch (final InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
			throw WrappedException.wrap(e);
		}
		final RequestBody formBodySession = RequestBody.create(MediaType.parse("application/json"), "{\"app_id\":\"" + app_id + "\", \"password\":\"" + password + "\"}");

		final Request requestSession = new Request.Builder()
				.url("http://mafreebox.freebox.fr/api/v8/login/session/")
				.post(formBodySession)
				.build();
		final JsonObject sessionResponse = callToJson(client, requestSession, JsonObject.class);
		return sessionResponse.getAsJsonObject("result").get("session_token").getAsString();
	}

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	private static String toHexString(final byte[] bytes) {
		try (Formatter formatter = new Formatter()) {
			for (final byte b : bytes) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		}
	}

	private static String calculateRFC2104HMAC(final String data, final String key) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		final SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		final Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return toHexString(mac.doFinal(data.getBytes()));
	}

}
