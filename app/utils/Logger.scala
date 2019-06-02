package utils

import org.slf4j

trait Logger {
  protected val logger: slf4j.Logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
}
