package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A control class for WorldEngine
 *
 * Created by sam on 5/10/2014.
 */
public class GameController implements KeyListener {
    private boolean[] keyStates;

    public GameController() {
        keyStates = new boolean[256];
    }

    public void update(Camera c, Terrain t) {
        // TODO think about scaling this, currently @ 60fps it's very fast
        if (keyStates[KeyEvent.VK_UP])
            c.forwardStep(0.1);
        if (keyStates[KeyEvent.VK_DOWN])
            c.backStep(0.1);
        if (keyStates[KeyEvent.VK_LEFT])
            c.rotate(-5);
        if (keyStates[KeyEvent.VK_RIGHT])
            c.rotate(5);
        if (keyStates[KeyEvent.VK_W])
            c.forwardStep(0.1);
        if (keyStates[KeyEvent.VK_S])
            c.backStep(0.1);
        if (keyStates[KeyEvent.VK_A])
            c.sideStep(0.1);
        if (keyStates[KeyEvent.VK_D])
            c.sideStep(-0.1);
        if (keyStates[KeyEvent.VK_ESCAPE])
            System.exit(0);
        if (keyStates[KeyEvent.VK_OPEN_BRACKET])
            t.tickClock(-0.2);
        if (keyStates[KeyEvent.VK_CLOSE_BRACKET])
            t.tickClock(0.2);
        if (keyStates[KeyEvent.VK_N]) {
            t.isNight = !t.isNight;
            keyStates[KeyEvent.VK_N] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_N && keyStates[KeyEvent.VK_N]) {
            // Stop key cycling too fast
            System.out.println("Repeat detected");
            keyStates[KeyEvent.VK_N] = false;
        } else {
            keyStates[e.getKeyCode()] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyStates[e.getKeyCode()] = false;
    }
}
