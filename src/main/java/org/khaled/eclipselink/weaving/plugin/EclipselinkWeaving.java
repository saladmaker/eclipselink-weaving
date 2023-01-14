package org.khaled.eclipselink.weaving.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.persistence.tools.weaving.jpa.StaticWeaveProcessor;

/**
 *
 * @author khaled
 */
@Mojo(requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.PROCESS_CLASSES, name = "weave",
        requiresProject = true)
public class EclipselinkWeaving extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File source;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File target;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File persistenceInfoLocation;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "false", property = "eclipselink.weave.skip")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            getLog().info("weaving skipped");
        } else {
            final ClassLoader classLoader
                    = new URLClassLoader(getClassPath(),
                            Thread.currentThread().getContextClassLoader());
            try {
                processWeaving(classLoader);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            getLog().info("Eclipselink weaving completed");
        }
    }

    private void processWeaving(ClassLoader classLoader) throws MojoExecutionException, MojoFailureException {
        if (!source.exists()) {
            throw new MojoExecutionException("Source directory " + source + " does not exist");
        }
        try {
            getLog().info("Source classes dir: " + source);
            getLog().info("Target classes dir: " + target);
            final StaticWeaveProcessor weaveProcessor = new StaticWeaveProcessor(source, target);
            weaveProcessor.setPersistenceInfo(persistenceInfoLocation);
            weaveProcessor.setClassLoader(classLoader);
            weaveProcessor.setLog(new PrintWriter(System.out));
            weaveProcessor.performWeaving();
        } catch (URISyntaxException | IOException e) {
            throw new MojoExecutionException("Error", e);
        }
    }


    private URL[] getClassPath() {
        final List<URL> urls = new ArrayList<>();
        try {
            for (File file : EclipselinkWeaving.getClassPathFiles(project)) {
                urls.add(file.toURI().toURL());
            }
            return urls.toArray(new URL[0]);
        } catch (MalformedURLException exc) {
            throw new RuntimeException(exc.getMessage(), exc);
        }
    }
    public static File[] getClassPathFiles(MavenProject project) {
        final List<File> files = new ArrayList<>();
        List<?> classpathElements;
        try {
            classpathElements = project.getTestClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        for (final Object o : classpathElements) {
            if (o != null) {
                final File file = new File(o.toString());
                if (file.canRead()) {
                    files.add(file);
                }
            }
        }
        return files.toArray(new File[0]);
    }
}
