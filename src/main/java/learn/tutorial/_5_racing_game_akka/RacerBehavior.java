package learn.tutorial._5_racing_game_akka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Random;

public class RacerBehavior extends AbstractBehavior<CommonCommand> {

    private final int averageSpeedAdjustmentFactor;
    private final Random random;
    private int raceLength;
    private double currentSpeed = 0;
    private double currentPosition = 0;

    private RacerBehavior(ActorContext<CommonCommand> context) {

        super(context);

        random = new Random();
        averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
    }

    public static Behavior<CommonCommand> create() {
        return Behaviors.setup(RacerBehavior::new);
    }

    @Override
    public Receive<CommonCommand> createReceive() {
        return newReceiveBuilder()

                .onMessage(RaceLengthCommand.class, message -> {
                    this.raceLength = message.getRaceLength();
                    return this;
                })

                .onMessage(AskPosition.class, message -> {

                    if (currentPosition < raceLength) {

                        determineNextSpeed();
                        currentPosition += getDistanceMovedPerSecond();

                        if (currentPosition > raceLength) currentPosition = raceLength;

                        // send current position to monitor
                        message.getSender().tell(new MonitorBehavior.PositionCommand(this.getContext().getSelf(), currentPosition));

                    } else {

                        // send current time to sender
                        message.getSender().tell(new MonitorBehavior.ResultCommand(this.getContext().getSelf(), System.currentTimeMillis()));
                    }

                    return this;

                })

                .build();
    }

    private double getMaxSpeed() {
        final double defaultAverageSpeed = 48.2;
        return defaultAverageSpeed * (1 + ((double) averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed() {
        if (currentPosition < (raceLength / 4.0)) {
            currentSpeed = currentSpeed + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
        } else {
            currentSpeed = currentSpeed * (0.5 + random.nextDouble());
        }

        if (currentSpeed > getMaxSpeed())
            currentSpeed = getMaxSpeed();

        if (currentSpeed < 5)
            currentSpeed = 5;

        if (currentPosition > (raceLength / 2.0) && currentSpeed < getMaxSpeed() / 2) {
            currentSpeed = getMaxSpeed() / 2;
        }
    }

    @AllArgsConstructor
    @Getter
    public abstract static class Command implements CommonCommand {
        private ActorRef<CommonCommand> sender;
    }

    @Getter
    public static class AskPosition extends Command {

        public AskPosition(ActorRef<CommonCommand> sender) {
            super(sender);
        }
    }

    @Getter
    public static class RaceLengthCommand extends Command {
        private final int raceLength;

        public RaceLengthCommand(ActorRef<CommonCommand> sender, int raceLength) {
            super(sender);
            this.raceLength = raceLength;
        }
    }
}
