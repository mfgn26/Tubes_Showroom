package com.unpas.showroom.ui.motor

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

class MotorFragment : Fragment() {

    val db by lazy { MotorDatabase(requireContext()) }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_motor, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        CoroutineScope(Dispatchers.IO).launch {
            val motorList = db.motorDao().getAllMotors()

            requireActivity().runOnUiThread {
                for (motor in motorList) {
                    val tableRow = TableRow(requireContext())

                    val modelCell = createTableCell(motor.model)
                    val warnaCell = createTableCell(motor.warna)
                    val kapasitasCell = createTableCell(motor.kapasitas.toString())
                    val tanggalRilisCell = createTableCell(motor.tanggal_rilis)
                    val hargaCell = createTableCell(motor.harga.toString())

                    tableRow.addView(modelCell)
                    tableRow.addView(warnaCell)
                    tableRow.addView(kapasitasCell)
                    tableRow.addView(tanggalRilisCell)
                    tableRow.addView(hargaCell)

                    // Add edit icon
                    val editIcon = createEditIcon(motor)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(motor)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_motor, container, false)

            val modelText = bottomSheetView.findViewById<TextInputEditText>(R.id.modelText)
            val warnaText = bottomSheetView.findViewById<TextInputEditText>(R.id.warnaText)
            val kapasitasText = bottomSheetView.findViewById<TextInputEditText>(R.id.kapasitasText)
            val tanggalRilisText = bottomSheetView.findViewById<TextInputEditText>(R.id.tanggalRilisText)
            tanggalRilisText.setOnClickListener(::onTanggalRilisClicked)
            val hargaText = bottomSheetView.findViewById<TextInputEditText>(R.id.hargaText)
            val button = bottomSheetView.findViewById<Button>(R.id.motorButton)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val motorApi = retrofit.create(MotorApi::class.java)

            button.setOnClickListener {
                val updatedInput1 = modelText.text.toString()
                val updatedInput2 = warnaText.text.toString()
                val updatedInput3 = kapasitasText.text.toString()
                val updatedInput4 = tanggalRilisText.text.toString()
                val updatedInput5 = hargaText.text.toString()

                if (updatedInput1.isEmpty() || updatedInput2.isEmpty() || updatedInput3.isEmpty() || updatedInput4.isEmpty() || updatedInput5.isEmpty()) {
                    showToast("Harap isi data terlebih dahulu")
                } else {
                    try {
                        val kapasitas = updatedInput3.toInt()
                        val harga = updatedInput5.toInt()

                        val motorData = MotorData(0, updatedInput1, updatedInput2, kapasitas, updatedInput4, harga)

                        CoroutineScope(Dispatchers.IO).launch {
                            db.motorDao().insertMotor(motorData)

                            // Tambahkan data ke endpoint menggunakan Retrofit
                            try {
                                val response = motorApi.addMotor(motorData)
                                if (response.isSuccessful) {
                                    bottomSheetDialog.dismiss()
                                    requireActivity().runOnUiThread{
                                        showToast("Data berhasil ditambahkan")
                                    }
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

                        bottomSheetDialog.dismiss()
                    } catch (e: NumberFormatException) {
                        requireActivity().runOnUiThread {
                            showToast("Kapasitas dan harga harus berupa angka")
                        }
                    }
                }

                refreshMotorList()
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    fun onTanggalRilisClicked(view: View) {
        val tanggalRilisText = view as TextInputEditText

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            view.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                tanggalRilisText.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun refreshMotorList() {
        CoroutineScope(Dispatchers.IO).launch {
            val motorList = db.motorDao().getAllMotors()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (motor in motorList) {
                    val tableRow = TableRow(requireContext())

                    val modelCell = createTableCell(motor.model)
                    val warnaCell = createTableCell(motor.warna)
                    val kapasitasCell = createTableCell(motor.kapasitas.toString())
                    val tanggalRilisCell = createTableCell(motor.tanggal_rilis)
                    val hargaCell = createTableCell(motor.harga.toString())

                    tableRow.addView(modelCell)
                    tableRow.addView(warnaCell)
                    tableRow.addView(kapasitasCell)
                    tableRow.addView(tanggalRilisCell)
                    tableRow.addView(hargaCell)

                    // Add edit icon
                    val editIcon = createEditIcon(motor)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(motor)
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

    private fun createEditIcon(motor: MotorData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editMotor(motor)
        }
        return imageView
    }

    private fun editMotor(motor: MotorData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_motor, null)

        val modelText = bottomSheetView.findViewById<TextInputEditText>(R.id.modelText)
        val warnaText = bottomSheetView.findViewById<TextInputEditText>(R.id.warnaText)
        val kapasitasText = bottomSheetView.findViewById<TextInputEditText>(R.id.kapasitasText)
        val tanggalRilisText = bottomSheetView.findViewById<TextInputEditText>(R.id.tanggalRilisText)
        tanggalRilisText.setOnClickListener(::onTanggalRilisClicked)
        val hargaText = bottomSheetView.findViewById<TextInputEditText>(R.id.hargaText)
        val button = bottomSheetView.findViewById<Button>(R.id.motorButton)

        modelText.setText(motor.model)
        warnaText.setText(motor.warna)
        kapasitasText.setText(motor.kapasitas.toString())
        tanggalRilisText.setText(motor.tanggal_rilis)
        hargaText.setText(motor.harga.toString())

        button.text = "Update"

        button.setOnClickListener {
            val updatedInput1 = modelText.text.toString()
            val updatedInput2 = warnaText.text.toString()
            val updatedInput3 = kapasitasText.text.toString()
            val updatedInput4 = tanggalRilisText.text.toString()
            val updatedInput5 = hargaText.text.toString()

            if (updatedInput1.isEmpty() || updatedInput2.isEmpty() || updatedInput3.isEmpty() || updatedInput4.isEmpty() || updatedInput5.isEmpty()) {
                showToast("Harap isi data terlebih dahulu")
            } else {
                try {
                    val kapasitas = updatedInput3.toInt()
                    val harga = updatedInput5.toInt()

                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedMotor = motor.copy(
                            model = updatedInput1,
                            warna = updatedInput2,
                            kapasitas = kapasitas,
                            tanggal_rilis = updatedInput4,
                            harga = harga
                        )
                        db.motorDao().updateMotor(updatedMotor)
                    }

                    bottomSheetDialog.dismiss()
                    requireActivity().runOnUiThread {
                        showToast("Data telah diperbarui")
                    }
                } catch (e: NumberFormatException) {
                    requireActivity().runOnUiThread {
                        showToast("Kapasitas dan harga harus berupa angka")
                    }
                }
            }

            refreshMotorList()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(motor: MotorData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteMotor(motor)
        }
        return imageView
    }

    private fun deleteMotor(motor: MotorData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.motorDao().deleteMotor(motor)
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