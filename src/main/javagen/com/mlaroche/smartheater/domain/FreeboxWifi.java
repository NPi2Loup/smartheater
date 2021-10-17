package com.mlaroche.smartheater.domain;

import io.vertigo.core.lang.Generated;
import io.vertigo.datamodel.structure.model.DtObject;
import io.vertigo.datamodel.structure.stereotype.Field;
import io.vertigo.datamodel.structure.util.DtObjectUtil;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class FreeboxWifi implements DtObject {
	private static final long serialVersionUID = 1L;

	private Boolean success;
	private Integer uptimeSecond;
	private java.time.Instant lastreboot;
	private Integer tempT1;
	private Integer tempT2;
	private Integer tempCpuB;
	private Integer fan0Speed;
	private Integer rateDown;
	private Integer rateUp;
	private Long bytesDown;
	private Long bytesUp;
	private Long bandwidthDown;
	private Long bandwidthUp;
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'success'.
	 * @return Boolean success
	 */
	@Field(smartType = "STyBoolean", label = "success")
	public Boolean getSuccess() {
		return success;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'success'.
	 * @param success Boolean
	 */
	public void setSuccess(final Boolean success) {
		this.success = success;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'UpTime en seconde'.
	 * @return Integer uptimeSecond
	 */
	@Field(smartType = "STyNumber", label = "UpTime en seconde")
	public Integer getUptimeSecond() {
		return uptimeSecond;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'UpTime en seconde'.
	 * @param uptimeSecond Integer
	 */
	public void setUptimeSecond(final Integer uptimeSecond) {
		this.uptimeSecond = uptimeSecond;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Dernier reboot'.
	 * @return Instant lastreboot
	 */
	@Field(smartType = "STyTimestamp", label = "Dernier reboot")
	public java.time.Instant getLastreboot() {
		return lastreboot;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Dernier reboot'.
	 * @param lastreboot Instant
	 */
	public void setLastreboot(final java.time.Instant lastreboot) {
		this.lastreboot = lastreboot;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Température 1'.
	 * @return Integer tempT1
	 */
	@Field(smartType = "STyNumber", label = "Température 1")
	public Integer getTempT1() {
		return tempT1;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Température 1'.
	 * @param tempT1 Integer
	 */
	public void setTempT1(final Integer tempT1) {
		this.tempT1 = tempT1;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Température 2'.
	 * @return Integer tempT2
	 */
	@Field(smartType = "STyNumber", label = "Température 2")
	public Integer getTempT2() {
		return tempT2;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Température 2'.
	 * @param tempT2 Integer
	 */
	public void setTempT2(final Integer tempT2) {
		this.tempT2 = tempT2;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Température CPU B'.
	 * @return Integer tempCpuB
	 */
	@Field(smartType = "STyNumber", label = "Température CPU B")
	public Integer getTempCpuB() {
		return tempCpuB;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Température CPU B'.
	 * @param tempCpuB Integer
	 */
	public void setTempCpuB(final Integer tempCpuB) {
		this.tempCpuB = tempCpuB;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Ventilateur 1'.
	 * @return Integer fan0Speed
	 */
	@Field(smartType = "STyNumber", label = "Ventilateur 1")
	public Integer getFan0Speed() {
		return fan0Speed;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Ventilateur 1'.
	 * @param fan0Speed Integer
	 */
	public void setFan0Speed(final Integer fan0Speed) {
		this.fan0Speed = fan0Speed;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Débit déscendant'.
	 * @return Integer rateDown
	 */
	@Field(smartType = "STyNumber", label = "Débit déscendant")
	public Integer getRateDown() {
		return rateDown;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Débit déscendant'.
	 * @param rateDown Integer
	 */
	public void setRateDown(final Integer rateDown) {
		this.rateDown = rateDown;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Débit montant'.
	 * @return Integer rateUp
	 */
	@Field(smartType = "STyNumber", label = "Débit montant")
	public Integer getRateUp() {
		return rateUp;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Débit montant'.
	 * @param rateUp Integer
	 */
	public void setRateUp(final Integer rateUp) {
		this.rateUp = rateUp;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Total déscendant'.
	 * @return Long bytesDown
	 */
	@Field(smartType = "STyBytes", label = "Total déscendant")
	public Long getBytesDown() {
		return bytesDown;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Total déscendant'.
	 * @param bytesDown Long
	 */
	public void setBytesDown(final Long bytesDown) {
		this.bytesDown = bytesDown;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Total montant'.
	 * @return Long bytesUp
	 */
	@Field(smartType = "STyBytes", label = "Total montant")
	public Long getBytesUp() {
		return bytesUp;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Total montant'.
	 * @param bytesUp Long
	 */
	public void setBytesUp(final Long bytesUp) {
		this.bytesUp = bytesUp;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Max descendant (Kb/s)'.
	 * @return Long bandwidthDown
	 */
	@Field(smartType = "STyBytes", label = "Max descendant (Kb/s)")
	public Long getBandwidthDown() {
		return bandwidthDown;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Max descendant (Kb/s)'.
	 * @param bandwidthDown Long
	 */
	public void setBandwidthDown(final Long bandwidthDown) {
		this.bandwidthDown = bandwidthDown;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Max montant (Kb/s)'.
	 * @return Long bandwidthUp
	 */
	@Field(smartType = "STyBytes", label = "Max montant (Kb/s)")
	public Long getBandwidthUp() {
		return bandwidthUp;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Max montant (Kb/s)'.
	 * @param bandwidthUp Long
	 */
	public void setBandwidthUp(final Long bandwidthUp) {
		this.bandwidthUp = bandwidthUp;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
