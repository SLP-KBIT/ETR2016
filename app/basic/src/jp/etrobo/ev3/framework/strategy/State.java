package jp.etrobo.ev3.framework.strategy;

public enum State {
    NOT_SELECTED, // 選択されていない
    WAIT_TRIGGER, // 障害物検知待ち
    SINGLE,       // シングル 攻略中
    DOUBLE,       // ダブル  攻略中
    CLEAR,        // クリア
    FAIL,         // 失敗
    EXCEPTION,    // 例外
}
