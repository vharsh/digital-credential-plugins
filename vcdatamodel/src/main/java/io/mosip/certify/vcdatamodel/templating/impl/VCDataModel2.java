package io.mosip.certify.vcdatamodel.templating.impl;

import io.mosip.certify.vcdatamodel.templating.VCDataModelFormatter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.URLResourceLoader;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

/**
 * VC Data Model 2.0 implementation;
 * Draft spec: https://www.w3.org/TR/vc-data-model-2.0/
 *
 */
public class VCDataModel2 implements VCDataModelFormatter {
    VelocityEngine engine;

    public VCDataModel2() {
        engine = new VelocityEngine();
        // TODO: Load a VM file from classpath & Spring config server conditionally.
        URLResourceLoader urlResourceLoader = new URLResourceLoader() {
            @Override
            public InputStream getResourceStream(String name) throws ResourceNotFoundException {
                try {
                    /*
                    URL url = new URL(name);
                    URLConnection connection = url.openConnection();
                     */
                    InputStream i = new FileInputStream("/Users/harshvardhan/work/mosip/digital-credential-plugins/vcdatamodel/src/main/resources/SchoolTemplate.vm");
                    return i;
                } catch (IOException e) {
                    // throw new ResourceNotFoundException("Unable to find resource '" + name + "'");
                }
                return null;
            }
        };
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "url");
        engine.setProperty("url.resource.loader.instance", urlResourceLoader);
        engine.init();
        // Validate all the supported VC
    }

    @Override
    public String format(Map<String, Object> templateInput) {
        Template template = engine.getTemplate("https://gist.githubusercontent.com/vharsh/b0c67b1e344fe4d957d4a5728c6efde1/raw/7ddec300243f5f81c35285acb57e2538cf3f370f/schooltemplate2.vm");
        // create context
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext(templateInput);
        template.merge(context, writer);
        // TODO: Convert this to a JSONObject and see if array elements are quoted.
        return writer.toString();
        }
    }
