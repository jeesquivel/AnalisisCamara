package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

class CustomAdapter extends ArrayAdapter<String> {

    //Directorio raíz de los buckets
    private File BUCKETS_PATH;

    //Contexto de la actividad que la lanza
    private Context mContext;

    //Última posición obtenida
    private int lastPosition = -1;

    //Holder, funciona como CACHE, para no estar recargando en xml con el layout cada vez
    private static class ViewHolder {
        ImageView imageView_image;
    }

    /**
     * Builder de la clase, instancia un nuevo objeto de tipo CustomAdapter
     *
     * @param data         Data que define la lista
     * @param context      Contexto que lanza la lista
     * @param buckets_path Directorio raíz de los buckets
     */
    CustomAdapter(ArrayList<String> data, Context context, File buckets_path) {
        super(context, R.layout.row_item, data);
        BUCKETS_PATH = buckets_path;
        mContext = context;
    }

    /**
     * Obtener cada view de cada item del listview
     *
     * @param position Position actual de view que se desea obtener
     * @param convertView View actual, puede ser null, se instancia nueva, o re utilizar otra, HOLDER con el chache
     * @param parent Grupo al que pertenece
     * @return View, la view creada con la información provenitente de la data
     */
    @Override
    public @NonNull View getView(int position,
                                 @Nullable View convertView, @NonNull ViewGroup parent) {

        String imageName = getItem(position);

        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.imageView_image = (ImageView) convertView.findViewById(R.id.rowItem_IM);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        File imageFile = new File(BUCKETS_PATH, imageName);

        Uri uri = Uri.fromFile(imageFile);

        viewHolder.imageView_image.setImageURI(uri);

        return convertView;
    }

}
