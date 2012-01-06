/*
 * Copyright (C) 209-2012 Felix Bechstein
 * 
 * This file is part of WebSMS.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.websms.connector.gmx;

import java.util.HashMap;
import java.util.Map;

/**
 * Character table.
 * 
 * @author Thomas Pilarski <Thomas.Pilarski@gmail.com>, flx
 */
final class CharacterTable {

	/** Mapping. */
	private static final Map<String, String> MAP = new HashMap<String, String>(
			512);

	static {
		// turkish
		MAP.put("\u0130", "I"); // İ
		MAP.put("\u0131", "i"); // ı
		MAP.put("\u015E", "S"); // Ş
		MAP.put("\u015F", "s"); // ş
		MAP.put("\u00C7", "C"); // Ç
		MAP.put("\u00E7", "c"); // ç
		MAP.put("\u011E", "G"); // Ğ
		MAP.put("\u011F", "g"); // ğ

		// polish
		MAP.put("\u0104", "A"); // Ą
		MAP.put("\u0105", "a"); // ą
		MAP.put("\u0106", "C"); // Ć
		MAP.put("\u0107", "c"); // ć
		MAP.put("\u0118", "E"); // Ę
		MAP.put("\u0119", "e"); // ę
		MAP.put("\u0141", "L"); // Ł
		MAP.put("\u0142", "l"); // ł
		MAP.put("\u0143", "N"); // Ń
		MAP.put("\u0144", "n"); // ń
		MAP.put("\u00D3", "O"); // Ó
		MAP.put("\u015A", "S"); // Ś
		MAP.put("\u015B", "s"); // ś
		MAP.put("\u0179", "Z"); // Ź
		MAP.put("\u017A", "z"); // ź
		MAP.put("\u017B", "Z"); // Ż
		MAP.put("\u017C", "z"); // ż
		MAP.put("\u00F3", "o"); // ó
	}

	/** Default constructor. */
	private CharacterTable() {

	}

	/**
	 * Encode {@link String}.
	 * 
	 * @param str
	 *            {@link String}
	 * @return encoded {@link String}
	 */
	public static String encodeString(final String str) {
		final int l = str.length();
		final StringBuffer strb = new StringBuffer(l);
		for (int i = 0; i < l; i++) {
			String s = str.substring(i, i + 1);
			String chr = MAP.get(s);
			if (chr == null) {
				strb.append(s);
			} else {
				strb.append(chr);
			}
		}
		return strb.toString();
	}
}