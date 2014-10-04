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

    public GameController(Camera c) {
        this.c = c;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                c.forwardStep(0.1);
                break;
            case KeyEvent.VK_DOWN:
                c.backStep(0.1);
                break;
            case KeyEvent.VK_LEFT:
                c.rotate(-10);
                break;
            case KeyEvent.VK_RIGHT:
                c.rotate(10);
                break;
            case KeyEvent.VK_A:
                c.sideStep(0.1);
                break;
            case KeyEvent.VK_D:
                c.sideStep(-0.1);
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
