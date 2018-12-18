package com.antkorwin.springtestmongo.internal;

import com.antkorwin.commonutils.exceptions.InternalException;
import com.antkorwin.commonutils.validation.GuardCheck;
import com.antkorwin.springtestmongo.Bar;
import com.antkorwin.springtestmongo.Foo;
import com.antkorwin.springtestmongo.errorinfo.MongoDbErrorInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;

import static org.mockito.Mockito.*;

/**
 * Created on 01.12.2018.
 *
 * @author Korovin Anatoliy
 */
class MongoDbTestTest {

    private MongoTemplate mongoTemplate;
    private MongoDbTest mongoDbTest;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        mongoDbTest = new MongoDbTest(mongoTemplate);
    }


    @Nested
    @DisplayName("Test Import")
    class ImportTests {

        @Test
        @DisplayName("One record with a simple entity")
        void documentWithOneRecordDataSet() throws Exception {
            // Arrange
            ArgumentCaptor<Bar> captor = ArgumentCaptor.forClass(Bar.class);
            // Act
            mongoDbTest.importFrom("/dataset/simple_dataset.json");
            // Assert
            verify(mongoTemplate).save(captor.capture());
            Assertions.assertThat(captor.getValue())
                      .isNotNull()
                      .extracting(Bar::getId, Bar::getData)
                      .containsOnly("55f3ed00b1375a48e61830bf", "TEST");
        }

        @Test
        @DisplayName("Collection of documents with simple entities")
        void simpleCollectionOfDocuments() throws Exception {
            // Arrange
            ArgumentCaptor<Bar> captor = ArgumentCaptor.forClass(Bar.class);
            // Act
            mongoDbTest.importFrom("/dataset/simple_collections_dataset.json");
            // Assert
            verify(mongoTemplate, times(3)).save(captor.capture());
            Assertions.assertThat(captor.getAllValues())
                      .isNotNull()
                      .extracting(Bar::getId, Bar::getData)
                      .containsOnly(Tuple.tuple("55f3ed00b1375a48e618300a", "A"),
                                    Tuple.tuple("55f3ed00b1375a48e618300b", "BB"),
                                    Tuple.tuple("55f3ed00b1375a48e618300c", "CCC"));
        }

        @Test
        @DisplayName("Collection with multiple document types")
        void multipleDocumentTypes() throws Exception {
            // Arrange
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            // Act
            mongoDbTest.importFrom("/dataset/multidocument_dataset.json");
            // Assert
            verify(mongoTemplate, times(3)).save(captor.capture());

            Assertions.assertThat(captor.getAllValues().get(0))
                      .extracting("id", "data", "class")
                      .containsOnly("55f3ed00b1375a48e618300a", "A", Bar.class);

            Assertions.assertThat(captor.getAllValues().get(1))
                      .extracting("id", "data", "class")
                      .containsOnly("55f3ed00b1375a48e618300b", "BB", Bar.class);

            Assertions.assertThat(captor.getAllValues().get(2))
                      .extracting("id", "time", "counter", "class")
                      .containsOnly("77f3ed00b1375a48e618300a", new Date(1516527720000L), 1, Foo.class);
        }

        @Nested
        @DisplayName("Corner cases")
        class CornerCases {

            @Test
            @DisplayName("Try to import from a not exists data file")
            void testWithWrongResourceFile() throws Exception {
                GuardCheck.check(() -> mongoDbTest.importFrom("unexist"),
                                 InternalException.class,
                                 MongoDbErrorInfo.FILE_NOT_FOUND);
            }

            @Test
            @DisplayName("Try to import from an empty data file")
            void testWithEmptyFile() throws Exception {
                GuardCheck.check(() -> mongoDbTest.importFrom("/dataset/emptyfile.json"),
                                 InternalException.class,
                                 MongoDbErrorInfo.JSON_PARSING_ERROR);
            }

            @Test
            @DisplayName("Try to import a wrong data set (not supported json format)")
            void testWithWrongDataSetFormat() throws Exception {
                GuardCheck.check(() -> mongoDbTest.importFrom("/dataset/wrong_format.json"),
                                 InternalException.class,
                                 MongoDbErrorInfo.JSON_PARSING_ERROR);
            }

            @Test
            @DisplayName("Try to import an undefined document class type")
            void testWithWrongDocumentCollectionType() throws Exception {
                GuardCheck.check(() -> mongoDbTest.importFrom("/dataset/wrong_collection_type.json"),
                                 InternalException.class,
                                 MongoDbErrorInfo.UNRESOLVED_DOCUMENT_COLLECTION_CLASS_TYPE);
            }

            @Test
            @DisplayName("Try to import unsupported format of entity(without nested collection)")
            void testWithWrongDataFormat() throws Exception {
                GuardCheck.check(() -> mongoDbTest.importFrom("/dataset/wrong_dataformat.json"),
                                 InternalException.class,
                                 MongoDbErrorInfo.JSON_PARSING_ERROR);
            }

            @Test
            @DisplayName("Try to import two records with wrong format of the second record")
            void testWithWrongDataInSecondRecord() throws Exception {
                GuardCheck.check(() -> mongoDbTest.importFrom("/dataset/wrong_dataformat_in_record.json"),
                                 InternalException.class,
                                 MongoDbErrorInfo.DOCUMENT_RECORD_PARSING_ERROR);
            }
        }


    }


    @Test
    @DisplayName("Try to instantiate a MongoDbTest without the MongoTemplate.")
    void testWithoutMongoTemplate() throws Exception {
        // Act & Assert
        GuardCheck.check(() -> new MongoDbTest(null),
                         InternalException.class,
                         MongoDbErrorInfo.MONGO_TEMPLATE_IS_MANDATORY);
    }
}