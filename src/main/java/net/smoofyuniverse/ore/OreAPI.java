/*
 * Copyright (c) 2019-2021 Hugo Dupanloup (Yeregorix)
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class OreAPI {
	public static final URL DEFAULT_URL = getDefaultURL();
	public static final String USER_AGENT = "SmoofyOreAPI/1.0.1";

	public final URL urlBase;
	public final SessionController controller;
	public final Gson gson;

	public OreAPI() {
		this(DEFAULT_URL, null);
	}

	public OreAPI(URL urlBase, String apiKey) {
		this(urlBase, apiKey, new Gson());
	}

	public OreAPI(URL urlBase, String apiKey, Gson gson) {
		if (urlBase == null)
			throw new IllegalArgumentException("urlBase");
		if (gson == null)
			throw new IllegalArgumentException("gson");

		this.urlBase = urlBase;
		this.controller = new SessionController(getUncheckedURL("authenticate"), apiKey, gson);
		this.gson = gson;
	}

	public URL getUncheckedURL(String path) {
		try {
			return getURL(path);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	public URL getURL(String path) throws MalformedURLException {
		return new URL(this.urlBase.getProtocol(), this.urlBase.getHost(), this.urlBase.getPort(), this.urlBase.getFile() + path);
	}

	public HttpURLConnection openConnection(String path) throws IOException {
		HttpURLConnection co = (HttpURLConnection) getURL(path).openConnection();
		configureConnection(co);
		configureSession(co);
		return co;
	}

	public void configureSession(URLConnection co) throws IOException {
		co.setRequestProperty("Authorization", "OreApi session=\"" + this.controller.getOrCreateSession() + "\"");
	}

	private static URL getDefaultURL() {
		try {
			return new URL("https://ore.spongepowered.org/api/v2/");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static void configureConnection(URLConnection co) {
		co.setUseCaches(false);
		// co.setDefaultUseCaches(false);
		co.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		co.setRequestProperty("Pragma", "no-cache");
		co.setRequestProperty("Expires", "0");
		co.setRequestProperty("User-Agent", USER_AGENT);
	}

	public static Instant parseInstant(String value) {
		return Instant.from(parseOffsetDateTime(value));
	}

	public static TemporalAccessor parseOffsetDateTime(String value) {
		return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value);
	}
}
