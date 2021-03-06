package de.htwBerlin.ai.inews.core.Analytics.TermOccurrence

final case class TermNotFoundException(private val message: String = "",
                                       private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
