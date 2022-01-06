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
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private SortedSet<BigInteger> sortedSet = new TreeSet<>();

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {

        return newReceiveBuilder()

                .onMessage(InstructionCommand.class, command -> {

                    if (command.getMessage().equals("start")) {
                        for (int i = 0; i < 20; i++) {
                            ActorRef<WorkerBehavior.Command> childActor = this.getContext().spawn(WorkerBehavior.create(), "worker_" + i);

                            // instead of using strings, I'm using the `Command` custom message object I created for this
                            childActor.tell(new WorkerBehavior.Command("start", this.getContext().getSelf()));
                        }
                    }

                    return this;

                })

                .onMessage(ResultCommand.class, command -> {
                    sortedSet.add(command.getResult());

                    // When finished: print the results altogether
                    if (sortedSet.size() >= 20) sortedSet.forEach(prime -> System.out.println(prime));
                    return this;
                })

                .build();
    }

    // If we take a look at manager, it needs to receive 2 types of messages
    // One is of `String` type -> "start" message from `Main` class
    // Other is the `reply` from worker class which is a `BigInteger`
    // Moreover it needs the `address` of worker actors too
    // So we need 2 different messages but we can only specify one message class in any `Behavior` implementations
    // Solution: have an interface

    public interface Command extends Serializable {
    }

    @Getter
    @AllArgsConstructor
    public static class InstructionCommand implements Command {
        private String message;
    }

    // similar to WorkerBehavior the message is written as a subclass on receiving behavior
    // for example, message of type `ResultCommand` will be sent from `WorkerBehavior` but we're adding it here. Why ?
    // because `ManagerBehavior` is going to receive the message
    @Getter
    @AllArgsConstructor
    public static class ResultCommand implements Command {
        private BigInteger result;
    }
}
