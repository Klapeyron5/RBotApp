package space.klapeyron.rbotapp;

import android.content.Context;


public class Robot extends ru.rbot.android.bridge.service.robotcontroll.robots.Robot {
    public int currentDirection;

    public Robot(Context pContext) {
        super(pContext);
    }
}
