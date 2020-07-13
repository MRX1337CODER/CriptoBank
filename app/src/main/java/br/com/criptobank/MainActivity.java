package br.com.criptobank;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import br.com.criptobank.model.Framework;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    RelativeLayout relativeLayout;
    Spinner spinner;
    TextInputLayout ti_code, ti_name;
    TextInputEditText et_code, et_name;
    MaterialButton btn_submit;
    String[] contaString = {"Selecionar Conta...", "Comum", "Especial", "Poupança"};
    Toolbar toolbar;
    ArrayAdapter<String> adapter;
    DatabaseReference mDatabaseReference;
    SharedPreferences sharedPreferences;
    Framework.Conta fConta;
    String existCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        actions();
        cliques();

        sharedPreferences = getSharedPreferences("keyCode", Context.MODE_PRIVATE);
        existCode = sharedPreferences.getString("codeUser", "0");
        Log.i("PrefSave", "Save:" + existCode);
        //if (!existCode.equals("0")) {
        if (!existCode.equals("0")){
            Intent intent = new Intent(getApplicationContext(), BankActivity.class);
            intent.putExtra("code", existCode);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out);
            ActivityCompat.startActivity(getApplicationContext(), intent, activityOptionsCompat.toBundle());
            finish();
        }

    }

    private void cliques() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cod = ti_code.getEditText().getText().toString();
                if (!validateCode()) {
                    return;
                } else {
                    mDatabaseReference.child(cod).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                final Framework.Cliente cliente = snapshot.getValue(Framework.Cliente.class);
                                assert cliente != null;
                                if (cliente.isDesativado()) {
                                    SnackBarCustom.SnackSetAction(getApplication(), relativeLayout, android.R.color.darker_gray, false, "Sua Conta Foi Desativa", "Ativar", Snackbar.LENGTH_LONG, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            cliente.setDesativado(false);
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("desativado", cliente.isDesativado());
                                            mDatabaseReference.child(cod).updateChildren(hashMap);

                                            sharedPreferences = getSharedPreferences("keyCode", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("codeUser", cod);
                                            editor.apply();

                                            Intent intent = new Intent(getApplicationContext(), BankActivity.class);
                                            intent.putExtra("code", cod);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out);
                                            ActivityCompat.startActivity(getApplicationContext(), intent, activityOptionsCompat.toBundle());
                                            finish();
                                        }
                                    });
                                } else {
                                    sharedPreferences = getSharedPreferences("keyCode", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("codeUser", cod);
                                    editor.apply();

                                    Intent intent = new Intent(getApplicationContext(), BankActivity.class);
                                    intent.putExtra("code", cod);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out);
                                    ActivityCompat.startActivity(getApplicationContext(), intent, activityOptionsCompat.toBundle());
                                    finish();
                                }
                            } else {
                                if (!validateCode() | !validateName()) {
                                    return;
                                }
                                if (spinner.getSelectedItem().toString().trim().equals("Selecionar Conta...") || spinner.getSelectedItemId() == 0) {
                                    SnackBarCustom.Snack(getApplication(), relativeLayout, "Você Precisa Selecionar Um Tipo De Conta", Snackbar.LENGTH_SHORT);
                                } else {
                                    String nome = ti_name.getEditText().getText().toString();
                                    String code = ti_code.getEditText().getText().toString();
                                    String conta = spinner.getSelectedItem().toString().trim();
                                    openDepositoDialog(nome, code, conta);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }
        });
    }

    private void openDepositoDialog(final String nome, final String code, final String conta) {
        AlertDialog.Builder alertDeposito = new AlertDialog.Builder(MainActivity.this);
        alertDeposito.setTitle("Depósito");
        alertDeposito.setMessage("Olá " + nome + " verificamos que você deseja abrir uma conta " + conta + ".\nPara realizarmos sua abertura com sucesso faça um depósito no valor de 15 reais.");

        alertDeposito.setPositiveButton("Depositar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Framework.Cliente cliente = new Framework.Cliente();
                cliente.setNome(nome);
                cliente.setCode(code);
                cliente.setTipoConta(conta);
                cliente.setDesativado(false);
                cliente.setSaque(0);
                cliente.setDeposito(0);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("nome", cliente.getNome());
                hashMap.put("code", cliente.getCode());
                hashMap.put("tipoConta", cliente.getTipoConta());
                hashMap.put("desativado", cliente.isDesativado());
                hashMap.put("saque", cliente.getSaque());
                hashMap.put("deposito", cliente.getDeposito());
                hashMap.put("saldo", 0);

                sharedPreferences = getSharedPreferences("keyCode", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("codeUser", code);
                editor.apply();

                mDatabaseReference.child(cliente.getCode()).setValue(hashMap);
                clearInput();
                Intent intent = new Intent(getApplicationContext(), BankActivity.class);
                intent.putExtra("code", cliente.getCode());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out);
                ActivityCompat.startActivity(getApplicationContext(), intent, activityOptionsCompat.toBundle());
                finish();

            }
        });

        alertDeposito.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDeposito.create();
        alertDialog.show();
    }

    private boolean validateCode() {
        String val = ti_code.getEditText().getText().toString();
        if (TextUtils.isEmpty(val)) {
            ti_code.setError("Código necessário.");
            return false;
        } else if (val.length() < 5) {
            ti_code.setError("São necessário no mínimo 5 dígitos");
            return false;
        } else {
            ti_code.setErrorEnabled(false);
            ti_code.setError(null);
            return true;
        }
    }

    private boolean validateName() {
        String val = ti_name.getEditText().getText().toString();
        if (TextUtils.isEmpty(val)) {
            ti_name.setError("Nome necessário.");
            return false;
        } else {
            ti_name.setErrorEnabled(false);
            ti_name.setError(null);
            return true;
        }
    }

    private void actions() {
        toolbar.setTitle("Cliente e Conta");
        setSupportActionBar(toolbar);

        spinner.setOnItemSelectedListener(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contaString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO: itens selecionado no spinner...
        Log.i("MainActivity", "Item: " + contaString[position]);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO: gerado altomaticamente...
    }

    private void initializeComponents() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        relativeLayout = findViewById(R.id.relativeLayout);
        toolbar = findViewById(R.id.toolbar);
        spinner = findViewById(R.id.spinner_conta);
        ti_code = findViewById(R.id.ti_code);
        ti_name = findViewById(R.id.ti_name);
        et_code = findViewById(R.id.et_code);
        et_name = findViewById(R.id.et_name);
        btn_submit = findViewById(R.id.submit);
    }

    private void clearInput() {
        et_code.setText("");
        et_name.setText("");
        spinner.setSelection(0);
        btn_submit.setEnabled(false);
    }

}