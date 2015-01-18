package com.esri

import com.datastax.spark.connector._
import com.esri.hex._
import org.apache.spark.SparkContext._
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

/**
 * Syntactic sugar implicits to convert a lat/lon double values into a mercator X/Y meter values.
 */
private object WebMerctorConversions {

  implicit class DoubleWithMercator(n: Double) {
    def mercatorX() = WebMercator.longitudeToX(n)

    def mercatorY() = WebMercator.latitudeToY(n)
  }

}

/**
 * http://stackoverflow.com/questions/25107028/jodatime-scala-and-sterilizing-datetimes
 *
 * Have to wrap Joda DateTimeFormatter in an Serializable object.
 * Spark serializes functions not objects - not sure exactly what this means !!
 * TODO - investigate this more
 */
private object HourExtractor extends Serializable {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  dateTimeFormatter.withZoneUTC

  def dateToHour(text: String): Int = {
    dateTimeFormatter.parseDateTime(text).hourOfDay().get
  }
}

object HexTrips {

  def main(args: Array[String]) = {

    val (cassandraHost, csvFile) = if (args.length == 2) (Some(args(0)), Some(args(1))) else (None, None)

    // For web mercator implicits on double :-)
    import com.esri.WebMerctorConversions._

    lazy val conf = new SparkConf()
      .setAppName(HexTrips.getClass.getSimpleName)
      .setMaster("local[*]")
      .set("spark.driver.memory", "2g")
      .set("spark.executor.memory", "2g")
      .set("spark.serializer", classOf[KryoSerializer].getName)
      .set("spark.cassandra.connection.host", cassandraHost.getOrElse("192.168.172.1"))
      .registerKryoClasses(Array(
      classOf[HexRowCol],
      classOf[HexGrid],
      classOf[HexXY]
    ))

    lazy val sc = new SparkContext(conf)
    try {
      // Somewhere on the lower left of Manhattan in mercator meter values.
      val hexGrid = HexGrid(100, -8300000.0, 4800000.0)
      sc.textFile(csvFile.getOrElse("/Users/mraad_admin/Share/trips-1M.csv"))
        .flatMap(line => {
        // Parse trip record and pull pickup lon/lat and extract the pickup hour.
        // Return an option in case of an error occurs and nulls are "bad" form in Scala.
        try {
          val tokens: Array[String] = line.split(',')
          val x = tokens(10).toDouble mercatorX() // plon
          val y = tokens(11).toDouble mercatorY() // plat
          val h = HourExtractor.dateToHour(tokens(5)) // pdate
          Some((x, y, h))
        }
        catch {
          case _: Throwable =>
            None
        }
      })
        // Only pass along pickups between 7 and 9 AM
        .filter { case (x, y, hour) => 7 <= hour && hour <= 9}
        // Convert to hex cell using https://github.com/mraad/hex-grid
        .map { case (x, y, hour) => (hexGrid.convertXYToRowCol(x, y), 1)}
        // Sum all 1's by (row,col)
        .reduceByKey(_ + _)
        // Map to Cassandra fields
        .map { case (rowcol, population) => (rowcol.row, rowcol.col, population)}
        // Save to keyspace/table
        .saveToCassandra("test", "hexgrid", SomeColumns("row", "col", "population"))
    }
    finally {
      sc.stop
    }
  }
}