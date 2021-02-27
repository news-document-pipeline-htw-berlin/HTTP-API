package de.htwBerlin.ai.inews.author

/**
 * Map for daysPublished / lastTexts / perDepartment.
 *
 * @param _1 key (weekday / weekday / department)
 * @param _2 value (amount / word count / department)
 */
final case class AuthorIntMap(
                               _1: String,
                               _2: Int
                             )

/**
 * Map for sentimentPerDay / sentimentPerDepartment.
 *
 * @param _1 key (weekday / department)
 * @param _2 value (sentiment score)
 */
final case class AuthorDoubleMap(
                                  _1: String,
                                  _2: Double
                                )

/**
 * Representation of author entity.
 */
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
