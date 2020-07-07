package com.example.pokedexapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.pokedexapp.model.Pokemon;
import com.example.pokedexapp.model.PokemonResposta;
import com.example.pokedexapp.pokeapi.PokeapiService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "POKEDEX";
    private Retrofit retrofit;
    private RecyclerView recyclerView;
    private ListaPokemonAdapter listaPokemonAdapter;
    private int offset;
    private boolean aptoCarregar;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        initialize();

    }

    private void initialize() {
        initializeRecycler();
        initializeRetrofit();
    }

    private void initializeRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        aptoCarregar = true;
        offset = 0;
        obterDados(offset);
    }

    private void initializeRecycler() {

        recyclerView = findViewById(R.id.recyclerView);
        listaPokemonAdapter = new ListaPokemonAdapter(this);
        recyclerView.setAdapter(listaPokemonAdapter);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastItemCount = layoutManager.findFirstVisibleItemPosition();

                    if (aptoCarregar) {
                        if ((visibleItemCount + pastItemCount) >= totalItemCount) {
                            Log.i(TAG, "Chegamos ao fim");

                            aptoCarregar = false;
                            offset += 20;
                            obterDados(offset);

                        }
                    }
                }
            }
        });
    }

    private void obterDados(int offset) {
        progressBar.setVisibility(View.VISIBLE);
        PokeapiService service = retrofit.create(PokeapiService.class);
        Call<PokemonResposta> pokemonRespostaCall = service.obterListaPokemon(20, offset);

        pokemonRespostaCall.enqueue(new Callback<PokemonResposta>() {
            @Override
            public void onResponse(Call<PokemonResposta> call, Response<PokemonResposta> response) {
                aptoCarregar = true;
                if (response.isSuccessful()){
                    PokemonResposta pokemonResposta = response.body();
                    ArrayList<Pokemon> listaPokemon = pokemonResposta.getResults();

                    listaPokemonAdapter.adicionarListaPokemon(listaPokemon);
                    listaPokemonAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                } else {
                    Log.e(TAG,"onResponse: " + response.body());
                }
            }
            @Override
            public void onFailure(Call<PokemonResposta> call, Throwable t) {
                aptoCarregar = true;
                Log.e(TAG,"onFailure: " + t.getMessage());
            }
        });
    }
}
