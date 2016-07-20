package jp.etrobo.ev3.sample;

public class LookupMode implements Mode {

	private EV3way body;

	LookupMode(EV3way body){
		this.body = body;
	}

	public void strategyRun(){
		body.controlLookup();
	}
}
