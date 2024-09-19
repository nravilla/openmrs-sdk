package org.openmrs.maven.plugins.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.openmrs.maven.plugins.model.Artifact;
import org.openmrs.maven.plugins.model.DistroProperties;

/**
 * This class is downloads and moves content backend config to respective configuration folders
 */
public class ContentHelper {
	
	public static final String TEMP_CONTENT_FOLDER = "temp-content";
	public static final String FRONTEND_CONFIG_FOLDER = File.separator + "configs" + File.separator + "frontend_config";
	public static final String BACKEND_CONFIG_FOLDER = "configs" + File.separator + "backend_config";
	
	public static void downloadContent(File serverDirectory, Artifact contentArtifact, ModuleInstaller moduleInstaller, File targetDir) throws MojoExecutionException {
		
		String artifactId = contentArtifact.getArtifactId();
		//create a artifact folder under temp_content
		File sourceDir = new File(serverDirectory, TEMP_CONTENT_FOLDER + File.separator + artifactId);
		sourceDir.mkdirs();
		
		moduleInstaller.installUnpackModule(contentArtifact, sourceDir.getAbsolutePath());		
		moveBackendConfig(artifactId, sourceDir, targetDir);
	}
	
	private static void moveBackendConfig(String artifactId, File sourceDir, File targetDir) throws MojoExecutionException {
		try {				
			File backendConfigFiles = new File(sourceDir, BACKEND_CONFIG_FOLDER);
			
			for (File config : backendConfigFiles.listFiles()) {
				// Find a corresponding directory in the configurationDir with the same name as backendConfig
				File matchingConfigurationDir = new File(targetDir, config.getName());
				File destDir = null;
				if (!matchingConfigurationDir.exists() || !matchingConfigurationDir.isDirectory()) {
					// Folder doesn't exist so create it 
					matchingConfigurationDir.mkdirs();
					destDir = matchingConfigurationDir;
				}
				// Create a new artifact folder under the matching configuration folder
				destDir = new File(matchingConfigurationDir, artifactId);
				
				//copy config files to the matching configuration folder
				FileUtils.copyDirectory(config, destDir);
			}
		}
		catch (IOException e) {
			throw new MojoExecutionException("Error copying backend configuration: " + e.getMessage(), e);
		}
	}	
	
	public static List<File> getContentFrontendConfigFiles(DistroProperties distroProperties, File serverDirectory) {
		List<File> contentConfigFiles = new ArrayList<File>();
		
		List<Artifact> artifacts = distroProperties.getContentArtifacts();
		if (artifacts != null) {
			for (Artifact artifact : artifacts) {
				String artifactId = artifact.getArtifactId();
				File artifactFrontendDir = new File(serverDirectory,
					TEMP_CONTENT_FOLDER + File.separator + artifactId + FRONTEND_CONFIG_FOLDER);
				
				// Add all files from the artifact directory
				if (artifactFrontendDir.exists() && artifactFrontendDir.isDirectory()) {
					//just include json files and set recursive to true
					Collection<File> files = FileUtils.listFiles(artifactFrontendDir, null, true);
					contentConfigFiles.addAll(files);
				}
			}
		}		
		return contentConfigFiles;
	}
	
	public static void deleteTempContentFolder(File serverDirectory) throws MojoExecutionException {
		try {
			File tempContentDir = new File(serverDirectory, TEMP_CONTENT_FOLDER);
			
			if (tempContentDir.exists()) {
				FileUtils.deleteDirectory(tempContentDir);
			}
		}
		catch (IOException e) {
			throw new MojoExecutionException("Error deleting temp content directory: " + e.getMessage(), e);
		}
	}
	
}
