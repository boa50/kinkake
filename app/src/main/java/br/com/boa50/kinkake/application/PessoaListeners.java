package br.com.boa50.kinkake.application;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.boa50.kinkake.model.Pessoa;
import br.com.boa50.kinkake.util.PessoaUtil;
import br.com.boa50.kinkake.util.VariaveisEstaticas;

public class PessoaListeners {
    public static ValueEventListener getListenerUpdateMusicasPessoaAtiva(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> updateMusicas = new HashMap<>();
                updateMusicas.put("codigosMusicas", VariaveisEstaticas.getPessoaAtiva().getCodigosMusicas());

                DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                ConfiguracaoFirebase.getReferenciaPessoa().child(snapshot.getKey()).updateChildren(updateMusicas);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public static ValueEventListener getListenerRemoverPessoas(final ArrayList<Pessoa> pessoasParaExcluir){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(Pessoa pessoa : pessoasParaExcluir){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(pessoa.getNome().equalsIgnoreCase(snapshot.child("nome").getValue().toString())){
                            ConfiguracaoFirebase.getReferenciaPessoa().child(snapshot.getKey()).removeValue();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public static ChildEventListener getListenerTodasPessoas(final RecyclerView.Adapter adapter, final ArrayList<Pessoa> todasPessoas, final ArrayList<String> nomesAdicionados){
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Pessoa pessoa = dataSnapshot.getValue(Pessoa.class);
                String nome = pessoa.getNome();

                if(!nomesAdicionados.contains(nome)){
                    todasPessoas.add(pessoa);
                    nomesAdicionados.add(nome);
                    PessoaUtil.ordenarPessoasPorNome(todasPessoas);
                    adapter.notifyItemInserted(todasPessoas.indexOf(pessoa));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Pessoa pessoa = dataSnapshot.getValue(Pessoa.class);
                int indice = -1;

                for(Pessoa pessoaObj : todasPessoas){
                    if(pessoaObj.getNome().equals(pessoa.getNome())){
                        pessoaObj.getCodigosMusicas().clear();
                        pessoaObj.getCodigosMusicas().addAll(pessoa.getCodigosMusicas());
                        indice = todasPessoas.indexOf(pessoaObj);
                        break;
                    }
                }

                PessoaUtil.ordenarPessoasPorNome(todasPessoas);
                adapter.notifyItemChanged(indice);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Pessoa pessoa = dataSnapshot.getValue(Pessoa.class);
                int indice = -1;

                for(Pessoa pessoaObj : todasPessoas){
                    if(pessoaObj.getNome().equals(pessoa.getNome())){
                        indice = todasPessoas.indexOf(pessoaObj);
                        todasPessoas.remove(pessoaObj);
                        nomesAdicionados.remove(pessoa.getNome());
                        break;
                    }
                }

                adapter.notifyItemRemoved(indice);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }
}
