package learn.tutorial._3_big_prime_example;

import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {

        ActorSystem<ManagerBehavior.Command> manager = ActorSystem.create(ManagerBehavior.create(), "manager");
        manager.tell(new ManagerBehavior.InstructionCommand("start"));
    }
}
