package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

public class ViewImagesActivity extends AppCompatActivity {

    //Constante de localización de archivos
    private static final File BUCKETS_PATH =
            new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buckets");

    //Variables para ejecución
    //Constantes
    private static final String TITLE_PIXELS = "Pixels: ";
    private static final String TITLE_LBP = "LBP";
    //Data
    private String HASH_NAME_COMPLETE;
    private int METHOD_NAME;
    private ArrayList<String> imagesOfBucket;
    //Ui
    private ListView listViewImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);
        initComponents();
        attendIntent();
        this.setTitle(generateTitle());

        CustomAdapter adapter = new CustomAdapter(
                imagesOfBucket, getApplicationContext(), BUCKETS_PATH);
        listViewImages.setAdapter(adapter);
    }

    /**
     * Atiende el Intent que la activa, pues esta actividad es solo creada por Intents
     */
    private void attendIntent() {
        Intent intent = getIntent();
        HASH_NAME_COMPLETE = intent.getStringExtra(MainActivity.EXTRA_HASH_SELECTED);
        METHOD_NAME = intent.getIntExtra(MainActivity.EXTRA_TYPE_SELECTED, 0);
        String IMAGES = intent.getStringExtra(MainActivity.EXTRA_IMAGES);
        imagesOfBucket = new ArrayList<>();
        String[] images = IMAGES.split(";");
        Collections.addAll(imagesOfBucket, images);
    }

    /**
     * En base a la información del intent que la lanzó, genera el título para la ventana
     * @return String, el título generado
     */
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

    /**
     * Inicia los componentes gráficos
     */
    private void initComponents() {
        listViewImages = (ListView) findViewById(R.id.listView_images);
    }

}
