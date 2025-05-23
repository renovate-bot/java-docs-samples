/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bigquery;

import com.google.cloud.Identity;
import com.google.cloud.Policy;
import com.google.cloud.Role;
import com.google.cloud.bigquery.Acl;
import com.google.cloud.bigquery.Acl.Entity;
import com.google.cloud.bigquery.Acl.Group;
import com.google.cloud.bigquery.Acl.Role;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.ViewDefinition;
import java.util.ArrayList;
import java.util.List;

public class Util {

  private static BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

  public static Dataset setUpTest_createDataset(String projectId, String datasetName)
      throws BigQueryException {
    String location = "US";
    DatasetId datasetId = DatasetId.of(projectId, datasetName);
    DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetId).setLocation(location).build();
    return bigquery.create(datasetInfo);
  }

  public static boolean tearDownTest_deleteDataset(String projectId, String datasetName) {
    DatasetId datasetId = DatasetId.of(projectId, datasetName);
    return bigquery.delete(datasetId, DatasetDeleteOption.deleteContents());
  }

  public static Table setUpTest_createTable(
      String projectId, String datasetName, String tableName, Schema schema)
      throws BigQueryException {
    TableId tableId = TableId.of(projectId, datasetName, tableName);
    TableDefinition tableDefinition = StandardTableDefinition.of(schema);
    TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

    return bigquery.create(tableInfo);
  }

  public static Table setUpTest_createView(
      String projectId, String datasetName, String viewName, String query)
      throws BigQueryException {
    TableId tableId = TableId.of(projectId, datasetName, viewName);
    ViewDefinition viewDefinition = ViewDefinition.newBuilder(query).setUseLegacySql(false).build();
    TableInfo tableInfo = TableInfo.of(tableId, viewDefinition);

    return bigquery.create(tableInfo);
  }

  public static boolean tearDownTest_deleteTableOrView(
      String projectId, String datasetName, String tableName) throws BigQueryException {
    TableId tableId = TableId.of(projectId, datasetName, tableName);
    return bigquery.delete(tableId);
  }

  public static Dataset setUpTest_grantAccessToDataset(
      String projectId, String datasetName, String entityEmail) throws BigQueryException {
    DatasetId datasetId = DatasetId.of(projectId, datasetName);
    Dataset dataset = bigquery.getDataset(datasetId);

    Entity entity = new Group(entityEmail);
    Acl newEntry = Acl.of(entity, Role.READER);
    List<Acl> acls = new ArrayList<>(dataset.getAcl());
    acls.add(newEntry);

    return bigquery.update(dataset.toBuilder().setAcl(acls).build());
  }

  public static Policy setUpTest_grantAccessToTableOrView(
      String projectId, String datasetName, String resourceName, Role role, Identity identity)
      throws BigQueryException {
    TableId tableId = TableId.of(projectId, datasetName, resourceName);
    Policy policy = bigquery.getIamPolicy(tableId);
    policy = policy.toBuilder().addIdentity(role, identity).build();

    return bigquery.setIamPolicy(tableId, policy);
  }
}
