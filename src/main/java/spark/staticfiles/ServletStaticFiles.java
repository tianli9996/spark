/*
 * Copyright 2015 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.staticfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.resource.AbstractFileResolvingResource;
import spark.resource.AbstractResourceHandler;
import spark.resource.ClassPathResource;
import spark.resource.ClassPathResourceHandler;
import spark.resource.ExternalResource;
import spark.resource.ExternalResourceHandler;
import spark.utils.Assert;
import spark.utils.IOUtils;

/**
 * Holds the static file information when Spark is run from Servlet.
 */
public class ServletStaticFiles {
    private static final Logger LOG = LoggerFactory.getLogger(ServletStaticFiles.class);

    private static List<AbstractResourceHandler> staticResourceHandlers = null;

    private static boolean staticResourcesSet = false;
    private static boolean externalStaticResourcesSet = false;

    /**
     * @return true if consumed, false otherwise.
     */
    public static boolean consumeStaticResources(HttpServletRequest httpRequest,
                                                 ServletResponse servletResponse) throws IOException {
        if (staticResourceHandlers != null) {
            for (AbstractResourceHandler staticResourceHandler : staticResourceHandlers) {
                AbstractFileResolvingResource resource = staticResourceHandler.getResource(httpRequest);
                if (resource != null && resource.isReadable()) {
                    IOUtils.copy(resource.getInputStream(), servletResponse.getOutputStream());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public static void configureStaticResources(String folder) {
        Assert.notNull(folder, "'folder' must not be null");

        if (staticResourcesSet) {
            try {
                ClassPathResource resource = new ClassPathResource(folder);
                if (!resource.getFile().isDirectory()) {
                    LOG.error("Static resource location must be a folder");
                    return;
                }

                if (staticResourceHandlers == null) {
                    staticResourceHandlers = new ArrayList<>();
                }
                staticResourceHandlers.add(new ClassPathResourceHandler(folder, "index.html"));
                System.out.println("StaticResourceHandler configured with folder = " + folder);
                LOG.info("StaticResourceHandler configured with folder = " + folder);
            } catch (IOException e) {
                LOG.error("Error when creating StaticResourceHandler", e);
            }
            staticResourcesSet = true;
        }

    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public static void configureExternalStaticResources(String folder) {
        Assert.notNull(folder, "'folder' must not be null");

        if (externalStaticResourcesSet) {
            try {
                ExternalResource resource = new ExternalResource(folder);
                if (!resource.getFile().isDirectory()) {
                    LOG.error("External Static resource location must be a folder");
                    return;
                }

                if (staticResourceHandlers == null) {
                    staticResourceHandlers = new ArrayList<>();
                }
                staticResourceHandlers.add(new ExternalResourceHandler(folder, "index.html"));
                LOG.info("External StaticResourceHandler configured with folder = " + folder);
            } catch (IOException e) {
                LOG.error("Error when creating external StaticResourceHandler", e);
            }
            externalStaticResourcesSet = true;
        }

    }

}
