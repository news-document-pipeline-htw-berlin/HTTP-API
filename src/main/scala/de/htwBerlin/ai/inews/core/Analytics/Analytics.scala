package de.htwBerlin.ai.inews.core.Analytics

case class Analytics(
  totalResult: Long,
  query: String,
  timeFrom: Long,
  timeTo: Long,
  occurrences: Seq[TermOccurrence]
)

