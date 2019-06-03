package repositories

import com.github.tminglei.slickpg._

trait PgProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgNetSupport
  with PgLTreeSupport
  with PgRangeSupport
  with PgHStoreSupport
  with PgSearchSupport {

  ///
  override val api = new API with ArrayImplicits
    with DateTimeImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {}
}

object PgProfile extends PgProfile
