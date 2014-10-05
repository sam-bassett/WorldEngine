package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A control class for WorldEngine
 *
 * Created by sam on 5/10/2014.
 */
public class GameController implements KeyListener {
    private Camera c;
    private boolean[] keyStates;

    public GameController(Camera c) {
        this.c = c;
        keyStates = new boolean[256];
    }

    public void update() {
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
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyStates[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyStates[e.getKeyCode()] = false;
    }
}
