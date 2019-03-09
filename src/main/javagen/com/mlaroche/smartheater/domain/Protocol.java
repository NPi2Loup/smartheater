package com.mlaroche.smartheater.domain;

import io.vertigo.dynamo.domain.model.DtStaticMasterData;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Generated;

/**
 * This class is automatically generated.
 * DO NOT EDIT THIS FILE DIRECTLY.
 */
@Generated
public final class Protocol implements DtStaticMasterData {
	private static final long serialVersionUID = 1L;

	private String proCd;
	private String label;

	/** {@inheritDoc} */
	@Override
	public UID<Protocol> getUID() {
		return UID.of(this);
	}
	
	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Id'.
	 * @return String proCd <b>Obligatoire</b>
	 */
	@Field(domain = "DO_LABEL", type = "ID", required = true, label = "Id")
	public String getProCd() {
		return proCd;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Id'.
	 * @param proCd String <b>Obligatoire</b>
	 */
	public void setProCd(final String proCd) {
		this.proCd = proCd;
	}
	
	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Name'.
	 * @return String label
	 */
	@Field(domain = "DO_LABEL", label = "Name")
	public String getLabel() {
		return label;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Name'.
	 * @param label String
	 */
	public void setLabel(final String label) {
		this.label = label;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
