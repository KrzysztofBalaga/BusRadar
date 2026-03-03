package com.example.busradar4

import Bus
import android.content.Context
import android.content.SharedPreferences

class FavouriteManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bus_prefs", Context.MODE_PRIVATE)

    // Lista wszystkich ulubionych linii (zapisana w pamięci telefonu)
    private var favourites = prefs.getStringSet("fav_lines", setOf())?.toMutableSet() ?: mutableSetOf()

    // Linie aktualnie wybrane przez użytkownika na pasku (do filtrowania mapy)
    var selectedLines = mutableSetOf<String>()

    fun getFavourites(): List<String> = favourites.toList().sorted()

    fun addToFavourites(line: String) {
        favourites.add(line)
        prefs.edit().putStringSet("fav_lines", favourites).apply()
    }

    fun removeFromFavourites(line: String) {
        favourites.remove(line)
        prefs.edit().putStringSet("fav_lines", favourites).apply()
    }

    fun toggleSelection(line: String) {
        if (selectedLines.contains(line)) {
            selectedLines.remove(line)
        } else {
            selectedLines.add(line)
        }
    }

    // Dodaj to do klasy FavouriteManager w pliku FavouriteLines.kt

    // Pobiera unikalne, posortowane numery linii z aktualnych danych API
    fun getAllAvailableLines(fullBusList: List<Bus>): List<String> {
        return fullBusList.map { it.Lines.trim() }
            .distinct()
            .filter { it.isNotBlank() }
            .sortedWith(compareBy({ it.length }, { it })) // Sortuje: 1, 2, 10, 100, N01...
    }

    // Sprawdza, czy dana linia jest już w ulubionych (do rysowania gwiazdki)
    fun isFavourite(line: String): Boolean {
        return favourites.contains(line)
    }

    // W FavouriteLines.kt wewnątrz klasy FavouriteManager
    fun refreshFavourites() {
        favourites = prefs.getStringSet("fav_lines", setOf())?.toMutableSet() ?: mutableSetOf()
    }
}