package upm.android.apigeolocalizacion.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import upm.android.apigeolocalizacion.R;
import upm.android.apigeolocalizacion.fragment.DialogFragmentNotificaciones;

public class Mapas extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
                                                        GoogleMap.OnMyLocationClickListener,
                                                        OnMapReadyCallback,
                                                        View.OnKeyListener,
                                                        View.OnCreateContextMenuListener {

    // DECLARACIÓN DE CONSTANTES.
    public static final String MENSAJE_NOTIFICACION = "notificacion";
    public final static String TAG_DIALOG_FRAGMENT_NOTIFICACIONES = "DialogFragmentNotificaciones";
    public static final int CODIGO_SOLICITUD_PERMISOS_GEOLOCALIZACION = 111;
    private static final String DIRECCION_VACIA = "";

    // DECLARACIÓN DE ATRIBUTOS.
    private GoogleMap mapa;
    private SupportMapFragment fragmentMapa;
    private Geocoder geocoder;
    private Toolbar toolbar;
    private LatLng direccionBuscada;
    private EditText editTextDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);
        setFragmentMapa((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_mapa));
        setGeocoder(new Geocoder(getApplicationContext()));
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        setSupportActionBar(getToolbar());
        setEditTextDireccion((EditText) findViewById(R.id.edit_text_direccion));
        getFragmentMapa().getMapAsync(this);
        desplazarBotonLocalizacion();
        getEditTextDireccion().setImeActionLabel(getResources().getString(R.string.key_text_direccion), KeyEvent.KEYCODE_ENTER);
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
                requestPermissions(permisos, CODIGO_SOLICITUD_PERMISOS_GEOLOCALIZACION);
            }
            else {
                getMapa().setMyLocationEnabled(true);
            }
        }
        else {
            getMapa().setMyLocationEnabled(true);
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
        if (respuestaSolicitud == CODIGO_SOLICITUD_PERMISOS_GEOLOCALIZACION) {
            if ((resultados.length > 0) && (resultados[0] == PackageManager.PERMISSION_GRANTED)) { // Permisos concedidos.
                activarMiLocalizacion();
            }
            else {
                final Bundle argumentos = new Bundle(1);
                argumentos.putString(MENSAJE_NOTIFICACION, getResources().getString(R.string.mensaje_permisos_geolocalizacion_denegados));
                DialogFragmentNotificaciones dialogNotificaciones = new DialogFragmentNotificaciones();
                dialogNotificaciones.setArguments(argumentos);
                dialogNotificaciones.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT_NOTIFICACIONES);
            }
        }
    }

    /**
     * Método para asignar todos los eventos de la aplicación.
     */
    private void asignarEventos() {
        getMapa().setOnMyLocationButtonClickListener(this);
        getMapa().setOnMyLocationClickListener(this);
        getEditTextDireccion().setOnKeyListener(this);
    }

    /**
     * Método para activar la geolocalización mediante la solicitud de los permisos.
     */
    private void activarMiLocalizacion() {
        if (getMapa() != null) {
            solicitarPermisosGeolocalizacion();
        }
    }

    /**
     * Método para obtener una coordenadas a través de una dirección.
     * @param direccion String con la dirección a buscar.
     * Mediante el uso del atributo geoCoder, transformamos la dirección escrita en unas coordenadas. El método
     * getFromLocationName() nos devuelve una lista de Address con todos los posibles resultados. Presuponemos
     * que el objeto Address de la primera posición almacenará la que buscamos, siendo esta la que transformamos
     * en unas coordenadas para ser mostradas en el mapa.
     */
    private LatLng obtenerCoordenadas(String direccion) throws IOException {
        LatLng coordenadas = null;
        List<Address> listLocalizaciones = getGeocoder().getFromLocationName(direccion, 1);
        if (listLocalizaciones.size() > 0) {
            Address localizacion = listLocalizaciones.get(0);
            double latitud = localizacion.getLatitude();
            double longitud = localizacion.getLongitude();
            coordenadas = new LatLng(latitud, longitud);
        }
        else {
            Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
        }
        return coordenadas;
    }

    /**
     * Método que muestra una dirección en el mapa.
     * @param direccion Dirección a buscar.
     * Obtenemos las coordenadas mediante el método obtenerCoordenadas. Si las coordenadas no son iguales a null,
     * creamos una instancia de tipo CameraUpdate y se la pasamos al método moveCamera() para realizar un movimiento
     * en el mapa de la aplicación, mostrándose en el centro la ubicación buscada.
     */
    private void buscarDireccion(String direccion) throws IOException {
        LatLng coordenadas = obtenerCoordenadas(direccion);
        if (coordenadas != null) {
            setDireccionBuscada(coordenadas);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordenadas, 17);
            getMapa().moveCamera(cameraUpdate);
            ocultarTeclado(); // Ocultamos el teclado al mostrar la dirección buscada.
        }
    }

    /**
     * Método para ocultar el teclado virtual de la pantalla.
     */
    private void ocultarTeclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Método para situar el botón de obtener mi localización en la esquina inferior derecha del mapa.
     */
    private void desplazarBotonLocalizacion() {
        View botonLocalizacion = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) botonLocalizacion.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 0, 40);
    }

    public GoogleMap getMapa() {
        return mapa;
    }

    public void setMapa(GoogleMap mapa) {
        this.mapa = mapa;
    }

    public SupportMapFragment getFragmentMapa() {
        return fragmentMapa;
    }

    public void setFragmentMapa(SupportMapFragment fragmentMapa) {
        this.fragmentMapa = fragmentMapa;
    }

    public Geocoder getGeocoder() {
        return geocoder;
    }

    public void setGeocoder(Geocoder geocoder) {
        this.geocoder = geocoder;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    public EditText getEditTextDireccion() {
        return editTextDireccion;
    }

    public void setEditTextDireccion(EditText editTextDireccion) {
        this.editTextDireccion = editTextDireccion;
    }

    public LatLng getDireccionBuscada() {
        return direccionBuscada;
    }

    public void setDireccionBuscada(LatLng direccionBuscada) {
        this.direccionBuscada = direccionBuscada;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setMapa(googleMap);
        asignarEventos();
        activarMiLocalizacion();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        String mensaje = "Localización actual: " + location.getLatitude() + ", " + location.getLongitude();
        final Bundle argumentos = new Bundle(1);
        argumentos.putString(MENSAJE_NOTIFICACION, mensaje);
        DialogFragmentNotificaciones dialogNotificaciones = new DialogFragmentNotificaciones();
        dialogNotificaciones.setArguments(argumentos);
        dialogNotificaciones.show(getSupportFragmentManager(), TAG_DIALOG_FRAGMENT_NOTIFICACIONES);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (view.getId() == getEditTextDireccion().getId()) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String direccion = getEditTextDireccion().getText().toString();
                if (!direccion.equals(DIRECCION_VACIA)) {
                    try {
                        buscarDireccion(direccion);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_action_bar_marcadores) {
            if (getDireccionBuscada() != null) {
                getMapa().addMarker(new MarkerOptions().position(getDireccionBuscada()));
                Toast.makeText(this, "Marcador Añadido", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (item.getItemId() == R.id.item_action_bar_localizacion) {
            Intent localizacionIntent = new Intent(getApplicationContext(), Localizacion.class);
            startActivity(localizacionIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}