/*
 * Copyright 2016 Netflix, Inc.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.metacat.connector.s3

import com.google.inject.persist.PersistService
import com.netflix.metacat.connector.s3.model.*
import spock.guice.UseModules
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Inject
import java.sql.DriverManager

@UseModules([
    S3TestModule.class
])
@Ignore
class BaseSpec extends Specification {
    @Shared
    @Inject
    PersistService persistService
    @Shared
    Map<String, Source> sources
    @Shared
    Map<String, Database> databases
    @Shared
    Map<String, Table> tables
    @Shared
    Map<String, Partition> partitions

    def setupSpec() {
        setupPersist()
        setModels()
    }

    def setModels() {
        // source
        def source = new Source(name: 's3', type: 's3')
        // databases
        def database = new Database(name: 'test', source: source)
        def database1 = new Database(name: 'test1', source: source)
        // Table 1
        def location = new Location(uri: 's3://')
        def info = new Info(owner: 'amajumdar', inputFormat: 'text', location: location)
        def schema = new Schema(location: location)
        def field = new Field(name: 'a', type: 'chararray', partitionKey: true, schema: schema)
        schema.setFields([field])
        location.setInfo(info)
        location.setSchema(schema)
        def table = new Table(name: 'part', location: location, database: database)
        location.setTable(table)
        // Table 2
        def location1 = new Location(uri: 's3://')
        def info1 = new Info(owner: 'amajumdar', inputFormat: 'text', location: location1)
        def schema1 = new Schema(location: location1)
        def field1 = new Field(name: 'a', type: 'chararray', partitionKey: true, schema: schema1)
        def field2 = new Field(name: 'b', type: 'chararray', partitionKey: true, schema: schema1)
        schema1.setFields([field1, field2])
        location1.setInfo(info1)
        location1.setSchema(schema1)
        def table1 = new Table(name: 'part1', location: location1, database: database)
        location1.setTable(table1)
        //Partitions
        def partition = new Partition(name: 'dateint=20171212', uri: 's3://part/dateint=20171212', table: table)
        def partition1 = new Partition(name: 'dateint=20171213', uri: 's3://part/dateint=20171213', table: table)
        def partition2 = new Partition(name: 'dateint=20171214', uri: 's3://part/dateint=20171214', table: table)

        sources = ['s3': source]
        databases = ['test': database, 'test1': database1]
        tables = ['part': table, 'part1': table1]
        partitions = ['dateint=20171212': partition, 'dateint=20171213': partition1, 'dateint=20171214': partition2]
    }

    def setupPersist() {
        persistService.start()
    }

    def cleanupSpec() {
        if (persistService != null) {
            DriverManager.getConnection("jdbc:hsqldb:mem:metacat", 'sa', '').createStatement().execute('SHUTDOWN')
            persistService.stop()
        }
    }
}
