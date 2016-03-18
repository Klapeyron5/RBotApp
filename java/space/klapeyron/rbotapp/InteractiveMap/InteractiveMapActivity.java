package space.klapeyron.rbotapp.InteractiveMap;

import android.app.Activity;
import android.os.Bundle;

public class InteractiveMapActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new InteractiveMapView(this));
    }
}
