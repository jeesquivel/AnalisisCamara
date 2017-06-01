package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ViewImagesActivity extends AppCompatActivity {

    //Variables para ejecución
    //Constantes
    private static final String TITLE_PIXELS = "Pixels: ";
    private static final String TITLE_LBP = "LBP";
    //Data
    private String HASH_NAME_COMPLETE;
    private String IMAGES;
    private int METHOD_NAME;
    //Ui
    private TextView textViewHashName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);
        initComponents();
        attendIntent();
    }

    private void attendIntent() {
        Intent intent = getIntent();
        HASH_NAME_COMPLETE = intent.getStringExtra(MainActivity.EXTRA_HASH_SELECTED);
        METHOD_NAME = intent.getIntExtra(MainActivity.EXTRA_TYPE_SELECTED, 0);
        IMAGES = intent.getStringExtra(MainActivity.EXTRA_IMAGES);
        configUI();
    }

    private void configUI() {
        textViewHashName.setText(generateTitle());
    }

    private String generateTitle() {
        String title = "";
        if (METHOD_NAME == MainActivity.PIXEL_SPINNER_ID)
            title += TITLE_PIXELS;
        if (METHOD_NAME == MainActivity.LBP_SPINNER_ID)
            title += TITLE_LBP;
        String[] forSplit = HASH_NAME_COMPLETE.split(",");
        String HASH_NAME;
        if (forSplit.length > 1) {
            HASH_NAME = forSplit[1];
        } else {
            HASH_NAME = forSplit[0];
        }
        title += HASH_NAME;
        return title;
    }

    private void initComponents() {
        textViewHashName = (TextView) findViewById(R.id.textViewHashName);
    }

    public void editName(View view) {

    }
}
