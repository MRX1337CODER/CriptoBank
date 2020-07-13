package br.com.criptobank;
/*
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;

import br.com.criptobank.model.Framework;

public class Socorro extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextInputLayout ti_code, ti_name, ti_valor;
    TextInputEditText et_code, et_name, et_valor;
    TextView tv_code, tv_valor;
    MaterialButton btn_submit, btn_deposito;
    String[] contaString = {"Selecionar Conta...", "Comum", "Poupança", "Especial"};
    Toolbar toolbar;
    ArrayAdapter<String> adapter;
    int valor = 0;
    Spinner spinner;
    Framework.Conta conta;
    Framework.Cliente cliente;
    double totalSacar;
    double totalDeposito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socorro);
        initializeComponents();
        actions();
        cliques();
    }

    private void cliques() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinner.getSelectedItem().toString().trim().equals("Selecionar Conta...") || spinner.getSelectedItemId() == 0) {
                    Toast.makeText(getApplicationContext(), "Você Precisa Selecionar Um Tipo De Conta", Toast.LENGTH_SHORT).show();
                } else {
                    TextView tv_conta = findViewById(R.id.tv_conta);
                    if (spinner.getSelectedItem().toString().trim().equals("Comum") || spinner.getSelectedItemId() == 1) {
                        conta = Framework.FabricaConta.conta(Framework.TipoConta.Corrente);
                        tv_conta.setText("Comum");
                    } else if (spinner.getSelectedItem().toString().trim().equals("Poupança") || spinner.getSelectedItemId() == 2) {
                        conta = Framework.FabricaConta.conta(Framework.TipoConta.Poupanca);
                        tv_conta.setText("Poupança");
                    } else {
                        conta = Framework.FabricaConta.conta(Framework.TipoConta.Especial);
                        tv_conta.setText("Especial");
                    }
                    conta.NumConta = ti_code.getEditText().getText().toString();
                    tv_code.setText(ti_code.getEditText().getText().toString());


                    cliente = new Framework.Cliente();
                    cliente.nome = ti_name.getEditText().getText().toString();
                    cliente.Conta = conta;
                    Framework.ContaCliente.Contas.add(cliente);
                }
            }
        });

        btn_deposito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cliente = Framework.ContaCliente.getClienteByNumConta(ti_code.getEditText().getText().toString());
                //String val = ti_valor.getEditText().getText().toString();
                double val = Double.parseDouble(ti_valor.getEditText().getText().toString());
                cliente.Conta.Depositar(val);

                totalSacar += val;
                String s = String.valueOf(cliente.Conta.getSaldo());
                tv_valor.setText(s);
            }
        });
    }

    private void initializeComponents() {
        toolbar = findViewById(R.id.toolbar);
        spinner = findViewById(R.id.spinner_conta);
        ti_code = findViewById(R.id.ti_code);
        ti_name = findViewById(R.id.ti_name);
        ti_valor = findViewById(R.id.ti_value);
        et_code = findViewById(R.id.et_code);
        et_name = findViewById(R.id.et_name);
        et_valor = findViewById(R.id.et_value);
        tv_code = findViewById(R.id.code);
        tv_valor = findViewById(R.id.valor);
        btn_submit = findViewById(R.id.submit);
        btn_deposito = findViewById(R.id.btn_deposit);
    }

    private void actions() {
//        toolbar.setTitle("Cliente e Conta");
        //      setSupportActionBar(toolbar);

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
}
*/