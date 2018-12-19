package upm.android.apigeolocalizacion.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import upm.android.apigeolocalizacion.activity.Mapas;

public class DialogFragmentNotificaciones extends DialogFragment {

    // DECLARACIÓN DE CONSTANTES.
    final static String TITULO = "ApiGeolocalización";
    final static String ENTENDIDO = "Entendido";

    // DECLARACIÓN DE ATRIBUTOS.
    private String mensajeNotificacion;

    public DialogFragmentNotificaciones() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMensajeNotificacion((String) getArguments().getString(Mapas.MENSAJE_NOTIFICACION));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(TITULO)
               .setMessage(getMensajeNotificacion())
               .setPositiveButton(ENTENDIDO, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                   }
               });
        return builder.create();
    }

    public String getMensajeNotificacion() {
        return mensajeNotificacion;
    }

    public void setMensajeNotificacion(String mensajeNotificacion) {
        this.mensajeNotificacion = mensajeNotificacion;
    }
}


