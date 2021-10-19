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

package net.smoofyuniverse.ore.project;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.smoofyuniverse.ore.OreAPI;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Optional;

/**
 * A project on Ore.
 */
public class OreProject {
	/**
	 * The plugin ID.
	 */
	public final String pluginId;

	/**
	 * The owner.
	 */
	public String owner;

	/**
	 * The name.
	 */
	public String name;

	/**
	 * Creates a project object.
	 *
	 * @param pluginId The plugin ID.
	 */
	public OreProject(String pluginId) {
		if (pluginId == null || pluginId.isEmpty())
			throw new IllegalArgumentException("pluginId");
		this.pluginId = pluginId;
	}

	/**
	 * Gets the URL of the project page.
	 *
	 * @return The URL as a string.
	 */
	public Optional<String> getPage() {
		if (this.owner == null || this.name == null)
			return Optional.empty();
		return Optional.of("https://ore.spongepowered.org/" + this.owner + "/" + this.name);
	}

	/**
	 * Gets the latest versions available.
	 *
	 * @param api The API.
	 * @return The versions.
	 * @throws IOException if an I/O error occurs.
	 */
	public OreVersion[] getVersions(OreAPI api) throws IOException {
		return getVersions(api, 0, 10);
	}

	/**
	 * Gets the versions available.
	 *
	 * @param api The API.
	 * @param offset The index of the first version to get. Sorted by creation date.
	 * @param limit The maximum number of versions to get.
	 * @return The versions.
	 * @throws IOException if an I/O error occurs.
	 */
	public OreVersion[] getVersions(OreAPI api, int offset, int limit) throws IOException {
		if (offset < 0)
			throw new IllegalArgumentException("offset");
		if (limit <= 0)
			throw new IllegalArgumentException("limit");

		HttpURLConnection co = null;
		try {
			co = api.openConnection("projects/" + this.pluginId + "/versions?offset=" + offset + "&limit=" + limit);
			co.connect();

			if (co.getResponseCode() == 404)
				return new OreVersion[0];

			JsonArray result = api.gson.fromJson(new InputStreamReader(co.getInputStream()), JsonObject.class).getAsJsonArray("result");
			OreVersion[] versions = new OreVersion[result.size()];
			for (int i = 0; i < versions.length; i++)
				versions[i] = OreVersion.from(this, (JsonObject) result.get(i));
			return versions;
		} finally {
			if (co != null)
				co.disconnect();
		}
	}
}
