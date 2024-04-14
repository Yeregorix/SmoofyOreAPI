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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * A wrapper for Ore API.
 */
public class OreAPI {
	/**
	 * The default base URL.
	 */
	public static final URL DEFAULT_URL = getDefaultURL();

	/**
	 * The user agent used when connecting.
	 */
	public static final String USER_AGENT = "SmoofyOreAPI/1.0.3";

	/**
	 * The base URL for endpoints.
	 */
	public final URL urlBase;

	/**
	 * The session manager.
	 */
	public final SessionManager sessionManager;

	/**
	 * The Gson.
	 */
	public final Gson gson;

	/**
	 * Creates a wrapper using default URL and no API key.
	 */
	public OreAPI() {
		this(DEFAULT_URL, null);
	}

	/**
	 * Creates a wrapper.
	 *
	 * @param urlBase The base URL.
	 * @param apiKey The API key.
	 */
	public OreAPI(URL urlBase, String apiKey) {
		this(urlBase, apiKey, new Gson());
	}

	/**
	 * Creates a wrapper.
	 *
	 * @param urlBase The base URL.
	 * @param apiKey The API key.
	 * @param gson The Gson.
	 */
	public OreAPI(URL urlBase, String apiKey, Gson gson) {
		if (urlBase == null)
			throw new IllegalArgumentException("urlBase");
		if (gson == null)
			throw new IllegalArgumentException("gson");

		this.urlBase = urlBase;
		this.sessionManager = new SessionManager(getUncheckedURL("authenticate"), apiKey, gson);
		this.gson = gson;
	}

	/**
	 * Gets the URL for the given endpoint.
	 * {@link MalformedURLException} is wrapped in a {@link UncheckedIOException}.
	 *
	 * @param path The path of the endpoint.
	 * @return The URL.
	 */
	public URL getUncheckedURL(String path) {
		try {
			return getURL(path);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Gets the URL for the given endpoint.
	 *
	 * @param path The path of the endpoint.
	 * @return The URL.
	 * @throws MalformedURLException if the generated URL is invalid.
	 */
	public URL getURL(String path) throws MalformedURLException {
		return new URL(this.urlBase.getProtocol(), this.urlBase.getHost(), this.urlBase.getPort(), this.urlBase.getFile() + path);
	}

	/**
	 * Opens and configures an HTTP connection to the given endpoint.
	 *
	 * @param path The path of the endpoint.
	 * @return The HTTP connection.
	 * @throws IOException if an I/O error occurs.
	 */
	public HttpURLConnection openConnection(String path) throws IOException {
		HttpURLConnection co = (HttpURLConnection) getURL(path).openConnection();
		configureConnection(co);
		configureSession(co);
		return co;
	}

	/**
	 * Sets the session header of the connection.
	 *
	 * @param co The connection.
	 * @throws IOException if an I/O error occurs.
	 */
	public void configureSession(URLConnection co) throws IOException {
		co.setRequestProperty("Authorization", "OreApi session=\"" + this.sessionManager.getOrCreateSession() + "\"");
	}

	private static URL getDefaultURL() {
		try {
			return new URL("https://ore.spongepowered.org/api/v2/");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Configures the URL connection.
	 * Disables cache and sets user agent.
	 *
	 * @param co The URL connection.
	 */
	public static void configureConnection(URLConnection co) {
		co.setUseCaches(false);
		// co.setDefaultUseCaches(false);
		co.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
		co.setRequestProperty("Pragma", "no-cache");
		co.setRequestProperty("Expires", "0");
		co.setRequestProperty("User-Agent", USER_AGENT);
	}

	/**
	 * Parses an instant from a string in the {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME} format.
	 *
	 * @param value The string.
	 * @return The instant.
	 */
	public static Instant parseInstant(String value) {
		return Instant.from(parseOffsetDateTime(value));
	}

	/**
	 * Parses a temporal accessor from a string in the {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME} format.
	 *
	 * @param value The string.
	 * @return The temporal accessor.
	 */
	public static TemporalAccessor parseOffsetDateTime(String value) {
		return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value);
	}
}
