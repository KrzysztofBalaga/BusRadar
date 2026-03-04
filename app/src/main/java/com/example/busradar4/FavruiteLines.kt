package com.example.busradar4

import Bus
import android.content.Context
import android.content.SharedPreferences

class FavouriteManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bus_prefs", Context.MODE_PRIVATE)

    // Lista ulubionych
    private var favourites = prefs.getStringSet("fav_lines", setOf())?.toMutableSet() ?: mutableSetOf()

    // Linie wybrane
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

    //pobranie posortowanych numerow
    fun getAllAvailableLines(fullBusList: List<Bus>): List<String> {
        return fullBusList.map { it.Lines.trim() }
            .distinct()
            .filter { it.isNotBlank() }
            .sortedWith(compareBy({ it.length }, { it }))
    }

    fun isFavourite(line: String): Boolean {
        return favourites.contains(line)
    }

    fun refreshFavourites() {
        favourites = prefs.getStringSet("fav_lines", setOf())?.toMutableSet() ?: mutableSetOf()
    }
}