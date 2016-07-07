package jp.etrobo.ev3.sample;

import lejos.hardware.lcd.LCD;

public final class StrategyMode {
	private static Mode mode;
    private static EV3way body;

    StrategyMode(){
    }

    /**
     * bodyの格納
     * サンプルコードを崩さないための処置
     */
    public static void initSetBody(EV3way b){
    	body = b;
    }

    /**
     * デバッグ用
     * bodyが格納されているかをチェック
     */
    public static void printBody(){
		LCD.clear();
		if(body == null){
			LCD.drawString("no", 0, 0);
		} else {
			LCD.drawString("yes", 0, 0);
		}
    }

    /**
     * ドライブモード(ライントレース)に変更
     *
     */
	public static void setDriveMode() {
		mode = new DriveMode(body);
	}

    /**
     * ルックアップゲートモードに変更
     *
     */
	public static void setLookupMode() {
		mode = new LookupMode(body);
	}

    /**
     * 戦略の実行
     *
     */
	public static void strategyRun(){
		mode.strategyRun();
	}
}
