/*
 * Copyright (c) 2019 Hugo Dupanloup (Yeregorix)
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

public class SessionController {
	private final URL authenticationUrl;
	private final String userAgent;
	private final String apiKey;
	private final Gson gson;

	private Instant expiration;
	private String session;

	public SessionController(URL authenticationUrl, String userAgent, String apiKey, Gson gson) {
		if (authenticationUrl == null)
			throw new IllegalArgumentException("authenticationUrl");
		if (userAgent == null)
			if (gson == null)
				throw new IllegalArgumentException("gson");

		this.authenticationUrl = authenticationUrl;
		this.userAgent = userAgent;
		this.apiKey = apiKey;
		this.gson = gson;
	}

	public Instant getExpiration() {
		if (this.expiration == null)
			throw new IllegalStateException("No session");
		return this.expiration;
	}

	public Optional<String> getSession() {
		return Optional.ofNullable(this.session);
	}

	public String getOrCreateSession() throws IOException {
		if (this.session == null || this.expiration.toEpochMilli() - System.currentTimeMillis() < 30_000)
			authenticate();
		return this.session;
	}

	public void authenticate() throws IOException {
		HttpURLConnection co = null;
		try {
			co = (HttpURLConnection) this.authenticationUrl.openConnection();
			co.setRequestMethod("POST");

			co.setUseCaches(false);
			co.setDefaultUseCaches(false);
			co.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
			co.setRequestProperty("Pragma", "no-cache");
			co.setRequestProperty("Expires", "0");

			if (this.userAgent != null)
				co.setRequestProperty("User-Agent", this.userAgent);

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
