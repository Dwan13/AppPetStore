package com.project.apppetstore.data.network

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ─────────────────────────────────────────────────────────────────────────────
// Resultado de la consulta a Directions API
// ─────────────────────────────────────────────────────────────────────────────

data class DirectionsResult(
    /** Waypoints decodificados de la polilínea. Vacío si falla la solicitud. */
    val waypoints: List<LatLng>,
    /** Duración real del trayecto en segundos (0 si no disponible). */
    val durationSeconds: Int
)

// ─────────────────────────────────────────────────────────────────────────────
// Servicio — llama a Google Directions API y decodifica la polilínea
// ─────────────────────────────────────────────────────────────────────────────

object DirectionsService {

    /**
     * Obtiene la ruta real entre [origin] y [destination] usando la API de
     * Google Directions (modo driving).
     *
     * Requiere que la clave [apiKey] tenga habilitada la "Directions API"
     * en Google Cloud Console (la misma clave que usas para el Maps SDK).
     *
     * Retorna [DirectionsResult.waypoints] vacío si hay un error de red o la
     * clave no tiene permiso; en ese caso usa la ruta interpolada como fallback.
     */
    suspend fun getRoute(
        origin      : LatLng,
        destination : LatLng,
        apiKey      : String
    ): DirectionsResult = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(origin, destination, apiKey)
            val json = fetchJson(url)
            parseResponse(json)
        } catch (e: Exception) {
            DirectionsResult(emptyList(), 0)
        }
    }

    // ── Construcción de la URL ────────────────────────────────────────────────

    private fun buildUrl(origin: LatLng, destination: LatLng, apiKey: String): String =
        "https://maps.googleapis.com/maps/api/directions/json" +
            "?origin=${origin.latitude},${origin.longitude}" +
            "&destination=${destination.latitude},${destination.longitude}" +
            "&mode=driving" +
            "&language=es" +
            "&key=$apiKey"

    // ── Llamada HTTP ──────────────────────────────────────────────────────────

    private fun fetchJson(urlString: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.connectTimeout = 8_000
        connection.readTimeout    = 8_000
        return try {
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }

    // ── Parseo de la respuesta JSON ───────────────────────────────────────────

    private fun parseResponse(json: String): DirectionsResult {
        val root   = JSONObject(json)
        val status = root.getString("status")

        if (status != "OK") return DirectionsResult(emptyList(), 0)

        val routes = root.getJSONArray("routes")
        if (routes.length() == 0) return DirectionsResult(emptyList(), 0)

        val route  = routes.getJSONObject(0)

        // Duración total del primer leg
        val legs            = route.getJSONArray("legs")
        val durationSeconds = if (legs.length() > 0)
            legs.getJSONObject(0).getJSONObject("duration").getInt("value")
        else 0

        // Polilínea general de la ruta (overview_polyline)
        val encodedPolyline = route
            .getJSONObject("overview_polyline")
            .getString("points")

        val waypoints = decodePolyline(encodedPolyline)
        return DirectionsResult(waypoints, durationSeconds)
    }

    // ── Decodificador de polilínea (algoritmo estándar de Google) ────────────

    private fun decodePolyline(encoded: String): List<LatLng> {
        val result = mutableListOf<LatLng>()
        var index  = 0
        val len    = encoded.length
        var lat    = 0
        var lng    = 0

        while (index < len) {
            // Decodifica latitud
            var shift  = 0
            var result1 = 0
            var b: Int
            do {
                b       = encoded[index++].code - 63
                result1 = result1 or ((b and 0x1f) shl shift)
                shift  += 5
            } while (b >= 0x20)
            lat += if (result1 and 1 != 0) (result1 shr 1).inv() else result1 shr 1

            // Decodifica longitud
            shift   = 0
            var result2 = 0
            do {
                b       = encoded[index++].code - 63
                result2 = result2 or ((b and 0x1f) shl shift)
                shift  += 5
            } while (b >= 0x20)
            lng += if (result2 and 1 != 0) (result2 shr 1).inv() else result2 shr 1

            result.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return result
    }
}
