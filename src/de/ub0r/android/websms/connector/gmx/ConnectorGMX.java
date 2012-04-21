/*
 * Copyright (C) 2010-2012 Felix Bechstein
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

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import de.ub0r.android.websms.connector.common.BasicConnector;
import de.ub0r.android.websms.connector.common.CharacterTable;
import de.ub0r.android.websms.connector.common.CharacterTableSMSLengthCalculator;
import de.ub0r.android.websms.connector.common.ConnectorCommand;
import de.ub0r.android.websms.connector.common.ConnectorSpec;
import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
import de.ub0r.android.websms.connector.common.Log;
import de.ub0r.android.websms.connector.common.Utils;
import de.ub0r.android.websms.connector.common.WebSMSException;

/**
 * AsyncTask to manage IO to GMX API.
 * 
 * @author flx
 */
public final class ConnectorGMX extends BasicConnector {
	/** Tag for output. */
	private static final String TAG = "GMX";

	/** Base URL. */
	private static final String URL_BASE = "https://"
			+ "sms-submission-service.gmx.de"
			+ "/sms-submission-service/gmx/sms/2.0/";
	/** URL for getting balance. */
	private static final String URL_BALANCE = URL_BASE + "SmsCapabilities";
	/** URL for sending message. */
	private static final String URL_SEND = URL_BASE + "SmsSubmission";

	/** Google's ad unit id. */
	private static final String AD_UNITID = "a14dd4e1997ce70";

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

	/** GMX's {@link CharacterTable}. */
	private static final CharacterTable REPLACE = new CharacterTable(MAP);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectorSpec initSpec(final Context context) {
		final String name = context.getString(R.string.connector_gmx_name);
		ConnectorSpec c = new ConnectorSpec(name);
		c.setAuthor(context.getString(R.string.connector_gmx_author));
		c.setAdUnitId(AD_UNITID);
		c.setSMSLengthCalculator(// .
		new CharacterTableSMSLengthCalculator(REPLACE));
		c.setBalance(null);
		c.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
				| ConnectorSpec.CAPABILITIES_SEND
				| ConnectorSpec.CAPABILITIES_PREFS);
		c.addSubConnector(TAG, c.getName(), SubConnectorSpec.FEATURE_SENDLATER);
		// SubConnectorSpec.FEATURE_MULTIRECIPIENTS);
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectorSpec updateSpec(final Context context,
			final ConnectorSpec connectorSpec) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (p.getBoolean(Preferences.PREFS_ENABLED, false)) {
			if (p.getString(Preferences.PREFS_MAIL, "").length() > 0
					&& p.getString(Preferences.PREFS_PASSWORD, "") // .
							.length() > 0) {
				connectorSpec.setReady();
			} else {
				connectorSpec.setStatus(ConnectorSpec.STATUS_ENABLED);
			}
		} else {
			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
		}
		return connectorSpec;
	}

	@Override
	protected boolean usePost(final ConnectorCommand command) {
		switch (command.getType()) {
		case ConnectorCommand.TYPE_SEND:
			return true;
		case ConnectorCommand.TYPE_UPDATE:
			return false;
		default:
			throw new IllegalArgumentException(
					"Invalid ConnectorCommand for usePost()");
		}
	}

	@Override
	protected HttpEntity addHttpEntity(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		switch (command.getType()) {
		case ConnectorCommand.TYPE_SEND:
			try {
				return new StringEntity(command.getText(), this.getEncoding());
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException("Unsupported encoding "
						+ this.getEncoding() + " in addHttpEntity()");
			}
		default:
			throw new IllegalArgumentException(
					"Invalid ConnectorCommand for addHttpEntity()");
		}
	}

	@Override
	protected boolean useBasicAuth() {
		return true;
	}

	@Override
	protected String getUserAgent() {
		return null;
	}

	@Override
	protected String getUrlSend(final ArrayList<BasicNameValuePair> d) {
		return URL_SEND;
	}

	@Override
	protected String getUrlBalance(final ArrayList<BasicNameValuePair> d) {
		return URL_BALANCE;
	}

	@Override
	protected String getParamUsername() {
		return null;
	}

	@Override
	protected String getParamPassword() {
		return null;
	}

	@Override
	protected String getParamRecipients() {
		return "destinationNumber";
	}

	@Override
	protected String getParamText() {
		return null;
	}

	@Override
	protected String getParamSender() {
		return "sourceNumber";
	}

	@Override
	protected String getUsername(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("gmx_mail", null);
	}

	@Override
	protected String getPassword(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("gmx_password", null);
	}

	@Override
	protected String getSender(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		return Utils.international2oldformat(Utils.getSender(context,
				command.getDefSender()));
	}

	@Override
	protected String getRecipients(final ConnectorCommand command) {
		return Utils.joinRecipientsNumbers(
				Utils.national2international(command.getDefPrefix(),
						command.getRecipients()), ",", true);
	}

	@Override
	protected String getEncoding() {
		return "UTF-8";
	}

	@Override
	protected void addExtraArgs(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs,
			final ArrayList<BasicNameValuePair> d) {
		if (command.getType() == ConnectorCommand.TYPE_SEND) {
			addParam(d, "clientType", "GMX_ANDROID");
			addParam(d, "messageType", "SMS");
			if (command.getSendLater() > System.currentTimeMillis()) {
				addParam(d, "sendDate", String.valueOf(command.getSendLater()));
			}
			addParam(d, "options", "SEND_ERROR_NOTIFY_MAIL");
		}
	}

	@Override
	protected void parseResponseCode(final Context context,
			final HttpResponse response) {
		final int resp = response.getStatusLine().getStatusCode();
		switch (resp) {
		case HttpURLConnection.HTTP_OK:
			return;
		case HttpURLConnection.HTTP_ACCEPTED:
			return;
		case HttpURLConnection.HTTP_FORBIDDEN:
			throw new WebSMSException(context, R.string.error_pw);
		default:
			super.parseResponseCode(context, response);
			break;
		}
	}

	@Override
	protected void parseResponse(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs,
			final String htmlText) {
		if (command.getType() == ConnectorCommand.TYPE_SEND) {
			// nothing to do..
			Log.d(TAG, htmlText);
		} else {
			// must be update
			HashMap<String, String> h = new HashMap<String, String>();
			for (String s : htmlText.split("&")) {
				String[] ss = s.split("=");
				if (ss.length > 1) {
					h.put(ss[0], ss[1]);
				}
			}
			String s = h.get("AVAILABLE_FREE_SMS");
			if (TextUtils.isEmpty(s)) {
				s = "?";
			}
			String s1 = h.get("MAX_MONTH_FREE_SMS");
			if (!TextUtils.isEmpty(s1)) {
				s += "/" + s1;
			}
			cs.setBalance(s);
		}
	}

}
