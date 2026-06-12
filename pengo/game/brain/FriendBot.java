package game.brain;

import java.util.Random;

import engine.brain.Bot;
import engine.model.BasicStunt;
import game.model.Friend;

public class FriendBot extends Bot {

    private Friend friend;
    private Random random;

    public FriendBot(BasicStunt stunt, Friend friend) {
        super(stunt);

        this.friend = friend;
        this.random = new Random();
    }

    @Override
    public void think() {

        if (stunt.isMoving())
            return;

        int direction = random.nextInt(4);

        switch(direction) {

        case 0:
            stunt.walk(BasicStunt.NORTH);
            break;

        case 1:
            stunt.walk(BasicStunt.SOUTH);
            break;

        case 2:
            stunt.walk(BasicStunt.EAST);
            break;

        case 3:
            stunt.walk(BasicStunt.WEST);
            break;
        }
    }

    @Override
    public void done() {
    }

    @Override
    public void collision(engine.model.Entity other) {
    }

    @Override
    public void collision(java.util.List<engine.model.Entity> others) {
    }
}