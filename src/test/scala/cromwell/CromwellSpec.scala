package cromwell

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy.Stop
import akka.testkit._
import com.typesafe.config.ConfigFactory
import cromwell.engine.WorkflowActor
import cromwell.engine.WorkflowActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

object CromwellSpec {
  val config = """
                 |akka {
                 |  loggers = ["akka.testkit.TestEventListener"]
                 |  loglevel = "WARNING"
                 |  test.timefactor = 2
                 |}
               """.stripMargin
}

// Copying from http://doc.akka.io/docs/akka/snapshot/scala/testkit-example.html#testkit-example
class CromwellSpec extends TestKit(ActorSystem("CromwellSpec", ConfigFactory.parseString(CromwellSpec.config)))
with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  val workflowActor = TestActorRef[WorkflowActor]

  override def afterAll() {
    shutdown()
  }

  "A WorkflowActor" should {

    val timeout: FiniteDuration = 500.milliseconds.dilated

    "Construct" in {
      within(timeout) {
        val workflowModel = "construct this"
        workflowActor ! Construct(workflowModel)
        expectMsgPF() {
          case Constructed => ()
        }
      }
    }

    "Start" in {
      within(timeout) {
        workflowActor ! Start
        expectMsgPF() {
          case Started => ()
        }
      }
    }

    "Stop" in {
      within(timeout) {
        val probe = TestProbe()
        probe watch workflowActor
        workflowActor ! Stop
        expectMsgPF() {
          case Stopped => ()
        }
        probe expectTerminated workflowActor
      }
    }
  }

}
