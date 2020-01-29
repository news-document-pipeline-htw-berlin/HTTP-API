package de.htwBerlin.ai.inews.core.Analytics.TermOccurrence

case class TermOccurrences(
  totalResult: Long,
  query: String,
  timeFrom: Long,
  timeTo: Long,
  occurrences: Seq[TermOccurrence]
)

