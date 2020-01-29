package de.htwBerlin.ai.inews.core.Analytics.MostRelevantLemmas

final case class LemmasNotFoundException(private val message: String = "",
                                         private val cause: Throwable = None.orNull)
  extends Exception(message, cause)