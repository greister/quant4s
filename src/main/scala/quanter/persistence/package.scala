package quanter
import java.util.Date

// import scala.slick.driver.MySQLDriver.simple._
import scala.slick.driver.H2Driver.simple._


/**
  *
  */
package object persistence {

  case class EStrategy (id: Option[Int], name: String, runMode: Int, status: Int, lang: String) {
    // def portfolio = portfolios.filter(_.strategyId === id.get).take(1).firstOption
  }
  class EStrategies(tag: Tag) extends Table[EStrategy](tag, "STRATEGIES") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def runMode = column[Int]("RUNMODE")
    def status = column[Int]("STATUS")
    def lang = column[String]("LANG")

    def * = (id.?, name, runMode, status, lang) <> (EStrategy.tupled, EStrategy.unapply)
  }
  val strategies = TableQuery[EStrategies]

  case class EPortfolio(id: Option[Int], cash: Double, date: String, strategyId: Int) {
    // def holdings = stockHoldings.filter(_.portfolioId === id.get).list.toList
  }
  class EPortfolios(tag: Tag) extends Table[EPortfolio](tag, "PORTFOLIOS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def cash = column[Double]("CASH")
    def date = column[String]("HOLDINGDATE")
    def strategyId = column[Int]("STRATEGY_ID")

    def strategy = foreignKey("STRATEGY_FK", strategyId, strategies)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def * = (id.?, cash, date, strategyId) <> (EPortfolio.tupled, EPortfolio.unapply)
  }
  val portfolios = TableQuery[EPortfolios]

  case class EStockHolding(id: Option[Int], portfolioId: Int, symbol: String, cost: Double)
  class EStockHoldings(tag: Tag) extends Table[EStockHolding](tag, "STOCKHOLDINGS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def portfolioId = column[Int]("PORTFOLIO_ID")
    def symbol = column[String]("SYMBOL")
    def cost = column[Double]("COST")

    def * = (id.?, portfolioId, symbol, cost) <> (EStockHolding.tupled, EStockHolding.unapply)
  }
  val stockHoldings = TableQuery[EStockHoldings]

  case class ETransaction(id: Option[Int], strategyId: Int,symbol: String) {
    def orders = gOrders.filter(_.strategyId === strategyId)
  }
  class ETransactions(tag: Tag) extends Table[ETransaction](tag, "TRANSACTION") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def symbol = column[String]("SYMBOL")
    def strategyId = column[Int]("STRATEGY_ID")

    def * = (id.?, strategyId, symbol) <> (ETransaction.tupled, ETransaction.unapply)
  }
  val transactions = TableQuery[ETransactions]

  case class EOrder(id: Option[Int], strategyId: Int, symbol: String)
  class EOrders(tag: Tag) extends Table[EOrder](tag, "ORDER") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def symbol = column[String]("SYMBOL")
    def strategyId = column[Int]("STRATEGY_ID")

    def * = (id.?, strategyId, symbol) <> (EOrder.tupled, EOrder.unapply)
  }
  val gOrders = TableQuery[EOrders]

  case class ETrader(id: Option[Int], name: String)
  class ETraders(tag: Tag) extends Table[ETrader](tag, "TRADERS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")

    def * = (id.?, name) <> (ETrader.tupled, ETrader.unapply)
  }
  val traders = TableQuery[ETraders]
}
