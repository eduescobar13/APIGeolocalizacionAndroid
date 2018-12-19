package upm.android.apigeolocalizacion.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

import upm.android.apigeolocalizacion.R;
import upm.android.apigeolocalizacion.fragment.DialogFragmentNotificaciones;

public class Localizacion extends AppCompatActivity implements View.OnClickListener,
                                                               OnSuccessListener<Location> {

    // DECLARACIÓN DE CONSTANTES.
    private static final int INTERVAL_LOCALIZACION = 1000;
    private static final int FASTEST_INTERVAL_LOCALIZACION = 1000;

    // DECLARACIÓN DE ATRIBUTOS.
    private TextView textViewLatitud;
    private TextView textViewLongitud;
    private TextView textViewAltitud;
    private TextView textViewDireccion;
    private Button botonIniciarRecepcion;
    private FusedLocationProviderClient proveedorLocalizacion;
    private LocationRequest solicitudLocalizacion;
    private LocationCallback callbackLocalizacion;
    private Geocoder geocoder;
    private Location localizacionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacion);
        setProveedorLocalizacion(LocationServices.getFusedLocationProviderClient(this));
        setGeocoder(new Geocoder(getApplicationContext()));
        inicializarComponentes();
        asignarEventos();
        solicitarPermisosGeolocalizacion();
    }

    private void inicializarComponentes() {
        setTextViewLatitud((TextView) findViewById(R.id.text_view_latitud));
        setTextViewLongitud((TextView) findViewById(R.id.text_view_longitud));
        setTextViewAltitud((TextView) findViewById(R.id.text_view_altitud));
        setTextViewDireccion((TextView) findViewById(R.id.text_view_direccion));
        setBotonIniciarRecepcion((Button) findViewById(R.id.boton_iniciar_recepcion));
    }

    private void asignarEventos() {
        getBotonIniciarRecepcion().setOnClickListener(this);
    }

    /**
     * Método para crear nuestra solicitud de localización, estableciendo todos los parámetros.
     */
    protected void crearSolicitudLocalizacion() {
        setSolicitudLocalizacion(LocationRequest.create());
        getSolicitudLocalizacion().setInterval(INTERVAL_LOCALIZACION);
        getSolicitudLocalizacion().setFastestInterval(FASTEST_INTERVAL_LOCALIZACION);
        getSolicitudLocalizacion().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Método para inicializar el atributo CallbackLocalizacion.
     * Creamos una nueva LocationCallback, en la que describimos que hacer con los resultados de la solicitud
     * de localización. Almacenamos la dirección devuelta y mostramos los datos en pantalla.
     */
    private void inicializarCallbackLocalizacion() {
        setCallbackLocalizacion(new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location: locationResult.getLocations()) {
                        try {
                            setLocalizacionActual(location);
                            mostrarDatosLocalizacion();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Método para iniciar la recepción de las localizaciones.
     * Debemos comprobar siempre antes que se han concedido los permisos.
     */
    private void iniciarRecepcionLocalizacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(Localizacion.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getProveedorLocalizacion().requestLocationUpdates(getSolicitudLocalizacion(), getCallbackLocalizacion(), null);
                Toast.makeText(this, "Recepción Iniciada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método para mostrar en pantalla los datos de la localización.
     */
    private void mostrarDatosLocalizacion() throws IOException {
        String latitud = String.valueOf(getLocalizacionActual().getLatitude());
        String longitud = String.valueOf(getLocalizacionActual().getLongitude());
        String altitud = String.valueOf(getLocalizacionActual().getAltitude());
        String direccion = obtenerDireccion();
        getTextViewLatitud().setText(latitud);
        getTextViewLongitud().setText(longitud);
        getTextViewAltitud().setText(altitud);
        getTextViewDireccion().setText(direccion);
    }

    /**
     * Función que obtiene la dirección a partir de las coordenadas almacenadas en el atributo localizacionActual.
     */
    private String obtenerDireccion() throws IOException {
        List<Address> listLocalizaciones = getGeocoder().getFromLocation(getLocalizacionActual().getLatitude(), getLocalizacionActual().getLongitude(), 1);
        String direccion = null;
        if (listLocalizaciones.size() > 0) {
            Address localizacion = listLocalizaciones.get(0);
            direccion = localizacion.getAddressLine(0);
        }
        else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
        }
        return direccion;
    }

    public TextView getTextViewLatitud() {
        return textViewLatitud;
    }

    public void setTextViewLatitud(TextView textViewLatitud) {
        this.textViewLatitud = textViewLatitud;
    }

    public TextView getTextViewLongitud() {
        return textViewLongitud;
    }

    public void setTextViewLongitud(TextView textViewLongitud) {
        this.textViewLongitud = textViewLongitud;
    }

    public TextView getTextViewAltitud() {
        return textViewAltitud;
    }

    public void setTextViewAltitud(TextView textViewAltitud) {
        this.textViewAltitud = textViewAltitud;
    }

    public TextView getTextViewDireccion() {
        return textViewDireccion;
    }

    public void setTextViewDireccion(TextView textViewDireccion) {
        this.textViewDireccion = textViewDireccion;
    }

    public Button getBotonIniciarRecepcion() {
        return botonIniciarRecepcion;
    }

    public void setBotonIniciarRecepcion(Button botonIniciarRecepcion) {
        this.botonIniciarRecepcion = botonIniciarRecepcion;
    }

    public FusedLocationProviderClient getProveedorLocalizacion() {
        return proveedorLocalizacion;
    }

    public void setProveedorLocalizacion(FusedLocationProviderClient proveedorLocalizacion) {
        this.proveedorLocalizacion = proveedorLocalizacion;
    }

    public LocationRequest getSolicitudLocalizacion() {
        return solicitudLocalizacion;
    }

    public void setSolicitudLocalizacion(LocationRequest solicitudLocalizacion) {
        this.solicitudLocalizacion = solicitudLocalizacion;
    }

    public LocationCallback getCallbackLocalizacion() {
        return callbackLocalizacion;
    }

    public void setCallbackLocalizacion(LocationCallback callbackLocalizacion) {
        this.callbackLocalizacion = callbackLocalizacion;
    }

    public Geocoder getGeocoder() {
        return geocoder;
    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public Location getLocalizacionActual() {
        return localizacionActual;
    }

    public void setLocalizacionActual(Location localizacionActual) {
        this.localizacionActual = localizacionActual;
    }

    /**
     * Método para solicitar los permisos de geolocalización al usuario.
     * Si nuestro dispositivo móvil está en un dispositivo con API 23 o superior (Build.VERSION_CODES.M)
     * solicitamos los permisos al usuario mediante el uso de checkSelfPermission y requestPermission.
     * Si la API es inferior, ejecutamos la acción, ya que los permisos han sido dados en el momento de
     * la instalación de la aplicación en el dispositivo.
     */
    private void solicitarPermisosGeolocalizacion() {
        String[] permisos = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permisos[0]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permisos, Mapas.CODIGO_SOLICITUD_PERMISOS_GEOLOCALIZACION);
            }
            else {
                getProveedorLocalizacion().getLastLocation().addOnSuccessListener(this);
            }
        }
        else {
            getProveedorLocalizacion().getLastLocation().addOnSuccessListener(this);
        }
    }

    /**
     * Método onRequestPermissionResult para obtener la respuesta del usuario a la solicitud de los permisos.
     * Si el usuario ha aceptado los permisos llamamos al método solicitarPermisosGeolocalización, ya con
     * los permisos concedidos y ejecutándose getMapa().setMyLocationEnabled(true). Si no los acepta, mostramos
     * un dialog fragment para notificar la necesidad de tener los permisos de localización.
     */
    @Override
    public void onRequestPermissionsResult(int respuestaSolicitud, @NonNull String[] permisos, @NonNull int[] resultados) {
        if (respuestaSolicitud == Mapas.CODIGO_SOLICITUD_PERMISOS_GEOLOCALIZACION) {
            if ((resultados.length > 0) && (resultados[0] == PackageManager.PERMISSION_GRANTED)) { // Permisos concedidos.
                solicitarPermisosGeolocalizacion();
            }
            else {
                final Bundle argumentos = new Bundle(1);
                argumentos.putString(Mapas.MENSAJE_NOTIFICACION, getResources().getString(R.string.mensaje_permisos_geolocalizacion_denegados));
                DialogFragmentNotificaciones dialogNotificaciones = new DialogFragmentNotificaciones();
                dialogNotificaciones.setArguments(argumentos);
                dialogNotificaciones.show(getSupportFragmentManager(), Mapas.TAG_DIALOG_FRAGMENT_NOTIFICACIONES);
            }
        }
    }

    /**
     * Método onSuccess llamado cuando los permisos han sido concedidos y queremos obtener la última localización
     * conocida del dispositivo.
     * @param location
     */
    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            setLocalizacionActual(location);
            try {
                mostrarDatosLocalizacion();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View vista) {
        if (vista.getId() == R.id.boton_iniciar_recepcion) {
            crearSolicitudLocalizacion();
            inicializarCallbackLocalizacion();
            iniciarRecepcionLocalizacion();
        }
    }
}
