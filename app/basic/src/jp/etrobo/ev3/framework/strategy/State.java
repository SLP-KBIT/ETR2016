package jp.etrobo.ev3.framework.strategy;

public enum State {
    NOT_SELECTED, // 選択されていない
    READY,        // 待機中
    BASIC,        // 標準ライントレース中（BasicStrategy専用)
    SINGLE,       // シングル 攻略中
    DOUBLE,       // ダブル  攻略中
    CLEAR,        // クリア
    FAIL,         // 失敗
    EXCEPTION,    // 例外
}
