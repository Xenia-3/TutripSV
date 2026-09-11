package com.tutripsv.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutripsv.app.R
import com.tutripsv.app.databinding.ItemDestinoBinding
import com.tutripsv.app.models.Destino

class DestinoAdapter(
    private var listaDestinos: List<Destino>,
    private val onEditar: (Destino) -> Unit,
    private val onEliminar: (Destino) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    inner class DestinoViewHolder(val binding: ItemDestinoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val binding = ItemDestinoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DestinoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = listaDestinos[position]
        val ctx = holder.itemView.context

        holder.binding.tvNombre.text = destino.nombre
        holder.binding.tvPais.text = destino.pais
        holder.binding.tvPrecio.text = ctx.getString(
            R.string.catalogo_precio_formato,
            String.format("%.2f", destino.precio)
        )
        holder.binding.tvDescripcion.text = destino.descripcion

        val idRecurso = ctx.resources.getIdentifier(
            destino.imagenRuta, "drawable", ctx.packageName
        )
        if (idRecurso != 0) {
            Glide.with(ctx)
                .load(idRecurso)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.binding.ivImagen)
        } else {
            holder.binding.ivImagen.setImageResource(R.mipmap.ic_launcher)
        }

        holder.binding.btnEditar.setOnClickListener { onEditar(destino) }
        holder.binding.btnEliminar.setOnClickListener { onEliminar(destino) }
    }

    override fun getItemCount(): Int = listaDestinos.size

    fun actualizarLista(nuevaLista: List<Destino>) {
        listaDestinos = nuevaLista
        notifyDataSetChanged()
    }
}