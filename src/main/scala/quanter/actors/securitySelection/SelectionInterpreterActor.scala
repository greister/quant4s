/**
  *
  */
package quanter.actors.securitySelection

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{RoundRobinPool, RoundRobinRouter}
import quanter.actors.zeromq.ZeroMQServerActor
import quanter.rest.FinanceIndi
import quanter.securitySelection.Selector

/**
  * 当接收到字符串的时候，进行分析 解释
  * PE GT 10 LT 15; 财务指标 送到 FinanceIndiActor 进行计算
  */
class SelectionInterpreterActor(cmds: List[FinanceIndi]) extends Actor with ActorLogging{
  var indiCount = cmds.length
  var resultCount = 0
  var result: Selector = null
  val financeIndiRef = context.actorSelection("/user")
  val pubRef = context.actorSelection("/user/" + ZeroMQServerActor.path)
  val finIndiRouter = context.actorOf(RoundRobinPool(5).props(Props[FinanceIndiActor]))

  override def receive: Receive = {
    case r: Selector => {
      // 接收到结果
      resultCount += 1
      if(result == null) result = r
      else result = result.intersect(r)

      if(resultCount == indiCount) // TODO:推送计算结果
        pubRef ! result
    }
  }

  def _parse(): Unit = {
    for(cmd <- cmds) { // TODO: 改成采用 router， 提高并发性
      finIndiRouter ! cmd
    }
  }
}

object SelectionInterpreterActor {
  def props(cmds: List[FinanceIndi]) = {
    Props(classOf[SelectionInterpreterActor], cmds)
  }
}




