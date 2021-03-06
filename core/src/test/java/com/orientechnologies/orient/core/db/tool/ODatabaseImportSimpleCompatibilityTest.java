package com.orientechnologies.orient.core.db.tool;

import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.*;
import java.io.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ODatabaseImportSimpleCompatibilityTest {
  private OrientDB orientDB;

  private ODatabaseSession importDatabase;
  private ODatabaseImport importer;

  private ODatabaseExport export;

  // Known compatibility issue: the deprecation of manual indexes is checked by maven, which makes
  // this test fail: `"manualIndexes":[{"name":"dictionary","content":[]}]`
  @Ignore
  @Test
  public void testImportExportOldEmpty() throws Exception {
    final InputStream emptyDbV2 = load("/databases/databases_2_2/Empty.json");
    Assert.assertNotNull("Input must not be null!", emptyDbV2);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    Assert.assertEquals(0, output.size());
    final String databaseName = "testImportExportOldEmpty";
    this.setup(databaseName, emptyDbV2, output);

    this.executeImport();
    this.executeExport(" -excludeAll -includeSchema=true -includeManualIndexes=false");

    this.tearDown(databaseName);
    Assert.assertTrue(output.size() > 0);
  }

  // The deprecation of manual indexes is checked by maven, which makes this test fail.
  @Ignore
  @Test
  public void testImportExportOldSimple() throws Exception {
    final InputStream simpleDbV2 = load("/databases/databases_2_2/OrderCustomer-sl-0.json");
    Assert.assertNotNull("Input must not be null!", simpleDbV2);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    Assert.assertEquals(0, output.size());
    final String databaseName = "testImportExportOldSimple";
    this.setup(databaseName, simpleDbV2, output);

    this.executeImport();
    this.executeExport(" -excludeAll -includeSchema=true -includeManualIndexes=false");

    Assert.assertTrue(importDatabase.getMetadata().getSchema().existsClass("OrderCustomer"));

    this.tearDown(databaseName);
    Assert.assertTrue(output.size() > 0);
  }

  @Test
  public void testImportExportNewerSimple() throws Exception {
    final InputStream simpleDbV3 = load("/databases/databases_3_1/OrderCustomer-sl-0.json");
    Assert.assertNotNull("Input must not be null!", simpleDbV3);
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    Assert.assertEquals(0, output.size());
    final String databaseName = "testImportExportNewerSimple";
    this.setup(databaseName, simpleDbV3, output);

    this.executeImport();
    this.executeExport(" -excludeAll -includeSchema=true");

    Assert.assertTrue(importDatabase.getMetadata().getSchema().existsClass("OrderCustomer"));

    this.tearDown(databaseName);
    Assert.assertTrue(output.size() > 0);
  }

  private InputStream load(final String path) throws FileNotFoundException {
    final File file = new File(getClass().getResource(path).getFile());
    return new FileInputStream(file);
  }

  private void setup(
      final String databaseName, final InputStream input, final OutputStream output) {
    final String importDbUrl = "memory:target/import_" + this.getClass().getSimpleName();
    orientDB = new ODatabaseImportTest().createDatabase(databaseName, importDbUrl);
    importDatabase = orientDB.open(databaseName, "admin", ODatabaseImportTest.NEW_ADMIN_PASSWORD);
    try {
      importer =
          new ODatabaseImport(
              (ODatabaseDocumentInternal) importDatabase,
              input,
              new OCommandOutputListener() {
                @Override
                public void onMessage(String iText) {}
              });
      export =
          new ODatabaseExport(
              (ODatabaseDocumentInternal) importDatabase,
              output,
              new OCommandOutputListener() {
                @Override
                public void onMessage(String iText) {}
              });
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void tearDown(String databaseName) {
    try {
      orientDB.drop(databaseName);
      orientDB.close();
    } catch (final Exception e) {
      System.out.println("Issues during teardown" + e.getMessage());
    }
  }

  private void executeImport() {
    importer.setOptions(" -includeManualIndexes=false");
    importer.importDatabase();
  }

  public void executeExport(final String options) {
    export.setOptions(options);
    export.exportDatabase();
  }
}
