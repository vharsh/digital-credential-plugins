package io.mosip.certify.vcdatamodel.templating.impl;

import io.mosip.certify.api.exception.VCIExchangeException;
import io.mosip.certify.vcdatamodel.templating.VCDataModelFormatter;
import jakarta.annotation.PostConstruct;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.URLResourceLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * VC Data Model 2.0 implementation;
 * Draft spec: https://www.w3.org/TR/vc-data-model-2.0/
 *
 */
public class VCDataModel2 implements VCDataModelFormatter {
    VelocityEngine engine;

    @PostConstruct
    public void initialize() throws VCIExchangeException {
        engine = new VelocityEngine();
        // TODO: Load a VM file from classpath & Spring config server conditionally.
        URLResourceLoader urlResourceLoader = new URLResourceLoader() {
            @Override
            public InputStream getResourceStream(String name) throws ResourceNotFoundException {
                try {
                    URL url = new URL(name);
                    URLConnection connection = url.openConnection();
                    return connection.getInputStream();
                } catch (IOException e) {
                    throw new ResourceNotFoundException("Unable to find resource '" + name + "'");
                }
            }
        };
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "url");
        engine.setProperty("url.resource.loader.instance", urlResourceLoader);
        engine.init();
        // Validate all the supported VC
    }

    @Override
    public JSONObject format(Map<String, Object> templateInput) {
        return null;
    }
}
