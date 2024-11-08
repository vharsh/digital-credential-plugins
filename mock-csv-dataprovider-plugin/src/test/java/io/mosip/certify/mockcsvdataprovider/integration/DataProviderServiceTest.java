package io.mosip.certify.mockcsvdataprovider.integration;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.mockcsvdataprovider.integration.service.DataProviderService;
import io.mosip.certify.mockcsvdataprovider.integration.utils.CSVReader;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataProviderServiceTest {
    @Mock
    CSVReader csvReader;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    DataProviderService dataProviderService = new DataProviderService();

    void setup() {
        csvReader = new CSVReader();
    }

    // TODO: Write test for initialize()

    @Test
    public void fetchDataFromValidFile_thenPass() throws JSONException, DataProviderExchangeException {
        JSONObject actual = dataProviderService.fetchDataFromCSVReader("1234567");
        JsonAssertions.assertThatJson(actual).isEqualTo(null);
    }
}
