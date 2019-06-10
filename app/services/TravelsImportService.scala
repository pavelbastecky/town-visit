package services

import java.time.LocalDate

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing, Source}
import akka.util.ByteString
import com.opencsv.CSVParserBuilder
import com.typesafe.config.Config
import exceptions.ImportErrorException
import javax.inject.Inject
import models.Travel
import play.api.libs.streams.Accumulator
import play.api.mvc.BodyParser
import repositories.TravelsRepository
import services.TravelsImportService.ProcessedBatch
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object TravelsImportService {
  case class ProcessedBatch(count: Int, minDate: LocalDate)
}

/**
  * This service contains methods to handle importing TSV file with travel entries.
  *
  * Core of the service is AKKA stream:
  *   1) Reads request body one by one
  *   2) Parses line using TSV parser to [[Travel]] objects
  *   3) Inserts batches of travels to database
  *   4) Rebuilds column with cumulative travels
  *   5) Returns number of processed items
  *
  *   Index column is rebuild from minimal date of processed travels.
  *
  * IMPORTANT:
  *   * Imported file must contain tab separated rows delimited by \n character.
  *   * Each row must contain exactly 3 columns in order: DATE CITY KILOMETERS
  *   * Invalid rows are ignored (check number of returned rows for reference)
  */
class TravelsImportService @Inject() (config: Config,
                                      travelsRepository: TravelsRepository)
                                     (implicit executionContext: ExecutionContext)
  extends Logger
{
  private val dbBatchSize = config.getLong("traveled.import.dbBatchSize")
  private val maximumLineCharacters = config.getInt("traveled.import.maximumLineCharacters")

  /** Opencsv parser delimited by tab */
  private val parser = new CSVParserBuilder()
    .withSeparator('\t')
    .build()

  /** Allows to call min method on sequence of dates */
  implicit def ordering: Ordering[LocalDate] = (x: LocalDate, y: LocalDate) => {
    x.toString.compareTo(y.toString)
  }

  /**
    * Definition of AKKA stream for processing. Can be used in play request.
    */
  val importTravelsTsvStream: BodyParser[Source[Int, _]] = BodyParser { req =>

    val transformFlow: Flow[ByteString, Int, NotUsed] = Flow[ByteString]
      // Chunk incoming bytes by newlines, truncating them if the lines is too big
      .via(Framing.delimiter(ByteString("\n"), maximumLineCharacters, allowTruncation = true))
      // Convert each input line to travel object
      // Malformed lines are ignored
      .mapConcat(parseTravel(_).toList)
      // Create batches to be inserted to database in reasonable chunks
      .batch(dbBatchSize, id => Seq(id))(_ :+ _)
      .mapAsync(1)(processBatch)
      // Obtain minimal date and recalculate index with sums
      .mapConcat(_.toList)
      .reduce(cumulativeResults)
      .mapAsync(1)(recalculateTotalKilometers)

    Accumulator.source[ByteString]
      .map(_.via(transformFlow))
      .map(Right.apply)
  }

  /**
    * Converts each line to travel object. Invalid lines are ignored.
    */
  private[services] def parseTravel(string: ByteString): Option[Travel] = {
    val line = string.utf8String.trim
    val parsed = parser.parseLine(string.utf8String.trim)

    if (parsed.size == 3) {
      val Array(date, city, kilometers) = parsed
      val t = for {
        parsedDate <- Try(LocalDate.parse(date.trim))
        parsedKilometers <- Try(kilometers.trim.toInt)
      } yield Travel(0, parsedDate, city, parsedKilometers, None)

      t match {
        case Success(value) => Some(value)
        case Failure(e) =>
          logger.debug(s"Error parsing given line, ignoring: line: $line, error: ${e.getMessage}")
          None
      }
    } else {
      logger.debug(s"Ignoring line with invalid TS element count: ${parsed.size}, line: $line")
      None
    }
  }

  /**
    * Inserts all batched travels to database and extracts minimal date and travels count.
    */
  private[services] def processBatch(travels: Seq[Travel]): Future[Option[ProcessedBatch]] = {
    logger.debug(s"Inserting ${travels.size} travels to database")
    if (travels.nonEmpty) {
      travelsRepository.insertAll(travels).map { _ =>
        Some(ProcessedBatch(travels.size, travels.map(_.date).min))
      }
      .recoverWith {
        case e: Exception =>
          logger.error("Failed to insert batch with imported travels to database: ", e)
          Future.failed(ImportErrorException(e))
      }
    } else {
      Future.successful(None)
    }
  }

  /**
    * Aggregates results of batch processing to single result containing total number of rows and minimal date seen
    * in the request.
    */
  private[services] def cumulativeResults(a: ProcessedBatch, b: ProcessedBatch): ProcessedBatch = {
    val minDate =
      if (a.minDate isBefore b.minDate) a.minDate
      else b.minDate

    ProcessedBatch(a.count + b.count, minDate)
  }

  /**
    * Rebuilds of the index column with total traveled distance.
    */
  private[services] def recalculateTotalKilometers(processed: ProcessedBatch): Future[Int] = {
    logger.info(s"Rebuilding traveled total column starting at ${processed.minDate}")

    val f = for {
      minTraveled <- travelsRepository.getMinTraveled(processed.minDate)
      _ <- travelsRepository.updateTraveledTotal(processed.minDate, minTraveled)
    } yield {
      logger.info(s"Imported ${processed.count} travels starting at ${processed.minDate}")
      processed.count
    }

    f.recoverWith {
      case e: Exception =>
        logger.error("Failed to recalculate total traveled distance in DB: ", e)
        Future.failed(ImportErrorException(e))
    }
  }
}
