package jp.etrobo.ev3.sample;

import jp.etrobo.ev3.sample.Section;

/**
 * @classdoc StrategySelector
 * @author MaxMellon
 * 現在の走行距離，走行状況に応じて，現在の区間を返す．
 * 区間が切り替わった時に，BaseStrategy を 継承した 具体的な戦略を返却する
 */
public class StrategySelector {

    /**
     * 走行距離，ソナーの物体検出フラグから，現在の区間を導く
     * @param distance {int} 距離
     * @param sonar {boolean} ソナー検出フラグ
     * @return section {Section} 区間
     */
    public static Section getCurrentSection() {
        // 難所・階段の コードのため階段を決打ちで返却
        Section section = Section.STAIRS;
        return section;
    }
}
