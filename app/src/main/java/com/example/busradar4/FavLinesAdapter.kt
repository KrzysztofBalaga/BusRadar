package com.example.busradar4

class FavLinesAdapter(
    private var lines: List<String>,
    private val favManager: FavouriteManager,
    val onClick: (String) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<FavLinesAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val button: android.widget.Button = view as android.widget.Button
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val btn = android.widget.Button(parent.context).apply {
            // Tworzymy parametry układu dla RecyclerView
            val params = androidx.recyclerview.widget.RecyclerView.LayoutParams(
                androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT,
                androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
            )

            // Ustawiamy marginesy (lewy, górny, prawy, dolny) w pikselach
            // 8dp to zazwyczaj dobra wartość. Dla uproszczenia wpiszmy 16 (px):
            params.setMargins(8, 0, 8, 0)

            layoutParams = params

            // Opcjonalnie: możesz też dodać wewnętrzny odstęp tekstu od krawędzi guzika
            setPadding(20, 0, 20, 0)
        }
        return ViewHolder(btn)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = lines[position]
        holder.button.text = line

        // LOGIKA PODŚWIETLANIA:
        if (favManager.selectedLines.contains(line)) {
            // Kolor dla wybranej linii (np. niebieski ZTM)
            holder.button.setBackgroundColor(android.graphics.Color.parseColor("#0493BF"))
            holder.button.setTextColor(android.graphics.Color.WHITE)
        } else {
            // Kolor dla niewybranej linii (jasny szary)
            holder.button.setBackgroundColor(android.graphics.Color.LTGRAY)
            holder.button.setTextColor(android.graphics.Color.BLACK)
        }

        holder.button.setOnClickListener {
            onClick(line)
            // KLUCZ: Informujemy adapter, że dane się zmieniły, aby przerysował kolory
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = lines.size

    fun updateData(newLines: List<String>) {
        this.lines = newLines
        notifyDataSetChanged()
    }
}