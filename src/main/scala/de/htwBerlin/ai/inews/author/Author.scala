package de.htwBerlin.ai.inews.author

final case class AuthorIntMap(
                            _1: String,
                            _2: Int
                          )
final case class AuthorDoubleMap(
                               _1: String,
                               _2: Double
                             )

final case class Author(
                         _id: String,
                         articles: Int,
                         averageWords: Double,
                         avgAmountOfSources: Double,
                         daysPublished: List[AuthorIntMap],
                         lastTexts: List[AuthorIntMap],
                         perDepartment: List[AuthorIntMap],
                         perWebsite: Map[String, Int],
                         score: Double,
                         sentimentPerDay: List[AuthorDoubleMap],
                         sentimentPerDepartment: List[AuthorDoubleMap])
