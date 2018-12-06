package com.antkorwin.springtestmongo.internal;

import com.antkorwin.springtestmongo.Bar;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 06.12.2018.
 *
 * @author Korovin Anatoliy
 */
class JsonExportTest {

    @Test
    void testExport() {
        // Arrange
        String expectedJson = getExpectedJson();
        DataSet dataSet = getDataSet();
        // Act
        String text = new JsonExport(dataSet).read();
        // Asserts
        assertThat(text).isNotNull()
                        .isEqualTo(expectedJson);
    }

    private DataSet getDataSet() {
        Bar bar1 = new Bar("111100001", "data-1");
        Bar bar2 = new Bar("111100002", "data-2");
        Map<String, List<?>> map = ImmutableMap.of(Bar.class.getCanonicalName(), Arrays.asList(bar1, bar2));
        return () -> map;
    }

    private String getExpectedJson() {
        // new ImportFile("/dataset/internal/json_expected.json").read();
        return "{\n" +
               "  \"com.antkorwin.springtestmongo.Bar\" : [ {\n" +
               "    \"id\" : \"111100001\",\n" +
               "    \"data\" : \"data-1\"\n" +
               "  }, {\n" +
               "    \"id\" : \"111100002\",\n" +
               "    \"data\" : \"data-2\"\n" +
               "  } ]\n" +
               "}";
    }
}