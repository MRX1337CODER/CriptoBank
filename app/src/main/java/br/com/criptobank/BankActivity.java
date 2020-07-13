package br.com.criptobank;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import br.com.criptobank.model.Framework;

public class BankActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout relativeLayout;
    TextView tv_code, tv_name, tv_conta, tv_saldo;
    TextInputLayout ti_value;
    TextInputEditText et_value;
    MaterialButton btn_close_account, btn_saque, btn_deposito, btn_extrato;
    Toolbar toolbar;
    DatabaseReference mDatabaseReference;
    LoadingDialog loadingDialog = new LoadingDialog(BankActivity.this);
    SharedPreferences sharedPreferences;
    Intent intent;
    String c;
    Framework.Conta conta;
    Framework.Cliente cliente;
    double totalSacar;
    double totalDeposito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        intent = getIntent();
        c = intent.getStringExtra("code");
        initializeComponents();
        loadingDialog.startLoadingDialog();
        actions();
        cliques();

    }

    private void cliques() {
        btn_extrato.setOnClickListener(this);
    }

    private void actions() {
        toolbar.setTitle("Operações Bancárias");
        setSupportActionBar(toolbar);

        if (!c.equals("0")) {
            mDatabaseReference.child(c).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final Framework.Cliente cliente = dataSnapshot.getValue(Framework.Cliente.class);
                        assert cliente != null;
                        if (!cliente.isDesativado) {

                            if (cliente.getTipoConta().equals("Comum")) {
                                conta = Framework.FabricaConta.conta(Framework.TipoConta.Corrente);
                            } else if (cliente.getTipoConta().equals("Poupança")) {
                                conta = Framework.FabricaConta.conta(Framework.TipoConta.Poupanca);
                            } else {
                                conta = Framework.FabricaConta.conta(Framework.TipoConta.Especial);
                            }

                            conta.NumConta = cliente.getCode();
                            cliente.Conta = conta;
                            Framework.ContaCliente.Contas.add(cliente);
                            addNew();

                            String name = "Usuário " + cliente.getNome();
                            String code = "Código " + cliente.getCode();
                            String tipoConta = "Conta " + cliente.getTipoConta();

                            loadingDialog.dismissDialog();
                            tv_name.setText(name);
                            tv_code.setText(code);
                            tv_conta.setText(tipoConta);
                            cliente.Conta.setSaldo(cliente.Conta.getSaldo());

                            cliente.Conta.Depositar(cliente.getDeposito(), getApplication());
                            cliente.Conta.Sacar(cliente.getSaque(), getApplication());

                            final HashMap<String, Object> hashMap = new HashMap<>();

                            btn_close_account.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (cliente.getTipoConta().equals("Comum")) {
                                        if (cliente.Conta.getSaldo() < 0) {
                                            AlertDialog.Builder alertCloseComum = new AlertDialog.Builder(BankActivity.this);
                                            alertCloseComum.setTitle("Saldo Negativo");
                                            final double val = cliente.Conta.getSaldo() * (-1);
                                            alertCloseComum.setMessage("Seu saldo é de R$ " + cliente.Conta.getSaldo() + "\nPara efetuar o desativamento da mesma você deve depositar R$ " + cliente.Conta.getSaldo() * (-1));
                                            alertCloseComum.setPositiveButton("Depositar e Desativar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    cliente.Conta.Depositar(val, getApplication());
                                                    cliente.setDeposito(val);

                                                    totalDeposito += val;
                                                    String s = String.valueOf(cliente.Conta.getSaldo());
                                                    String sa = "R$ " + s;
                                                    tv_saldo.setText(sa);

                                                    et_value.setText("");
                                                    hashMap.put("saldo", cliente.Conta.getSaldo());
                                                    hashMap.put("desativado", cliente.isDesativado());

                                                    mDatabaseReference.child(c).updateChildren(hashMap);
                                                    gotoMainActivity();

                                                }

                                            });
                                            alertCloseComum.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            AlertDialog alertDialog = alertCloseComum.create();
                                            alertDialog.show();
                                        } else {
                                            AlertDialog.Builder alertCloseComum = new AlertDialog.Builder(BankActivity.this);
                                            alertCloseComum.setTitle("Desativar Conta");
                                            alertCloseComum.setMessage("Desaja realmente desativar sua conta?");
                                            alertCloseComum.setPositiveButton("Sim, desativar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    cliente.setDesativado(true);

                                                    hashMap.put("desativado", cliente.isDesativado());
                                                    mDatabaseReference.child(c).updateChildren(hashMap);
                                                    gotoMainActivity();
                                                }

                                            });
                                            alertCloseComum.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            AlertDialog alertDialog = alertCloseComum.create();
                                            alertDialog.show();
                                        }
                                    }

                                }
                            });


                            btn_saque.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    String emp = ti_value.getEditText().getText().toString();
                                    if (TextUtils.isEmpty(emp)) {
                                        ti_value.setError("Nenhum Valor Inserido");
                                    } else {
                                        double val = Double.parseDouble(ti_value.getEditText().getText().toString());
                                        ti_value.setErrorEnabled(false);
                                        ti_value.setError(null);

                                        if (val < 0) {
                                            ti_value.setError("Mínimo de 1 real para saque");
                                        } else if (val > 10000) {
                                            ti_value.setError("Limite de até 10mil por saque");
                                        } else {
                                            ti_value.setErrorEnabled(false);
                                            ti_value.setError(null);
                                            cliente.Conta.Sacar(val, getApplication());
                                            cliente.setSaque(val);

                                            totalSacar += val;
                                            String s = String.valueOf(cliente.Conta.getSaldo());
                                            String sa = "R$ " + s;
                                            tv_saldo.setText(sa);

                                            et_value.setText("");
                                            hashMap.put("saque", totalSacar);
                                            hashMap.put("saldo", cliente.Conta.getSaldo());

                                            mDatabaseReference.child(c).updateChildren(hashMap);
                                        }
                                    }
                                }
                            });

                            btn_deposito.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String emp = ti_value.getEditText().getText().toString();
                                    if (TextUtils.isEmpty(emp)) {
                                        ti_value.setError("Nenhum Valor Inserido");
                                    } else {
                                        double val = Double.parseDouble(ti_value.getEditText().getText().toString());
                                        ti_value.setErrorEnabled(false);
                                        ti_value.setError(null);

                                        if (val < 20) {
                                            ti_value.setError("Mínimo de 20 reais para depósito");
                                        } else if (val > 5000) {
                                            ti_value.setError("Limite de até 5mil por depósitos");
                                        } else {
                                            ti_value.setErrorEnabled(false);
                                            ti_value.setError(null);
                                            cliente.Conta.Depositar(val, getApplication());
                                            cliente.setDeposito(val);

                                            totalDeposito += val;
                                            String s = String.valueOf(cliente.Conta.getSaldo());
                                            String sa = "R$ " + s;
                                            tv_saldo.setText(sa);

                                            et_value.setText("");
                                            hashMap.put("deposito", totalDeposito);
                                            hashMap.put("saldo", cliente.Conta.getSaldo());

                                            mDatabaseReference.child(c).updateChildren(hashMap);
                                        }
                                    }
                                }
                            });

                            String s = String.valueOf(cliente.Conta.getSaldo());
                            String sa = "R$ " + s;
                            tv_saldo.setText(sa);

                        } else {
                            gotoMainActivity();
                        }
                    } else {
                        gotoMainActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    private void addNew() {
        Framework.Cliente cliente1 = new Framework.Cliente();
        cliente1 = new Framework.Cliente();
        cliente1.nome = cliente1.getNome();
        cliente1.Conta = conta;
        Framework.ContaCliente.Contas.add(cliente1);
    }

    private void initializeComponents() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        relativeLayout = findViewById(R.id.relativeLayout);
        toolbar = findViewById(R.id.toolbar);
        ti_value = findViewById(R.id.ti_value);
        et_value = findViewById(R.id.et_value);
        tv_name = findViewById(R.id.nameCliente);
        tv_code = findViewById(R.id.codeCliente);
        tv_conta = findViewById(R.id.contaCliente);
        tv_saldo = findViewById(R.id.saldoCliente);
        btn_close_account = findViewById(R.id.btn_closeAccount);
        btn_saque = findViewById(R.id.btn_saque);
        btn_deposito = findViewById(R.id.btn_deposito);
        btn_extrato = findViewById(R.id.btn_extrato);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.alunos:
                AlertDialog.Builder alertAlunos = new AlertDialog.Builder(this);
                alertAlunos.setTitle("Alunos");
                String[] alu = {"Gabriel Agrela", "Gabryel Aires", "Kaique Alexandre", "Luccas Hernanny", "Ryan Figueiredo"};
                alertAlunos.setMessage("\u2022" + alu[0] + "\n\u2022" + alu[1] + "\n\u2022" + alu[2] + "\n\u2022" + alu[3] + "\n\u2022" + alu[4]);
                alertAlunos.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertAlunos.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //final HashMap<String, Object> hashMap = new HashMap<>();
        //cliente = new Framework.Cliente();
        //cliente.Conta = new Framework.Cliente().Conta;
        switch (id) {
            case R.id.btn_extrato:
                AlertDialog.Builder alertExtrato = new AlertDialog.Builder(this);
                alertExtrato.setTitle("Extrato");
                alertExtrato.setMessage("Total De Saques: " + totalSacar + "\nTotal De Depósitos: " + totalDeposito);
                alertExtrato.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertExtrato.create();
                alertDialog.show();
                break;
        }
    }

    public void gotoMainActivity() {
        sharedPreferences = getSharedPreferences("keyCode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("codeUser", "0");
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), android.R.anim.fade_in, android.R.anim.fade_out);
        ActivityCompat.startActivity(getApplicationContext(), intent, activityOptionsCompat.toBundle());
        finish();
    }
}