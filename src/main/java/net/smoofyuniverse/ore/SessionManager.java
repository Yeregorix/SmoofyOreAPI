/*
 * Copyright (c) 2019-2022 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.ore;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

/**
 * A manager for the session.
 */
public class SessionManager {
	private final URL authenticationUrl;
	private final String apiKey;
	private final Gson gson;

	private Instant expiration;
	private String session;

	/**
	 * Creates a session manager.
	 *
	 * @param authenticationUrl The authentication endpoint.
	 * @param apiKey The API key.
	 * @param gson The Gson.
	 */
	public SessionManager(URL authenticationUrl, String apiKey, Gson gson) {
		if (authenticationUrl == null)
			throw new IllegalArgumentException("authenticationUrl");
		if (gson == null)
			throw new IllegalArgumentException("gson");

		this.authenticationUrl = authenticationUrl;
		this.apiKey = apiKey;
		this.gson = gson;
	}

	/**
	 * Gets the expiration instant of the current session.
	 *
	 * @return The expiration instant.
	 */
	public Instant getExpiration() {
		if (this.expiration == null)
			throw new IllegalStateException("No session");
		return this.expiration;
	}

	/**
	 * Gets the current session token.
	 *
	 * @return The session token.
	 */
	public Optional<String> getSession() {
		return Optional.ofNullable(this.session);
	}

	/**
	 * Gets the session token.
	 * If uninitialized or outdated, a new token is created.
	 *
	 * @return The session token.
	 * @throws IOException if an I/O error occurs.
	 */
	public String getOrCreateSession() throws IOException {
		if (this.session == null || this.expiration.toEpochMilli() - System.currentTimeMillis() < 30_000)
			authenticate();
		return this.session;
	}

	/**
	 * Authenticates and stores a new session token.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void authenticate() throws IOException {
		HttpURLConnection co = null;
		try {
			co = (HttpURLConnection) this.authenticationUrl.openConnection();
			co.setRequestMethod("POST");
			OreAPI.configureConnection(co);
			if (this.apiKey != null)
				co.setRequestProperty("Authorization", "OreApi apikey=\"" + this.apiKey + "\"");
			co.connect();

			try (InputStream in = co.getInputStream()) {
				JsonObject obj = this.gson.fromJson(new InputStreamReader(in), JsonObject.class);
				this.expiration = OreAPI.parseInstant(obj.get("expires").getAsString());
				this.session = obj.get("session").getAsString();
			}
		} finally {
			if (co != null)
				co.disconnect();
		}
	}
}
