package com.unpas.showroom.ui.mobil

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.Spinner
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

class MobilFragment : Fragment() {

    val db by lazy { MobilDatabase(requireContext()) }
    private lateinit var bahanBakarAdapter: ArrayAdapter<MobilData.BahanBakar>
    private var isDijual: Boolean = true

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_mobil, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        CoroutineScope(Dispatchers.IO).launch {
            val mobilList = db.mobilDao().getAllMobils()

            requireActivity().runOnUiThread {
                for (mobil in mobilList) {
                    val tableRow = TableRow(requireContext())

                    val merkCell = createTableCell(mobil.merk)
                    val modelCell = createTableCell(mobil.model)
                    val bahanBakarCell = createTableCell(mobil.bahan_bakar)
                    val dijualCell = createTableCell(mobil.dijual.toString())
                    val deskripsiCell = createTableCell(mobil.deskripsi)

                    tableRow.addView(merkCell)
                    tableRow.addView(modelCell)
                    tableRow.addView(bahanBakarCell)
                    tableRow.addView(dijualCell)
                    tableRow.addView(deskripsiCell)

                    // Add edit icon
                    val editIcon = createEditIcon(mobil)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(mobil)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_mobil, container, false)

            val merkText = bottomSheetView.findViewById<TextInputEditText>(R.id.merkText)
            val modelText = bottomSheetView.findViewById<TextInputEditText>(R.id.modelText)
            val bahanBakarText = bottomSheetView.findViewById<Spinner>(R.id.bahanBakarText)
            val dijualText = bottomSheetView.findViewById<CheckBox>(R.id.dijuaText)
            val deskripsiText = bottomSheetView.findViewById<TextInputEditText>(R.id.deskripsiText)
            val button = bottomSheetView.findViewById<Button>(R.id.mobilButton)

            // Inisialisasi Spinner
            val bahanBakarAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                MobilData.BahanBakar.values().map { it.name }
            )

            bahanBakarText.adapter = bahanBakarAdapter

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val mobilApi = retrofit.create(MobilApi::class.java)

            button.setOnClickListener {
                val merk = merkText.text.toString()
                val model = modelText.text.toString()
                val bahanBakar = bahanBakarText.selectedItem.toString()
                val isDijual = dijualText.isChecked
                val deskripsi = deskripsiText.text.toString()

                if (TextUtils.isEmpty(merk) || TextUtils.isEmpty(model) || TextUtils.isEmpty(deskripsi)) {
                    showToast("Harap isi data terlebih dahulu")
                } else {
                    val mobilData = MobilData(0, merk, model, bahanBakar, isDijual, deskripsi)

                    CoroutineScope(Dispatchers.IO).launch {
                        db.mobilDao().insertMobil(mobilData)

                        // Tambahkan data ke endpoint menggunakan Retrofit
                        try {
                            val response = mobilApi.addMobil(mobilData)
                            if (response.isSuccessful) {
                                bottomSheetDialog.dismiss()
                            } else {
                                showToast("Gagal menambahkan data ke server")
                            }
                        } catch (e: Exception) {
                            showToast("Gagal menambahkan data ke server: ${e.message}")
                        }
                    }

                    bottomSheetDialog.dismiss()
                    showToast("Data berhasil ditambahkan")
                }

                refreshMotorList()
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    private fun refreshMotorList() {
        CoroutineScope(Dispatchers.IO).launch {
            val mobilList = db.mobilDao().getAllMobils()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (mobil in mobilList) {
                    val tableRow = TableRow(requireContext())

                    val merkCell = createTableCell(mobil.merk)
                    val modelCell = createTableCell(mobil.model)
                    val bahanBakarCell = createTableCell(mobil.bahan_bakar)
                    val dijualCell = createTableCell(mobil.dijual.toString())
                    val deskripsiCell = createTableCell(mobil.deskripsi)

                    tableRow.addView(merkCell)
                    tableRow.addView(modelCell)
                    tableRow.addView(bahanBakarCell)
                    tableRow.addView(dijualCell)
                    tableRow.addView(deskripsiCell)

                    // Add edit icon
                    val editIcon = createEditIcon(mobil)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(mobil)
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

    private fun createEditIcon(mobil: MobilData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editMotor(mobil)
        }
        return imageView
    }

    private fun editMotor(mobil: MobilData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_mobil, null)

        val merkText = bottomSheetView.findViewById<TextInputEditText>(R.id.merkText)
        val modelText = bottomSheetView.findViewById<TextInputEditText>(R.id.modelText)
        val bahanBakarText = bottomSheetView.findViewById<Spinner>(R.id.bahanBakarText)
        val dijualText = bottomSheetView.findViewById<CheckBox>(R.id.dijuaText)
        val deskripsiText = bottomSheetView.findViewById<TextInputEditText>(R.id.deskripsiText)
        val button = bottomSheetView.findViewById<Button>(R.id.mobilButton)

        merkText.setText(mobil.merk)
        modelText.setText(mobil.model)
        val bahanBakarAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            MobilData.BahanBakar.values().map { it.name }
        )
        bahanBakarText.adapter = bahanBakarAdapter
        val bahanBakarPosition = bahanBakarAdapter.getPosition(mobil.bahan_bakar.toString())
        bahanBakarText.setSelection(bahanBakarPosition)
        dijualText.isChecked = mobil.dijual
        deskripsiText.setText(mobil.deskripsi)

        button.text = "Update"

        button.setOnClickListener {
            val updatedInput1 = merkText.text.toString()
            val updatedInput2 = modelText.text.toString()
            val updatedInput3 = bahanBakarText.selectedItem.toString()
            val updatedInput4 = dijualText.isChecked
            val updatedInput5 = deskripsiText.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val updatedMobil = mobil.copy(
                    merk = updatedInput1,
                    model = updatedInput2,
                    bahan_bakar = updatedInput3,
                    dijual = updatedInput4,
                    deskripsi = updatedInput5
                )
                db.mobilDao().updateMobil(updatedMobil)
            }

            bottomSheetDialog.dismiss()
            showToast("Data telah diperbarui")
            refreshMotorList()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(mobil: MobilData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteMotor(mobil)
        }
        return imageView
    }

    private fun deleteMotor(mobil: MobilData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.mobilDao().deleteMobil(mobil)
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