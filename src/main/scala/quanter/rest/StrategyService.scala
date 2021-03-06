package quanter.rest

import org.json4s.{DefaultFormats, Extraction, Formats}
import spray.routing.HttpService
import org.json4s.jackson.JsonMethods._
import quanter.actors.strategy._
import akka.pattern.ask
import akka.util.Timeout
import quanter.actors._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  *
  */
trait StrategyService extends HttpService {
//  val strategiesManager = new StrategiesManager()
  val manager = actorRefFactory.actorSelection("/user/" + StrategiesManagerActor.path)
//  val strategyServiceRoute = {
//    post {
//      path("/strategy") {
//        requestInstance {
//          request => {
//            complete {
//              "code: 0"
//            }
//          }
//        }
//      }
//    } ~
//    get {
//      path("strategy") {
//        complete("""{"code":404}""")
//      }
//    }
//  }
  val strategyServiceRoute = {
    get {
      path("strategy") {
        complete("""{"code":404}""")
      } ~
      path("strategy" / "list") {
        complete {
          _getAllStrategies()
        }
      } ~
      path("strategy" / "history" / IntNumber) {
        id =>
          complete {
            _readHistoryStrategy(id)
          }
      } ~
//      path("strategy" / "backtest" / IntNumber) {
//        id =>
//          complete {
//            _backtestReport(id)
//          }
//      } ~
      path("strategy" / "real" / IntNumber) {
        id =>
          complete {
            "历史实盘"
          }
      } ~
      path("strategy" / IntNumber) {
        id =>
          complete {
            _getStrategy(id)
          }
      }~
      path("strategy"/ IntNumber / "portfolio") {
        id =>
          complete {
            """{"code":0, "portfolio":{"cash":10000, "frozen":0, "available":10000,"assets":300000, "positions":[{"symbol":"000001.XSHE","price":13, "cost": 10.3},{"symbol":"000002.XSHE","price":13, "cost": 9.3}]}}"""
          }
      }
    }  ~
    post {
      path("strategy") {
        requestInstance {
          request =>
            complete {
              _createStrategy(request.entity.data.asString)
            }
        }
      }~
      path("strategy" / "run" / IntNumber) {
        id => {
          complete {
            _runStrategy(id)
          }
        }
      }
    } ~
    put {
      path("strategy") {
        requestInstance {
          request =>
            complete {
              _updateStrategy(request.entity.data.asString)
            }
        }
      }
    } ~
    delete {
      path("strategy" /IntNumber) {
        id =>
        complete {
          _deleteStrategy(id)
        }
      }
    }
  }

  /**
    * 从数据库中读取所有的策略
    *
    * @return
    */
  private def _getAllStrategies(): String = {
    try {
      implicit val timeout = Timeout(5 seconds)
      val future = manager ? ListStrategies

      val result = Await.result(future, timeout.duration).asInstanceOf[Option[Array[Strategy]]]
      implicit val formats: Formats = DefaultFormats
      val retStrategyList = RetStrategyList(0, "成功", result)
      val json = Extraction.decompose(retStrategyList)
      compact(render(json))
    }  catch {
      case ex: Exception => """{"code":1, "message":"%s"}""".format(ex.getMessage)
    }
  }

  private def _getStrategy(id: Int): String = {
    try {
      implicit val timeout = Timeout(10 seconds)
      val future = manager ? new GetStrategy(id)
      var  ret = ""
      val result = Await.result(future, timeout.duration).asInstanceOf[Option[Strategy]]
      if(result == None) ret = """{"code":1}"""
      else {
        implicit val formats: Formats = DefaultFormats
        val retStrategy = RetStrategy(0, "获取成功", result)
        val json = Extraction.decompose(retStrategy)
        ret = compact(render(json))
      }

      ret
    } catch {
      case ex: Exception => """{"code":1, "message":"%s"}""".format(ex.getMessage)
    }
  }

  private def _runStrategy(id: Int): String = {
    try {
      //manager ! RunStrategy(id)
      """{"code":0}"""
    } catch {
      case ex: Exception => """{"code":1, "message":"%s"}""".format(ex.getMessage)
    }
  }
  private def _readHistoryStrategy(id: Int): String = {
    "历史信息，等待支持"
  }

  private def _backtestReport(id: Int): String = {
    "回测报告，等待支持"
  }

  private def _createStrategy(json: String): String = {
    // 将JSON转换为strategy，加入到strategy
    implicit val formats = DefaultFormats
    try {
      val jv = parse(json)
      val strategy = jv.extract[Strategy]

      manager ! NewStrategy(strategy)
      """{"code":0}"""
    }catch {
      case ex: Exception => """{"code":1, "message":"%s"}""".format(ex.getMessage)
    }
  }

  private def _updateStrategy(json: String): String = {
    implicit val formats = DefaultFormats
    try {
      val jv = parse(json)
      val strategy = jv.extract[Strategy]

//      strategiesManager.modifyStrategy(strategy)
      manager ! UpdateStrategy(strategy)
      """{"code":0}"""
    }catch {
      case ex: Exception => """{"code":1, "message":"%s"}""".format(ex.getMessage)
    }

  }

  private def _deleteStrategy(id: Int): String = {
    try {
//      strategiesManager.removeStrategy(id)
      manager ! DeleteStrategy(id)
      """{"code":0, "message":"成功删除"}"""
    }catch {
      case ex: Exception => """{"code":1, "message":"%s"}""".format(ex.getMessage)
    }
  }
}
