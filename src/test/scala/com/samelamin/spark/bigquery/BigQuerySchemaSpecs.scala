package com.samelamin.spark.bigquery

import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.scalatest.Matchers._
import org.scalatest.{FeatureSpec, GivenWhenThen}
import com.samelamin.spark.bigquery.converters._
import org.apache.spark.sql.types.{DateType, DecimalType, StructType}

class BigQuerySchemaSpecs extends FeatureSpec with GivenWhenThen with DataFrameSuiteBase {
  feature("Schema Converters. Dataframe To BQ Schema") {
    scenario("When converting a simple dataframe") {
      Given("A dataframe")
      val sampleJson = """{
                         |	"id": 1,
                         |	"error": null
                         |}""".stripMargin
      val df = sqlContext.read.json(sc.parallelize(List(sampleJson)))

      When("Passing the schema to the converter")
      val tableSchema = SchemaConverters.SqlToBQSchema(df)

      Then("We should receive a BQ Table Schema")
      val expectedSchema = """[ {
                             |  "name" : "error",
                             |  "mode" : "NULLABLE",
                             |  "type" : "STRING"
                             |}, {
                             |  "name" : "id",
                             |  "mode" : "NULLABLE",
                             |  "type" : "INTEGER"
                             |} ]
                             |""".stripMargin.trim

      tableSchema should not be null
      tableSchema should be (expectedSchema)

    }

    scenario("When converting a complex dataframe with nested data") {
      Given("A dataframe")
      val sqlCtx = sqlContext
      val sampleNestedJson = """{
                               |	"id": 1,
                               |	"error": null,
                               |	"result": {
                               |		"nPeople": 2,
                               |		"people": [{
                               |			"namePeople": "Inca",
                               |			"power": "1235",
                               |			"location": "asdfghjja",
                               |			"idPeople": 189,
                               |			"mainItems": "brownGem",
                               |			"verified": false,
                               |			"description": "Lorem impsum bla bla",
                               |			"linkAvatar": "avatar_12.jpg",
                               |			"longitude": 16.2434263,
                               |			"latitude": 89.355118
                               |		}, {
                               |			"namePeople": "Maya",
                               |			"power": "1235",
                               |			"location": "hcjkjhljhl",
                               |			"idPeople": 119,
                               |			"mainItems": "greenstone",
                               |			"verified": false,
                               |			"description": "Lorem impsum bla bla",
                               |			"linkAvatar": "avatar_6.jpg",
                               |			"longitude": 16.2434263,
                               |			"latitude": 89.3551185
                               |		}]
                               |	}
                               |}""".stripMargin
      val df = sqlContext.read.json(sc.parallelize(List(sampleNestedJson)))

      When("Passing the schema to the converter")
      val tableSchema = SchemaConverters.SqlToBQSchema(df)

      Then("We should receive a BQ Table Schema")
      tableSchema should not be null

      val expectedSchema = """[ {
                             |  "name" : "error",
                             |  "mode" : "NULLABLE",
                             |  "type" : "STRING"
                             |}, {
                             |  "name" : "id",
                             |  "mode" : "NULLABLE",
                             |  "type" : "INTEGER"
                             |}, {
                             |  "name" : "result",
                             |  "mode" : "NULLABLE",
                             |  "type" : "RECORD",
                             |  "fields" : [ {
                             |    "name" : "nPeople",
                             |    "mode" : "NULLABLE",
                             |    "type" : "INTEGER"
                             |  }, {
                             |    "name" : "people",
                             |    "mode" : "REPEATED",
                             |    "type" : "RECORD",
                             |    "fields" : [ {
                             |      "name" : "description",
                             |      "mode" : "NULLABLE",
                             |      "type" : "STRING"
                             |    }, {
                             |      "name" : "idPeople",
                             |      "mode" : "NULLABLE",
                             |      "type" : "INTEGER"
                             |    }, {
                             |      "name" : "latitude",
                             |      "mode" : "NULLABLE",
                             |      "type" : "FLOAT"
                             |    }, {
                             |      "name" : "linkAvatar",
                             |      "mode" : "NULLABLE",
                             |      "type" : "STRING"
                             |    }, {
                             |      "name" : "location",
                             |      "mode" : "NULLABLE",
                             |      "type" : "STRING"
                             |    }, {
                             |      "name" : "longitude",
                             |      "mode" : "NULLABLE",
                             |      "type" : "FLOAT"
                             |    }, {
                             |      "name" : "mainItems",
                             |      "mode" : "NULLABLE",
                             |      "type" : "STRING"
                             |    }, {
                             |      "name" : "namePeople",
                             |      "mode" : "NULLABLE",
                             |      "type" : "STRING"
                             |    }, {
                             |      "name" : "power",
                             |      "mode" : "NULLABLE",
                             |      "type" : "STRING"
                             |    }, {
                             |      "name" : "verified",
                             |      "mode" : "NULLABLE",
                             |      "type" : "BOOLEAN"
                             |    } ]
                             |  } ]
                             |} ]
                             |""".stripMargin.trim
      tableSchema should be (expectedSchema)
    }

    scenario("When converting from more obscure types") {

      Given("A dataframe")

      val eventSchema = (new StructType).add("event_day", DateType).add("event_value", DecimalType.USER_DEFAULT)
      val eventJsonRDD = sc.parallelize("""{"event_day":"20160102", "event_value":"123.1234"}""" :: Nil )

      val df = sqlContext.read.schema(eventSchema).json(eventJsonRDD)

      When("Passing the schema to the converter")
	  val tableSchema = SchemaConverters.SqlToBQSchema(df)

      Then("We should receive a BQ Table Schema")
      val expectedSchema = """[ {
                             |  "name" : "event_day",
                             |  "mode" : "NULLABLE",
                             |  "type" : "DATE"
                             |}, {
                             |  "name" : "event_value",
                             |  "mode" : "NULLABLE",
                             |  "type" : "FLOAT"
                             |} ]
                             |""".stripMargin.trim

      tableSchema should not be null
      tableSchema should be (expectedSchema)
    }
  }
}