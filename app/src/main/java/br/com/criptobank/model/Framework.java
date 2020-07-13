package br.com.criptobank.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class Framework {
    public static abstract class Conta {

        protected double _saldo;
        public String NumConta;

        public void Sacar(double valor, Context cont) {
            this._saldo = this._saldo - valor;
        }

        public void Depositar(double valor, Context cont) {
            this._saldo = this._saldo + valor;
        }

        /*public double getSaldo() {
            return this._saldo;
        }*/

        public double getSaldo() {
            return _saldo;
        }

        public void setSaldo(double _saldo) {
            this._saldo = _saldo;
        }
    }

    public static class ContaComum extends Conta { //Herança
        private static final double limiteChequeEspecial = 500;
        @Override
        public void Sacar(double valor, Context cont) {
            if (_saldo - valor >= -limiteChequeEspecial) {
                super.Sacar(valor, cont);
            } else {
                Log.i("ContaComum", "Limite cheque especial ultrapassado");
                Toast.makeText(cont, "Limite cheque especial ultrapassado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class ContaPoupanca extends Conta { //Herança
        @Override
        public void Sacar(double valor, Context cont) {
            if (valor <= _saldo) { // não pode ficar - pq n tem cheque especial
                super.Sacar(valor, cont);
            } else {
                Log.i("ContaPoupanca", "Saldo não pode ser negativo");
                Toast.makeText(cont, "Saldo não pode ser negativo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class ContaEspecial extends Conta { //Herança
        @Override
        public void Depositar(double valor, Context cont) {
            Toast.makeText(cont, "Conta não permite depósito", Toast.LENGTH_SHORT).show();
            Log.i("ContaEspecial", "Conta não permite depósito");
        }
    }

    public static class Cliente {
        public String nome;
        public String code;
        public Conta Conta; // Associação
        public String tipoConta;
        public boolean isDesativado;
        public double saque;
        public double deposito;
        public double saldo;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTipoConta() {
            return tipoConta;
        }

        public void setTipoConta(String tipoConta) {
            this.tipoConta = tipoConta;
        }

        public boolean isDesativado() {
            return isDesativado;
        }

        public void setDesativado(boolean desativado) {
            isDesativado = desativado;
        }

        public double getSaque() {
            return saque;
        }

        public void setSaque(double saque) {
            this.saque = saque;
        }

        public double getDeposito() {
            return deposito;
        }

        public void setDeposito(double deposito) {
            this.deposito = deposito;
        }

        public double getSaldo() {
            return saldo;
        }

        public void setSaldo(double saldo) {
            this.saldo = saldo;
        }
    }

    public static class ContaCliente{
        public static ArrayList<Cliente> Contas = new ArrayList<Cliente>();
        public static Cliente getClienteByNumConta(String numConta) {
            return getClienteByNumConta(numConta);
        }
    }

    public enum TipoConta {
        Corrente(0), Poupanca(1), Especial(2);
        TipoConta(int valorOpcao) {
        }
    }

    public static class FabricaConta{
        public static Conta conta(TipoConta tipo){
            switch (tipo){
                case Corrente: return new ContaComum();
                case Poupanca: return new ContaPoupanca();
                case Especial: return new ContaEspecial();
                default:
                    return null;
            }
        }
    }
}