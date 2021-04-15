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

import com.google.gson.JsonObject;
import net.smoofyuniverse.ore.OreAPI;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

public class OreVersion {
	public final OreProject project;
	public final String name, apiVersion;
	public final Instant createdAt;

	public OreVersion(OreProject project, String name, Instant createdAt, String apiVersion) {
		if (project == null)
			throw new IllegalArgumentException("project");
		if (name == null)
			throw new IllegalArgumentException("name");
		if (createdAt == null)
			throw new IllegalArgumentException("createdAt");

		this.project = project;
		this.name = name;
		this.createdAt = createdAt;
		this.apiVersion = apiVersion;
	}

	public Optional<String> getPage() {
		return this.project.getPage().map(p -> p + "/versions/" + this.name);
	}

	static OreVersion from(OreProject project, JsonObject obj) {
		String apiVersion = null;
		for (Object e : obj.getAsJsonArray("dependencies")) {
			JsonObject d = (JsonObject) e;
			if (d.get("plugin_id").getAsString().equals("spongeapi")) {
				apiVersion = d.get("version").getAsString();
				break;
			}
		}

		return new OreVersion(project, obj.get("name").getAsString(), OreAPI.parseInstant(obj.get("created_at").getAsString()), apiVersion);
	}

	public static Optional<OreVersion> getLatest(OreVersion[] versions, Predicate<OreVersion> predicate) {
		OreVersion latestVersion = null;
		for (OreVersion version : versions) {
			if (latestVersion == null || version.createdAt.isAfter(latestVersion.createdAt)) {
				try {
					if (predicate.test(version))
						latestVersion = version;
				} catch (Exception ignored) {
				}
			}
		}
		return Optional.ofNullable(latestVersion);
	}
}
