package com.unpas.showroom.ui.promo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.unpas.showroom.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class PromoFragment : Fragment() {

    val db by lazy { PromoDatabase(requireContext()) }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_promo, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        CoroutineScope(Dispatchers.IO).launch {
            val promoList = db.promoDao().getAllPromos()

            requireActivity().runOnUiThread {
                for (promo in promoList) {
                    val tableRow = TableRow(requireContext())

                    val modelCell = createTableCell(promo.model)
                    val tanggalAwalCell = createTableCell(promo.tanggal_awal)
                    val tanggalAkhirCell = createTableCell(promo.tanggal_akhir)
                    val persentaseCell = createTableCell(promo.persentase.toString())

                    tableRow.addView(modelCell)
                    tableRow.addView(tanggalAwalCell)
                    tableRow.addView(tanggalAkhirCell)
                    tableRow.addView(persentaseCell)

                    // Add edit icon
                    val editIcon = createEditIcon(promo)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(promo)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_promo, container, false)

            val modelText = bottomSheetView.findViewById<TextInputEditText>(R.id.modelText)
            val tanggalAwalText = bottomSheetView.findViewById<TextInputEditText>(R.id.tanggalAwalText)
            tanggalAwalText.setOnClickListener(::onTanggalAwalClicked)
            val tanggalAkhirText = bottomSheetView.findViewById<TextInputEditText>(R.id.tanggalAkhirText)
            tanggalAkhirText.setOnClickListener(::onTanggalAkhirClicked)
            val persentaseText = bottomSheetView.findViewById<TextInputEditText>(R.id.persentaseText)
            val button = bottomSheetView.findViewById<Button>(R.id.promoButton)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val promoApi = retrofit.create(PromoApi::class.java)

            button.setOnClickListener {
                val updatedInput1 = modelText.text.toString()
                val updatedInput2 = tanggalAwalText.text.toString()
                val updatedInput3 = tanggalAkhirText.text.toString()
                val updatedInput4 = persentaseText.text.toString()

                if (updatedInput1.isNotEmpty() && updatedInput2.isNotEmpty() && updatedInput3.isNotEmpty() && updatedInput4.isNotEmpty()) {
                    try {
                        val persentase = updatedInput4.toInt()
                        val promoData = PromoData(0, updatedInput1, updatedInput2, updatedInput3, persentase)

                        CoroutineScope(Dispatchers.IO).launch {
                            db.promoDao().insertPromo(promoData)

                            // Tambahkan data ke endpoint menggunakan Retrofit
                            try {
                                val response = promoApi.addPromo(promoData)
                                if (response.isSuccessful) {
                                    requireActivity().runOnUiThread {
                                        showToast("Data berhasil ditambahkan")
                                    }
                                    bottomSheetDialog.dismiss()
                                } else {
                                    requireActivity().runOnUiThread {
                                        showToast("Gagal menambahkan data ke server")
                                    }
                                }
                            } catch (e: Exception) {
                                requireActivity().runOnUiThread {
                                    showToast("Gagal menambahkan data ke server: ${e.message}")
                                }
                            }
                        }
                    } catch (e: NumberFormatException) {
                        requireActivity().runOnUiThread {
                            showToast("Data harus berupa angka integer")
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        showToast("Harap isi semua data terlebih dahulu")
                    }
                }

                bottomSheetDialog.dismiss()
                refreshMotorList()
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    fun onTanggalAwalClicked(view: View) {
        val tanggalAwalText = view as TextInputEditText

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            view.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                tanggalAwalText.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    fun onTanggalAkhirClicked(view: View) {
        val tanggalAkhirText = view as TextInputEditText

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            view.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                tanggalAkhirText.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun refreshMotorList() {
        CoroutineScope(Dispatchers.IO).launch {
            val promoList = db.promoDao().getAllPromos()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (promo in promoList) {
                    val tableRow = TableRow(requireContext())

                    val modelCell = createTableCell(promo.model)
                    val tanggalAwalCell = createTableCell(promo.tanggal_awal)
                    val tanggalAkhirCell = createTableCell(promo.tanggal_akhir)
                    val persentaseCell = createTableCell(promo.persentase.toString())

                    tableRow.addView(modelCell)
                    tableRow.addView(tanggalAwalCell)
                    tableRow.addView(tanggalAkhirCell)
                    tableRow.addView(persentaseCell)

                    // Add edit icon
                    val editIcon = createEditIcon(promo)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(promo)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }
    }

    private fun createTableCell(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(72, 16, 16, 16)
        return textView
    }

    private fun createEditIcon(promo: PromoData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editMotor(promo)
        }
        return imageView
    }

    private fun editMotor(promo: PromoData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_promo, null)

        val modelText = bottomSheetView.findViewById<TextInputEditText>(R.id.modelText)
        val tanggalAwalText = bottomSheetView.findViewById<TextInputEditText>(R.id.tanggalAwalText)
        tanggalAwalText.setOnClickListener(::onTanggalAwalClicked)
        val tanggalAkhirText = bottomSheetView.findViewById<TextInputEditText>(R.id.tanggalAkhirText)
        tanggalAkhirText.setOnClickListener(::onTanggalAkhirClicked)
        val persentaseText = bottomSheetView.findViewById<TextInputEditText>(R.id.persentaseText)
        val button = bottomSheetView.findViewById<Button>(R.id.promoButton)

        modelText.setText(promo.model)
        tanggalAwalText.setText(promo.tanggal_awal)
        tanggalAkhirText.setText(promo.tanggal_akhir)
        persentaseText.setText(promo.persentase.toString())

        button.text = "Update"

        button.setOnClickListener {
            val updatedInput1 = modelText.text.toString()
            val updatedInput2 = tanggalAwalText.text.toString()
            val updatedInput3 = tanggalAkhirText.text.toString()
            val updatedInput4 = persentaseText.text.toString()

            if (updatedInput1.isNotEmpty() && updatedInput2.isNotEmpty() && updatedInput3.isNotEmpty() && updatedInput4.isNotEmpty()) {
                try {
                    val persentase = updatedInput4.toInt()

                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedPromo = promo.copy(
                            model = updatedInput1,
                            tanggal_awal = updatedInput2,
                            tanggal_akhir = updatedInput3,
                            persentase = persentase,
                        )
                        db.promoDao().updatePromo(updatedPromo)

                        // Menampilkan Toast di utas UI utama
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                        }
                    }

                    bottomSheetDialog.dismiss()
                } catch (e: NumberFormatException) {
                    // Menampilkan Toast di utas UI utama
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Data harus berupa angka integer", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Menampilkan Toast di utas UI utama
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Harap isi semua data terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }

            refreshMotorList()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(promo: PromoData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteMotor(promo)
        }
        return imageView
    }

    private fun deleteMotor(promo: PromoData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.promoDao().deletePromo(promo)
                    refreshMotorList()
                }
                dialog.dismiss()
                showToast("Data telah dihapus") // Custom function to show a toast
            }
            .setNegativeButton("Tidak") { dialog, id ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}