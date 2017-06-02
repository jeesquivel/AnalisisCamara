package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

class LocalitySensitiveHashingHandler {

    //Data
    private LocalitySensitiveHashingPixels hashForPixels;
    private LocalitySensitiveHashingLBP hashForLBP;

    /**
     * Constructor de la clase, holder de otras dos clases, se encarga de instanciar ambas
     *
     * @param context             Context de la application que crea esta clase
     * @param cantidadHiperplanos Cantidad de hiperplanos que trabajaran las calses
     */
    LocalitySensitiveHashingHandler(Context context, int cantidadHiperplanos) {
        hashForPixels = new LocalitySensitiveHashingPixels(context, cantidadHiperplanos);
        hashForLBP = new LocalitySensitiveHashingLBP(context, cantidadHiperplanos);
    }

    /**
     * Retorna las imágenes dado un hash
     *
     * @param item String con el nombre del hash que se desea obtener las imágenes
     * @param type Lo necesita el método píxeles o LBP?
     * @return String, todas las imágenes de un hash separadas por un cracter ';'
     */
    String getImagesFromType(String item, int type) {
        if (type == MainActivity.PIXEL_SPINNER_ID)
            return hashForPixels.getImagesOfHash(item);
        else
            return hashForLBP.getImagesOfHash(item);
    }

    /**
     * Cambiar el nombre de un hash, para un método dado
     *
     * @param hash    Hash al que se le cambiará el nombre
     * @param newName Nuevo nombre para el hash
     * @param type    Lo necesita el método píxeles o LBP?
     */
    void nameHash(String hash, String newName, int type) {
        if (type == MainActivity.PIXEL_SPINNER_ID)
            hashForPixels.nameHash(hash, newName);
        else
            hashForLBP.nameHash(hash, newName);
    }

    /**
     * Dado el método requerido, retorna todos los nombres de hashes para ese método en concreto
     *
     * @param type Lo necesita el método píxeles o LBP?
     * @return ArrayList<String> Todos los nombres de bucketes para un método dado
     */
    ArrayList<String> getNames(int type) {
        if (type == MainActivity.PIXEL_SPINNER_ID)
            return hashForPixels.getNames();
        else
            return hashForLBP.getNames();
    }

    /**
     * Envía a prccesar la imagen con ambos métodos
     *
     * @param currentPhotoPath dirección de la imagen
     */
    void processImage(String currentPhotoPath) {
        hashForLBP.processImage(currentPhotoPath);
        hashForPixels.processImage(currentPhotoPath);
    }
}
