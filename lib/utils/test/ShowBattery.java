package test;

import lejos.hardware.Battery;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

public class ShowBattery {

    public static void main(String[] args) {
        while( ! Button.ESCAPE.isDown() ) {
            LCD.clear();
            LCD.drawString("Voltage : " + Battery.getVoltageMilliVolt(), 0, 0);
            LCD.drawString("Battery : " + Battery.getBatteryCurrent(), 0, 1);
            LCD.drawString("Motor : " + Battery.getMotorCurrent(), 0, 2);
            LCD.refresh();
        }
        LCD.clear();
        LCD.refresh();
    }
}
