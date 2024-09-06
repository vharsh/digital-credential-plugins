package io.mosip.certify.vcdatamodel.templating;

import org.json.JSONObject;

import java.util.Map;

/**
 * VCDataModelFormatter is a templating engine which takes @param templateInput and returns a templated VC.
 * Some implementations include
 * - VC 2.0 data model templating engine
 */
public interface VCDataModelFormatter {
    JSONObject format(Map<String, Object> templateInput);
}
