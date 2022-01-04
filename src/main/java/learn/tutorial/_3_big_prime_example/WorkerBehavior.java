package learn.tutorial._3_big_prime_example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {

        return newReceiveBuilder()

                .onAnyMessage(message -> {
                    // because this is a custom message object and not a string,
                    // we cannot use `.onMessageEquals()`

                    if (message.getMessage().equals("start")) {
                        BigInteger integer = new BigInteger(3000, new Random());
                        BigInteger result = integer.nextProbablePrime();

                        // worker returns result to manager
                        // its all SAME method
                        message.getSender().tell(new ManagerBehavior.ResultCommand(result));
                    }
                    return this;
                })

                .build();
    }

    // In first example, we used String as a message type. But what if we want to send multiple forms of data in a single message
    // This is where custom messages' comes in: we create them ourselves
    // Any class can be used as a message if it is serializable
    // Hence we can create a message class referred to as `Command` in Akka terms
    // By convention, this class is usually created into the recipient behavior class -> in this case `WorkerBehavior`
    @Getter
    @AllArgsConstructor
    public static class Command implements Serializable {
        // we need to make this static so this is accessible to other classes in the public

        String message;

        // The sender (which is the actor that implements `ManagerBehavior`) sends ManagerBehavior.Command type messages
        // So here the type of sender is still `String`
        ActorRef<ManagerBehavior.Command> sender;

        // no need for setters

    }
}
