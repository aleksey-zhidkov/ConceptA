package lxx.services;

import lxx.BattleConstants;
import lxx.events.WavePassedEvent;
import lxx.model.BattleModel;
import lxx.model.BattleModelListener;
import lxx.model.CaRobot;
import lxx.model.Wave;
import lxx.paint.Canvas;
import lxx.paint.Circle;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Aleksey Zhidkov
 * Date: 28.06.12
 */
public class WavesService implements BattleModelListener {

    public static final Color MY_WAVE_COLOR = new Color(0, 150, 255, 135);
    public static final Color ENEMY_WAVE_COLOR = new Color(255, 50, 0, 135);
    public static final Color TEAMMATE_WAVE_COLOR = new Color(100, 255, 0, 135);
    private LinkedList<W> waves = new LinkedList<W>();

    public Wave launchWave(BattleModel fireTimeState, CaRobot owner, double speed, WaveCallback waveCallback, CaRobot... targets) {
        final Wave w = new Wave(fireTimeState, owner, speed, targets);
        waves.add(new W(w, waveCallback));

        return w;
    }

    @Override
    public void battleModelUpdated(BattleModel newState) {
        for (Iterator<W> iter = waves.iterator(); iter.hasNext();) {
            final W w = iter.next();
            final List<WavePassedEvent> wavePassedEvents = w.w.check(newState);
            for (WavePassedEvent e : wavePassedEvents) {
                for (WaveCallback wc : w.wcs) {
                    wc.wavePassed(w.w, e.passedRobot, e.hitInterval);
                }
            }
            if (Canvas.WAVES.enabled()) {
                final Color c;
                if (w.w.owner.getName().equals(BattleConstants.myName)) {
                    c = MY_WAVE_COLOR;
                } else if (BattleConstants.isTeammate(w.w.owner.getName())) {
                    c = TEAMMATE_WAVE_COLOR;
                } else {
                    c = ENEMY_WAVE_COLOR;
                }
                Canvas.WAVES.draw(new Circle(w.w.startPos, w.w.getTravelledDistance()), c);
            }

            if (!w.w.hasRemainingTargets()) {
                iter.remove();
            }
        }
    }

    private final class W {

        public final List<WaveCallback> wcs = new LinkedList<WaveCallback>();
        public final Wave w;

        private W(Wave w, WaveCallback wc) {
            this.w = w;
            this.wcs.add(wc);
        }
    }

}
