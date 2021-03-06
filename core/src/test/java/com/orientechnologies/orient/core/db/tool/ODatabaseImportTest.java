package com.orientechnologies.orient.core.db.tool;

import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/** Created by tglman on 23/05/16. */
public class ODatabaseImportTest {
  static final String NEW_ADMIN_PASSWORD = "adminpwd";

  @Test
  public void exportImportOnlySchemaTest() throws IOException {
    final String databaseName = "test";
    final String exportDbUrl = "memory:target/export_" + ODatabaseImportTest.class.getSimpleName();
    final OrientDB orientDB = createDatabase(databaseName, exportDbUrl);

    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (final ODatabaseSession db = orientDB.open(databaseName, "admin", NEW_ADMIN_PASSWORD)) {
      db.createClass("SimpleClass");

      final ODatabaseExport export =
          new ODatabaseExport(
              (ODatabaseDocumentInternal) db,
              output,
              new OCommandOutputListener() {
                @Override
                public void onMessage(String iText) {}
              });
      export.setOptions(" -excludeAll -includeSchema=true");
      export.exportDatabase();
    }

    final String importDbUrl = "memory:target/import_" + ODatabaseImportTest.class.getSimpleName();
    createDatabase(databaseName, importDbUrl);

    try (final ODatabaseSession db = orientDB.open(databaseName, "admin", NEW_ADMIN_PASSWORD)) {
      final ODatabaseImport importer =
          new ODatabaseImport(
              (ODatabaseDocumentInternal) db,
              new ByteArrayInputStream(output.toByteArray()),
              new OCommandOutputListener() {
                @Override
                public void onMessage(String iText) {}
              });
      importer.importDatabase();
      Assert.assertTrue(db.getMetadata().getSchema().existsClass("SimpleClass"));
    }
    orientDB.drop(databaseName);
    orientDB.close();
  }

  @Test
  public void exportImportExcludeClusters() throws IOException {
    final String databaseName = "test";
    final String exportDbUrl =
        "memory:target/export_" + ODatabaseImportTest.class.getSimpleName() + "_excludeclusters";
    final OrientDB orientDB = createDatabase(databaseName, exportDbUrl);

    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (final ODatabaseSession db = orientDB.open(databaseName, "admin", NEW_ADMIN_PASSWORD)) {
      db.createClass("SimpleClass");

      final ODatabaseExport export =
          new ODatabaseExport(
              (ODatabaseDocumentInternal) db,
              output,
              new OCommandOutputListener() {
                @Override
                public void onMessage(String iText) {}
              });
      export.setOptions(" -includeClusterDefinitions=false");
      export.exportDatabase();
    }

    final String importDbUrl =
        "memory:target/import_" + ODatabaseImportTest.class.getSimpleName() + "_excludeclusters";
    createDatabase(databaseName, importDbUrl);

    try (final ODatabaseSession db = orientDB.open(databaseName, "admin", NEW_ADMIN_PASSWORD)) {
      final ODatabaseImport importer =
          new ODatabaseImport(
              (ODatabaseDocumentInternal) db,
              new ByteArrayInputStream(output.toByteArray()),
              new OCommandOutputListener() {
                @Override
                public void onMessage(String iText) {}
              });
      importer.importDatabase();
      Assert.assertTrue(db.getMetadata().getSchema().existsClass("SimpleClass"));
    }
    orientDB.drop(databaseName);
    orientDB.close();
  }

  OrientDB createDatabase(String database, String url) {
    final OrientDB orientDB =
        new OrientDB(
            url,
            OrientDBConfig.builder()
                .addConfig(OGlobalConfiguration.CREATE_DEFAULT_USERS, false)
                .build());
    // orientDB.create(database, ODatabaseType.PLOCAL);
    orientDB.execute(
        "create database "
            + database
            + " plocal users ( admin identified by '"
            + NEW_ADMIN_PASSWORD
            + "' role admin)");
    return orientDB;
  }
}
