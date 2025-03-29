package com.example.appsql;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appsql.adapter.EtudiantRecyclerAdapter;
import com.example.appsql.classes.Etudiant;
import com.example.appsql.service.EtudiantService;
import com.example.appsql.util.ImageUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


    public class Tableactivity extends AppCompatActivity implements EtudiantRecyclerAdapter.OnEtudiantListener {
        private static final int REQUEST_IMAGE_PICK = 1;

        private RecyclerView recyclerView;
        private EtudiantRecyclerAdapter adapter;
        private EtudiantService etudiantService;
        private List<Etudiant> etudiantList;

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private Calendar calendar = Calendar.getInstance();

        private ImageView editImageView;
        private String selectedImagePath;
        private AlertDialog currentDialog;
        private Etudiant currentEditingEtudiant;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_table);

            etudiantService = new EtudiantService(this);

            etudiantList = etudiantService.findAll();

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            adapter = new EtudiantRecyclerAdapter(this, etudiantList, this);
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void onEtudiantClick(int position) {
            Etudiant etudiant = etudiantList.get(position);
            showOptionsDialog(etudiant, position);
        }

        private void showOptionsDialog(final Etudiant etudiant, final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Options pour " + etudiant.getNom() + " " + etudiant.getPrenom());

            String[] options = {"Modifier", "Supprimer"};

            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            showEditDialog(etudiant);
                            break;
                        case 1:
                            showDeleteConfirmation(etudiant, position);
                            break;
                    }
                }
            });

            builder.setNegativeButton("Annuler", null);

            builder.create().show();
        }

        private void showEditDialog(final Etudiant etudiant) {
            currentEditingEtudiant = etudiant;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Modifier Étudiant");

            View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_etudiant, null);
            final EditText editNom = view.findViewById(R.id.edit_nom);
            final EditText editPrenom = view.findViewById(R.id.edit_prenom);
            final TextView txtDateNaissance = view.findViewById(R.id.edit_date_naissance);
            final Button btnSelectDate = view.findViewById(R.id.btn_edit_select_date);
            editImageView = view.findViewById(R.id.edit_image_etudiant);
            Button btnSelectImage = view.findViewById(R.id.btn_select_image);

            editNom.setText(etudiant.getNom());
            editPrenom.setText(etudiant.getPrenom());

            final Date[] selectedDate = {etudiant.getDateNaissance()};
            if (selectedDate[0] != null) {
                txtDateNaissance.setText(dateFormat.format(selectedDate[0]));
                calendar.setTime(selectedDate[0]);
            } else {
                txtDateNaissance.setText("Non sélectionnée");
            }

            selectedImagePath = etudiant.getImagePath();
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                Bitmap bitmap = ImageUtil.loadBitmapFromPath(selectedImagePath);
                if (bitmap != null) {
                    editImageView.setImageBitmap(bitmap);
                } else {
                    editImageView.setImageResource(R.drawable.ic_person_placeholder);
                }
            } else {
                editImageView.setImageResource(R.drawable.ic_person_placeholder);
            }

            btnSelectDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            Tableactivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    selectedDate[0] = calendar.getTime();
                                    txtDateNaissance.setText(dateFormat.format(selectedDate[0]));
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePickerDialog.show();
                }
            });

            btnSelectImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_IMAGE_PICK);
                }
            });

            builder.setView(view);

            builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String nom = editNom.getText().toString().trim();
                    String prenom = editPrenom.getText().toString().trim();

                    if (!nom.isEmpty() && !prenom.isEmpty()) {
                        etudiant.setNom(nom);
                        etudiant.setPrenom(prenom);
                        etudiant.setDateNaissance(selectedDate[0]);
                        etudiant.setImagePath(selectedImagePath);
                        etudiantService.update(etudiant);

                        refreshList();

                        Toast.makeText(Tableactivity.this, "Modification de l'étudiant réussie", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Tableactivity.this, "Veuillez compléter tous les champs requis", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Annuler", null);

            currentDialog = builder.create();
            currentDialog.show();
        }

        private void showDeleteConfirmation(final Etudiant etudiant, final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Supprimer l'Etudiant");
            builder.setMessage("Confirmez-vous la suppression de cet étudiant ?");

            builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    etudiantService.delete(etudiant);
                    etudiantList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, etudiantList.size());
                    Toast.makeText(Tableactivity.this, "Suppression de l'étudiant réussie", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Non", null);

            builder.create().show();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();

                try {
                    selectedImagePath = ImageUtil.saveImageToPrivateStorage(this, selectedImageUri);

                    if (selectedImagePath != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        editImageView.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Une erreur s'est produite lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void refreshList() {
            etudiantList.clear();
            etudiantList.addAll(etudiantService.findAll());
            adapter.notifyDataSetChanged();
        }
    }


