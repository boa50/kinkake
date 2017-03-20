package br.com.boa50.kinkake.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.boa50.kinkake.R;
import br.com.boa50.kinkake.activity.MusicasAdicionarActivity;
import br.com.boa50.kinkake.adapter.MusicaReservadaAdapter;
import br.com.boa50.kinkake.model.Musica;
import br.com.boa50.kinkake.util.MusicaUtil;
import br.com.boa50.kinkake.util.PessoaUtil;

public class MusicasPorPessoaFragment extends Fragment{

    private ListView listView;
    private ArrayAdapter adapter;
    private TextView textViewVazio;
    private FloatingActionButton fabAddMusica;
    private static ArrayList<Musica> musicas;
    private ArrayList<Musica> musicasParaExcluir;
    private ArrayList<Integer> posicoesViewsSelecionadas;
    private ActionBar toolbar;
    private MenuItem itemExcluir;

    public MusicasPorPessoaFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musicas_reservadas, container, false);

        listView = (ListView) view.findViewById(R.id.lv_fragmento);
        fabAddMusica = (FloatingActionButton) view.findViewById(R.id.fab_add);
        textViewVazio = (TextView) view.findViewById(R.id.tv_reservadas_vazio);
        if(musicas == null)
            musicas = new ArrayList<>();
        adapter = new MusicaReservadaAdapter(getActivity(), musicas);
        listView.setAdapter(adapter);
        musicasParaExcluir = new ArrayList<>();
        posicoesViewsSelecionadas = new ArrayList<>();
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        setHasOptionsMenu(true);
        PessoaUtil.setAdapterMusicasPessoa(adapter);
        mostrarFabDelay();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                adicionarMusicaExcluir(view, position);
                itemExcluir.setVisible(true);
                toolbar.setTitle(R.string.tituloMusicasPessoaExcluir);
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!musicasParaExcluir.isEmpty()){
                    if(posicoesViewsSelecionadas.contains(position)){
                        removerPessoasExcluir(view, position);
                        if(musicasParaExcluir.isEmpty())
                            voltarEstadoTela();
                    }else{
                        adicionarMusicaExcluir(view, position);
                    }
                }
            }
        });

        fabAddMusica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MusicasAdicionarActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_reservadas, menu);
        super.onCreateOptionsMenu(menu, inflater);

        itemExcluir = menu.findItem(R.id.item_excluir);
        itemExcluir.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.item_excluir:
                excluirMusicasSelecionadas();
                return true;
            case android.R.id.home:
                gerenciarVoltar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void excluirMusicasSelecionadas(){
        musicas.removeAll(musicasParaExcluir);
        PessoaUtil.removerMusicasPessoaAtiva(musicasParaExcluir);
        verificarListaMusicasVazia();
        adapter.notifyDataSetChanged();
        PessoaUtil.getAdapterPessoa().notifyDataSetChanged();
        voltarEstadoTela();
    }

    private void gerenciarVoltar(){
        if(musicasParaExcluir.isEmpty()){
            getActivity().onBackPressed();
            MusicasReservadasFragment.mostrarFabDelay();
        }else{
            for(Integer posicao : posicoesViewsSelecionadas){
                listView.getChildAt(posicao).setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.background));
            }
            voltarEstadoTela();
        }
    }

    private void voltarEstadoTela(){
        musicasParaExcluir.clear();
        posicoesViewsSelecionadas.clear();
        itemExcluir.setVisible(false);
        toolbar.setTitle("Músicas de " + PessoaUtil.getPessoaAtiva().getNome());
    }

    private void adicionarMusicaExcluir(View view, int position){
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.highlightList));
        posicoesViewsSelecionadas.add(position);
        musicasParaExcluir.add(musicas.get(position));
    }

    private void removerPessoasExcluir(View view, int position){
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.background));
        posicoesViewsSelecionadas.remove(Integer.valueOf(position));
        musicasParaExcluir.remove(musicas.get(position));
    }

    public static void setMusicas(ArrayList<Integer> codigosMusicas){
        if(musicas == null)
            musicas = new ArrayList<>();
        else
            musicas.clear();

        musicas.addAll(MusicaUtil.getMusicasPorCodigos(codigosMusicas));
        if(PessoaUtil.getAdapterMusicasPessoa() != null)
            PessoaUtil.getAdapterMusicasPessoa().notifyDataSetChanged();
        if(PessoaUtil.getAdapterPessoa() != null)
            PessoaUtil.getAdapterPessoa().notifyDataSetChanged();
    }

    private void verificarListaMusicasVazia(){
        if(musicas.isEmpty())
            textViewVazio.setText(R.string.reservadasMusicasVazio);
        else
            textViewVazio.setText("");
    }

    private void mostrarFabDelay(){
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fabAddMusica.show();
            }
        }, 500);
    }

    @Override
    public void onPause() {
        super.onPause();
        fabAddMusica.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        verificarListaMusicasVazia();
        mostrarFabDelay();
    }
}
