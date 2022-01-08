package learn.tutorial._9_akka_blockchain_example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import learn.tutorial._6_blockchain_core.Block;
import learn.tutorial._6_blockchain_core.HashResult;
import learn.tutorial._6_blockchain_core.utils.BlocksData;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.List;

public class MinerBehaviorTest {

    /**
     * @see MinerBehavior#mine()
     */
    @Test
    public void testMiningFailIfNonceNotInRange() {

        // This is going to be synchronous testing -> we are sending a message and expecting a reply

        // NOTE: This test is designed to fail

        BehaviorTestKit<Message> testActor = BehaviorTestKit.create(MinerBehavior.create());

        // prepare the data -> this is not what we do in real cases
        // In real cases we have a dummy dataset -> but simplicity I'm re-using the BlocksData dataset for testing
        Block block = BlocksData.getNextBlock(0, "0");
        Message message = new MinerBehavior.Mine(block, 0, 5, null);

        // Now send this message into the test actor and see how it behaves -> we use `run` instead of `tell` -> that's for real actors
        testActor.run(message);

        // we retrieve the logs and check whether they match
        List<CapturedLogEvent> logs = testActor.getAllLogEntries();


        // if failure, we will have only 1 log message
        assertEquals(logs.size(), 1);
        // but this is not enough -> 1 log for failure too
        // so second check is to retrieve the log and match it
        assertEquals(logs.get(0).message(), LogMessages.FAILED_TO_FIND_HASH.getCorrespondingString());
        assertEquals(logs.get(0).level(), Level.DEBUG);

    }

    @Test
    public void testMiningPassIfNonceInRange() {

        // This is the mock actor which is going to send messages
        BehaviorTestKit<Message> testActor = BehaviorTestKit.create(MinerBehavior.create());

        // This is mock actor for receiving messages
        TestInbox<Message> testInbox = TestInbox.create();

        Block block = BlocksData.getNextBlock(0, "0");
        Message message = new MinerBehavior.Mine(block, 82700, 5, testInbox.getRef());

        testActor.run(message);

        HashResult expectedHashResult = new HashResult();
        expectedHashResult.foundAHash("0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b", 82741);

        Message hashMessage = new ControllerBehavior.HashResultMessage(testActor.getRef(), expectedHashResult);

        // 2 assertions
        assertTrue(testInbox.hasMessages());
        testInbox.expectMessage(hashMessage);
    }
}
