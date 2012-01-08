/*
 * Copyright 2004-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.resolve.config

import org.apache.ivy.plugins.resolver.DependencyResolver
import org.apache.ivy.plugins.resolver.FileSystemResolver
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.resolver.RepositoryResolver
import org.apache.ivy.util.Message
import org.codehaus.griffon.resolve.SnapshotAwareM2Resolver

class RepositoriesConfigurer extends AbstractDependencyManagementConfigurer {
    RepositoriesConfigurer(DependencyConfigurationContext context) {
        super(context)
    }

    void inherit(boolean b) {
        dependencyManager.inheritRepositories = b
    }

    void inherits(boolean b) {
        dependencyManager.inheritRepositories = b
    }

    void flatDir(Map args) {
        String name = args.name?.toString()
        if (name && args.dirs) {
            def fileSystemResolver = new FileSystemResolver()
            fileSystemResolver.local = true
            fileSystemResolver.name = name

            def dirs = args.dirs instanceof Collection ? args.dirs : [args.dirs]

            dependencyManager.repositoryData << ['type': 'flatDir', name: name, dirs: dirs.join(',')]
            dirs.each { dir ->
                def path = new File(dir?.toString()).absolutePath
                fileSystemResolver.addIvyPattern("${path}/[module]-[revision](-[classifier]).xml")
                fileSystemResolver.addArtifactPattern "${path}/[module]-[revision](-[classifier]).[ext]"
            }
            fileSystemResolver.settings = dependencyManager.ivySettings

            addToChainResolver(fileSystemResolver)
        }
    }

    void griffonPlugins() {
        /*
        if (!context.offline) {
            if (isResolverNotAlreadyDefined('griffonPlugins')) {
                dependencyManager.repositoryData << [type: 'griffonPlugins', name: "griffonPlugins"]
                if (dependencyManager.buildSettings != null) {
                    def pluginResolver = new GriffonPluginsDirectoryResolver(dependencyManager.buildSettings, dependencyManager.ivySettings)
                    addToChainResolver(pluginResolver)
                }
            }
        }
        */
    }

    void griffonHome() {
        if (!isResolverNotAlreadyDefined('griffonHome')) {
            return
        }

        def griffonHome = dependencyManager.buildSettings?.griffonHome?.absolutePath ?: System.getenv("GRIFFON_HOME")
        if (!griffonHome) {
            return
        }

        griffonHome = new File(griffonHome).absolutePath
        /*
        def fileSystemResolver = new FileSystemResolver()
        fileSystemResolver.local = true
        fileSystemResolver.name = "griffonHome"
        fileSystemResolver.addIvyPattern("${griffonHome}/lib/ivy-[revision](-[classifier]).xml")
        fileSystemResolver.addIvyPattern("${griffonHome}/dist/ivy-[revision](-[classifier]).xml")
        fileSystemResolver.addArtifactPattern "${griffonHome}/lib/[module]-[revision](-[classifier]).[ext]"
        fileSystemResolver.addArtifactPattern "${griffonHome}/dist/[module]-[revision](-[classifier]).[ext]"
        fileSystemResolver.settings = dependencyManager.ivySettings
        addToChainResolver(fileSystemResolver)
        */

        flatDir(name: "griffonHome", dirs: ["${griffonHome}/dist", "${griffonHome}/libs"])
        /*
        final workDir = dependencyManager.buildSettings?.griffonWorkDir
        if (workDir) flatDir(name: "griffonHome", dirs: "${workDir}/cached-installed-plugins")
        if (griffonHome != '.') {
            def resolver = createLocalPluginResolver("griffonHome", griffonHome)
            addToChainResolver(resolver)
        }
        */
    }

    void mavenRepo(String url) {
        if (!context.offline && isResolverNotAlreadyDefined(url)) {
            dependencyManager.repositoryData << ['type': 'mavenRepo', root: url, name: url, m2compatbile: true]
            def resolver = new SnapshotAwareM2Resolver(name: url, root: url, m2compatible: true, settings: dependencyManager.ivySettings, changingPattern: ".*SNAPSHOT")
            addToChainResolver(resolver)
        }
    }

    void mavenRepo(Map args) {
        if (args && args.name) {
            if (!context.offline && isResolverNotAlreadyDefined(args.name)) {
                dependencyManager.repositoryData << (['type': 'mavenRepo'] + args)
                args.settings = dependencyManager.ivySettings
                def resolver = new SnapshotAwareM2Resolver(args)
                addToChainResolver(resolver)
            }
        }
        else {
            Message.warn("A mavenRepo specified doesn't have a name argument. Please specify one!")
        }
    }

    void resolver(DependencyResolver resolver) {
        if (resolver) {
            resolver.setSettings(dependencyManager.ivySettings)
            addToChainResolver(resolver)
        }
    }

    void ebr() {
        if (!context.offline && isResolverNotAlreadyDefined('ebr')) {
            dependencyManager.repositoryData << ['type': 'ebr']
            IBiblioResolver ebrReleaseResolver = new SnapshotAwareM2Resolver(name: "ebrRelease",
                    root: "http://repository.springsource.com/maven/bundles/release",
                    m2compatible: true,
                    settings: dependencyManager.ivySettings)
            addToChainResolver(ebrReleaseResolver)

            IBiblioResolver ebrExternalResolver = new SnapshotAwareM2Resolver(name: "ebrExternal",
                    root: "http://repository.springsource.com/maven/bundles/external",
                    m2compatible: true,
                    settings: dependencyManager.ivySettings)

            addToChainResolver(ebrExternalResolver)
        }
    }

    /**
     * Defines a repository that uses Griffon plugin repository format. Griffon repositories are
     * SVN repositories that follow a particular convention that is not Maven compatible.
     *
     * Ivy is flexible enough to allow the configuration of a resolver that resolves artifacts
     * against non-Maven repositories
     */
    void griffonRepo(String url, String name = null) {
        // if (!context.offline && isResolverNotAlreadyDefined(name ?: url)) {
        //     dependencyManager.repositoryData << ['type': 'griffonRepo', url: url]
        //     def urlResolver = new GriffonRepoResolver(name ?: url, new URL(url))
        //     urlResolver.addArtifactPattern("${url}/griffon-[module]/tags/RELEASE_*/griffon-[module]-[revision].[ext]")
        //     urlResolver.addIvyPattern("${url}/griffon-[module]/tags/RELEASE_*/[module]-[revision].pom")
        //     urlResolver.settings = dependencyManager.ivySettings
        //     urlResolver.latestStrategy = new LatestTimeStrategy()
        //     urlResolver.changingPattern = ".*SNAPSHOT"
        //     urlResolver.setCheckmodified(true)
        //     addToChainResolver(urlResolver)
        // }
    }

    void griffonCentral() {
        /*
        if (!context.offline && isResolverNotAlreadyDefined('griffonCentral')) {
            griffonRepo("http://plugins.griffon.org", "griffonCentral")
            mavenRepo("http://repo.griffon.org/griffon/plugins")
            mavenRepo("http://repo.griffon.org/griffon/core")
            griffonRepo("http://svn.codehaus.org/griffon/trunk/griffon-plugins", "griffonCore")
        }
        */
    }

    void mavenCentral() {
        if (!context.offline && isResolverNotAlreadyDefined('mavenCentral')) {
            dependencyManager.repositoryData << ['type': 'mavenCentral']
            IBiblioResolver mavenResolver = new SnapshotAwareM2Resolver(name: "mavenCentral")
            mavenResolver.m2compatible = true
            mavenResolver.settings = dependencyManager.ivySettings
            mavenResolver.changingPattern = ".*SNAPSHOT"
            addToChainResolver(mavenResolver)
        }
    }

    void mavenLocal(String repoPath) {
        if (isResolverNotAlreadyDefined('mavenLocal')) {
            dependencyManager.repositoryData << ['type': 'mavenLocal']
            FileSystemResolver localMavenResolver = new FileSystemResolver(name: 'localMavenResolver')
            localMavenResolver.local = true
            localMavenResolver.m2compatible = true
            localMavenResolver.changingPattern = ".*SNAPSHOT"

            String m2UserDir = "${System.getProperty('user.home')}/.m2"
            String repositoryPath = repoPath

            if (!repositoryPath) {
                repositoryPath = m2UserDir + "/repository"

                File mavenSettingsFile = new File("${m2UserDir}/settings.xml")
                if (mavenSettingsFile.exists()) {
                    def settingsXml = new XmlSlurper().parse(mavenSettingsFile)
                    String localRepository = settingsXml.localRepository.text()

                    if (localRepository.trim()) {
                        repositoryPath = localRepository
                    }
                }
            }

            localMavenResolver.addIvyPattern(
                    "${repositoryPath}/[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).pom")

            localMavenResolver.addArtifactPattern(
                    "${repositoryPath}/[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]")

            localMavenResolver.settings = dependencyManager.ivySettings
            addToChainResolver(localMavenResolver)
        }
    }

    /*
    private createLocalPluginResolver(String name, String location) {
        def pluginResolver = new FileSystemResolver(name: name)
        pluginResolver.addArtifactPattern("${location}/plugins/[artifact]-[revision].[ext]")
        pluginResolver.settings = dependencyManager.ivySettings
        pluginResolver.latestStrategy = new LatestTimeStrategy()
        pluginResolver.changingPattern = ".*SNAPSHOT"
        pluginResolver.setCheckmodified(true)
        return pluginResolver
    }
    */

    private addToChainResolver(DependencyResolver resolver) {
        if (context.pluginName && !dependencyManager.inheritRepositories) return

        if (dependencyManager.transferListener != null && (resolver instanceof RepositoryResolver)) {
            ((RepositoryResolver) resolver).repository.addTransferListener dependencyManager.transferListener
        }

        // Fix for GRAILS-5805
        synchronized (dependencyManager.chainResolver.resolvers) {
            dependencyManager.chainResolver.add resolver
        }
    }

    private boolean isResolverNotAlreadyDefined(String name) {
        def resolver
        // Fix for GRAILS-5805
        synchronized (dependencyManager.chainResolver.resolvers) {
            resolver = dependencyManager.chainResolver.resolvers.any { it.name == name }
        }
        if (resolver) {
            Message.debug("Dependency resolver $name already defined. Ignoring...")
            return false
        }
        return true
    }
}
