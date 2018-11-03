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
package com.mlaroche.smartheater.domain;

import java.util.Arrays;
import java.util.Iterator;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.lang.Generated;

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
		/** Objet de données Heater. */
		Heater(com.mlaroche.smartheater.domain.Heater.class),
		/** Objet de données HeatersByMode. */
		HeatersByMode(com.mlaroche.smartheater.domain.HeatersByMode.class),
		/** Objet de données HeaterMode. */
		HeaterMode(com.mlaroche.smartheater.domain.HeaterMode.class),
		/** Objet de données Protocol. */
		Protocol(com.mlaroche.smartheater.domain.Protocol.class),
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
	 * Enumération des champs de Heater.
	 */
	public enum HeaterFields implements DtFieldName<com.mlaroche.smartheater.domain.Heater> {
		/** Propriété 'Id'. */
		HEA_ID,
		/** Propriété 'Name'. */
		NAME,
		/** Propriété 'DNS Name'. */
		DNS_NAME,
		/** Propriété 'Active'. */
		ACTIVE,
		/** Propriété 'Mode Auto'. */
		AUTO,
		/** Propriété 'Retour au mode auto'. */
		AUTO_SWITCH,
		/** Propriété 'WeeklyCalendar'. */
		WCA_ID,
		/** Propriété 'Protocol'. */
		PRO_CD,
		/** Propriété 'Mode'. */
		MOD_CD	}

	/**
	 * Enumération des champs de HeatersByMode.
	 */
	public enum HeatersByModeFields implements DtFieldName<com.mlaroche.smartheater.domain.HeatersByMode> {
		/** Propriété 'Id'. */
		MODE,
		/** Propriété 'Nombre'. */
		COUNT	}

	/**
	 * Enumération des champs de HeaterMode.
	 */
	public enum HeaterModeFields implements DtFieldName<com.mlaroche.smartheater.domain.HeaterMode> {
		/** Propriété 'Id'. */
		MOD_CD,
		/** Propriété 'Name'. */
		LABEL	}

	/**
	 * Enumération des champs de Protocol.
	 */
	public enum ProtocolFields implements DtFieldName<com.mlaroche.smartheater.domain.Protocol> {
		/** Propriété 'Id'. */
		PRO_CD,
		/** Propriété 'Name'. */
		LABEL	}

	/**
	 * Enumération des champs de WeeklyCalendar.
	 */
	public enum WeeklyCalendarFields implements DtFieldName<com.mlaroche.smartheater.domain.WeeklyCalendar> {
		/** Propriété 'Id'. */
		WCA_ID,
		/** Propriété 'Name'. */
		NAME,
		/** Propriété 'Value as json'. */
		JSON_VALUE	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Class<?>> iterator() {
		return new Iterator<Class<?>>() {
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
