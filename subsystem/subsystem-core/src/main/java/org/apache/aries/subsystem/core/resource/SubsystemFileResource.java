package org.apache.aries.subsystem.core.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import org.apache.aries.subsystem.core.archive.SubsystemSymbolicNameHeader;
import org.apache.aries.subsystem.core.internal.OsgiIdentityCapability;
import org.apache.aries.util.filesystem.FileSystem;
import org.apache.aries.util.filesystem.IDirectory;
import org.apache.aries.util.manifest.ManifestProcessor;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.ResourceConstants;
import org.osgi.service.repository.RepositoryContent;
import org.osgi.service.subsystem.SubsystemConstants;

public class SubsystemFileResource implements Resource, RepositoryContent {
	private static final String REGEX = "([^@])(?:@(.*))?.ssa";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	private final List<Capability> capabilities;
	private final IDirectory directory;
	private final File file;
	
	public SubsystemFileResource(File content) throws IOException {
		file = content;
		directory = FileSystem.getFSRoot(content);
		Manifest manifest = ManifestProcessor.obtainManifestFromAppDir(directory, "OSGI-INF/DEPLOYMENT.MF");
		if (manifest == null)
			manifest = ManifestProcessor.obtainManifestFromAppDir(directory, "OSGI-INF/SUBSYSTEM.MF");
		String symbolicName = null;
		Version version = Version.emptyVersion;
		if (manifest != null) {
			String value = manifest.getMainAttributes().getValue(SubsystemConstants.SUBSYSTEM_SYMBOLICNAME);
			if (value != null)
				symbolicName = new SubsystemSymbolicNameHeader(value).getSymbolicName();
			value = manifest.getMainAttributes().getValue(SubsystemConstants.SUBSYSTEM_VERSION);
			if (value != null)
				version = Version.parseVersion(value);
		}
		Matcher matcher = PATTERN.matcher(content.getName());;
		if (symbolicName == null) {
			if (!matcher.matches())
				throw new IllegalArgumentException("No symbolic name");
			symbolicName = new SubsystemSymbolicNameHeader(matcher.group(1)).getSymbolicName();
		}
		if (version == Version.emptyVersion && matcher.matches()) {
			String group = matcher.group(2);
			if (group != null)
				version = Version.parseVersion(group);
		}
		List<Capability> capabilities = new ArrayList<Capability>(1);
		capabilities.add(new OsgiIdentityCapability(this, symbolicName, version, SubsystemConstants.IDENTITY_TYPE_SUBSYSTEM));
		this.capabilities = Collections.unmodifiableList(capabilities);
	}
	
	@Override
	public List<Capability> getCapabilities(String namespace) {
		if (namespace == null || ResourceConstants.IDENTITY_NAMESPACE.equals(namespace))
			return capabilities;
		return Collections.emptyList();
	}

	@Override
	public InputStream getContent() throws IOException {
		return new FileInputStream(file);
	}
	
	public String getLocation() {
		return file.getAbsolutePath();
	}

	@Override
	public List<Requirement> getRequirements(String namespace) {
		return Collections.emptyList();
	}
}