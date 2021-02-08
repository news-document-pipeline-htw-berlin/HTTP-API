package de.htwBerlin.ai.inews.author

import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader}

import scala.util.Try

final case class Author(
                         _id: String,
                         articles: Int,
                         averageWords: Double,
                         avgAmountOfSources: Double,
                         daysPublished: List[String],
                         lastTexts: List[String],
                         perDepartment: List[String],
                         score: Double,
                         sentimentPerDay: List[String],
                         sentimentPerDepartment: List[String])

object Author {
  implicit object AuthorReader extends BSONDocumentReader[Author] {
    /**
     * Reader for parsing author in mongoDB to Author object.
     * @param doc entry in mongoDB
     * @return Author
     */
    override def readDocument(doc: BSONDocument): Try[Author] = {
      Try(Author(
        doc.getAsTry[String]("_id").get,
        doc.getAsTry[Int]("articles").get,
        doc.getAsTry[Double]("averageWords").get,
        doc.getAsTry[Double]("avgAmountOfSources").get,
        doc.getAsTry[List[String]]("daysPublished").get,
        doc.getAsTry[List[String]]("lastTexts").get,
        doc.getAsTry[List[String]]("perDepartment").get,
        doc.getAsTry[Double]("score").get,
        doc.getAsTry[List[String]]("sentimentPerDay").get,
        doc.getAsTry[List[String]]("sentimentPerDepartment").get
      ))
    }
  }

}